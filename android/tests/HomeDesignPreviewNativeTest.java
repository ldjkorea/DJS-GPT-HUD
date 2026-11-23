package dev.bennett.codexmeter;

import android.app.Instrumentation;
import android.app.Activity;
import android.app.LocaleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.view.inspector.WindowInspector;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** Capture the production home with a fresh local observation on a signed-in synthetic preview. */
public final class HomeDesignPreviewNativeTest extends Instrumentation {
    private String theme, locale;
    private boolean coachExample;
    private int checks;
    private volatile MainActivity activeHome;

    @Override public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        if(activity instanceof MainActivity)activeHome=(MainActivity)activity;
    }

    @Override public void onCreate(Bundle args) {
        super.onCreate(args);
        theme=args.getString("theme","dark");
        locale=args.getString("locale","ko");
        coachExample="true".equals(args.getString("coach_example","false"));
        start();
    }

    private void require(boolean value,String reason) {
        if (!value) throw new AssertionError(reason);
        checks++;
    }

    /** Restore absence as well as values. Only these individual fixture keys are touched. */
    private static final class SavedKey {
        final SharedPreferences prefs;
        final String key;
        final Object value;
        SavedKey(SharedPreferences prefs,String key) {
            this.prefs=prefs;this.key=key;value=prefs.getAll().get(key);
        }
        void restore() {
            SharedPreferences.Editor e=prefs.edit();
            if(value==null)e.remove(key);
            else if(value instanceof String)e.putString(key,(String)value);
            else if(value instanceof Long)e.putLong(key,(Long)value);
            else if(value instanceof Boolean)e.putBoolean(key,(Boolean)value);
            else throw new AssertionError("Unexpected preference type");
            if(!e.commit()||!Objects.equals(value,prefs.getAll().get(key)))throw new AssertionError("Fixture restoration failed");
        }
    }

    private static String hiddenReason(TextView text,String name) {
        if(text==null)return name+" absent";
        if(text.length()==0)return name+" empty";
        if(!text.isShown())return name+" hidden";
        android.text.Layout layout=text.getLayout();
        if(layout==null||layout.getLineCount()==0)return name+" not laid out";
        Rect bounds=new Rect();
        if(!text.getLocalVisibleRect(bounds))return name+" outside viewport";
        int top=text.getTotalPaddingTop()+layout.getLineTop(0);
        int bottom=text.getTotalPaddingTop()+layout.getLineBottom(0);
        if(bounds.top>top+1||bounds.bottom+1<bottom)
            return name+" first rendered line "+top+".."+bottom+" outside "+bounds;
        return null;
    }

    private String pending(MainActivity a,UsageSnapshot sample,String account) {
        if(a==null||a.isDestroyed()||a.isFinishing())return "home activity is being replaced";
        ViewGroup body=a.findViewById(R.id.dashboard_content);
        if(body==null)return "home content absent";
        if(!a.hasWindowFocus())return "home window does not have focus";
        TextView five=body.findViewWithTag("beta998:quota:five-hour:value");
        TextView week=body.findViewWithTag("beta998:quota:weekly:value");
        TextView coach=body.findViewWithTag("home:coach-text");
        TextView total=body.findViewWithTag("beta999:weekly-used");
        ViewGroup graph=body.findViewWithTag("beta999:weekly-graph");
        int digits=AppPreferences.usageDecimalPlaces(a);
        String reason=hiddenReason(five,"five-hour quota");
        if(reason==null)reason=hiddenReason(week,"weekly quota");
        if(reason==null && coach==null){
            SpicyQuotes.Selection current=SpicyQuotes.peek(a,account);
            Context app=a.getApplicationContext();
            return "coach absent; card="+(body.findViewWithTag("beta9911:coach")!=null)
                    +", tier="+(body.findViewWithTag("beta996:tier-badge")!=null)
                    +", tone="+CoachPolicy.tone(a)+", selected="+(current!=null)
                    +", locales="+a.getResources().getConfiguration().getLocales().toLanguageTags()
                    +"/"+app.getResources().getConfiguration().getLocales().toLanguageTags()
                    +", same identity="+SpicyQuotes.identity(a,account).equals(SpicyQuotes.identity(app,account));
        }
        if(reason==null)reason=hiddenReason(coach,"coach");
        if(reason!=null)return reason;
        if(!UsagePrecision.remaining(sample.fiveHour,digits).contentEquals(five.getText()))return "wrong five-hour value: "+five.getText();
        if(!UsagePrecision.remaining(sample.weekly,digits).contentEquals(week.getText()))return "wrong weekly quota: "+week.getText();
        if(total==null)return "weekly used total absent";
        if(!total.getText().toString().contains(UsagePrecision.used(sample.weekly,digits)))return "wrong weekly total: "+total.getText();
        if(graph==null||graph.getChildCount()==0)return "weekly graph absent";
        if(graph.findViewWithTag("home:weekly-error")!=null)return "weekly graph reported read failure";
        if(graph.getChildAt(0) instanceof TextView
                &&BetaExperienceUi.t(a,"관측 기록 확인 중","Loading observed records").contentEquals(((TextView)graph.getChildAt(0)).getText()))return "weekly graph loading";
        SpicyQuotes.Selection selection=SpicyQuotes.peek(a,account);
        if(locale.equals("ko")&&selection==null)return "Korean coach selection absent";
        if(locale.equals("ko")&&!selection.text.contentEquals(coach.getText()))return "rendered coach differs from selected quote";
        return null;
    }

    /** Set up only the already-checkpointed account key. Selection still runs through FunHome. */
    private void prepareLinkedExamples(Context c,String account) throws Exception {
        java.lang.reflect.Method loader=SpicyQuotes.class.getDeclaredMethod("load",Context.class);
        loader.setAccessible(true);
        CoachCatalog catalog=(CoachCatalog)loader.invoke(null,c);
        LinkedHashMap<String,CoachCatalog.Quote> byGroup=new LinkedHashMap<>();
        for(CoachCatalog.Quote quote:catalog.quotes){
            CoachCatalog.Tip tip=catalog.tips.get(quote.tipId);
            if(tip==null||!quote.tone.equals("spicy")||CoachCatalog.constrained(quote.group))continue;
            CoachCatalog.Quote previous=byGroup.get(quote.group);
            if(previous==null||quote.text.length()+tip.prompt.length()
                    <previous.text.length()+catalog.tips.get(previous.tipId).prompt.length())byGroup.put(quote.group,quote);
        }
        require(!byGroup.isEmpty(),"Bundled linked examples required");
        JSONArray hidden=new JSONArray();
        for(CoachCatalog.Quote quote:catalog.quotes)if(quote!=byGroup.get(quote.group))hidden.put(quote.id);
        JSONObject state=catalog.migrate(new JSONObject()).put("hidden",hidden);
        require(c.getSharedPreferences("coach_v2",0).edit().putString(account,state.toString()).commit(),
                "Synthetic linked examples saved");
    }

    private void ui(Runnable action){
        Throwable[] error={null};
        runOnMainSync(()->{try{action.run();}catch(Throwable e){error[0]=e;}});
        if(error[0]!=null)throw new AssertionError(error[0]);
    }

    private void screenshot(Context c,String name) throws Exception{
        Bitmap screen=getUiAutomation().takeScreenshot();
        require(screen!=null,"System screenshot available");
        File directory=new File(c.getExternalFilesDir(null),"home-design");
        require(directory.isDirectory()||directory.mkdirs(),"Screenshot directory");
        try(FileOutputStream output=new FileOutputStream(new File(directory,name+".png"))){
            require(screen.compress(Bitmap.CompressFormat.PNG,100,output),"Screenshot saved");
        }finally{screen.recycle();}
    }

    private void captureLinkedTip(MainActivity home,String account) throws Exception {
        SpicyQuotes.Selection selection=SpicyQuotes.peek(home,account);
        require(selection!=null&&selection.tip!=null,"Production selection contains a linked tip");
        ui(()->{
            View coach=home.findViewById(R.id.dashboard_content).findViewWithTag("beta9911:coach");
            ArrayList<View> actions=new ArrayList<>();
            String label=home.getString(R.string.coach_try);
            coach.findViewsWithText(actions,label,View.FIND_VIEWS_WITH_TEXT);
            Button action=null;
            for(View candidate:actions)if(candidate instanceof Button&&label.contentEquals(((Button)candidate).getText()))action=(Button)candidate;
            require(action!=null&&action.isEnabled()&&hiddenReason(action,"coach action")==null,"Linked action visible in production home");
            require(action.performClick(),"Opened actual coach action");
        });
        View[] dialog={null};
        long deadline=SystemClock.elapsedRealtime()+5000;
        do{
            waitForIdleSync();
            ui(()->{for(View root:WindowInspector.getGlobalWindowViews())
                if(root.hasWindowFocus()&&root.findViewWithTag("coach:prompt")!=null)dialog[0]=root;});
            if(dialog[0]==null)SystemClock.sleep(80);
        }while(dialog[0]==null&&SystemClock.elapsedRealtime()<deadline);
        require(dialog[0]!=null,"Actual tip dialog gained focus");
        View root=dialog[0];
        ui(()->{
            TextView prompt=root.findViewWithTag("coach:prompt");
            require(selection.tip.prompt.contentEquals(prompt.getText())&&hiddenReason(prompt,"prompt")==null,"Displayed prompt matches the selected tip");
            View notes=root.findViewWithTag("coach:guide");
            require(notes!=null&&notes.getVisibility()==View.GONE,"Guidance starts folded");
            Button copy=root.findViewById(android.R.id.button1);
            require(copy!=null&&copy.isEnabled()&&copy.isShown()&&home.getString(R.string.coach_copy).contentEquals(copy.getText()),"Explicit copy button is available");
        });
        screenshot(home,"coach-tip-"+locale+"-"+theme);
        ui(()->{
            View notes=root.findViewWithTag("coach:guide"),toggle=root.findViewWithTag("coach:guide-toggle");
            require(toggle!=null&&toggle.performClick()&&notes.getVisibility()==View.VISIBLE,"Guidance expands");
            require(toggle.performClick()&&notes.getVisibility()==View.GONE,"Guidance folds again");
            Button close=root.findViewById(android.R.id.button2);
            require(close!=null&&close.performClick(),"Closed the tip without copying");
        });
    }

    @Override public void onStart() {
        Context c=getTargetContext();
        Bundle result=new Bundle();
        List<SavedKey> saved=new ArrayList<>();
        LocaleManager languages=c.getSystemService(LocaleManager.class);
        LocaleList previousLanguages=languages.getApplicationLocales();
        MainActivity activity=null;
        Throwable error=null;
        try{
            require(Build.MODEL.contains("sdk")&&"dev.bennett.codexmeter".equals(c.getPackageName()),"Owned Android emulator required");
            AuthTokens auth=SecureTokenStore.load(c);
            require(auth!=null&&"stable316-qa".equals(auth.accountId)&&auth.accessToken.startsWith("SYNTHETIC_"),"Synthetic fixture required");
            require(!UpdatePreferences.automaticChecks(c)&&!AppPreferences.getRefreshOnLaunch(c)&&!AppPreferences.getAutomaticRefresh(c),
                    "Disable automatic requests on the synthetic fixture before capture");
            require(!c.getSharedPreferences("codex_lan_sync",0).contains(LanSync.account(c))&&!AppPreferences.isOAuthPending(c),"Unpaired fixture with no OAuth in progress required");
            String account=SubscriptionStore.accountKey(c);
            require(account!=null,"Synthetic account namespace required");
            SharedPreferences prefs=c.getSharedPreferences("codex_meter_settings_v1",0);
            for(String key:new String[]{"last_snapshot","last_error","last_error_at","app_theme"})saved.add(new SavedKey(prefs,key));
            saved.add(new SavedKey(c.getSharedPreferences("coach_v2",0),account));
            saved.add(new SavedKey(c.getSharedPreferences("codex_fun_settings",0),"tone"));
            saved.add(new SavedKey(BetaIdentity.prefs(c),"name"));
            saved.add(new SavedKey(BetaIdentity.prefs(c),"setup_done"));
            if(coachExample){
                require("ko".equals(locale),"Linked example requires the Korean catalog");
                prepareLinkedExamples(c,account);
            }
            long now=System.currentTimeMillis();
            UsageSnapshot sample=new UsageSnapshot("plus",true,false,
                    new UsageWindow(38,18000,0,(now+96*60000L)/1000),
                    new UsageWindow(19,604800,0,(now+3*86400000L)/1000),now);
            require(AppPreferences.saveSnapshot(c,sample),"Fresh fixture cached");
            require(FunStore.tone(c,"spicy"),"Built-in coach enabled");
            BetaIdentity.save(c,"해솔");
            AppPreferences.setAppTheme(c,theme);
            languages.setApplicationLocales(LocaleList.forLanguageTags(locale));
            activity=(MainActivity)startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            long deadline=SystemClock.elapsedRealtime()+25000;
            String[] reason={"no resumed home"};
            do{
                waitForIdleSync();
                runOnMainSync(()->reason[0]=pending(activeHome,sample,account));
                if(reason[0]!=null)SystemClock.sleep(140);
            }while(reason[0]!=null&&SystemClock.elapsedRealtime()<deadline);
            require(reason[0]==null,"Home preview never settled: "+reason[0]);
            MainActivity home=activeHome;
            require(Ui.isDark(home)==theme.equals("dark"),"Actual home theme");
            require(home.getResources().getConfiguration().getLocales().get(0).getLanguage().equals(locale),"Actual home locale");
            screenshot(c,(coachExample?"coach-home-":"home-")+locale+"-"+theme);
            if(coachExample)captureLinkedTip(home,account);
        }catch(Throwable problem){error=problem;}
        finally{
            try{
                if(activity!=null){
                    MainActivity home=activeHome!=null?activeHome:activity;
                    runOnMainSync(home::finish);
                    waitForIdleSync();
                    // The normal home can queue more than one selection while it attaches.
                    java.lang.reflect.Field worker=FunHome.class.getDeclaredField("WORKER");
                    worker.setAccessible(true);
                    ((ExecutorService)worker.get(null)).submit(()->{}).get(20,TimeUnit.SECONDS);
                }
            }catch(Throwable cleanup){if(error==null)error=cleanup;else error.addSuppressed(cleanup);}
            for(SavedKey setting:saved){
                try{setting.restore();}catch(Throwable cleanup){if(error==null)error=cleanup;else error.addSuppressed(cleanup);}
            }
            try{
                if(!languages.getApplicationLocales().equals(previousLanguages))languages.setApplicationLocales(previousLanguages);
            }catch(Throwable cleanup){if(error==null)error=cleanup;else error.addSuppressed(cleanup);}
        }
        result.putString("stream",error==null?"Home design preview: "+checks+" checks passed; synthetic only; "+locale+" "+theme+"\n":android.util.Log.getStackTraceString(error));
        finish(error==null?-1:0,result);
    }
}
