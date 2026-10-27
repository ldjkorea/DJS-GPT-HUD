package dev.bennett.codexmeter;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.*;

/** Account-guarded records. Reading failures never turn into zero activity. */
public final class RecordsActivity extends AppCompatActivity {
    LinearLayout body;
    boolean dark, registered, loadFailed, loading;
    String account;
    long generation;
    JSONObject state;
    YearMonth month = YearMonth.from(BetaV4.day(System.currentTimeMillis()));
    LocalDate anchor = BetaV4.day(System.currentTimeMillis());
    float calendarWidthDp;
    final BroadcastReceiver updated = new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) { load(); }
    };
    @Override protected void onCreate(Bundle saved) {
        Ui.applySelectedTheme(this); super.onCreate(saved); dark = Ui.isDark(this);
        if (getIntent().getBooleanExtra("manual_record", false) || getIntent().hasExtra("selected_date") || getIntent().getIntExtra("screen", 0) > 0) {
            details(getIntent().getBooleanExtra("manual_record", false)); finish(); return;
        }
        if (saved != null) try {
            month = YearMonth.parse(saved.getString("medal_month", month.toString()));
            anchor = LocalDate.parse(saved.getString("medal_anchor", anchor.toString()));
        } catch (Exception ignored) { }
        body = Ui.installPage(this, getString(R.string.matte_records), false).content;
        MatteNav.install(this, 2); findViewById(R.id.dashboard_refresh).setEnabled(false);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent);
        if (intent.getBooleanExtra("medal_current_month", false)) {
            anchor = BetaV4.day(System.currentTimeMillis()); month = YearMonth.from(anchor);
        }
        if (body != null) load();
    }
    @Override protected void onResume() {
        super.onResume(); if (body == null) return;
        if (!registered) {
            androidx.core.content.ContextCompat.registerReceiver(this, updated, new IntentFilter(AppConstants.ACTION_USAGE_UPDATED), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
            registered = true;
        }
        load();
    }
    @Override protected void onPause() { if (registered) { unregisterReceiver(updated); registered = false; } super.onPause(); }
    @Override protected void onDestroy() { generation++; super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putString("medal_month", month.toString()); out.putString("medal_anchor", anchor.toString());
    }
    void details(boolean manual) {
        startActivity(new Intent(this, LedgerAnalyticsActivity.class).putExtras(getIntent()).putExtra("medal_usage_details", true).putExtra("manual_record", manual));
    }
    void load() {
        String expected = SubscriptionStore.accountKey(this);
        if (loading && java.util.Objects.equals(expected, account)) return;
        if (!java.util.Objects.equals(expected, account)) { state = null; loadFailed = false; account = expected; }
        long ticket = ++generation; loading = true;
        if (state == null || loadFailed) render();
        BetaMedalStore.WORKER.execute(() -> {
            JSONObject loaded = null; boolean error = false;
            try { loaded = BetaMedalStore.load(getApplicationContext(), expected); } catch (Exception unavailable) { error = true; }
            JSONObject value = loaded; boolean failed = error;
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing() || ticket != generation || !java.util.Objects.equals(expected, SubscriptionStore.accountKey(this))) return;
                loading = false; loadFailed = failed; if (!failed) state = value; render();
            });
        });
    }
    void render() {
        body.removeAllViews(); body.setTag("beta9911:records");
        if (TrialDemo.active(this)) body.addView(BetaUi.note(this, BetaCopy.t(this, "demo"), dark));
        body.addView(BetaUi.row(this, t("사용량 상세", "Usage details"), "", dark, () -> details(false)));
        if (loadFailed) {
            LinearLayout error = BetaUi.group(body, t("기록을 불러오지 못했어요", "Could not load records"), dark);
            error.setTag("beta9915:records-error");
            TextView note = BetaUi.note(this, state == null ? t("기록이 없는 상태와는 달라요. 원본은 보존됩니다.", "This does not mean there are no records. Original data is retained.") : t("마지막으로 읽은 기록을 표시합니다. 원본은 보존됩니다.", "Showing the last successfully read records. Original data is retained."), dark);
            note.setTextColor(BetaUi.warning(dark)); error.addView(note);
            Button retry=BetaUi.action(this, loading?t("확인 중…", "Checking…"):t("다시 시도", "Retry"), true, dark, this::load);
            retry.setEnabled(!loading);error.addView(retry);
            if (state == null) return;
        }
        if (loading && state == null) { body.addView(BetaUi.note(this, getString(R.string.next_loading), dark)); return; }
        if (account == null) {
            body.addView(BetaUi.note(this, BetaCopy.t(this,"no_account"), dark));
            body.addView(BetaUi.row(this,BetaCopy.t(this,"account"),"",dark,()->MatteNav.open(this,1)));
            return;
        }
        JSONObject days = state == null ? new JSONObject() : state.optJSONObject("days"); if (days == null) days = new JSONObject();
        LinearLayout calendar = BetaUi.group(body, t("나의 메달 기록", "My medal records"), dark);
        calendar.setTag("beta9911:calendar"); calendar.setPadding(Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 12));
        navigation(calendar, true);
        TextView count = BetaUi.note(this, t("메달 기록 %d일", "Medals recorded on %d days", BetaMedalModel.count(days, month.atDay(1), month.atEndOfMonth())), dark);
        count.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), Ui.dp(this, 8)); calendar.addView(count);
        float width = calendarWidthDp > 0 ? calendarWidthDp : getResources().getConfiguration().screenWidthDp - 48;
        boolean grid = HudUiModel.calendarGrid(width, getResources().getConfiguration().fontScale);
        if (grid) grid(calendar, days); else week(calendar, days);
        calendar.addView(BetaUi.row(this, t("기호·메달 기준", "Symbols and medal rules"), "", dark, () -> BetaMedalUi.legend(this, dark)));
        calendar.post(() -> {
            if (!calendar.isAttachedToWindow() || isDestroyed()) return;
            float actual = (calendar.getWidth() - calendar.getPaddingLeft() - calendar.getPaddingRight()) / getResources().getDisplayMetrics().density;
            if (actual <= 0) return; calendarWidthDp = actual;
            if (grid != HudUiModel.calendarGrid(actual, getResources().getConfiguration().fontScale)) render();
        });
    }
    void navigation(LinearLayout parent, boolean months) {
        LinearLayout nav = new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        Button before = BetaUi.action(this, "‹", false, dark, () -> move(months, -1));
        before.setContentDescription(t(months ? "이전 달" : "이전 주", months ? "Previous month" : "Previous week"));
        nav.addView(before, new LinearLayout.LayoutParams(Ui.dp(this, 48), -2));
        java.util.List<LocalDate> week = HudUiModel.week(month, anchor);
        String label = months ? month.format(DateTimeFormatter.ofPattern(BetaCopy.ko(this) ? "yyyy년 M월" : "MMMM yyyy", getResources().getConfiguration().getLocales().get(0))) : week.get(0).format(DateTimeFormatter.ofPattern("M/d")) + "–" + week.get(week.size() - 1).format(DateTimeFormatter.ofPattern("M/d"));
        TextView title = BetaUi.body(this, label, dark); title.setGravity(Gravity.CENTER); nav.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button after = BetaUi.action(this, "›", false, dark, () -> move(months, 1));
        after.setContentDescription(t(months ? "다음 달" : "다음 주", months ? "Next month" : "Next week")); nav.addView(after, new LinearLayout.LayoutParams(Ui.dp(this, 48), -2)); parent.addView(nav);
    }
    void move(boolean months, int direction) {
        if (months) { month = month.plusMonths(direction); anchor = direction > 0 ? month.atDay(1) : month.atEndOfMonth(); }
        else { anchor = GrowthCalendarNavigation.adjacentAnchor(month, anchor, direction); month = YearMonth.from(anchor); }
        render();
    }
    void week(LinearLayout parent, JSONObject days) {
        parent.setTag("beta9911:calendar-list"); navigation(parent, false); String expected = account;
        for (LocalDate day : HudUiModel.week(month, anchor)) {
            String date = day.toString(); JSONObject row = days.optJSONObject(date); boolean future = day.isAfter(BetaV4.day(System.currentTimeMillis()));
            View entry = BetaUi.row(this, day.format(DateTimeFormatter.ofPattern(BetaCopy.ko(this) ? "M월 d일 EEEE" : "EEE, MMM d", getResources().getConfiguration().getLocales().get(0))), future ? t("예정", "Upcoming") : BetaMedalUi.status(this, row), dark, () -> BetaMedalUi.day(this, date, row, dark, expected));
            entry.setEnabled(!future); entry.setTag("beta9911:day:" + date); parent.addView(entry);
        }
    }
    void grid(LinearLayout parent, JSONObject days) {
        GridLayout grid = new GridLayout(this); grid.setColumnCount(7); parent.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        String[] weekdays = BetaCopy.ko(this) ? new String[]{"월", "화", "수", "목", "금", "토", "일"} : new String[]{"M", "T", "W", "T", "F", "S", "S"};
        for (String label : weekdays) {
            TextView name = BetaUi.note(this, label, dark); name.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(); lp.width = 0; lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); grid.addView(name, lp);
        }
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1; String expected = account;
        for (int cell = 0; cell < offset + month.lengthOfMonth(); cell++) {
            LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setMinimumHeight(Ui.dp(this, 64));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(1 + cell / 7), GridLayout.spec(cell % 7, 1f)); lp.width = 0; lp.height = -2; grid.addView(tile, lp);
            int number = cell - offset + 1; if (number < 1) continue;
            String date = month.atDay(number).toString(); JSONObject row = days.optJSONObject(date); tile.addView(BetaUi.body(this, Integer.toString(number), dark));
            boolean future = month.atDay(number).isAfter(BetaV4.day(System.currentTimeMillis())); int grade = row == null ? future ? -4 : -1 : row.optBoolean("active") ? row.optInt("grade") : row.optBoolean("measured") ? -2 : -3;
            ImageView icon = new ImageView(this); icon.setImageDrawable(new BetaMedalUi.Mark(this, grade, row != null && row.optBoolean("active"), dark)); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); tile.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 28), Ui.dp(this, 28)));
            tile.setTag("beta9911:day:" + date); tile.setContentDescription(date + " · " + (future ? t("예정", "Upcoming") : BetaMedalUi.status(this, row)));
            tile.setFocusable(!future); tile.setClickable(!future); tile.setEnabled(!future);
            android.util.TypedValue ripple = new android.util.TypedValue(); getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true); if (ripple.resourceId != 0) tile.setBackgroundResource(ripple.resourceId);
            tile.setOnClickListener(v -> BetaMedalUi.day(this, date, row, dark, expected));
        }
    }
    String t(String ko, String en, Object... args) { return BetaExperienceUi.t(this, ko, en, args); }
}
