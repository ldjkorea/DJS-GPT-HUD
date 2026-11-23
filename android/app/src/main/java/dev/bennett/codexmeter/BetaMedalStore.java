package dev.bennett.codexmeter;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.os.Looper;
import java.util.concurrent.*;
import org.json.*;

/** Independent additive ledger. Existing growth/usage/auth tables are never migrated or edited. */
final class BetaMedalStore extends SQLiteOpenHelper {
    static final String NAME="beta-personal-medals-v1.sqlite",CACHE="beta_personal_medal_display_v1";
    static final Object LOCK=new Object();
    static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    BetaMedalStore(Context c){super(c,NAME,null,1);}
    public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE medals(account TEXT PRIMARY KEY,document TEXT NOT NULL)");}
    public void onUpgrade(SQLiteDatabase db,int old,int next){throw new IllegalStateException("Unknown medal schema retained");}
    static void workerOnly(){if(Looper.myLooper()==Looper.getMainLooper())throw new IllegalStateException("Medal database requires a worker");}
    static JSONObject row(SQLiteDatabase db,String account)throws Exception {
        try(Cursor cursor=db.rawQuery("SELECT document FROM medals WHERE account=?",new String[]{account})){
            if(!cursor.moveToFirst())return null;JSONObject state=new JSONObject(cursor.getString(0));BetaMedalModel.validate(state,account,System.currentTimeMillis());return state;
        }
    }
    static JSONObject load(Context c,String account)throws Exception {
        workerOnly();if(!BetaGrowthStore.current(c,account)||!c.getDatabasePath(NAME).exists())return null;
        synchronized(LOCK){try(BetaMedalStore db=new BetaMedalStore(c)){JSONObject state=row(db.getReadableDatabase(),account);if(!BetaGrowthStore.current(c,account))return null;publish(c,account,state);return state;}}
    }
    static JSONObject peek(Context c){String account=SubscriptionStore.accountKey(c);if(account==null)return null;
        try{JSONObject data=new JSONObject(c.getSharedPreferences(CACHE,0).getString(account,""));return account.equals(data.optString("account"))&&BetaV4.day(System.currentTimeMillis()).toString().equals(data.optString("date"))?data:null;}catch(Exception missing){return null;}}
    static boolean failed(Context c){String account=SubscriptionStore.accountKey(c);return account!=null&&c.getSharedPreferences(CACHE,0).getBoolean(account+":error",false);}
    static void refresh(Context context){Context c=context.getApplicationContext();String account=SubscriptionStore.accountKey(c);if(account==null)return;
        WORKER.execute(()->{try{load(c,account);}catch(Exception unavailable){markFailed(c,account,true);}});}
    static void response(Context c,String account,UsageSnapshot snapshot){
        JSONObject growth=BetaGrowthStore.peek(c);
        if(TrialDemo.active(c)||growth==null||!growth.optBoolean("enabled")||!BetaGrowthStore.current(c,account))return;
        try{record(c,account,snapshot,false);}catch(Exception unavailable){markFailed(c,account,true);}
    }
    static boolean record(Context c,String account,UsageSnapshot snapshot,boolean demo)throws Exception {
        workerOnly();if(!BetaGrowthStore.current(c,account)||demo!=TrialDemo.active(c)||snapshot==null)return false;
        UsageWindow window=snapshot.weekly!=null?snapshot.weekly:snapshot.monthly!=null?snapshot.monthly:snapshot.fiveHour;
        if(window==null||window.windowSeconds<=0)return false;
        String meter=snapshot.weekly!=null?"weekly":snapshot.monthly!=null?"monthly":"five_hour";
        LedgerRecord record=new LedgerRecord(meter,snapshot.planType,snapshot.fetchedAtMillis,window.preciseUsedPercent,Double.toString(window.preciseUsedPercent),window.effectiveResetAtMillis(snapshot.fetchedAtMillis),window.windowSeconds,"api_precise",false);
        long now=System.currentTimeMillis();synchronized(LOCK){try(BetaMedalStore helper=new BetaMedalStore(c)){
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();JSONObject state;boolean changed;
            try{if(!BetaGrowthStore.current(c,account))return false;state=row(db,account);if(state==null)state=BetaMedalModel.empty(account,Math.min(now,record.at),demo);
                if(state.getBoolean("demo")!=demo)throw new IllegalArgumentException("Synthetic scope");
                changed=BetaMedalModel.observe(state,account,record,now);
                if(changed)db.execSQL("INSERT OR REPLACE INTO medals(account,document) VALUES(?,?)",new Object[]{account,state.toString()});db.setTransactionSuccessful();
            }finally{db.endTransaction();}
            if(changed)publish(c,account,state);return changed;
        }}
    }
    private static void markFailed(Context c, String account, boolean notify) {
        synchronized (LOCK) {
            if (!BetaGrowthStore.current(c, account)) return;
            SharedPreferences prefs = c.getSharedPreferences(CACHE, 0);
            if (prefs.getBoolean(account + ":error", false)) return;
            prefs.edit().putBoolean(account + ":error", true).apply();
            if (notify) c.sendBroadcast(new Intent(AppConstants.ACTION_USAGE_UPDATED).setPackage(c.getPackageName()));
        }
    }
    static void publish(Context c,String account,JSONObject state)throws Exception {
        if(!BetaGrowthStore.current(c,account))return;
        android.content.SharedPreferences prefs=c.getSharedPreferences(CACHE,0);
        String value=state==null?null:BetaMedalModel.summary(state,System.currentTimeMillis()).toString();
        boolean wasFailed = prefs.getBoolean(account+":error",false);
        if(java.util.Objects.equals(value,prefs.getString(account,null))&&!wasFailed)return;
        android.content.SharedPreferences.Editor edit=prefs.edit().remove(account+":error");if(value==null)edit.remove(account);else edit.putString(account,value);
        if(!edit.commit()){
            // commit() can have cleared the in-memory flag even when the disk write failed.
            markFailed(c,account,!wasFailed);
            throw new java.io.IOException("Medal cache failed; ledger retained");
        }
        if(BetaGrowthStore.current(c,account))c.sendBroadcast(new Intent(AppConstants.ACTION_USAGE_UPDATED).setPackage(c.getPackageName()));
    }
    static JSONObject export(Context c,String account)throws Exception {JSONObject value=load(c,account);return value==null?null:new JSONObject(value.toString());}
    static void validate(Context c,String account,JSONObject value)throws Exception {
        workerOnly();if(!BetaGrowthStore.current(c,account))throw new IllegalArgumentException("Account changed");
        BetaMedalModel.validate(value,account,System.currentTimeMillis());if(value.getBoolean("demo")!=TrialDemo.active(c))throw new IllegalArgumentException("Synthetic scope");
    }
    static void restore(Context c,String account,JSONObject incoming)throws Exception {
        validate(c,account,incoming);synchronized(LOCK){try(BetaMedalStore helper=new BetaMedalStore(c)){
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();JSONObject value;
            try{if(!BetaGrowthStore.current(c,account))throw new IllegalStateException("Account changed");JSONObject current=row(db,account);
                if(current==null)current=BetaMedalModel.empty(account,System.currentTimeMillis(),TrialDemo.active(c));
                value=BetaMedalModel.merge(current,incoming,account,System.currentTimeMillis());
                db.execSQL("INSERT OR REPLACE INTO medals(account,document) VALUES(?,?)",new Object[]{account,value.toString()});db.setTransactionSuccessful();
            }finally{db.endTransaction();}publish(c,account,value);
        }}
    }
    static void equip(Context c,String account,String id)throws Exception {
        workerOnly();synchronized(LOCK){try(BetaMedalStore helper=new BetaMedalStore(c)){
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();JSONObject state;
            try{if(!BetaGrowthStore.current(c,account))throw new IllegalStateException("Account changed");state=row(db,account);if(state==null){if(id.isEmpty())return;throw new IllegalArgumentException("No titles");}
                if(!id.isEmpty()&&!state.getJSONObject("titles").has(id))throw new IllegalArgumentException("Unowned medal title");state.put("equipped",id);
                db.execSQL("UPDATE medals SET document=? WHERE account=?",new Object[]{state.toString(),account});db.setTransactionSuccessful();
            }finally{db.endTransaction();}publish(c,account,state);
        }}
    }
}
