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
        TextView value=BetaUi.text(a,t(a,"%s 사용","%s used",UsagePrecision.used(window,AppPreferences.usageDecimalPlaces(a))),28,Ui.mainText(dark),true);
        value.setTypeface(PretendardFont.bold(a));
        value.setLetterSpacing(-.025f);
        value.setFontFeatureSettings("tnum");
        value.setTag("beta999:weekly-used");card.addView(value);
        TextView period=BetaUi.note(a,range,dark);period.setTag("beta999:weekly-period");card.addView(period);
        TextView evidence=BetaUi.note(a,"",dark);evidence.setTag("beta999:weekly-evidence");card.addView(evidence);
        Runnable update=()->{
            UsageEvidence.State state=UsageEvidence.state(snapshot,window,!AppPreferences.getLastError(a).isEmpty(),System.currentTimeMillis(),RefreshScheduler.effectiveRefreshMinutes(a));
            long resetAt=window.effectiveResetAtMillis(snapshot.fetchedAtMillis);
            boolean expired=resetAt>0 && resetAt<=System.currentTimeMillis();
            value.setVisibility(expired ? View.GONE : View.VISIBLE);
            if (!expired) {
                String used=UsagePrecision.used(window,AppPreferences.usageDecimalPlaces(a));
                String label=state==UsageEvidence.State.CONFIRMED ? t(a,"%s 사용","%s used",used)
                        : t(a,"마지막 확인 시 %s 사용","%s used at last check",used);
                if (!label.contentEquals(value.getText())) value.setText(label);
            }
            int id=expired?R.string.quota_new_period:state==UsageEvidence.State.CACHED_AFTER_FAILURE?R.string.phase_a_cached_failed:state==UsageEvidence.State.RESET_PASSED?R.string.phase_a_reset_passed:state==UsageEvidence.State.STALE?R.string.phase_a_stale:state==UsageEvidence.State.UNAVAILABLE?R.string.phase_a_unavailable:R.string.phase_a_confirmed;
            evidence.setText(id);evidence.setVisibility(state==UsageEvidence.State.CONFIRMED?android.view.View.GONE:android.view.View.VISIBLE);
        };clocks.add(update);update.run();
        LinearLayout graph=new LinearLayout(a);graph.setOrientation(LinearLayout.VERTICAL);graph.setTag("beta999:weekly-graph");card.addView(graph);
        load(a,graph,snapshot,dark);
    }
    static void load(MainActivity a,LinearLayout graph,UsageSnapshot snapshot,boolean dark){
        final String expectedAccount=SubscriptionStore.accountKey(a);
        final String expected=stamp(a,snapshot);
        graph.removeAllViews();graph.setOnClickListener(null);graph.setClickable(false);graph.setFocusable(false);
        graph.addView(BetaUi.note(a,t(a,"관측 기록 확인 중","Loading observed records"),dark));
        WORKER.execute(()->{
            BetaWeeklyUsage.Model result=null;
            try{if(expected.equals(stamp(a,AppPreferences.loadSnapshot(a))))result=BetaWeeklyReader.read(a.getApplicationContext(),snapshot,System.currentTimeMillis());}
            catch(Exception ignored){ /* A read failure must not look like an empty history. */ }
            BetaWeeklyUsage.Model model=result;
            a.runOnUiThread(()->{
                if(a.isDestroyed()||a.isFinishing()||!graph.isAttachedToWindow())return;
                if(!expected.equals(stamp(a,AppPreferences.loadSnapshot(a)))){
                    if(stillViewing(a,expectedAccount,snapshot))load(a,graph,snapshot,dark);
                    return;
                }
                if(model!=null){render(a,graph,model,dark,expectedAccount,expected);return;}
                graph.removeAllViews();
                TextView error=BetaUi.note(a,t(a,"기록을 불러오지 못했어요. 저장된 기록은 유지됩니다.","Could not load records. Saved records are unchanged."),dark);
                error.setTag("home:weekly-error");graph.addView(error);
                Button retry=BetaUi.action(a,t(a,"다시 시도","Try again"),false,dark,()->{
                    if(expected.equals(stamp(a,AppPreferences.loadSnapshot(a)))||stillViewing(a,expectedAccount,snapshot))
                        load(a,graph,snapshot,dark);
                });retry.setTag("home:weekly-retry");graph.addView(retry);
            });
        });
    }
    private static boolean stillViewing(MainActivity a,String account,UsageSnapshot snapshot){
        if(account==null||!account.equals(SubscriptionStore.accountKey(a)))return false;
        // Compare the two observations with today's account, history and clear generation.
        return stamp(a,snapshot).equals(stamp(a,AppPreferences.loadSnapshot(a)));
    }
    static String stamp(android.content.Context c,UsageSnapshot s){return LiveUsageStore.signature(c,s)+":"+UsageLedgerDatabase.clearGeneration();}
    static String date(long at){return Instant.ofEpochMilli(at).atZone(LedgerAggregation.ZONE).format(DateTimeFormatter.ofPattern("MM.dd"));}
    static void render(MainActivity a,LinearLayout graph,BetaWeeklyUsage.Model model,boolean dark){
        render(a,graph,model,dark,SubscriptionStore.accountKey(a),stamp(a,AppPreferences.loadSnapshot(a)));
    }
    private static void render(MainActivity a,LinearLayout graph,BetaWeeklyUsage.Model model,
            boolean dark,String graphAccount,String graphStamp){
        graph.removeAllViews();graph.setOnClickListener(null);graph.setClickable(false);graph.setFocusable(false);
        if(model==null||model.days.isEmpty()||model.measuredDays==0){
            TextView empty=BetaUi.note(a,model!=null&&model.recordedDays>0?t(a,"변화를 비교할 기록이 더 필요해요","More observations are needed to compare changes"):t(a,"날짜별 변화는 관측이 쌓이면 표시돼요","Daily changes appear as observations accumulate"),dark);empty.setTag("beta999:weekly-missing");graph.addView(empty);return;
        }

        TextView legend=BetaUi.note(a,t(a,"일별 관측 증가 · 전체 사용량과 별도","Observed daily changes · not total usage"),dark);
        legend.setTag("home:weekly-legend");legend.setTextSize(13);graph.addView(legend);
        DayGrid grid=new DayGrid(a);
        graph.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        graph.setFocusable(true);
        graph.setClickable(true);
        graph.setContentDescription(t(a,"일별 관측 증가 상세 보기","View daily observed changes"));
        graph.setOnClickListener(v -> {
            if (!graphStamp.equals(stamp(a,AppPreferences.loadSnapshot(a)))) return;
            LinearLayout details=BetaMedalUi.content(a);
            for (BetaWeeklyUsage.Day day : model.days) {
                details.addView(BetaUi.body(a,day.date+" · "+dayDescription(a,day),dark));
            }
            showObservedDetails(a,details,t(a,"일별 관측 증가","Daily observed changes"),graphAccount,dark);
        });
        double max=0;for(BetaWeeklyUsage.Day day:model.days)if(day.measured)max=Math.max(max,day.points);
        for(int i=0;i<model.days.size();i++){
            BetaWeeklyUsage.Day day=model.days.get(i);LinearLayout col=new LinearLayout(a);col.setOrientation(LinearLayout.VERTICAL);col.setGravity(Gravity.CENTER_HORIZONTAL);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams();params.width=0;params.height=-2;params.topMargin=Ui.dp(a,8);grid.addView(col,params);
            FrameLayout track=new FrameLayout(a);track.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);col.addView(track,new LinearLayout.LayoutParams(-1,Ui.dp(a,42)));
            if(day.measured&&day.points>0){
                View bar=new View(a);
                bar.setBackground(LedgerUi.shape(a,Ui.accent(a,dark),4));
                FrameLayout.LayoutParams fill=new FrameLayout.LayoutParams(Ui.dp(a,14),
                        Math.max(1,Ui.dp(a,(int)Math.round(40*day.points/max))),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                track.addView(bar,fill);
            }else{
                String symbol=day.future?t(a,"예정","Later"):day.measured?"0":day.observations>0?"·":"—";
                TextView mark=BetaUi.note(a,symbol,dark);
                mark.setTextSize(12);mark.setGravity(Gravity.CENTER);
                track.addView(mark,new FrameLayout.LayoutParams(-1,-1));
            }
            // The full date range is above the chart; short day labels remain legible in an
            // eight-date reset cycle without treating that cycle as a calendar week.
            boolean today=day.date.equals(java.time.LocalDate.now(LedgerAggregation.ZONE));
            TextView label=BetaUi.note(a,day.date.format(DateTimeFormatter.ofPattern("d"))+(today?"\n"+t(a,"오늘","Today"):""),dark);
            if(today){label.setTextColor(Ui.accent(a,dark));label.setTypeface(label.getTypeface(),android.graphics.Typeface.BOLD);label.setTag("home:weekly-today");}label.setTextSize(13);label.setGravity(Gravity.CENTER);label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);col.addView(label,new LinearLayout.LayoutParams(-1,-2));
            col.setMinimumHeight(Ui.dp(a,48));col.setFocusable(true);col.setClickable(true);
            android.util.TypedValue ripple=new android.util.TypedValue();
            if(a.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true)&&ripple.resourceId!=0)col.setBackgroundResource(ripple.resourceId);
            String description=dayDescription(a,day);
            col.setOnClickListener(v->{
                if(!graphStamp.equals(stamp(a,AppPreferences.loadSnapshot(a))))return;
                LinearLayout details=BetaMedalUi.content(a);
                details.addView(BetaUi.body(a,description,dark));
                showObservedDetails(a,details,day.date.toString(),graphAccount,dark);
            });
            col.setContentDescription((today?t(a,"오늘 · ","Today · "):"")+day.date+" · "+description);
        }

    }
    /** Fits whole touch targets into the measured card, including in split-screen. */
    private static final class DayGrid extends GridLayout {
        private int columns;

        DayGrid(android.content.Context context) { super(context); }

        @Override protected void onMeasure(int widthSpec,int heightSpec) {
            int count=getChildCount();
            if(count>0){
                int minWidth=Ui.dp(getContext(),getResources().getConfiguration().fontScale>1.3f?72:48);
                int available=MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight();
                int capacity=MeasureSpec.getMode(widthSpec)==MeasureSpec.UNSPECIFIED?count:Math.max(1,available/minWidth);
                // Eight dates on a phone become 4+4 rather than 6+2.
                int rows=(count+capacity-1)/capacity;
                int next=(count+rows-1)/rows;
                if(next!=columns){
                    columns=next;
                    for(int i=0;i<count;i++){
                        View cell=getChildAt(i);
                        GridLayout.LayoutParams lp=(GridLayout.LayoutParams)cell.getLayoutParams();
                        lp.rowSpec=GridLayout.spec(i/columns,GridLayout.FILL);
                        lp.columnSpec=GridLayout.spec(i%columns,1f);
                        cell.setLayoutParams(lp);
                    }
                }
            }
            super.onMeasure(widthSpec,heightSpec);
        }
    }
    static String dayDescription(android.content.Context c,BetaWeeklyUsage.Day day){
        if(day.future)return t(c,"예정","Upcoming");
        if(day.measured)return t(c,"관측 증가 %s%%p","Observed increase %s percentage points",UsagePrecision.number(day.points));
        if(day.observations>0)return t(c,"관측 기록 있음 · 증가량 비교 불가","Observed · change unavailable");
        return t(c,"자료 없음","No data");
    }

    private static void showObservedDetails(MainActivity a,LinearLayout details,String title,String account,boolean dark){
        details.addView(BetaUi.note(a,t(a,"관측한 구간의 변화이며 전체 사용량·토큰 수를 뜻하지 않습니다.","Changes in observed intervals, not complete usage or token counts."),dark));
        BetaMedalUi.show(a,title,details,account);
    }
}
