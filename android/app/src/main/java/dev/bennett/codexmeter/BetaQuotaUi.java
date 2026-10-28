package dev.bennett.codexmeter;

import android.content.res.ColorStateList;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

/** Primary home content in every preset. Reads the cached observation only. */
final class BetaQuotaUi {
    private BetaQuotaUi() {}

    static void add(MainActivity activity, LinearLayout parent, UsageSnapshot snapshot,
            boolean dark, List<Runnable> clockUpdates) {
        LinearLayout card = BetaUi.group(parent,
                BetaExperienceUi.t(activity, "남은 사용 한도", "Remaining limits"), dark);
        fill(activity,card,snapshot,dark,clockUpdates,true);
        Ui.addSpacer(parent,16);
    }

    static void addContent(MainActivity activity,LinearLayout parent,UsageSnapshot snapshot,
            boolean dark,List<Runnable> clockUpdates){
        LinearLayout section=new LinearLayout(activity);section.setOrientation(LinearLayout.VERTICAL);
        parent.addView(section,new LinearLayout.LayoutParams(-1,-2));
        fill(activity,section,snapshot,dark,clockUpdates,true);
        String expected=SubscriptionStore.accountKey(activity);
        section.addView(BetaUi.row(activity,BetaMedalUi.t(activity,"한도·내 상태 상세","Limits and status details"),"",dark,()->BetaHomeDetailsUi.show(activity,snapshot,dark,expected)));
    }

    static void details(MainActivity activity,LinearLayout parent,UsageSnapshot snapshot,boolean dark){fill(activity,parent,snapshot,dark,new java.util.ArrayList<>(),false);}

