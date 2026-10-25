package dev.bennett.codexmeter;

import android.app.*;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.util.concurrent.TimeUnit;
import org.json.*;

/** Synthetic owned-AVD checks of the actual Activities and SQLite failure presentation. */
public final class Stable315UiNativeTest extends Instrumentation {
    int checks;Throwable failure;String locale,theme;boolean longName;
    void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    void main(Runnable action){runOnMainSync(()->{try{action.run();}catch(Throwable t){failure=t;}});if(failure!=null){Throwable t=failure;failure=null;throw new AssertionError(t);}}
    static String text(View view){StringBuilder result=new StringBuilder();if(view instanceof TextView)result.append(((TextView)view).getText()).append('\n');if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)result.append(text(((ViewGroup)view).getChildAt(i)));return result.toString();}
    void workers()throws Exception{BetaGrowthStore.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);BetaMedalStore.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);waitForIdleSync();}
    void ready(Activity activity,String tag)throws Exception{for(int i=0;i<100;i++){workers();final boolean[] found={false};main(()->found[0]=activity.findViewById(android.R.id.content).findViewWithTag(tag)!=null);if(found[0])return;Thread.sleep(100);}throw new AssertionError("Not ready: "+tag);}
    void bounds(View view){
        if(view==null||!view.isShown())return;
        if(view instanceof TextView){TextView t=(TextView)view;if(t.getLayout()!=null&&t.length()>0){android.text.Layout l=t.getLayout();check(l.getEllipsisCount(l.getLineCount()-1)==0,"No new-body ellipsis: "+t.getText());check(t.getHeight()+2>=t.getCompoundPaddingTop()+t.getCompoundPaddingBottom()+l.getLineBottom(l.getLineCount()-1),"No clipped new-body text: "+t.getText());}}
        if(view.isClickable()&&view.getTag()!=null&&view.getTag().toString().startsWith("beta9911:day:"))check(view.getWidth()+1>=48*view.getResources().getDisplayMetrics().density,"Calendar date at least 48dp wide");
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)bounds(((ViewGroup)view).getChildAt(i));
    }
    int accountBodies(View view){int found=0;if(view instanceof ScrollView&&((ScrollView)view).getChildCount()>0){bounds(((ScrollView)view).getChildAt(0));return 1;}if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)found+=accountBodies(((ViewGroup)view).getChildAt(i));return found;}
    void rootControls(Activity a){
        check(a.getResources().getConfiguration().getLocales().get(0).getLanguage().equals(locale),"Actual Activity language matches requested "+locale);
        dev.oneuiproject.oneui.layout.ToolbarLayout bar=a.findViewById(R.id.toolbar_layout);View gear=bar.getToolbar().findViewWithTag("beta9915:settings");
        check(gear!=null&&gear.isShown(),"Root settings shown");float density=a.getResources().getDisplayMetrics().density;
        check(gear.getWidth()+1>=48*density&&gear.getHeight()+1>=48*density,"Root settings has measured 48dp target");
        android.graphics.Rect visible=new android.graphics.Rect();a.getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);int[] pos=new int[2];gear.getLocationOnScreen(pos);
        check(visible.contains(pos[0]+gear.getWidth()/2,pos[1]+gear.getHeight()/2),"Root settings center within visible screen");
    }
    void shot(Context c,String name)throws Exception{waitForIdleSync();Thread.sleep(500);Bitmap bitmap=getUiAutomation().takeScreenshot();if(bitmap==null)throw new IllegalStateException("No screenshot");java.io.File folder=new java.io.File(c.getExternalFilesDir(null),"stable315");folder.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}}
    @Override public void onCreate(Bundle args){super.onCreate(args);locale=args.getString("locale","ko");theme=args.getString("theme","dark");longName="true".equals(args.getString("long_name","false"));start();}
    @Override public void onStart(){
        Bundle result=new Bundle();int code=0;Activity active=null;Context c=getTargetContext();
        try{
            check(Build.FINGERPRINT.contains("generic")||Build.MODEL.contains("sdk"),"Emulator only");
            check(c.getPackageName().equals("dev.bennett.codexmeter"),"Stable sandbox only");
            check(c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName.equals("3.1.5"),"Candidate version installed");
            check("stable315-qa".equals(SecureTokenStore.load(c).accountId),"Existing synthetic stable fixture required");UpdatePreferences.setAutomaticChecks(c,false);BetaIdentity.save(c,longName?"정식 화면 검수 · 긴 이름도 확인합니다":"정식 검수자");
            AppPreferences.setAppTheme(c,theme);c.getSystemService(android.app.LocaleManager.class).setApplicationLocales(LocaleList.forLanguageTags(locale));Thread.sleep(350);
            String account=SubscriptionStore.accountKey(c);long now=System.currentTimeMillis();
            JSONObject medal=BetaMedalModel.empty(account,now-20L*86400000,false);
            for(int i=14;i>=0;i--){LocalDate day=BetaV4.day(now).minusDays(i);long at=i==0?now-120000:day.atStartOfDay(BetaV4.ZONE).toInstant().toEpochMilli()+3600000;long reset=at+86400000;
                BetaMedalModel.observe(medal,account,new LedgerRecord("weekly","free",at,10,"10",reset,604800,"api_precise",false),at);
                BetaMedalModel.observe(medal,account,new LedgerRecord("weekly","free",at+60000,20,"20",reset,604800,"api_precise",false),at+60000);
            }
            BetaMedalStore.restore(c,account,medal);workers();
            JSONObject growthBefore=BetaGrowthStore.load(c,account);long xp=growthBefore.getLong("total");String acquired=growthBefore.getJSONObject("honors").toString(),titles=growthBefore.getJSONObject("titles").toString();

            active=startActivitySync(new Intent(c,RecordsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));RecordsActivity records=(RecordsActivity)active;ready(records,"beta9911:records");workers();Thread.sleep(250);
            main(()->{rootControls(records);bounds(records.body);});shot(c,"records-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale);
            String stored=BetaMedalStore.export(c,account).toString();
            try(BetaMedalStore db=new BetaMedalStore(c)){db.getWritableDatabase().execSQL("UPDATE medals SET document=? WHERE account=?",new Object[]{"{",account});}
            main(()->records.load());workers();Thread.sleep(150);
            main(()->{check(records.loadFailed&&records.state!=null,"SQLite failure retains last good same-account record");records.move(true,-1);check(records.loadFailed,"Month change retains failure state");records.state=null;records.render();check(records.body.findViewWithTag("beta9915:records-error")!=null,"Failure has an explicit retry state");check(records.body.findViewWithTag("beta9911:calendar")==null&&records.body.findViewWithTag("beta9911:calendar-list")==null,"Failure never renders zero-day calendar");});
            try(BetaMedalStore db=new BetaMedalStore(c)){db.getWritableDatabase().execSQL("UPDATE medals SET document=? WHERE account=?",new Object[]{stored,account});}
            main(records::load);workers();main(()->check(!records.loadFailed,"Retry recovers actual SQLite record"));
            AuthTokens fixtureTokens=SecureTokenStore.load(c);
            synchronized(UsageApi.NETWORK_LOCK){SecureTokenStore.save(c,new AuthTokens("SYNTHETIC_UI_OTHER_ACCOUNT","SYNTHETIC_REFRESH","",now+86400000,"synthetic-ui-other-account",""));}
            main(()->{records.load();check(records.state==null,"Account transition clears the old record immediately");});
            synchronized(UsageApi.NETWORK_LOCK){SecureTokenStore.save(c,fixtureTokens);}
            main(records::load);workers();main(()->check(records.state!=null&&!records.loadFailed,"Original account record returns after transition"));main(records::finish);active=null;

            active=startActivitySync(new Intent(c,BetaCatalogActivity.class).putExtra("titles",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));BetaCatalogActivity catalog=(BetaCatalogActivity)active;workers();Thread.sleep(150);
            main(()->{try{JSONObject viewOnly=new JSONObject(growthBefore.toString());viewOnly.put("titles",new JSONObject());catalog.state=viewOnly;catalog.filter=1;catalog.render();check(catalog.body.findViewWithTag("beta9911:collectible:medal.title1")!=null,"Medal-only title remains visible");check(!text(catalog.body).contains(BetaCopy.t(catalog,"no_items")),"Visible medals exclude empty catalog message");check(!BetaCatalog.name("rpg4.title_gold_months_3","x",true).equals(BetaMedalUi.name(catalog,"medal.title3")),"Different evidence has a distinct title");bounds(catalog.body);}catch(Exception e){throw new RuntimeException(e);}});main(catalog::finish);active=null;

            active=startActivitySync(new Intent(c,GrowthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));GrowthActivity growth=(GrowthActivity)active;ready(growth,"beta9911:next-goal");main(()->{rootControls(growth);bounds(growth.findViewById(R.id.dashboard_content));});shot(c,"growth-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale);
            ActivityMonitor monitor=addMonitor(RecordsActivity.class.getName(),null,false);main(()->check(growth.findViewById(R.id.dashboard_content).findViewWithTag("beta9915:medal-records").performClick(),"Actual record action clicked"));Activity destination=waitForMonitorWithTimeout(monitor,10000);removeMonitor(monitor);check(destination instanceof RecordsActivity,"Medal records opens RecordsActivity");main(destination::finish);main(growth::finish);active=null;

            active=startActivitySync(new Intent(c,MeterSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity settings=active;ready(settings,"beta9915:coach-setting");main(()->{View setting=settings.findViewById(android.R.id.content).findViewWithTag("beta9915:coach-setting");check(setting.isShown(),"Coach ON/OFF visible without advanced expansion");bounds(settings.findViewById(R.id.dashboard_content));});shot(c,"settings-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale);main(settings::finish);active=null;

            active=startActivitySync(new Intent(c,BetaHubActivity.class).putExtra("page","health").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));BetaHubActivity health=(BetaHubActivity)active;waitForIdleSync();main(health::recheckRecords);workers();Thread.sleep(150);main(()->{check(!health.healthBusy&&!health.healthFailed,"Recovery recheck finishes in place");check(health.body.findViewWithTag("beta9915:recheck-result")!=null,"Recovery result visible on same screen");});main(health::finish);active=null;

            active=startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));MainActivity home=(MainActivity)active;ready(home,"beta999:weekly");Thread.sleep(500);waitForIdleSync();
            main(()->rootControls(home));
            main(()->{View root=home.findViewById(R.id.dashboard_content);check(root.findViewWithTag("beta999:summary")!=null,"Tier and quota summary retained");check(root.findViewWithTag("beta998:quota:five-hour:value")!=null&&root.findViewWithTag("beta998:quota:weekly:value")!=null,"Both primary remaining limits visible");check(root.findViewWithTag("beta999:weekly-period").getVisibility()==View.VISIBLE,"Quota-cycle dates visible");bounds(root);});shot(c,"home-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale);
            BetaWeeklyUi.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);waitForIdleSync();
            main(()->{java.util.List<BetaWeeklyUsage.Day> days=new java.util.ArrayList<>();for(int i=0;i<3;i++)days.add(new BetaWeeklyUsage.Day(BetaV4.day(now).minusDays(2-i),false));days.get(0).measured=true;days.get(0).points=0;days.get(2).measured=true;days.get(2).points=4;LinearLayout graph=home.findViewById(R.id.dashboard_content).findViewWithTag("beta999:weekly-graph");BetaWeeklyUi.render(home,graph,new BetaWeeklyUsage.Model(now-86400000,now+86400000,now,days),Ui.isDark(home));check(text(graph).contains("0")&&text(graph).contains("—"),"Zero and missing observations have different visible marks");});
            main(()->home.onNewIntent(new Intent(c,MainActivity.class).putExtra("matte_action","account")));waitForIdleSync();
            main(()->{int found=0;for(View window:android.view.inspector.WindowInspector.getGlobalWindowViews())if(window.isShown())found+=accountBodies(window);check(found>0,"Actual account dialog body measured");});shot(c,"account-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale);sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
            dev.oneuiproject.oneui.layout.ToolbarLayout toolbar=home.findViewById(R.id.toolbar_layout);ActivityMonitor settingsMonitor=addMonitor(MeterSettingsActivity.class.getName(),null,false);main(()->check(toolbar.getToolbar().findViewWithTag("beta9915:settings").performClick(),"Root settings action works"));Activity opened=waitForMonitorWithTimeout(settingsMonitor,10000);removeMonitor(settingsMonitor);check(opened instanceof MeterSettingsActivity,"Settings icon destination");main(opened::finish);main(home::finish);active=null;
            JSONObject after=BetaGrowthStore.load(c,account);check(after.getLong("total")==xp,"UI visits and recovery award no XP");check(after.getJSONObject("honors").toString().equals(acquired)&&after.getJSONObject("titles").toString().equals(titles),"Growth acquisition records unchanged");check(BetaMedalStore.export(c,account).toString().equals(stored),"Medal ledger unchanged after UI and recovery");
            result.putString("stream","Stable315 UI native: "+checks+" checks passed; "+locale+" / "+theme+" / font "+c.getResources().getConfiguration().fontScale+"; actual SQLite failure, retry, navigation and no-XP.\n");code=-1;
        }catch(Throwable error){result.putString("stream",android.util.Log.getStackTraceString(error));}
        finally{if(active!=null){Activity last=active;main(last::finish);}}
        finish(code,result);
    }
}
