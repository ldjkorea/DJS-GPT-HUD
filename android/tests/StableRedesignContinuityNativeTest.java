package dev.bennett.codexmeter;

import android.app.Instrumentation;
import android.content.*;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import org.json.*;

/** Full synthetic checkpoint around public 3.1.5 -> local redesign replacement. */
public final class StableRedesignContinuityNativeTest extends Instrumentation {
    int checks;String phase;
    void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
    JSONObject legacy(Context c,String account)throws Exception{
        try(SQLiteDatabase db=SQLiteDatabase.openDatabase(c.getDatabasePath(RpgProgressStore.NAME).getPath(),null,SQLiteDatabase.OPEN_READONLY);Cursor row=db.rawQuery("SELECT document FROM profiles WHERE account=?",new String[]{account})){
            if(!row.moveToFirst())throw new AssertionError("Missing legacy document");return new JSONObject(row.getString(0));
        }
    }
    JSONArray observations(Context c)throws Exception{
        JSONArray result=new JSONArray();try(SQLiteDatabase db=SQLiteDatabase.openDatabase(c.getDatabasePath(UsageLedgerDatabase.NAME).getPath(),null,SQLiteDatabase.OPEN_READONLY);Cursor row=db.rawQuery("SELECT meter,policy,observed_at,used_decimal,reset_at,source,manual FROM observations ORDER BY meter,observed_at",null)){
            while(row.moveToNext()){JSONArray item=new JSONArray();for(int i=0;i<row.getColumnCount();i++)item.put(row.getString(i));result.put(item);}
        }return result;
    }
    @Override public void onCreate(Bundle args){super.onCreate(args);phase=args.getString("phase","verify");start();}
    @Override public void onStart(){Context c=getTargetContext();Bundle result=new Bundle();try{
        check(Build.MODEL.contains("sdk")&&c.getPackageName().equals("dev.bennett.codexmeter"),"Owned stable sandbox only");
        check(SecureTokenStore.load(c)!=null&&"stable315-qa".equals(SecureTokenStore.load(c).accountId),"Existing synthetic account only");
        PackageInfo info=c.getPackageManager().getPackageInfo(c.getPackageName(),0);check(info.versionName.equals("3.1.5")&&info.getLongVersionCode()==92,"Local candidate retains current identity");
        String account=SubscriptionStore.accountKey(c);SharedPreferences p=c.getSharedPreferences("stable_redesign_continuity_qa",0);JSONObject state=BetaGrowthStore.load(c,account);
        if(phase.equals("seed")){
            check(!p.contains("growth"),"Checkpoint cannot be overwritten");
            check(p.edit().putString("account",account).putString("tokens",SecureTokenStore.load(c).toJson().toString()).putString("growth",state.toString()).putString("archive",BetaGrowthStore.archive(c,account).toString()).putString("medals",BetaMedalStore.export(c,account).toString()).putString("legacy",legacy(c,account).toString()).putString("observations",observations(c).toString()).putString("name",BetaIdentity.name(c)).putString("theme",AppPreferences.getAppTheme(c)).putString("tone",FunStore.tone(c)).putInt("uid",c.getApplicationInfo().uid).putLong("first",info.firstInstallTime).putLong("updated",info.lastUpdateTime).commit(),"Durable checkpoint saved");
        }else{
            check(p.contains("growth"),"Previous checkpoint exists");check(account.equals(p.getString("account","")),"Same account namespace");
            check(c.getApplicationInfo().uid==p.getInt("uid",-1)&&info.firstInstallTime==p.getLong("first",-1),"Same app UID and original install time");
            check(info.lastUpdateTime>p.getLong("updated",Long.MAX_VALUE),"Actual APK replacement occurred");
            check(SecureTokenStore.load(c).toJson().toString().equals(p.getString("tokens","")),"Encrypted synthetic credentials decrypt unchanged");
            JSONObject before=new JSONObject(p.getString("growth",""));for(String field:new String[]{"rules","namespace","account","cutover","total","level","progress","next_cost","last_response","days","weeks","months","rewards","honors","titles","equipped","enabled","catalog_revision"})check(before.get(field).toString().equals(state.get(field).toString()),"Preserve growth "+field);
            check(BetaGrowthStore.archive(c,account).toString().equals(p.getString("archive","")),"Exact original archive retained");check(BetaMedalStore.export(c,account).toString().equals(p.getString("medals","")),"Exact dated medal ledger retained");
            JSONObject oldLegacy=new JSONObject(p.getString("legacy","")),raw=legacy(c,account),old=RpgEngine.state(oldLegacy),now=RpgEngine.state(raw);for(String field:new String[]{"xp","level","awards","equipped_title"})check(old.get(field).toString().equals(now.get(field).toString()),"Preserve original "+field);
            check(oldLegacy.getJSONObject("future").toString().equals(raw.getJSONObject("future").toString()),"Unknown future fields retained");check(legacy(c,"b".repeat(64)).getBoolean("other_account"),"Other account retained");
            check(observations(c).toString().equals(p.getString("observations","")),"Exact existing usage observations retained");check(BetaIdentity.name(c).equals(p.getString("name","")),"Name retained");check(AppPreferences.getAppTheme(c).equals(p.getString("theme",""))&&FunStore.tone(c).equals(p.getString("tone","")),"Theme and coach preference retained");
            check(!AppPreferences.getAutomaticRefresh(c)&&!AppPreferences.getRefreshOnLaunch(c)&&!UpdatePreferences.automaticChecks(c),"Disabled automatic settings retained");
            for(String file:new String[]{BetaGrowthStore.NAME,BetaMedalStore.NAME,UsageLedgerDatabase.NAME,RpgProgressStore.NAME})try(SQLiteDatabase db=SQLiteDatabase.openDatabase(c.getDatabasePath(file).getPath(),null,SQLiteDatabase.OPEN_READONLY);Cursor row=db.rawQuery("PRAGMA integrity_check",null)){check(row.moveToFirst()&&row.getString(0).equals("ok"),"SQLite integrity "+file);}
        }
        result.putString("stream","Stable redesign continuity "+phase+": "+checks+" checks passed; synthetic only.\n");finish(-1,result);
    }catch(Throwable e){result.putString("stream",android.util.Log.getStackTraceString(e));finish(0,result);}}
}
