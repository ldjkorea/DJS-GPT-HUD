package dev.bennett.codexmeter;

import android.content.Intent;
import android.view.*;
import android.widget.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

/** Current weekly quota plus bounded observed day changes; no synchronous database work. */
final class BetaWeeklyUi {
    static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    private static String t(android.content.Context c,String ko,String en,Object... args){return BetaExperienceUi.t(c,ko,en,args);}
    static void add(MainActivity a,LinearLayout parent,UsageSnapshot snapshot,boolean dark,java.util.List<Runnable> clocks){
        LinearLayout card=BetaUi.linkedGroup(parent,t(a,"이번 주간 사용량","This week's usage"),t(a,"상세","Details"),dark,()->a.startActivity(new Intent(a,LedgerAnalyticsActivity.class).putExtra("medal_usage_details",true)));card.setTag("beta999:weekly");
        UsageWindow window=snapshot==null?null:snapshot.weekly;
        if(window==null){
            card.addView(BetaUi.body(a,snapshot==null?t(a,"사용량 확인이 필요해요","Check usage first"):t(a,"주간 한도가 제공되지 않았어요","A weekly limit was not provided"),dark));
            if(snapshot!=null&&snapshot.monthly!=null)card.addView(BetaUi.note(a,t(a,"월간 한도 사용 %s · 주간 수치와 별도","Monthly limit used %s · separate from weekly",UsagePrecision.used(snapshot.monthly,AppPreferences.usageDecimalPlaces(a))),dark));
            return;
        }
        LedgerPeriods.Span span=LedgerPeriods.span(window.effectiveResetAtMillis(snapshot.fetchedAtMillis),window.windowSeconds,snapshot.fetchedAtMillis);
        String range=span==null?t(a,"한도 주기 · 기간 정보 없음","Quota cycle · timeline unavailable")
                :t(a,"한도 초기화 기준 · %s–%s","Quota reset cycle · %s–%s",date(span.start),date(span.end));
        TextView period=BetaUi.note(a,range,dark);period.setTag("beta999:weekly-period");card.addView(period);
        TextView value=BetaUi.text(a,t(a,"%s 사용","%s used",UsagePrecision.used(window,AppPreferences.usageDecimalPlaces(a))),28,Ui.mainText(dark),true);
        value.setTag("beta999:weekly-used");card.addView(value);
        TextView evidence=BetaUi.note(a,"",dark);evidence.setTag("beta999:weekly-evidence");card.addView(evidence);
        Runnable update=()->{
            UsageEvidence.State state=UsageEvidence.state(snapshot,window,!AppPreferences.getLastError(a).isEmpty(),System.currentTimeMillis(),RefreshScheduler.effectiveRefreshMinutes(a));
            int id=state==UsageEvidence.State.CACHED_AFTER_FAILURE?R.string.phase_a_cached_failed:state==UsageEvidence.State.RESET_PASSED?R.string.phase_a_reset_passed:state==UsageEvidence.State.STALE?R.string.phase_a_stale:state==UsageEvidence.State.UNAVAILABLE?R.string.phase_a_unavailable:R.string.phase_a_confirmed;
            evidence.setText(id);evidence.setVisibility(state==UsageEvidence.State.CONFIRMED?android.view.View.GONE:android.view.View.VISIBLE);
        };clocks.add(update);update.run();
        LinearLayout graph=new LinearLayout(a);graph.setOrientation(LinearLayout.VERTICAL);graph.setTag("beta999:weekly-graph");card.addView(graph);
        graph.addView(BetaUi.note(a,t(a,"관측 기록 확인 중","Loading observed records"),dark));
        final String stamp=stamp(a,snapshot);
        WORKER.execute(()->{
            BetaWeeklyUsage.Model result=null;try{if(stamp.equals(stamp(a,AppPreferences.loadSnapshot(a))))result=BetaWeeklyReader.read(a.getApplicationContext(),snapshot,System.currentTimeMillis());}catch(Exception ignored){}
            BetaWeeklyUsage.Model model=result;
            a.runOnUiThread(()->{if(a.isDestroyed()||a.isFinishing()||!graph.isAttachedToWindow()||!stamp.equals(stamp(a,AppPreferences.loadSnapshot(a))))return;render(a,graph,model,dark);});
        });
    }
    static String stamp(android.content.Context c,UsageSnapshot s){return LiveUsageStore.signature(c,s);}
    static String date(long at){return Instant.ofEpochMilli(at).atZone(LedgerAggregation.ZONE).format(DateTimeFormatter.ofPattern("MM.dd"));}
    static void render(MainActivity a,LinearLayout graph,BetaWeeklyUsage.Model model,boolean dark){
        graph.removeAllViews();graph.setOnClickListener(null);graph.setClickable(false);graph.setFocusable(false);
        if(model==null||model.days.isEmpty()||model.measuredDays==0){
            TextView empty=BetaUi.note(a,t(a,"일별 기록 수집 중","Collecting daily observations"),dark);empty.setTag("beta999:weekly-missing");graph.addView(empty);return;
        }

        TextView legend=BetaUi.note(a,t(a,"일별 관측 증가 · 전체 사용량과 별도","Observed daily changes · not total usage"),dark);
        legend.setTag("home:weekly-legend");legend.setTextSize(13);graph.addView(legend);
        int columns=a.getResources().getConfiguration().fontScale>1.3f||a.getResources().getConfiguration().screenWidthDp<360?Math.min(4,model.days.size()):model.days.size();
        GridLayout grid=new GridLayout(a);grid.setColumnCount(columns);grid.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));graph.addView(grid);
        final String graphAccount=SubscriptionStore.accountKey(a),graphStamp=stamp(a,AppPreferences.loadSnapshot(a));
        graph.setFocusable(true);graph.setClickable(true);graph.setContentDescription(t(a,"일별 관측 증가 상세 보기","View daily observed changes"));graph.setOnClickListener(v->{if(!java.util.Objects.equals(graphAccount,SubscriptionStore.accountKey(a))||!graphStamp.equals(stamp(a,AppPreferences.loadSnapshot(a))))return;String expected=graphAccount;LinearLayout details=BetaMedalUi.content(a);for(BetaWeeklyUsage.Day day:model.days)details.addView(BetaUi.body(a,day.date+" · "+(day.measured?t(a,"관측 증가 %s%%p","Observed increase %s percentage points",UsagePrecision.number(day.points)):day.future?t(a,"예정","Upcoming"):t(a,"자료 없음","No data")),dark));details.addView(BetaUi.note(a,t(a,"관측한 구간의 변화이며 전체 사용량·토큰 수를 뜻하지 않습니다.","Changes in observed intervals, not complete usage or token counts."),dark));BetaMedalUi.show(a,t(a,"일별 관측 증가","Daily observed changes"),details,expected);});
        double max=0;for(BetaWeeklyUsage.Day day:model.days)if(day.measured)max=Math.max(max,day.points);
        for(int i=0;i<model.days.size();i++){
            BetaWeeklyUsage.Day day=model.days.get(i);LinearLayout col=new LinearLayout(a);col.setOrientation(LinearLayout.VERTICAL);col.setGravity(Gravity.CENTER_HORIZONTAL);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams(GridLayout.spec(i/columns),GridLayout.spec(i%columns,1f));params.width=0;params.height=-2;params.topMargin=Ui.dp(a,8);grid.addView(col,params);
            TextView amount=BetaUi.note(a,day.measured?UsagePrecision.number(day.points):day.future?t(a,"예정","Next"):"—",dark);amount.setVisibility(android.view.View.GONE);amount.setGravity(Gravity.CENTER);col.addView(amount,new LinearLayout.LayoutParams(-1,-2));
            FrameLayout track=new FrameLayout(a);col.addView(track,new LinearLayout.LayoutParams(-1,Ui.dp(a,42)));
            if(day.measured&&day.points>0){View bar=new View(a);bar.setBackgroundColor(Ui.accent(a,dark));FrameLayout.LayoutParams fill=new FrameLayout.LayoutParams(Ui.dp(a,12),Math.max(1,Ui.dp(a,(int)Math.round(40*day.points/max))),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);track.addView(bar,fill);}
            if(!day.measured||day.points==0){TextView mark=BetaUi.note(a,day.future?"":day.measured?"0":"—",dark);mark.setGravity(Gravity.CENTER);track.addView(mark,new FrameLayout.LayoutParams(-1,-1));}
            // The full date range is above the chart; short day labels remain legible in an
            // eight-date reset cycle without treating that cycle as a calendar week.
            TextView label=BetaUi.note(a,day.date.format(DateTimeFormatter.ofPattern("d")),dark);label.setTextSize(13);label.setGravity(Gravity.CENTER);col.addView(label,new LinearLayout.LayoutParams(-1,-2));
            col.setContentDescription(day.date+" · "+(day.measured?t(a,"관측 증가 %s%%p","Observed increase %s percentage points",UsagePrecision.number(day.points)):day.future?t(a,"예정","Upcoming"):t(a,"자료 없음","No data")));
        }

    }
}
