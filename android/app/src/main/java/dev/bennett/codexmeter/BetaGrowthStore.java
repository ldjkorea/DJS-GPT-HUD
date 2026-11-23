package dev.bennett.codexmeter;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.concurrent.*;

/** Separate, account-scoped growth DB. Never deletes or resets the usage/legacy stores. */
final class BetaGrowthStore extends SQLiteOpenHelper {
    static final String NAME="beta-growth-v4.sqlite";
    static final Object LOCK=new Object();
    static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    BetaGrowthStore(Context c){super(c,NAME,null,1);}
    @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE profiles(account TEXT PRIMARY KEY,rules INTEGER NOT NULL,document TEXT NOT NULL)");db.execSQL("CREATE TABLE archives(account TEXT PRIMARY KEY,created_at INTEGER NOT NULL,document TEXT NOT NULL)");}
    @Override public void onUpgrade(SQLiteDatabase db,int from,int to){throw new IllegalStateException("Preserve unknown growth schema");}
    static boolean current(Context c,String expected){return expected!=null&&expected.equals(SubscriptionStore.accountKey(c));}
    private static JSONObject row(SQLiteDatabase db,String account)throws Exception{try(Cursor cursor=db.rawQuery("SELECT document,rules FROM profiles WHERE account=?",new String[]{account})){if(!cursor.moveToFirst())return null;if(cursor.getInt(1)!=BetaV4.RULES)throw new IllegalArgumentException("Newer rules preserved");JSONObject value=new JSONObject(cursor.getString(0));BetaV4.validateIdentity(value,account);long xp=new java.math.BigDecimal(value.get("total").toString()).longValueExact();int level=value.getInt("level");long progress=value.getLong("progress"),cost=BetaV4.cost(level);if(progress<0||progress>=cost||value.getLong("next_cost")!=cost||xp!=Math.addExact(BetaV4.threshold(level),progress))throw new IllegalArgumentException("Stored growth balance");return value;}}
    static JSONObject load(Context c,String account)throws Exception{
        if(!current(c,account))return null;
        synchronized(LOCK){if(!c.getDatabasePath(NAME).exists()){publish(c,account,null);return null;}try(BetaGrowthStore helper=new BetaGrowthStore(c)){SQLiteDatabase db=helper.getWritableDatabase();JSONObject result;db.beginTransaction();try{result=row(db,account);if(result!=null&&result.optInt("catalog_revision",1)<2){JSONObject upgraded=BetaV4.upgrade(result,System.currentTimeMillis());if(upgraded.getLong("total")!=result.getLong("total")||upgraded.getInt("level")!=result.getInt("level")||upgraded.getLong("progress")!=result.getLong("progress"))throw new IllegalStateException("Catalog must preserve economy");result=upgraded;db.execSQL("UPDATE profiles SET document=? WHERE account=?",new Object[]{result.toString(),account});}db.setTransactionSuccessful();}finally{db.endTransaction();}if(!current(c,account))return null;publish(c,account,result);return result;}}
    }
    /** Activation is a DB fact; failure to save a display cache must never resume legacy rewards. */
    static boolean activeWorker(Context c,String account){
        if(!current(c,account)||!c.getDatabasePath(NAME).exists())return false;
        synchronized(LOCK){try(BetaGrowthStore helper=new BetaGrowthStore(c);
            Cursor cursor=helper.getReadableDatabase().rawQuery("SELECT 1 FROM profiles WHERE account=?",new String[]{account})){
            return current(c,account)&&cursor.moveToFirst();
        }catch(Exception unavailable){
            // Preserve existing growth until the unreadable DB can be checked safely.
            return current(c,account);
        }}
    }
    static JSONObject peek(Context c){String key=SubscriptionStore.accountKey(c);if(key==null)return null;try{JSONObject result=new JSONObject(c.getSharedPreferences("beta_growth_display_v4",0).getString(key,""));return key.equals(result.optString("account"))?result:null;}catch(Exception ignored){return null;}}
    /** Foreground repair runs on the existing worker; never reads the DB while rendering. */
    static void refreshDisplay(Context context){
        Context c=context.getApplicationContext();String account=SubscriptionStore.accountKey(c);
        if(account==null)return;
        WORKER.execute(()->{if(!current(c,account))return;try{load(c,account);}catch(Exception unavailable){
            if(!current(c,account))return;
            android.content.SharedPreferences prefs=c.getSharedPreferences("beta_growth_display_v4",0);
            boolean alreadyFailed=prefs.getBoolean(account+":error",false);
            prefs.edit().putBoolean(account+":error",true).apply();
            if(!alreadyFailed)c.sendBroadcast(new Intent(AppConstants.ACTION_USAGE_UPDATED).setPackage(c.getPackageName()));
        }});
    }
    static boolean displayError(Context c){String account=SubscriptionStore.accountKey(c);return account!=null&&c.getSharedPreferences("beta_growth_display_v4",0).getBoolean(account+":error",false);}
    private static void publish(Context c,String account,JSONObject state)throws Exception{
        if(!current(c,account))return;
        android.content.SharedPreferences prefs=c.getSharedPreferences("beta_growth_display_v4",0);
        long now=System.currentTimeMillis();
        String value=state==null?null:BetaV4.summary(state,now).put("next_goal",BetaNextGoal.choose(state,now)).toString();
        if(java.util.Objects.equals(value,prefs.getString(account,null))&&!prefs.getBoolean(account+":error",false))return;
        android.content.SharedPreferences.Editor editor=prefs.edit().remove(account+":error");
        if(value==null)editor.remove(account);else editor.putString(account,value);
        if(!editor.commit()){
            prefs.edit().putBoolean(account+":error",true).apply();
            throw new java.io.IOException("Growth display save failed; original DB retained");
        }
        if(current(c,account))c.sendBroadcast(new Intent(AppConstants.ACTION_USAGE_UPDATED).setPackage(c.getPackageName()));
    }
    static boolean start(Context c,String account)throws Exception{return start(c,account,System.currentTimeMillis(),false);}
    static boolean start(Context c,String account,long cutover,boolean synthetic)throws Exception{
        if(!current(c,account)||synthetic&&!TrialDemo.active(c))return false;
        synchronized(UsageApi.NETWORK_LOCK){
            JSONObject old=RpgProgressStore.export(c,account);
            synchronized(LOCK){JSONObject state;
                try(BetaGrowthStore helper=new BetaGrowthStore(c)){SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();try{
                    if(!current(c,account))return false;JSONObject existing=row(db,account);if(existing!=null)return false;
                    state=BetaV4.create(account,cutover);
                    db.execSQL("INSERT INTO archives(account,created_at,document) VALUES(?,?,?)",new Object[]{account,System.currentTimeMillis(),old==null?"":old.toString()});
                    db.execSQL("INSERT INTO profiles(account,rules,document) VALUES(?,?,?)",new Object[]{account,BetaV4.RULES,state.toString()});db.setTransactionSuccessful();
                }finally{db.endTransaction();}}
                publish(c,account,state);return true;
            }
        }
    }
    /** Call only after UsageApi has completed a fresh successful HTTP response and durable usage save. */
    static void response(Context c,String account,UsageSnapshot snapshot){
        if(!current(c,account)||TrialDemo.active(c)||snapshot==null||!snapshot.hasDisplayableData())return;
        try{confirm(c,account,snapshot.fetchedAtMillis,System.currentTimeMillis(),false);}catch(Exception ignored){c.getSharedPreferences("beta_growth_display_v4",0).edit().putBoolean(account+":error",true).apply();}
    }
    static boolean confirm(Context c,String account,long at,long now,boolean synthetic)throws Exception{
        if(!current(c,account)||synthetic&&!TrialDemo.active(c)||!c.getDatabasePath(NAME).exists())return false;
        synchronized(LOCK){JSONObject state;boolean changed;
            try(BetaGrowthStore helper=new BetaGrowthStore(c)){SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();try{
                if(!current(c,account))return false;state=row(db,account);if(state==null)return false;
                changed=BetaV4.confirm(state,account,at,synthetic?at:now,true);if(changed)db.execSQL("UPDATE profiles SET document=? WHERE account=?",new Object[]{state.toString(),account});db.setTransactionSuccessful();
            }finally{db.endTransaction();}}
            if(changed)publish(c,account,state);return changed;
        }
    }
    static boolean equip(Context c,String account,String id)throws Exception{return change(c,account,id,null);}
    static boolean enabled(Context c,String account,boolean enabled)throws Exception{return change(c,account,null,enabled);}
    private static boolean change(Context c,String account,String title,Boolean enabled)throws Exception{
        if(!current(c,account))return false;synchronized(LOCK){JSONObject state;
            try(BetaGrowthStore helper=new BetaGrowthStore(c)){SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();try{
                if(!current(c,account))return false;state=row(db,account);if(state==null)return false;
                if(title!=null){if(!title.isEmpty()&&!state.getJSONObject("titles").has(title))return false;state.put("equipped",title);}
                if(enabled!=null)state.put("enabled",enabled);
                db.execSQL("UPDATE profiles SET document=? WHERE account=?",new Object[]{state.toString(),account});db.setTransactionSuccessful();
            }finally{db.endTransaction();}}publish(c,account,state);return true;
        }
    }
    static JSONObject archive(Context c,String account)throws Exception{
        if(!current(c,account)||!c.getDatabasePath(NAME).exists())return null;synchronized(LOCK){try(BetaGrowthStore helper=new BetaGrowthStore(c);Cursor row=helper.getReadableDatabase().rawQuery("SELECT document,created_at FROM archives WHERE account=?",new String[]{account})){if(!row.moveToFirst()||row.getString(0).isEmpty())return null;return new JSONObject(row.getString(0)).put("archived_at",row.getLong(1));}}
    }
    static JSONObject export(Context c,String account)throws Exception{JSONObject state=load(c,account);if(state==null)throw new IllegalArgumentException("No growth record");return new JSONObject().put("format","gpt-hud-beta-growth").put("version",1).put("account",account).put("state",state).put("archive",archive(c,account));}
    static JSONObject preview(Context c,String account,JSONObject file)throws Exception{
        JSONObject old=file.optJSONObject("archive");if(old!=null&&(!account.equals(old.optString("account"))||old.optInt("version")!=RpgEngine.VERSION||old.optJSONObject("rpg")==null))throw new IllegalArgumentException("Archive scope");
        if(old!=null){String raw=old.toString();for(String secret:new String[]{"access_token","refresh_token","id_token","private_key"})if(raw.contains("\""+secret+"\""))throw new IllegalArgumentException("Authentication in backup");}
        if(!current(c,account)||!"gpt-hud-beta-growth".equals(file.getString("format"))||file.getInt("version")!=1||!account.equals(file.getString("account")))throw new IllegalArgumentException("Format/account");
        return BetaV4.restore(load(c,account),file.getJSONObject("state"),account,System.currentTimeMillis());
    }
    static boolean restore(Context c,String account,JSONObject file)throws Exception{
        if(!current(c,account))return false;synchronized(UsageApi.NETWORK_LOCK){JSONObject old=RpgProgressStore.export(c,account);synchronized(LOCK){JSONObject result;
            try(BetaGrowthStore helper=new BetaGrowthStore(c)){SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();try{
                if(!current(c,account))return false;
                if(!"gpt-hud-beta-growth".equals(file.getString("format"))||file.getInt("version")!=1||!account.equals(file.getString("account")))throw new IllegalArgumentException("Format/account");
                result=BetaV4.restore(row(db,account),file.getJSONObject("state"),account,System.currentTimeMillis());
                JSONObject prior=file.optJSONObject("archive");if(prior!=null&&(!account.equals(prior.optString("account"))||prior.optInt("version")!=RpgEngine.VERSION||prior.optJSONObject("rpg")==null))throw new IllegalArgumentException("Archive account/version");
                if(prior!=null){String raw=prior.toString();for(String secret:new String[]{"access_token","refresh_token","id_token","private_key"})if(raw.contains("\""+secret+"\""))throw new IllegalArgumentException("Authentication in backup");old=prior;}
                db.execSQL("INSERT OR IGNORE INTO archives(account,created_at,document) VALUES(?,?,?)",new Object[]{account,System.currentTimeMillis(),old==null?"":old.toString()});
                db.execSQL("INSERT OR REPLACE INTO profiles(account,rules,document) VALUES(?,?,?)",new Object[]{account,BetaV4.RULES,result.toString()});db.setTransactionSuccessful();
            }finally{db.endTransaction();}}publish(c,account,result);return true;
        }}
    }
    static long acknowledge(Context c, String account, long displayedTotal) throws Exception {
        synchronized (UsageApi.NETWORK_LOCK) {
            if (!current(c, account) || !c.getDatabasePath(NAME).exists()) return 0;
            synchronized (LOCK) {
                try (BetaGrowthStore helper = new BetaGrowthStore(c)) {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.beginTransaction();
                    try {
                        JSONObject state = row(db, account);
                        if (state == null || displayedTotal > state.getLong("total")) return 0;
                        long seen = state.optLong("seen_total");
                        if (displayedTotal <= seen) return 0;
                        state.put("seen_total", displayedTotal);
                        db.execSQL("UPDATE profiles SET document=? WHERE account=?", new Object[]{state.toString(), account});
                        db.setTransactionSuccessful();
                        return displayedTotal - seen;
                    } finally {
                        db.endTransaction();
                    }
                }
            }
        }
    }
}
