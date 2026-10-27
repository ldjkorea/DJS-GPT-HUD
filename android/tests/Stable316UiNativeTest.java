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

/** Synthetic, task-owned emulator only. Exercises actual Activities and SQLite, never OAuth. */
public final class Stable316UiNativeTest extends Instrumentation {
    int checks; Throwable failure; String phase,locale,theme; boolean longName;
    void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    void main(Runnable action){runOnMainSync(()->{try{action.run();}catch(Throwable t){failure=t;}});if(failure!=null){Throwable t=failure;failure=null;throw new AssertionError(t);}}
    void workers()throws Exception{BetaGrowthStore.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);BetaMedalStore.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);BetaWeeklyUi.WORKER.submit(()->{}).get(30,TimeUnit.SECONDS);waitForIdleSync();}
    static String text(View v){StringBuilder b=new StringBuilder();if(v instanceof TextView)b.append(((TextView)v).getText()).append('\n');if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)b.append(text(((ViewGroup)v).getChildAt(i)));return b.toString();}
    void ready(Activity a,String tag)throws Exception{for(int i=0;i<80;i++){workers();boolean[] found={false};main(()->found[0]=a.findViewById(android.R.id.content).findViewWithTag(tag)!=null);if(found[0])return;Thread.sleep(100);}throw new AssertionError("Missing "+tag);}
    void bounds(View v){if(v==null||!v.isShown())return;if(v instanceof TextView){TextView t=(TextView)v;if(t.getLayout()!=null&&t.length()>0){android.text.Layout l=t.getLayout();check(l.getEllipsisCount(l.getLineCount()-1)==0,"No ellipsis: "+t.getText());check(t.getHeight()+2>=t.getCompoundPaddingTop()+t.getCompoundPaddingBottom()+l.getLineBottom(l.getLineCount()-1),"Text fits: "+t.getText());}}if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)bounds(((ViewGroup)v).getChildAt(i));}
    void shot(Context c,String name)throws Exception{waitForIdleSync();Thread.sleep(250);Bitmap bitmap=getUiAutomation().takeScreenshot();check(bitmap!=null,"Screenshot available");java.io.File dir=new java.io.File(c.getExternalFilesDir(null),"stable316");dir.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}}
    int top(View v){int[] p=new int[2];v.getLocationOnScreen(p);return p[1];}
    JSONObject metrics(MainActivity a)throws Exception{JSONObject out=new JSONObject();View content=a.findViewById(R.id.dashboard_content),summary=content.findViewWithTag("beta999:summary"),weekly=content.findViewWithTag("beta999:weekly");dev.oneuiproject.oneui.layout.ToolbarLayout bar=a.findViewById(R.id.toolbar_layout);float d=a.getResources().getDisplayMetrics().density;out.put("toolbar_top_dp",top(bar.getToolbar())/d).put("summary_top_dp",top(summary)/d).put("summary_height_dp",summary.getHeight()/d).put("weekly_top_dp",top(weekly)/d).put("weekly_height_dp",weekly.getHeight()/d).put("screen_height_dp",a.getResources().getConfiguration().screenHeightDp).put("font",a.getResources().getConfiguration().fontScale).put("locale",locale).put("theme",theme);return out;}
    @Override public void onCreate(Bundle args){super.onCreate(args);phase=args.getString("phase","verify");locale=args.getString("locale","ko");theme=args.getString("theme","dark");longName="true".equals(args.getString("long_name","false"));start();}
    @Override public void onStart(){Context c=getTargetContext();Bundle result=new Bundle();Activity active=null;String originalTone=null;try{
        check(Build.MODEL.contains("sdk")&&c.getPackageName().equals("dev.bennett.codexmeter"),"Owned emulator stable package");
        check("stable316-qa".equals(SecureTokenStore.load(c).accountId),"Synthetic fixture required");
        BetaIdentity.save(c,longName?"정식 화면 검수 · 긴 이름도 확인합니다":"정식 검수자");
        String account=SubscriptionStore.accountKey(c);
        if(BetaMedalStore.export(c,account)==null){JSONObject medals=BetaMedalModel.empty(account,System.currentTimeMillis()-20L*86400000,false);long at=System.currentTimeMillis()-120000;
            BetaMedalModel.observe(medals,account,new LedgerRecord("weekly","plus",at,10,"10",at+86400000,604800,"api_precise",false),at);
            BetaMedalModel.observe(medals,account,new LedgerRecord("weekly","plus",at+60000,20,"20",at+86400000,604800,"api_precise",false),at+60000);BetaMedalStore.restore(c,account,medals);}
        JSONObject before=BetaGrowthStore.load(c,account);String medalBefore=BetaMedalStore.export(c,account).toString();
        originalTone=FunStore.tone(c);FunStore.tone(c,"playful");AppPreferences.setAppTheme(c,theme);c.getSystemService(android.app.LocaleManager.class).setApplicationLocales(LocaleList.forLanguageTags(locale));
        active=startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));MainActivity home=(MainActivity)active;ready(home,"beta999:weekly");ready(home,"beta9911:coach");workers();Thread.sleep(400);
        JSONObject[] measured={null};main(()->{try{check(home.getResources().getConfiguration().getLocales().get(0).getLanguage().equals(locale),"Actual activity locale matches requested locale");measured[0]=metrics(home);bounds(home.findViewById(R.id.dashboard_content));}catch(Exception e){throw new RuntimeException(e);}});
        String suffix=phase+"-"+locale+"-"+theme+"-"+c.getResources().getConfiguration().fontScale+"-"+c.getResources().getConfiguration().screenWidthDp+"x"+c.getResources().getConfiguration().screenHeightDp;java.io.File dir=new java.io.File(c.getExternalFilesDir(null),"stable316");dir.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,"metrics-"+suffix+".json"))){out.write(measured[0].toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));}shot(c,"redesign-home-"+suffix);
        if(!phase.equals("before")){
            main(()->{LinearLayout body=home.findViewById(R.id.dashboard_content);View summary=body.findViewWithTag("beta999:summary"),weekly=body.findViewWithTag("beta999:weekly"),coach=body.findViewWithTag("beta9911:coach");check(top(summary)<top(weekly)&&top(weekly)<top(coach),"Profile then weekly then coach");check(body.findViewWithTag("beta998:quota:five-hour:value")!=null&&body.findViewWithTag("beta998:quota:weekly:value")!=null,"Both remaining limits retained");check(body.findViewWithTag("beta996:tier-badge")!=null&&body.findViewWithTag("beta996:level")!=null,"Tier and level retained");View gear=((dev.oneuiproject.oneui.layout.ToolbarLayout)home.findViewById(R.id.toolbar_layout)).getToolbar().findViewWithTag("beta9915:settings");float d=home.getResources().getDisplayMetrics().density;check(gear.getWidth()+1>=48*d&&gear.getHeight()+1>=48*d,"Settings target 48dp");check(measured[0].optDouble("toolbar_top_dp")<65,"No duplicate top safe area");});
            main(home::finish);active=null;
            active=startActivitySync(new Intent(c,RecordsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));RecordsActivity records=(RecordsActivity)active;ready(records,"beta9911:records");
            main(()->{records.month=YearMonth.of(2026,10);records.anchor=LocalDate.of(2026,10,26);records.move(false,1);check(records.anchor.equals(LocalDate.of(2026,11,1)),"Next week includes Nov1");records.move(false,-1);check(records.anchor.equals(LocalDate.of(2026,10,31)),"Previous week returns Oct31");bounds(records.body);});shot(c,"redesign-records-"+suffix);main(records::finish);active=null;
            active=startActivitySync(new Intent(c,BetaCatalogActivity.class).putExtra("titles",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));BetaCatalogActivity catalog=(BetaCatalogActivity)active;ready(catalog,"stable-redesign:collection-summary");
            String stored=BetaMedalStore.export(c,account).toString();try(BetaMedalStore db=new BetaMedalStore(c)){db.getWritableDatabase().execSQL("UPDATE medals SET document=? WHERE account=?",new Object[]{"{",account});}
            main(catalog::load);workers();main(()->{check(catalog.medalLoadFailed&&catalog.state!=null,"Real medal DB error retains growth");check(catalog.body.findViewWithTag("stable-redesign:catalog-error")!=null,"Catalog error visible");});
            try(BetaMedalStore db=new BetaMedalStore(c)){db.getWritableDatabase().execSQL("UPDATE medals SET document=? WHERE account=?",new Object[]{stored,account});}
            main(catalog::load);workers();main(()->{check(!catalog.medalLoadFailed&&!catalog.loadFailed,"Catalog retry succeeds");check(catalog.body.findViewWithTag("stable-redesign:catalog-filter:1").isSelected(),"Owned filter selected");check(catalog.stacked()==(catalog.getResources().getConfiguration().fontScale>1.3f||catalog.getResources().getConfiguration().screenWidthDp<360),"Catalog responds to width and font");bounds(catalog.body);});shot(c,"redesign-catalog-"+suffix);main(catalog::finish);active=null;
            active=startActivitySync(new Intent(c,MeterSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity settings=active;ready(settings,"stable-redesign:update-settings");
            main(()->{View row=settings.findViewById(android.R.id.content).findViewWithTag("stable-redesign:update-settings");android.view.accessibility.AccessibilityNodeInfo info=row.createAccessibilityNodeInfo();check(Button.class.getName().contentEquals(info.getClassName()),"Settings row exposes button role");View advanced=settings.findViewById(android.R.id.content).findViewWithTag("beta995:advanced");LinearLayout holder=(LinearLayout)advanced.getParent();LinearLayout legacy=(LinearLayout)holder.getChildAt(1);check(legacy.getChildCount()==0,"Advanced content is lazy");advanced.performClick();check(legacy.getChildCount()>0&&legacy.getVisibility()==View.VISIBLE,"Advanced creates on demand");bounds(settings.findViewById(R.id.dashboard_content));});shot(c,"redesign-settings-"+suffix);main(settings::finish);active=null;
            JSONObject after=BetaGrowthStore.load(c,account);for(String field:new String[]{"total","level","progress","honors","titles","rewards","equipped"})check(before.get(field).toString().equals(after.get(field).toString()),"No UI award or loss: "+field);check(medalBefore.equals(BetaMedalStore.export(c,account).toString()),"Medal record unchanged");
        }
        result.putString("stream","Stable redesign native: "+checks+" checks passed; "+suffix+"; synthetic only.\n");finish(-1,result);
    }catch(Throwable e){result.putString("stream",android.util.Log.getStackTraceString(e));finish(0,result);}finally{if(active!=null){Activity last=active;main(last::finish);}if(originalTone!=null)FunStore.tone(c,originalTone);}}
}
