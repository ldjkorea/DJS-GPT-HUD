package dev.bennett.codexmeter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.widget.Button;
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
        fill(activity,card,snapshot,dark,clockUpdates,true,SubscriptionStore.accountKey(activity),null);
        Ui.addSpacer(parent,16);
    }

    static void addContent(MainActivity activity,LinearLayout parent,UsageSnapshot snapshot,
            boolean dark,List<Runnable> clockUpdates){
        LinearLayout section=new LinearLayout(activity);section.setOrientation(LinearLayout.VERTICAL);
        parent.addView(section,new LinearLayout.LayoutParams(-1,-2));
        String expected=SubscriptionStore.accountKey(activity);
        fill(activity,section,snapshot,dark,clockUpdates,true,expected,
                () -> BetaHomeDetailsUi.show(activity,dark,expected));
    }

    static void details(MainActivity activity, LinearLayout parent, boolean dark, String account) {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        parent.addView(section);
        activity.bindQuotaDetails(section, account, (snapshot, clocks) -> {
            fill(activity, section, snapshot, dark, clocks, false, account, null);
            WeeklyQuotaIndexUi.addEventSetting(activity, section, snapshot, account, dark);
        });
    }

    private static void fill(MainActivity activity,LinearLayout card,UsageSnapshot snapshot,
            boolean dark,List<Runnable> clockUpdates,boolean compact,String account,Runnable onDetails){
        card.setTag("beta998:quota");
        if (!compact && TrialDemo.active(activity)) card.addView(BetaUi.note(activity,
                BetaExperienceUi.t(activity, "가상 데모 · 실제 계정 한도 아님",
                        "Synthetic demo · not real account limits"), dark));
        boolean stacked = activity.getResources().getConfiguration().fontScale > 1.3f
                || activity.getResources().getConfiguration().screenWidthDp < 360;
        LinearLayout meters = new LinearLayout(activity);
        boolean vertical = stacked || !compact;
        meters.setOrientation(vertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        card.addView(meters, new LinearLayout.LayoutParams(-1, -2));
        UsageWindow five = snapshot == null ? null : snapshot.fiveHour;
        UsageWindow longer = snapshot == null ? null : snapshot.longWindow();
        String longKey = snapshot != null && snapshot.longWindowIsMonthly() ? "monthly" : "weekly";
        if (compact && five != null)
            addWindow(activity, meters, snapshot, five, "five-hour", dark, vertical, clockUpdates, true, account);
        if (longer != null)
            addWindow(activity, meters, snapshot, longer, longKey, dark, vertical, clockUpdates, compact, account);
        if (!compact && five != null)
            addWindow(activity, meters, snapshot, five, "five-hour", dark, vertical, clockUpdates, false, account);
        if (!compact && snapshot != null && snapshot.weekly != null && snapshot.monthly != null)
            addWindow(activity, meters, snapshot, snapshot.monthly, "monthly", dark, true, clockUpdates, false, account);
        if (five == null && longer == null) {
            int message = snapshot != null ? R.string.quota_no_period :
                    AppPreferences.getLastError(activity).isEmpty() ? R.string.quota_first_check : R.string.quota_check_failed;
            meters.addView(BetaUi.note(activity, activity.getString(message), dark));
        }
        LinearLayout footer=new LinearLayout(activity);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams footerParams=new LinearLayout.LayoutParams(-1,-2);
        footerParams.topMargin=Ui.dp(activity,8);
        card.addView(footer,footerParams);
        if (snapshot != null) {
            TextView observed=BetaUi.note(activity,"",dark);
            observed.setTextSize(13);
            observed.setTag("home:quota-observed");
            footer.addView(observed,new LinearLayout.LayoutParams(stacked?-1:0,-2,stacked?0:1));
            Runnable common=()->{
                String text=BetaExperienceUi.t(activity,"마지막 확인 · %s","Last checked · %s",UsageFormat.absolute(activity,snapshot.fetchedAtMillis,System.currentTimeMillis()));
                if (five == null && longer != null)
                    text += " · " + activity.getString(R.string.quota_five_missing);
                else if (five != null && longer == null)
                    text += " · " + activity.getString(R.string.quota_long_missing);
                UsageEvidence.State state=commonState(activity,snapshot);
                if(compact&&state!=null)text+="\n"+activity.getString(statusResource(state));
                observed.setText(text);
            };clockUpdates.add(common);common.run();
        }
        if (!compact && account != null) {
            Button retry = Ui.button(activity, activity.getString(R.string.ui_refresh_56e3ba), false, dark);
            retry.setMinHeight(Ui.dp(activity, 48));
            retry.setOnClickListener(v -> {
                if (account.equals(SubscriptionStore.accountKey(activity))) activity.refreshNow(retry);
            });
            card.addView(retry);
            Runnable updateRetry = () -> {
                long now = System.currentTimeMillis();
                boolean failed = !AppPreferences.getLastError(activity).isEmpty();
                int interval = RefreshScheduler.effectiveRefreshMinutes(activity);
                boolean needed = (five == null && longer == null) || failed
                        || needsReading(snapshot, five, now, interval)
                        || needsReading(snapshot, longer, now, interval)
                        || (snapshot != null && needsReading(snapshot, snapshot.monthly, now, interval));
                retry.setVisibility(needed ? android.view.View.VISIBLE : android.view.View.GONE);
            };
            clockUpdates.add(updateRetry);
            updateRetry.run();
        }
        if(onDetails!=null){
            Button details=BetaUi.action(activity,BetaExperienceUi.t(activity,"한도 상세","Limit details"),false,dark,onDetails);
            details.setTag("home:quota-details");
            details.setContentDescription(details.getText());
            details.setTextSize(14);
            details.setPadding(Ui.dp(activity,10),0,Ui.dp(activity,10),0);
            android.util.TypedValue ripple=new android.util.TypedValue();
            activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);
            if(ripple.resourceId!=0)details.setBackgroundResource(ripple.resourceId);
            footer.addView(details,new LinearLayout.LayoutParams(-2,-2));
        }
    }

    private static void addWindow(MainActivity activity, LinearLayout parent,
            UsageSnapshot snapshot, UsageWindow window, String key, boolean dark,
            boolean stacked, List<Runnable> clockUpdates,boolean compact,String account) {
        boolean monthly = "monthly".equals(key);
        boolean fiveHour = "five-hour".equals(key);
        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(stacked ? -1 : 0, -2,
                stacked ? 0 : 1);
        if (parent.getChildCount() > 0) {
            if (stacked) params.topMargin = Ui.dp(activity, 16);
            else{
                android.view.View divider=new android.view.View(activity);
                divider.setBackgroundColor(Ui.divider(dark));
                LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(Ui.dp(activity,1),-1);
                line.setMarginStart(Ui.dp(activity,8));
                line.setMarginEnd(Ui.dp(activity,8));
                parent.addView(divider,line);
            }
        }
        parent.addView(column, params);
        int title = fiveHour ? (compact ? R.string.quota_five_remaining : R.string.quota_five_used)
                : monthly ? (compact ? R.string.quota_month_remaining : R.string.quota_month_used_heading)
                : compact ? R.string.quota_week_remaining : R.string.quota_week_used_heading;
        TextView heading = BetaUi.text(activity, activity.getString(title), 14, Ui.secondaryText(dark), false);
        column.addView(heading);
        int precision = AppPreferences.usageDecimalPlaces(activity);
        String percent = compact ? UsagePrecision.remaining(window, precision) : UsagePrecision.used(window, precision);
        TextView value = BetaUi.text(activity, percent, 32, Ui.mainText(dark), true);
        Ui.styleDigits(value,true);
        if(window.preciseUsedPercent>=100)value.setTextColor(BetaUi.warning(dark));
        value.setTag("beta998:quota:" + key + ":value");
        value.setContentDescription(heading.getText() + " · " + percent);
        column.addView(value);
        ProgressBar progress = Ui.progress(activity, dark);
        progress.setMax(100000);
        progress.setProgress((int) Math.round((compact ? 100 - window.preciseUsedPercent : window.preciseUsedPercent) * 1000));
        progress.setProgressTintList(ColorStateList.valueOf(Ui.accent(activity, dark)));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(dark?0xff476065:0xffc7d3cc));
        progress.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams bar=new LinearLayout.LayoutParams(-1,Ui.dp(activity,6));
        bar.bottomMargin=Ui.dp(activity,3);
        column.addView(progress,bar);
        if (!fiveHour && !monthly)
            WeeklyQuotaIndexUi.add(activity, column, snapshot, account, dark,
                    compact ? WeeklyQuotaIndexUi.Display.REMAINING : WeeklyQuotaIndexUi.Display.USED, clockUpdates);
        TextView status = BetaUi.note(activity, "", dark);
        status.setTextSize(13);
        status.setTag("beta998:quota:" + key + ":status");
        column.addView(status);
        TextView reset = BetaUi.note(activity, "", dark);
        reset.setTextSize(13);
        reset.setTag("beta998:quota:" + key + ":reset");
        column.addView(reset);
        Runnable update = () -> {
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
            boolean expired = resetAt > 0 && resetAt <= now;
            boolean current = state == UsageEvidence.State.CONFIRMED && !expired;
            String headingText = current ? activity.getString(title) :
                    activity.getString(R.string.quota_last_value, activity.getString(title));
            if (!headingText.contentEquals(heading.getText())) heading.setText(headingText);
            value.setVisibility(expired ? android.view.View.GONE : android.view.View.VISIBLE);
            progress.setVisibility(expired ? android.view.View.GONE : android.view.View.VISIBLE);
            if (expired) {
                heading.setText(title);
                status.setText(R.string.quota_new_period);
                status.setVisibility(android.view.View.VISIBLE);
            } else {
                value.setContentDescription(headingText + " · " + percent);
            }
            if (resetAt > now) {
                String absolute=UsageFormat.absolute(activity,resetAt,now);
                String schedule=activity.getString(R.string.home_quota_reset_schedule,absolute,
                        UsageFormat.compactDuration(activity,resetAt-now));
                if(!schedule.contentEquals(reset.getText())){
                    SpannableString styled=new SpannableString(schedule);
                    int start=schedule.indexOf(absolute);
                    if(start>=0){
                        styled.setSpan(new ForegroundColorSpan(Ui.mainText(dark)),start,start+absolute.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        styled.setSpan(new StyleSpan(Typeface.BOLD),start,start+absolute.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    reset.setText(styled);
                }
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
    private static boolean needsReading(UsageSnapshot snapshot, UsageWindow window, long now, int interval) {
        return window != null && UsageEvidence.state(snapshot, window, false, now, interval) != UsageEvidence.State.CONFIRMED;
    }

    private static UsageEvidence.State commonState(MainActivity a,UsageSnapshot snapshot){
        if(snapshot==null||snapshot.fiveHour==null||snapshot.longWindow()==null)return null;
        long now=System.currentTimeMillis();boolean failed=!AppPreferences.getLastError(a).isEmpty();int interval=RefreshScheduler.effectiveRefreshMinutes(a);
        UsageEvidence.State five=UsageEvidence.state(snapshot,snapshot.fiveHour,failed,now,interval);
        UsageWindow longer=snapshot.longWindow();
        UsageEvidence.State week=UsageEvidence.state(snapshot,longer,failed,now,interval);
        // Reset expiry stays next to the affected window, including when a fetch also failed.
        if(snapshot.fiveHour.effectiveResetAtMillis(snapshot.fetchedAtMillis)>0&&snapshot.fiveHour.effectiveResetAtMillis(snapshot.fetchedAtMillis)<=now)return null;
        if(longer.effectiveResetAtMillis(snapshot.fetchedAtMillis)>0&&longer.effectiveResetAtMillis(snapshot.fetchedAtMillis)<=now)return null;
        return five==week&&(five==UsageEvidence.State.CACHED_AFTER_FAILURE||five==UsageEvidence.State.STALE)?five:null;
    }
    private static int statusResource(UsageEvidence.State state){
        return state==UsageEvidence.State.CACHED_AFTER_FAILURE?R.string.phase_a_cached_failed:R.string.phase_a_stale;
    }
}