    private static void fill(MainActivity activity,LinearLayout card,UsageSnapshot snapshot,
            boolean dark,List<Runnable> clockUpdates,boolean compact){
        card.setTag("beta998:quota");
        if (!compact && TrialDemo.active(activity)) card.addView(BetaUi.note(activity,
                BetaExperienceUi.t(activity, "가상 데모 · 실제 계정 한도 아님",
                        "Synthetic demo · not real account limits"), dark));
        boolean stacked = activity.getResources().getConfiguration().fontScale > 1.3f
                || activity.getResources().getConfiguration().screenWidthDp < 360;
        LinearLayout meters = new LinearLayout(activity);
        meters.setOrientation(stacked ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        card.addView(meters, new LinearLayout.LayoutParams(-1, -2));
        addWindow(activity, meters, snapshot, snapshot == null ? null : snapshot.fiveHour,
                true, dark, stacked, clockUpdates,compact);
        addWindow(activity, meters, snapshot, snapshot == null ? null : snapshot.weekly,
                false, dark, stacked, clockUpdates,compact);
        if (snapshot != null) {
            Ui.addSpacer(card, 8);
            TextView observed=BetaUi.note(activity,"",dark);observed.setTag("home:quota-observed");card.addView(observed);
            Runnable common=()->{
                String text=BetaExperienceUi.t(activity,"마지막 확인 · %s","Last checked · %s",UsageFormat.absolute(activity,snapshot.fetchedAtMillis,System.currentTimeMillis()));
                UsageEvidence.State state=commonState(activity,snapshot);
                if(compact&&state!=null)text+=" · "+activity.getString(statusResource(state));
                observed.setText(text);
            };clockUpdates.add(common);common.run();
        }
    }

    private static void addWindow(MainActivity activity, LinearLayout parent,
            UsageSnapshot snapshot, UsageWindow window, boolean fiveHour, boolean dark,
            boolean stacked, List<Runnable> clockUpdates,boolean compact) {
        String key = fiveHour ? "five-hour" : "weekly";
        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(stacked ? -1 : 0, -2,
                stacked ? 0 : 1);
        if (!fiveHour) {
            if (stacked) params.topMargin = Ui.dp(activity, 16);
            else params.setMarginStart(Ui.dp(activity, 16));
        }
        parent.addView(column, params);
        String label = BetaExperienceUi.t(activity,
                fiveHour ? "5시간 한도 · 남음" : "주간 한도 · 남음",
                fiveHour ? "5-hour remaining" : "Weekly remaining");
        TextView heading=BetaUi.text(activity,label,14,Ui.secondaryText(dark),false);
        column.addView(heading);
        TextView value = BetaUi.text(activity, window == null ? "—"
                : UsagePrecision.remaining(window, AppPreferences.usageDecimalPlaces(activity)),
                32, Ui.mainText(dark), true);
        value.setTag("beta998:quota:" + key + ":value");
        value.setContentDescription(label + ": " + value.getText());
        column.addView(value);
        ProgressBar progress = Ui.progress(activity, dark);
        progress.setMax(100000);
        progress.setProgress(window == null ? 0 : (int) Math.round((100 - window.preciseUsedPercent) * 1000));
        progress.setProgressTintList(ColorStateList.valueOf(Ui.accent(activity, dark)));
        progress.setVisibility(window == null ? android.view.View.GONE : android.view.View.VISIBLE);
        progress.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        column.addView(progress, new LinearLayout.LayoutParams(-1, Ui.dp(activity, 6)));
        TextView status = BetaUi.note(activity, "", dark);
        status.setTextSize(13);
        status.setTag("beta998:quota:" + key + ":status");
        column.addView(status);
        TextView reset = BetaUi.note(activity, "", dark);
        reset.setTextSize(13);
        reset.setTag("beta998:quota:" + key + ":reset");
        column.addView(reset);
        Runnable update = () -> {
            if (window == null) {
                status.setText(BetaExperienceUi.t(activity,
                        snapshot == null ? "사용량 확인 필요" : "제공된 한도 없음",
                        snapshot == null ? "Check usage first" : "Limit not provided"));
                reset.setText("");
                return;
            }
            long now = System.currentTimeMillis();
            UsageEvidence.State state = UsageEvidence.state(snapshot, window,
                    !AppPreferences.getLastError(activity).isEmpty(), now,
                    RefreshScheduler.effectiveRefreshMinutes(activity));
            int resource = state == UsageEvidence.State.CACHED_AFTER_FAILURE ? R.string.phase_a_cached_failed
                    : state == UsageEvidence.State.RESET_PASSED ? R.string.phase_a_reset_passed
                    : state == UsageEvidence.State.STALE ? R.string.phase_a_stale
                    : state == UsageEvidence.State.UNAVAILABLE ? R.string.phase_a_unavailable
                    : R.string.phase_a_confirmed;
            status.setText(resource);
            status.setVisibility(state==UsageEvidence.State.CONFIRMED||compact&&state==commonState(activity,snapshot)?android.view.View.GONE:android.view.View.VISIBLE);
            long resetAt = window.effectiveResetAtMillis(snapshot.fetchedAtMillis);
            if (resetAt > now) {
                reset.setText(activity.getString(R.string.home_quota_reset_schedule,
                        UsageFormat.absolute(activity, resetAt, now),
                        UsageFormat.compactDuration(activity, resetAt - now)));
                return;
            }
            if(compact){
                reset.setText(resetAt<=0?activity.getString(R.string.ui_reset_time_unavailable_5c3030)
                        :activity.getString(R.string.pub_five_expired_short));
                return;
            }
            reset.setText(fiveHour ? FiveHourResetDisplay.text(activity, window,
                    snapshot.fetchedAtMillis, now, WidgetOptions.RESET_BOTH, false)
                    : resetAt <= 0 ? activity.getString(R.string.ui_reset_time_unavailable_5c3030)
                    : resetAt <= now ? activity.getString(R.string.phase_a_reset_passed)
                    : UsageFormat.reset(activity, window, WidgetOptions.RESET_BOTH,
                            snapshot.fetchedAtMillis, now));
        };
        clockUpdates.add(update);
        update.run();
    }
    private static UsageEvidence.State commonState(MainActivity a,UsageSnapshot snapshot){
        if(snapshot==null||snapshot.fiveHour==null||snapshot.weekly==null)return null;
        long now=System.currentTimeMillis();boolean failed=!AppPreferences.getLastError(a).isEmpty();int interval=RefreshScheduler.effectiveRefreshMinutes(a);
        UsageEvidence.State five=UsageEvidence.state(snapshot,snapshot.fiveHour,failed,now,interval);
        UsageEvidence.State week=UsageEvidence.state(snapshot,snapshot.weekly,failed,now,interval);
        // Reset expiry stays next to the affected window, including when a fetch also failed.
        if(snapshot.fiveHour.effectiveResetAtMillis(snapshot.fetchedAtMillis)>0&&snapshot.fiveHour.effectiveResetAtMillis(snapshot.fetchedAtMillis)<=now)return null;
        if(snapshot.weekly.effectiveResetAtMillis(snapshot.fetchedAtMillis)>0&&snapshot.weekly.effectiveResetAtMillis(snapshot.fetchedAtMillis)<=now)return null;
        return five==week&&(five==UsageEvidence.State.CACHED_AFTER_FAILURE||five==UsageEvidence.State.STALE)?five:null;
    }
    private static int statusResource(UsageEvidence.State state){
        return state==UsageEvidence.State.CACHED_AFTER_FAILURE?R.string.phase_a_cached_failed:R.string.phase_a_stale;
    }
}
