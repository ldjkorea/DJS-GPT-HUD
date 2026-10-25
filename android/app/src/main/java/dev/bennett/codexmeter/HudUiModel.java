package dev.bennett.codexmeter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Presentation decisions only. Never reads, edits or awards stored activity. */
final class HudUiModel {
    private HudUiModel() { }
    static boolean calendarGrid(float widthDp, float fontScale) {
        return fontScale <= 1.3f && widthDp >= 7 * 48;
    }
    static List<LocalDate> week(YearMonth month, LocalDate anchor) {
        LocalDate day = anchor.isBefore(month.atDay(1)) ? month.atDay(1)
                : anchor.isAfter(month.atEndOfMonth()) ? month.atEndOfMonth() : anchor;
        LocalDate monday = day.minusDays(day.getDayOfWeek().getValue() - 1);
        ArrayList<LocalDate> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            if (YearMonth.from(date).equals(month)) result.add(date);
        }
        return result;
    }
    static int visibleMedals(JSONObject facts, String[] ids, int filter) {
        int count = 0;
        for (String id : ids) {
            boolean owned = facts != null && facts.has(id);
            if (filter == 1 && !owned || filter == 2 && owned) continue;
            count++;
        }
        return count;
    }
    static int remaining(int current, int target) {
        return (int) Math.max(0, (long) target - Math.max(0, current));
    }
}
