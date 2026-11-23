package dev.bennett.codexmeter;

import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Measure the real weekly day views; no ledger, account or preference writes. */
final class WeeklyDesignNativeChecks {
    static void verify(Stable319UiNativeTest test, MainActivity home) {
        test.main(() -> {
            LinearLayout card = home.findViewById(R.id.dashboard_content).findViewWithTag("beta999:weekly");
            View total = card.findViewWithTag("beta999:weekly-used");
            View period = card.findViewWithTag("beta999:weekly-period");
            test.check(total != null && period != null && card.indexOfChild(total) < card.indexOfChild(period),
                    "Weekly total is shown before the reset cycle");

            LinearLayout actual = card.findViewWithTag("beta999:weekly-graph");
            test.check(actual.getWidth() >= Ui.dp(home, 48), "Rendered weekly graph has usable width");
            LocalDate today = LocalDate.now(LedgerAggregation.ZONE);
            List<BetaWeeklyUsage.Day> days = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                LocalDate date = today.minusDays(5).plusDays(i);
                BetaWeeklyUsage.Day day = new BetaWeeklyUsage.Day(date, date.isAfter(today));
                if (i < 4) {
                    day.measured = true;
                    day.observations = 2;
                    day.points = i;
                } else if (i == 4) {
                    day.observations = 1;
                }
                days.add(day);
            }
            LinearLayout graph = new LinearLayout(home);
            graph.setOrientation(LinearLayout.VERTICAL);
            long now = System.currentTimeMillis();
            BetaWeeklyUi.render(home, graph, new BetaWeeklyUsage.Model(now-604800000L, now, now, days), Ui.isDark(home));
            GridLayout grid = null;
            for (int i = 0; i < graph.getChildCount(); i++) {
                if (graph.getChildAt(i) instanceof GridLayout) grid = (GridLayout) graph.getChildAt(i);
            }
            test.check(grid != null && grid.getChildCount() == days.size(), "All server-cycle dates are present");
            int minTarget = Ui.dp(home, 48);
            for (int width : new int[]{actual.getWidth(), Ui.dp(home, 640), Ui.dp(home, 288), Ui.dp(home, 154)}) {
                graph.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                graph.layout(0, 0, width, graph.getMeasuredHeight());
                int firstRow = 0;
                for (int i = 0; i < grid.getChildCount(); i++) {
                    View cell = grid.getChildAt(i);
                    test.check(cell.isClickable() && cell.isFocusable() && cell.getWidth() >= minTarget
                            && cell.getHeight() >= minTarget, "Each day has a full 48dp touch target");
                    test.check(cell.getRight() <= width && cell.getLeft() >= 0, "Dates fit the measured width");
                    test.check(cell.getContentDescription().toString().contains(BetaWeeklyUi.dayDescription(home, days.get(i))),
                            "Date accessibility state matches measured, missing or upcoming status");
                    if (cell.getTop() == grid.getChildAt(0).getTop()) firstRow++;
                }
                if (width == Ui.dp(home, 288)) test.check(firstRow == 4, "Eight dates form two balanced rows in a phone card");
                if (width == Ui.dp(home, 640)) test.check(firstRow == 8, "All eight dates fit a wide card");
            }
        });
    }
}
