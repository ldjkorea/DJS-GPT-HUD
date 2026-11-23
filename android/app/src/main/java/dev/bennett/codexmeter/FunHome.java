package dev.bennett.codexmeter;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Home-only presentation over the unchanged 2.8.4 interpretation engine and stores. */
final class FunHome {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    private static String cachedStamp="",cachedQuote="",cachedCoachIdentity="";private static LiveUtilization.Result cachedLive;private static double cachedDaily;
    private static final int[] TIERS={R.string.fun_tier_0,R.string.fun_tier_1,R.string.fun_tier_2,R.string.fun_tier_3,R.string.fun_tier_4,R.string.fun_tier_5,R.string.fun_tier_6,R.string.fun_tier_7,R.string.fun_tier_8,R.string.fun_tier_9};
    private static final int[] BADGES={R.drawable.badge_iron,R.drawable.badge_bronze,R.drawable.badge_silver,R.drawable.badge_gold,R.drawable.badge_platinum,R.drawable.badge_emerald,R.drawable.badge_diamond,R.drawable.badge_master,R.drawable.badge_grandmaster,R.drawable.badge_challenger};
    private final AppCompatActivity activity;private final LinearLayout target,crestTarget;private final boolean dark;private final Runnable finished;
    private FunHome(AppCompatActivity activity,LinearLayout target,boolean dark,Runnable finished,LinearLayout crestTarget){this.activity=activity;this.target=target;this.dark=dark;this.finished=finished;this.crestTarget=crestTarget;}
    static void add(AppCompatActivity activity,LinearLayout parent,boolean dark,Runnable finished){add(activity,parent,dark,finished,null);}
    static void add(AppCompatActivity activity,LinearLayout parent,boolean dark,Runnable finished,LinearLayout crestTarget){
        LiveUsageStore.schedule(activity);BetaMedalStore.refresh(activity);
        LinearLayout target=new LinearLayout(activity);target.setOrientation(LinearLayout.VERTICAL);parent.addView(target);
        FunHome home=new FunHome(activity,target,dark,finished,crestTarget);target.addView(LedgerUi.caption(activity,activity.getString(R.string.next_loading),dark));
        UsageSnapshot snapshot=AppPreferences.loadSnapshot(activity);String key=SubscriptionStore.key(activity,snapshot);
        String stamp=LiveUsageStore.signature(activity,snapshot);
        String account=SubscriptionStore.accountKey(activity);
        String coachIdentity=SpicyQuotes.cacheKey(activity,account);
        if(key==null){target.removeAllViews();target.addView(LedgerUi.caption(activity,activity.getString(R.string.fun_placing),dark));return;}
        if(stamp.equals(cachedStamp)&&cachedLive!=null&&SpicyQuotes.cacheKey(activity,account).equals(cachedCoachIdentity)){try{UsageWindow w=snapshot.longWindow();long now=System.currentTimeMillis();boolean fresh=w!=null&&now<w.effectiveResetAtMillis(snapshot.fetchedAtMillis)&&UsageEvidence.state(snapshot,w,!AppPreferences.getLastError(activity).isEmpty(),now,RefreshScheduler.effectiveRefreshMinutes(activity))==UsageEvidence.State.CONFIRMED;
            SpicyQuotes.Selection selection=SpicyQuotes.peek(activity,account);String quote=selection==null?null:selection.text;home.render(snapshot,w,"",java.util.Collections.emptyList(),java.util.Collections.emptyList(),new JSONObject(),cachedLive,cachedDaily,quote);return;
        }catch(Exception ignored){/* Fall back to the guarded worker. */}}
        Runnable load = ()->{try{
            RpgProgressStore.backup(activity.getApplicationContext());UsageLedgerDatabase.Data data=UsageLedgerDatabase.load(activity.getApplicationContext());UsageWindow window=snapshot.longWindow();
            String policy=(snapshot.weekly!=null?"weekly":"monthly")+"|"+LedgerRecord.cleanPlan(snapshot.planType)+"|"+(window==null?0:window.windowSeconds);
            List<FunInsights.Window> completed=FunInsights.completed(data.records,policy,System.currentTimeMillis());
            if(!key.equals(SubscriptionStore.key(activity.getApplicationContext(),AppPreferences.loadSnapshot(activity.getApplicationContext()))))return;
            if(!FunStore.settle(activity.getApplicationContext(),key,completed,(tier,tone,variant)->FunCoach.text(activity.getApplicationContext(),tone,tier,FunInsights.Situation.TIER,variant)))throw new IllegalStateException();
            TierStore.evaluate(activity.getApplicationContext(),key,policy,completed);
            JSONObject doc=FunStore.load(activity.getApplicationContext(),key);
            LiveUtilization.Result live=LiveUsageStore.calculate(activity.getApplicationContext(),snapshot,data);
            double daily=LiveCards.daily(data,policy,System.currentTimeMillis());
            long quoteAt=System.currentTimeMillis();boolean quoteFresh=window!=null&&quoteAt<window.effectiveResetAtMillis(snapshot.fetchedAtMillis)&&UsageEvidence.state(snapshot,window,!AppPreferences.getLastError(activity).isEmpty(),quoteAt,RefreshScheduler.effectiveRefreshMinutes(activity))==UsageEvidence.State.CONFIRMED;
            if(!stamp.equals(LiveUsageStore.signature(activity.getApplicationContext(),AppPreferences.loadSnapshot(activity.getApplicationContext()))))return;
            RpgProgressStore.reconcile(activity.getApplicationContext(),snapshot,data,account);
            // Per-app languages can differ between the activity and application during recreation.
            String quote=SpicyQuotes.select(activity,account,snapshot,quoteFresh,live.bonus,daily,quoteAt);
            activity.runOnUiThread(()->{if(activity.isDestroyed()||activity.isFinishing()||!target.isAttachedToWindow()||!key.equals(SubscriptionStore.key(activity,AppPreferences.loadSnapshot(activity)))||!stamp.equals(LiveUsageStore.signature(activity,AppPreferences.loadSnapshot(activity)))||!coachIdentity.equals(SpicyQuotes.cacheKey(activity,account)))return;
                try{cachedStamp=stamp;cachedCoachIdentity=coachIdentity;cachedLive=live;cachedDaily=daily;cachedQuote=quote;home.render(snapshot,window,policy,data.records,completed,doc,live,daily,quote);}catch(Exception ignored){home.error();}});
        }catch(Exception ignored){activity.runOnUiThread(()->{if(!activity.isDestroyed()&&target.isAttachedToWindow())home.error();});}};
        // onCreate builds the first home before its window attaches. An old, replaced target
        // must not render, but a new target needs one load when it actually reaches the window.
        target.post(() -> {
            if (!activity.isDestroyed() && !activity.isFinishing() && target.isAttachedToWindow()) {
                WORKER.execute(load);
            }
        });
    }
    private boolean merged(){return crestTarget!=null&&"beta999:identity-slot".equals(crestTarget.getTag());}
    private void error(){if(crestTarget!=null&&!merged())crestTarget.removeAllViews();target.removeAllViews();target.addView(LedgerUi.caption(activity,getString(R.string.fun_load_failed),dark));finished.run();}
    private String getString(int id,Object... args){return activity.getString(id,args);}
    private void render(UsageSnapshot snapshot,UsageWindow window,String policy,List<LedgerRecord> records,List<FunInsights.Window> completed,JSONObject doc,LiveUtilization.Result live,double daily,String quote)throws Exception{
        target.removeAllViews();if(crestTarget!=null)crestTarget.removeAllViews();long now=System.currentTimeMillis();
        boolean fresh=window!=null&&now<window.effectiveResetAtMillis(snapshot.fetchedAtMillis)&&UsageEvidence.state(snapshot,window,!AppPreferences.getLastError(activity).isEmpty(),now,RefreshScheduler.effectiveRefreshMinutes(activity))==UsageEvidence.State.CONFIRMED;
        LinearLayout tier=merged()?BetaHomeUi.card(activity,dark,false):LiveCards.crest(activity,dark,live,fresh);
        if(crestTarget!=null){crestTarget.addView(tier);if(merged())TierTheme.frame((android.view.View)crestTarget.getParent(),TierTheme.tier(activity),dark);else Ui.addSpacer(crestTarget,10);}else target.addView(tier);
        LinearLayout quotation=LiveCards.ai(activity,dark,live,snapshot,fresh,daily,now,quote);
        target.addView(quotation);finished.run();
    }
    private boolean stacked(){return LedgerUi.stacked(activity)||activity.getResources().getConfiguration().screenWidthDp<420;}
    private static String num(double value){return LedgerUi.number(value);}
    private static int styleId(FunInsights.Style style){switch(style){case FOCUS:return R.string.fun_style_focus;case SPRINT:return R.string.fun_style_sprint;case RESERVE:return R.string.fun_style_reserve;default:return R.string.fun_style_steady;}}
}
