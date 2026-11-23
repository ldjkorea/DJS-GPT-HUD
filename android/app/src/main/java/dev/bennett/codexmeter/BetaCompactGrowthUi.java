package dev.bennett.codexmeter;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
        View records = collectionRow(a,R.drawable.matte_records, BetaMedalUi.t(a, "메달 기록", "Medal records"), record, dark,
                () -> a.startActivity(new Intent(a, RecordsActivity.class).putExtra("medal_current_month", true)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        records.setTag("beta9915:medal-records");
        collection.addView(records);
        String account = SubscriptionStore.accountKey(a);
        View wardrobe = collectionRow(a,R.drawable.rpg_growth, BetaMedalUi.t(a, "업적 · 칭호", "Achievements and titles"), "", dark,
                () -> openCollection(a, account, dark));
        wardrobe.setTag("beta9911:collections");
        collection.addView(wardrobe);
        String level = BetaMedalUi.t(a, "성장 상세", "Growth details");
        if (state.optInt("level") > 0) level = "Lv. " + state.optInt("level") + " · " + level;
        collection.addView(collectionRow(a,R.drawable.matte_analysis, level, "", dark,
                () -> a.startActivity(new Intent(a, GrowthActivity.class).putExtra("beta_details", true))));
    }

    private static View collectionRow(Context context, int resource, String label, String detail, boolean dark, Runnable open) {
        LinearLayout row = (LinearLayout) BetaUi.row(context,label,detail,dark,open);
        ImageView symbol = new ImageView(context);
        symbol.setImageResource(resource);
        symbol.setColorFilter(Ui.accent(context,dark));
        symbol.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        symbol.setBackground(LedgerUi.shape(context,Ui.controlSurface(context,dark),10));
        int inset = Ui.dp(context,7);
        symbol.setPadding(inset,inset,inset,inset);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(Ui.dp(context,34),Ui.dp(context,34));
        size.setMarginEnd(Ui.dp(context,10));
        row.addView(symbol,0,size);
        return row;
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
        LinearLayout next = BetaUi.group(parent, "", dark);
        next.setTag("beta9911:next-goal");
        int accent = period != null ? BetaMedalUi.color(period.optInt("grade"),dark)
                : needsRecovery && !paused ? Ui.solar(dark) : Ui.accent(a,dark);
        int base = Ui.cardColor(a,dark);
        GradientDrawable cover = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{androidx.core.graphics.ColorUtils.blendARGB(base,accent,.12f),base});
        cover.setCornerRadius(Ui.dp(a,20));
        cover.setStroke(Ui.dp(a,1),androidx.core.graphics.ColorUtils.blendARGB(base,accent,.33f));
        next.setBackground(cover);
        TextView kicker = BetaUi.text(a,heading,14,accent,true);
        androidx.core.view.ViewCompat.setAccessibilityHeading(kicker,true);
        next.addView(kicker);
        Ui.addSpacer(next,6);
        String label = BetaCopy.t(a, paused ? "paused" : model.key());
        if (medalReadFailed && !paused && !needsObservation) {
            label = BetaMedalUi.t(a, "메달 기록을 읽지 못했어요", "Could not read medal records");
        }
        long now = System.currentTimeMillis();
        TextView status = needsRecovery ? BetaUi.text(a,label,22,Ui.mainText(dark),true) : BetaUi.note(a,label,dark);
        status.setTag("stable-redesign:growth-status");
        if (needsRecovery || period == null || model.status == BetaGrowthModel.Status.DEMO) next.addView(status);
        if (needsRecovery && model.lastObserved > 0 && model.lastObserved <= now && model.status != BetaGrowthModel.Status.DEMO) {
            next.addView(BetaUi.note(a,BetaCopy.t(a, "last_check", UsageFormat.absolute(a, model.lastObserved, now)),dark));
        }

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
        TextView goal = BetaUi.text(a, title, 28, Ui.mainText(dark), true);
        goal.setTypeface(PretendardFont.bold(a));
        goal.setTag("growth:medal-goal");
        androidx.core.view.ViewCompat.setAccessibilityHeading(goal, true);
        hero.addView(goal, new LinearLayout.LayoutParams(0,-2,1));

        int color = BetaMedalUi.color(grade, dark);
        ImageView icon = BetaMedalUi.mark(a,grade,dark);
        boolean largePrint = a.getResources().getConfiguration().fontScale > 1.3f;
        int size = Ui.dp(a,largePrint ? 42 : 60);
        int inset = Ui.dp(a,largePrint ? 5 : 8);
        icon.setPadding(inset,inset,inset,inset);
        GradientDrawable disk = LedgerUi.shape(a,androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(a,dark),color,.13f),60);
        disk.setStroke(Ui.dp(a,1),androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(a,dark),color,.4f));
        icon.setBackground(disk);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size,size);
        iconParams.setMarginStart(Ui.dp(a,8));
        hero.addView(icon,iconParams);

        Ui.addSpacer(card,10);
        card.addView(BetaUi.body(a, BetaMedalUi.t(a, "활동이 확인된 날 %d / %d일", "Recorded activity on %d / %d days",
                current, target), dark));
        LinearLayout stamps = new LinearLayout(a);
        stamps.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        stamps.setTag("growth:day-stamps");
        card.addView(stamps);
        int track = androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(a,dark),Ui.secondaryText(dark),.67f);
        for (int day=0; day<target; day++) {
            stamps.addView(new DayStamp(a,day,current,target,color,track,Ui.cardColor(a,dark)),
                    new LinearLayout.LayoutParams(0,Ui.dp(a,24),1));
        }
    }

    /** Filled suns mark recorded dates. The first pending stop has a second ring. */
    private static final class DayStamp extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int day, current, target, color, track, surface;
        final float density;
        DayStamp(Context c, int day, int current, int target, int color, int track, int surface) {
            super(c);
            this.day=day; this.current=current; this.target=target;
            this.color=color; this.track=track; this.surface=surface;
            density=c.getResources().getDisplayMetrics().density;
        }
        @Override protected void onDraw(Canvas c) {
            float x = getWidth() / 2f, y = getHeight() / 2f;
            float r = Math.min(4.7f*density,x*.52f);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1.5f*density);
            if (day>0) { paint.setColor(day<current?color:track); c.drawLine(0,y,x,y,paint); }
            if (day+1<target) { paint.setColor(day+1<current?color:track); c.drawLine(x,y,getWidth(),y,paint); }
            paint.setColor(day<current?color:surface); c.drawCircle(x,y,r,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(day<=current?color:track); c.drawCircle(x,y,r,paint);
            if (day==current) c.drawCircle(x,y,r+2.4f*density,paint);
        }
    }
}
