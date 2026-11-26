package dev.bennett.codexmeter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import java.util.List;

/** A local display preference. It never writes to the observed usage or the growth ledger. */
final class WeeklyQuotaIndexUi {
    private WeeklyQuotaIndexUi() {}
    enum Display { REMAINING, USED, USED_WITH_PERCENT }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("weekly_quota_index", Context.MODE_PRIVATE);
    }

    private static String eventKey(String account, String plan) {
        return account == null || plan == null ? null : account + ":" + plan;
    }

    static void add(MainActivity activity, LinearLayout parent, UsageSnapshot snapshot,
            String account, boolean dark, Display display, List<Runnable> clocks) {
        if (snapshot == null || snapshot.weekly == null) return;
        String plan = WeeklyQuotaIndex.planFor(snapshot);
        String key = eventKey(account, plan);
        boolean home = display == Display.REMAINING;
        if (display != Display.USED_WITH_PERCENT && key == null) return;

        UsageWindow week = snapshot.weekly;
        SharedPreferences settings = preferences(activity);
        TextView result = BetaUi.note(activity, "", dark);
        result.setTag(home ? "weekly-index:remaining" : "weekly-index:used");
        parent.addView(result);

        Runnable update = () -> {
            long now = System.currentTimeMillis();
            long reset = week.effectiveResetAtMillis(snapshot.fetchedAtMillis);
            UsageEvidence.State state = UsageEvidence.state(snapshot, week,
                    !AppPreferences.getLastError(activity).isEmpty(), now,
                    RefreshScheduler.effectiveRefreshMinutes(activity));
            boolean fresh = state == UsageEvidence.State.CONFIRMED && (reset <= 0 || reset > now);
            boolean doubled = key != null && settings.getBoolean(key, false);
            WeeklyQuotaIndex index = fresh && key != null
                    ? WeeklyQuotaIndex.estimate(plan, week.preciseUsedPercent, doubled) : null;
            String text = "";
            if (display == Display.USED_WITH_PERCENT) {
                text = activity.getString(fresh ? R.string.quota_week_used : R.string.quota_week_last_used,
                        UsagePrecision.used(week, AppPreferences.usageDecimalPlaces(activity)));
            }
            if (index != null) {
                String scale = activity.getString(home ? R.string.quota_index_remaining : R.string.quota_index_used,
                        home ? index.remaining : index.used, index.total);
                if (doubled) scale = activity.getString(R.string.quota_index_doubled, scale);
                text = text.isEmpty() ? scale : text + "\n" + scale;
            }
            if (!text.contentEquals(result.getText())) result.setText(text);
            result.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        };
        clocks.add(update);
        update.run();
    }

    static void addEventSetting(MainActivity activity, LinearLayout parent, UsageSnapshot snapshot,
            String account, boolean dark) {
        if (snapshot == null || snapshot.weekly == null) return;
        String key = eventKey(account, WeeklyQuotaIndex.planFor(snapshot));
        if (key == null) return;

        SharedPreferences settings = preferences(activity);
        SwitchCompat toggle = new SwitchCompat(activity);
        toggle.setTag("weekly-index:event");
        toggle.setText(R.string.quota_event_switch);
        toggle.setTextColor(Ui.mainText(dark));
        toggle.setMinHeight(Ui.dp(activity, 48));
        toggle.setSwitchPadding(Ui.dp(activity, 12));
        toggle.setChecked(settings.getBoolean(key, false));
        parent.addView(toggle, new LinearLayout.LayoutParams(-1, -2));
        parent.addView(BetaUi.note(activity, activity.getString(R.string.quota_index_explanation), dark));
        toggle.setOnCheckedChangeListener((button, checked) -> {
            UsageSnapshot current = AppPreferences.loadSnapshot(activity);
            String currentKey = eventKey(SubscriptionStore.accountKey(activity), WeeklyQuotaIndex.planFor(current));
            if (current == null || current.weekly == null || !key.equals(currentKey)) {
                button.setOnCheckedChangeListener(null);
                button.setChecked(settings.getBoolean(key, false));
                button.setEnabled(false);
                return;
            }
            if (checked) settings.edit().putBoolean(key, true).apply();
            else settings.edit().remove(key).apply();
            activity.rebuild();
        });
    }
}
