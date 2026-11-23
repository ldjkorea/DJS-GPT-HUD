package dev.bennett.codexmeter;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

/** One recommended goal, compact period summary, collection entry points. Details are opt-in. */
final class BetaCompactGrowthUi {
    static boolean render(GrowthActivity a,LinearLayout parent,JSONObject journal,boolean dark){
        JSONObject state=journal==null?null:journal.optJSONObject("beta_v4");BetaGrowthModel model=a.betaGrowthModel;
        if(state==null||model==null||model.summary==null)return BetaGrowthUi.renderDetails(a,parent,journal,dark);
        overview(a, parent, state, model, BetaMedalStore.peek(a), BetaMedalStore.failed(a), dark);
        return true;
    }

    static void overview(GrowthActivity a, LinearLayout parent, JSONObject state, BetaGrowthModel model,
                         JSONObject medals, boolean medalReadFailed, boolean dark) {
        goalSection(a, parent, state, model, medals, medalReadFailed, dark);
        LinearLayout collection = BetaUi.group(parent, BetaCopy.t(a, "collection"), dark);
        collection.setTag("beta9911:medals");
        JSONObject today = medals == null ? null : medals.optJSONObject("today");
        String record = BetaMedalUi.t(a, "메달 확인 대기", "Awaiting medal records");
        if (medals != null) {
            record = BetaMedalUi.t(a, "이번 주 %d일 · 이번 달 %d일", "Week · %d days / Month · %d days",
                    medals.optInt("week"), medals.optInt("month"));
            if (today != null && today.length() > 0) {
                record = BetaMedalUi.t(a, "오늘 %s", "Today · %s", BetaMedalUi.status(a, today)) + " · " + record;
            }
        }
        if (medalReadFailed) record = BetaMedalUi.t(a, "메달 기록을 다시 확인해 주세요", "Medal records need another check");
        View records = BetaUi.row(a, BetaMedalUi.t(a, "메달 기록", "Medal records"), record, dark,
                () -> a.startActivity(new Intent(a, RecordsActivity.class).putExtra("medal_current_month", true)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        records.setTag("beta9915:medal-records");
        collection.addView(records);
        String account = SubscriptionStore.accountKey(a);
        View wardrobe = BetaUi.row(a, BetaMedalUi.t(a, "업적 · 칭호", "Achievements and titles"), "", dark,
                () -> openCollection(a, account, dark));
        wardrobe.setTag("beta9911:collections");
        collection.addView(wardrobe);
        String level = BetaMedalUi.t(a, "성장 상세", "Growth details");
        if (state.optInt("level") > 0) level = "Lv. " + state.optInt("level") + " · " + level;
        collection.addView(BetaUi.row(a, level, "", dark,
                () -> a.startActivity(new Intent(a, GrowthActivity.class).putExtra("beta_details", true))));
    }

    private static void openCollection(GrowthActivity a, String account, boolean dark) {
        LinearLayout options = BetaMedalUi.content(a);
        AlertDialog dialog = BetaMedalUi.show(a, BetaMedalUi.t(a, "업적 · 칭호", "Achievements and titles"), options, account);
        if (dialog == null) return;
        options.addView(BetaUi.row(a, BetaCopy.t(a, "honors"), "", dark, () -> {
            dialog.dismiss();
            if (BetaUi.current(a, account)) a.startActivity(new Intent(a, BetaCatalogActivity.class));
        }));
        options.addView(BetaUi.row(a, BetaCopy.t(a, "titles"), "", dark, () -> {
            dialog.dismiss();
            if (BetaUi.current(a, account)) a.startActivity(new Intent(a, BetaCatalogActivity.class).putExtra("titles", true));
        }));
    }
    static void goalSection(GrowthActivity a, LinearLayout parent, JSONObject state,
                            BetaGrowthModel model, JSONObject medals, boolean medalReadFailed, boolean dark) {
        boolean paused = !model.collectionEnabled;
        boolean needsObservation = model.status == BetaGrowthModel.Status.ERROR
                || model.status == BetaGrowthModel.Status.STALE || model.status == BetaGrowthModel.Status.BASELINE;
        JSONObject period = null;
        if (!paused && !needsObservation && !medalReadFailed) {
            try {
                period = BetaMedalModel.nextPeriod(medals, System.currentTimeMillis());
            } catch (JSONException invalidSummary) {
                medalReadFailed = true;
            }
        }

        boolean needsRecovery = paused || needsObservation || medalReadFailed;
        String heading = needsRecovery ? BetaMedalUi.t(a, "성장 기록", "Growth recording")
                : period == null ? BetaCopy.t(a, "next")
                : period.optBoolean("month") ? BetaMedalUi.t(a, "이번 달 목표", "This month's goal")
                : BetaMedalUi.t(a, "이번 주 목표", "This week's goal");
        LinearLayout next = BetaUi.group(parent, period == null ? heading : "", dark);
        next.setTag("beta9911:next-goal");
        if (period != null) {
            next.addView(BetaUi.text(a, heading, 14, BetaMedalUi.color(period.optInt("grade"), dark), true));
            Ui.addSpacer(next, 4);
        }
        String label = BetaCopy.t(a, paused ? "paused" : model.key());
        if (medalReadFailed && !paused && !needsObservation) {
            label = BetaMedalUi.t(a, "메달 기록을 읽지 못했어요", "Could not read medal records");
        }
        long now = System.currentTimeMillis();
        if (needsRecovery && model.lastObserved > 0 && model.lastObserved <= now && model.status != BetaGrowthModel.Status.DEMO) {
            label += "\n" + BetaCopy.t(a, "last_check", UsageFormat.absolute(a, model.lastObserved, now));
        }
        TextView status = BetaUi.note(a, label, dark);
        status.setTag("stable-redesign:growth-status");
        if (needsRecovery && !paused) status.setTextColor(BetaUi.warning(dark));
        if (needsRecovery || period == null || model.status == BetaGrowthModel.Status.DEMO) next.addView(status);

        if (paused) {
            next.addView(BetaUi.row(a, BetaMedalUi.t(a, "수집 설정 확인", "Check collection settings"), "", dark,
                    () -> a.startActivity(new Intent(a, MeterSettingsActivity.class))));
        } else if (needsObservation) {
            next.addView(BetaUi.row(a, BetaCopy.t(a, "check_home"), "", dark, () -> MatteNav.open(a, 1)));
        } else if (medalReadFailed) {
            next.addView(BetaUi.row(a, BetaCopy.t(a, "retry_record"), "", dark, () -> a.load(true)));
        } else if (period != null) {
            int current = period.optInt("current"), target = period.optInt("target");
            int grade = period.optInt("grade");
            medalGoal(a, next, grade, current, target, dark);
            JSONObject today = medals.optJSONObject("today");
            if (today != null && today.optBoolean("active")) {
                next.addView(BetaUi.note(a, BetaMedalUi.t(a, "오늘 도장 꾹. 다음 칸은 다른 날에!",
                        "Stamped for today. The next space is for another day!"), dark));
            } else if (model.status != BetaGrowthModel.Status.DEMO) {
                next.addView(status);
            }
            next.addView(BetaUi.row(a, BetaMedalUi.t(a, "달성 조건", "Conditions"), "", dark,
                    () -> BetaMedalUi.periods(a, dark)));
        } else {
            JSONObject goal = model.summary.optJSONObject("next_goal");
            String id = goal == null ? "" : goal.optString("id");
            if (id.isEmpty()) {
                next.addView(BetaUi.body(a, BetaCopy.t(a, "complete_all"), dark));
                return;
            }
            String account = SubscriptionStore.accountKey(a);
            next.addView(BetaUi.title(a, BetaCatalog.name(id, BetaIdentity.name(a), BetaCopy.ko(a)), dark));
            int target = Math.max(1, goal.optInt("target"));
            int current = Math.min(target, goal.optInt("current"));
            String[] entry = BetaCatalog.find(id);
            if (entry != null) next.addView(BetaUi.body(a, entry[BetaCopy.ko(a) ? 3 : 4], dark));
            String progress = current + " / " + target;
            switch (goal.optString("metric")) {
                case "days": case "week_now": progress += BetaMedalUi.t(a, "일", " days"); break;
                case "weeks": case "perfect": case "month_weeks": progress += BetaMedalUi.t(a, "주", " weeks"); break;
                case "bronze": case "silver": case "gold": progress += BetaMedalUi.t(a, "개월", " months"); break;
                case "years": progress += BetaMedalUi.t(a, "년", " years"); break;
                case "level": progress = "Lv. " + progress; break;
            }
            addProgress(a, next, current, target, progress, dark);
            next.addView(BetaUi.row(a, BetaMedalUi.t(a, "달성 조건", "Conditions"), "", dark,
                    () -> BetaGrowthUi.detail(a, state, id, dark, account)));
        }
    }

    private static void addProgress(GrowthActivity a, LinearLayout parent, int current, int target,
                                    String progress, boolean dark) {
        ProgressBar bar = Ui.progress(a, dark);
        bar.setMax(target);
        bar.setProgress(current);
        bar.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        parent.addView(bar);
        parent.addView(BetaUi.note(a, progress, dark));
    }
    private static void medalGoal(GrowthActivity a, LinearLayout card, int grade, int current, int target, boolean dark) {
        int remaining = HudUiModel.remaining(current, target);
        String medal = BetaMedalUi.grade(a, grade);
        String title = remaining == 1 ? BetaMedalUi.t(a, "%s까지 하루", "One more day for %s", medal)
                : BetaMedalUi.t(a, "%s까지 %d일", "%1$s in %2$d active days", medal, remaining);
        LinearLayout hero = new LinearLayout(a);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(hero);
        ImageView icon = new ImageView(a);
        icon.setImageDrawable(new BetaMedalUi.Mark(a, grade, true, dark));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        int size = Ui.dp(a, 44);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconParams.setMarginEnd(Ui.dp(a, 8));
        hero.addView(icon, iconParams);
        TextView goal = BetaUi.text(a, title, 24, Ui.mainText(dark), true);
        goal.setTag("growth:medal-goal");
        androidx.core.view.ViewCompat.setAccessibilityHeading(goal, true);
        hero.addView(goal, new LinearLayout.LayoutParams(0,-2,1));

        int color = BetaMedalUi.color(grade, dark);
        LinearLayout stamps = new LinearLayout(a);
        stamps.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        stamps.setTag("growth:day-stamps");
        Ui.addSpacer(card,12);
        card.addView(stamps);
        for (int day=0; day<target; day++) {
            View stamp = new View(a);
            GradientDrawable mark = new GradientDrawable();
            mark.setCornerRadius(Ui.dp(a, 4));
            mark.setColor(day < current ? color : Color.TRANSPARENT);
            mark.setStroke(Ui.dp(a, 1), day < current ? color : LedgerUi.muted(dark));
            stamp.setBackground(mark);
            LinearLayout.LayoutParams slot = new LinearLayout.LayoutParams(0, Ui.dp(a, 10), 1);
            if (day > 0) slot.setMarginStart(Ui.dp(a, 3));
            stamps.addView(stamp, slot);
        }
        Ui.addSpacer(card,8);
        card.addView(BetaUi.body(a, BetaMedalUi.t(a, "활동이 확인된 날 %d / %d일", "Recorded activity on %d / %d days",
                current, target), dark));
    }
}
