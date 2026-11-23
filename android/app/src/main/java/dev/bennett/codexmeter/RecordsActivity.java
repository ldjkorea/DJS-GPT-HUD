package dev.bennett.codexmeter;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.json.*;

/** Account-guarded records. Reading failures never turn into zero activity. */
public final class RecordsActivity extends AppCompatActivity {
    LinearLayout body;
    boolean dark, registered, loadFailed, loading, reloadPending;
    String account;
    long generation;
    JSONObject state;
    LocalDate anchor = BetaV4.day(System.currentTimeMillis());
    YearMonth month = YearMonth.from(anchor);
    boolean monthView;
    final OnBackPressedCallback closeMonth = new OnBackPressedCallback(false) {
        @Override public void handleOnBackPressed() { showMonth(false); }
    };
    float calendarWidthDp;
    final BroadcastReceiver updated = new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) { load(); }
    };
    @Override protected void onCreate(Bundle saved) {
        Ui.applySelectedTheme(this); super.onCreate(saved); dark = Ui.isDark(this);
        if (requestsDetails(getIntent())) {
            details(getIntent()); finish(); return;
        }
        monthView = saved == null ? getIntent().getBooleanExtra("medal_current_month", false)
                : saved.getBoolean("records_show_month");
        if (saved != null) try {
            month = YearMonth.parse(saved.getString("medal_month", month.toString()));
            anchor = LocalDate.parse(saved.getString("medal_anchor", anchor.toString()));
        } catch (Exception ignored) { }
        body = Ui.installPage(this, getString(R.string.matte_records), false).content;
        MatteNav.install(this, 2); findViewById(R.id.dashboard_refresh).setEnabled(false);
        getOnBackPressedDispatcher().addCallback(this, closeMonth);
        closeMonth.setEnabled(monthView);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (requestsDetails(intent)) { details(intent); return; }
        setIntent(intent);
        if (intent.getBooleanExtra("medal_current_month", false)) {
            anchor = BetaV4.day(System.currentTimeMillis()); month = YearMonth.from(anchor);
            monthView = true; closeMonth.setEnabled(true);
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
        out.putBoolean("records_show_month", monthView);
    }
    private boolean requestsDetails(Intent intent) {
        return intent.getBooleanExtra("manual_record", false) || intent.getStringExtra("selected_date") != null
                || intent.getIntExtra("screen", 0) > 0;
    }
    private void details(Intent source) {
        startActivity(new Intent(this, LedgerAnalyticsActivity.class).putExtras(source).putExtra("medal_usage_details", true));
    }
    void load() {
        String expected = SubscriptionStore.accountKey(this);
        if (loading && java.util.Objects.equals(expected, account)) { reloadPending = true; return; }
        if (!java.util.Objects.equals(expected, account)) { state = null; loadFailed = false; account = expected; }
        long ticket = ++generation;
        reloadPending = false;
        loading = expected != null;
        if (!loading) { render(); return; }
        if (state == null || loadFailed) render();
        BetaMedalStore.WORKER.execute(() -> {
            JSONObject loaded = null; boolean error = false;
            try { loaded = BetaMedalStore.load(getApplicationContext(), expected); } catch (Exception unavailable) { error = true; }
            JSONObject value = loaded; boolean failed = error;
            runOnUiThread(() -> {
                if (isDestroyed() || isFinishing() || ticket != generation) return;
                loading = false;
                if (!java.util.Objects.equals(expected, SubscriptionStore.accountKey(this))) { load(); return; }
                loadFailed = failed;
                if (!failed) state = value;
                if (reloadPending) load();
                else render();
            });
        });
    }
    void render() {
        body.removeAllViews(); body.setTag("beta9911:records");
        if (TrialDemo.active(this)) body.addView(BetaUi.note(this, BetaCopy.t(this, "demo"), dark));
        renderRecords();
        body.addView(BetaUi.row(this, t("사용량 상세", "Usage details"), "", dark, () -> details(getIntent())));
    }
    private void renderRecords() {
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
        LocalDate today = BetaV4.day(System.currentTimeMillis());
        LocalDate monday = monday(anchor);
        boolean thisWeek = monday.equals(monday(today));
        String heading = monthView ? t("한 달의 기록", "Monthly record")
                : thisWeek ? t("이번 주의 발자국", "This week’s footprints") : t("한 주의 발자국", "Weekly footprints");
        LinearLayout calendar = BetaUi.linkedGroup(body, heading,
                monthView ? t("주간 보기", "View week") : t("한 달 모아보기", "View month"), dark, () -> showMonth(!monthView));
        LinearLayout header = (LinearLayout) calendar.getChildAt(0);
        header.getChildAt(1).setTag("records:change-period");
        calendar.setTag("beta9911:calendar");
        calendar.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
        navigation(calendar, monthView);
        int medals = BetaMedalModel.count(days, monthView ? month.atDay(1) : monday,
                monthView ? month.atEndOfMonth() : monday.plusDays(6));
        TextView count = BetaUi.note(this, t("메달을 남긴 날 %d일", "Medal days: %d", medals), dark);
        count.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
        count.setTag("records:period-count");calendar.addView(count);
        float width = calendarWidthDp > 0 ? calendarWidthDp : getResources().getConfiguration().screenWidthDp - 48;
        float fontScale = getResources().getConfiguration().fontScale;
        boolean grid = HudUiModel.calendarGrid(width, fontScale);
        if (monthView && grid) {
            grid(calendar, days);
        } else if (monthView) {
            navigation(calendar, false);
            List<LocalDate> dates = HudUiModel.week(month, anchor);
            dayList(calendar, days, dates.get(0), dates.size(), today);
        } else if (fontScale > 1.3f || width < 4 * 48) {
            dayList(calendar, days, monday, 7, today);
        } else {
            GridLayout strip = new GridLayout(this);
            int columns = grid ? 7 : 4;
            strip.setColumnCount(columns); strip.setTag("records:week-strip");
            calendar.addView(strip, new LinearLayout.LayoutParams(-1, -2));
            for (int i=0; i<7; i++) addTile(strip, monday.plusDays(i), today, days, i / columns, i % columns, true);
        }
        if (!(monthView ? month.equals(YearMonth.from(today)) : thisWeek)) {
            calendar.addView(BetaUi.row(this, t("오늘로 돌아가기", "Back to today"), "", dark, () -> {
                anchor = BetaV4.day(System.currentTimeMillis()); month = YearMonth.from(anchor); render();
            }));
        }
        calendar.addView(BetaUi.row(this, t("기호·메달 기준", "Symbols and medal rules"), "", dark, () -> BetaMedalUi.legend(this, dark)));
        calendar.post(() -> {
            if (!calendar.isAttachedToWindow() || isDestroyed()) return;
            float actual = (calendar.getWidth() - calendar.getPaddingLeft() - calendar.getPaddingRight()) / getResources().getDisplayMetrics().density;
            if (actual <= 0) return; calendarWidthDp = actual;
            if (grid != HudUiModel.calendarGrid(actual, fontScale) || !monthView && (width < 4 * 48) != (actual < 4 * 48)) render();
        });
    }
    private static LocalDate monday(LocalDate day) {
        return day.minusDays(day.getDayOfWeek().getValue() - 1);
    }
    private void showMonth(boolean value) {
        monthView = value; closeMonth.setEnabled(value); render();
    }
    void navigation(LinearLayout parent, boolean months) {
        LinearLayout nav = new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        Button before = BetaUi.action(this, "‹", false, dark, () -> browse(months, -1));
        before.setPadding(0,0,0,0); before.setTag(months ? "records:previous-month" : "records:previous-week");
        before.setContentDescription(t(months ? "이전 달" : "이전 주", months ? "Previous month" : "Previous week"));
        nav.addView(before, new LinearLayout.LayoutParams(Ui.dp(this, 48), -2));
        String label;
        if (months) {
            label = month.format(DateTimeFormatter.ofPattern(BetaCopy.ko(this) ? "yyyy년 M월" : "MMMM yyyy", getResources().getConfiguration().getLocales().get(0)));
        } else {
            LocalDate start = monday(anchor), end = start.plusDays(6);
            if (monthView) {
                if (start.isBefore(month.atDay(1))) start = month.atDay(1);
                if (end.isAfter(month.atEndOfMonth())) end = month.atEndOfMonth();
            }
            boolean showYear = start.getYear() != BetaV4.day(System.currentTimeMillis()).getYear()
                    || start.getYear() != end.getYear();
            DateTimeFormatter format = DateTimeFormatter.ofPattern(showYear ? "yyyy/M/d" : "M/d");
            label = start.format(format) + " – " + end.format(format);
        }
        TextView title = BetaUi.body(this, label, dark); title.setGravity(Gravity.CENTER); nav.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button after = BetaUi.action(this, "›", false, dark, () -> browse(months, 1));
        after.setPadding(0,0,0,0); after.setTag(months ? "records:next-month" : "records:next-week");
        after.setContentDescription(t(months ? "다음 달" : "다음 주", months ? "Next month" : "Next week")); nav.addView(after, new LinearLayout.LayoutParams(Ui.dp(this, 48), -2)); parent.addView(nav);
    }
    private void browse(boolean months, int direction) {
        if (monthView) { move(months, direction); return; }
        anchor = anchor.plusWeeks(direction); month = YearMonth.from(anchor); render();
    }
    void move(boolean months, int direction) {
        if (months) { month = month.plusMonths(direction); anchor = direction > 0 ? month.atDay(1) : month.atEndOfMonth(); }
        else { anchor = GrowthCalendarNavigation.adjacentAnchor(month, anchor, direction); month = YearMonth.from(anchor); }
        render();
    }
    private void dayList(LinearLayout parent, JSONObject days, LocalDate start, int length, LocalDate today) {
        LinearLayout rows = new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL);
        rows.setTag("beta9911:calendar-list"); parent.addView(rows);
        String expected = account;
        for (int i=0; i<length; i++) {
            LocalDate day = start.plusDays(i);
            String date = day.toString(); JSONObject row = days.optJSONObject(date); boolean future = day.isAfter(today);
            String label = day.format(DateTimeFormatter.ofPattern(BetaCopy.ko(this) ? "M월 d일 EEEE" : "EEE, MMM d", getResources().getConfiguration().getLocales().get(0)));
            if (day.equals(today)) label = t("오늘 · %s", "Today · %s", label);
            View entry = BetaUi.row(this, label, BetaMedalUi.recordStatus(this, day, today, row), dark, () -> BetaMedalUi.day(this, date, row, dark, expected));
            if (future) { entry.setOnClickListener(null); entry.setClickable(false); entry.setFocusable(false); }
            entry.setEnabled(!future); entry.setTag("beta9911:day:" + date); rows.addView(entry);
        }
    }
    void grid(LinearLayout parent, JSONObject days) {
        GridLayout grid = new GridLayout(this); grid.setColumnCount(7); parent.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        String[] weekdays = BetaCopy.ko(this) ? new String[]{"월", "화", "수", "목", "금", "토", "일"} : new String[]{"M", "T", "W", "T", "F", "S", "S"};
        for (String label : weekdays) {
            TextView name = BetaUi.note(this, label, dark); name.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(); lp.width = 0; lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); grid.addView(name, lp);
        }
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
        LocalDate today = BetaV4.day(System.currentTimeMillis());
        for (int i=0; i<month.lengthOfMonth(); i++) {
            int cell = offset + i;
            addTile(grid, month.atDay(i+1), today, days, 1 + cell / 7, cell % 7, false);
        }
    }
    private void addTile(GridLayout grid, LocalDate day, LocalDate today, JSONObject days, int line, int column, boolean withWeekday) {
        String date = day.toString(), expected = account;
        JSONObject record = days.optJSONObject(date);
        boolean future = day.isAfter(today), active = record != null && record.optBoolean("active");
        int grade = record == null ? future ? -4 : -1 : active ? record.optInt("grade") : record.optBoolean("measured") ? -2 : -3;
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setMinimumHeight(Ui.dp(this,64));
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(line), GridLayout.spec(column,1f));
        lp.width = 0; lp.height = -2; grid.addView(tile,lp);
        if (withWeekday) {
            String label = day.equals(today) ? t("오늘","Today") : day.format(DateTimeFormatter.ofPattern("EEE",getResources().getConfiguration().getLocales().get(0)));
            TextView weekday = BetaUi.note(this,label,dark);
            weekday.setGravity(Gravity.CENTER);tile.addView(weekday);
        }
        TextView number=BetaUi.text(this,String.valueOf(day.getDayOfMonth()),withWeekday ? 20 : 15,
                day.equals(today) ? Ui.accent(this,dark) : Ui.mainText(dark),day.equals(today) || withWeekday);
        number.setGravity(Gravity.CENTER);tile.addView(number);
        if(withWeekday&&future){
            TextView upcoming=BetaUi.note(this,t("예정","Upcoming"),dark);
            upcoming.setTextSize(12);upcoming.setGravity(Gravity.CENTER);upcoming.setMinHeight(Ui.dp(this,28));
            tile.addView(upcoming);
        }else{
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(new BetaMedalUi.Mark(this,grade,active,dark));
            tile.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,28),Ui.dp(this,28)));
        }
        for (int i=0; i<tile.getChildCount(); i++) tile.getChildAt(i).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        if (withWeekday || day.equals(today)) {
            int fill=active ? androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(this,dark),BetaMedalUi.color(grade,dark),0.10f)
                    : Ui.controlSurface(this,dark);
            android.graphics.drawable.GradientDrawable outline = LedgerUi.shape(this,fill,12);
            if(day.equals(today))outline.setStroke(Ui.dp(this,1),Ui.accent(this,dark));
            tile.setBackground(new android.graphics.drawable.InsetDrawable(outline,Ui.dp(this,2)));
        }
        String status = BetaMedalUi.recordStatus(this,day,today,record);
        tile.setTag("beta9911:day:" + date);
        tile.setContentDescription((day.equals(today) ? t("오늘 · ","Today · ") : "") + date + " · " + status);
        tile.setFocusable(!future); tile.setEnabled(!future);
        if (!future) {
            android.util.TypedValue ripple = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);
            if (ripple.resourceId != 0) tile.setForeground(
                    androidx.appcompat.content.res.AppCompatResources.getDrawable(this,ripple.resourceId));
            tile.setOnClickListener(v -> BetaMedalUi.day(this,date,record,dark,expected));
            tile.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override public void onInitializeAccessibilityNodeInfo(View host, android.view.accessibility.AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());
                }
            });
        }
    }
    String t(String ko, String en, Object... args) { return BetaExperienceUi.t(this, ko, en, args); }
}
