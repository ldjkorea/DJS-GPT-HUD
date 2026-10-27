package dev.bennett.codexmeter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Date navigation only. Displayed weeks are clipped at month boundaries. */
final class GrowthCalendarNavigation {
    private GrowthCalendarNavigation() { }
    static LocalDate adjacentAnchor(YearMonth month, LocalDate anchor, int direction) {
        List<LocalDate> displayed = HudUiModel.week(month, anchor);
        if (direction == 0) return anchor;
        return direction < 0 ? displayed.get(0).minusDays(1)
                : displayed.get(displayed.size() - 1).plusDays(1);
    }
}
