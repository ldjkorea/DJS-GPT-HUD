package dev.bennett.codexmeter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.IdentityHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Preview the actual native overview using only in-memory medal summaries. */
final class GrowthDesignNativeChecks {
    static void verify(Stable319UiNativeTest test, GrowthActivity growth) {
        try {
            long now = System.currentTimeMillis();
            JSONObject state = new JSONObject().put("level", 31);
            BetaGrowthModel model = model(now);
            JSONObject medals = new JSONObject().put("date", BetaV4.day(now).toString()).put("week", 2).put("month", 2);
            LinearLayout overview = new LinearLayout(growth);
            BetaCompactGrowthUi.overview(growth, overview, state, model, medals, false, Ui.isDark(growth));
            TextView title = overview.findViewWithTag("growth:medal-goal");
            test.check(title != null && title.getText().toString().contains(BetaMedalUi.grade(growth,1)),
                    "Next bronze medal is identified by text");
            LinearLayout stamps = overview.findViewWithTag("growth:day-stamps");
            test.check(stamps.getChildCount() == 3, "Each bronze day has a slot");
            test.check(stamps.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS,
                    "Decorative stamps do not create individual accessibility stops");
            test.check(Stable319UiNativeTest.text(overview).contains("2 / 3"), "Activity dates readable independently of color");
            View records = overview.findViewWithTag("beta9915:medal-records");
            View collection = overview.findViewWithTag("beta9911:collections");
            test.check(records.isClickable() && collection.isClickable() && records.getMinimumHeight() >= Ui.dp(growth,48)
                    && collection.getMinimumHeight() >= Ui.dp(growth,48), "Collection and record navigation retain touch targets");
            test.check(Stable319UiNativeTest.text(overview).contains("Lv. 31"), "Level detail uses the captured level");

            medals.put("week",5).put("month",13);
            LinearLayout monthly = new LinearLayout(growth);
            BetaCompactGrowthUi.goalSection(growth,monthly,state,model,medals,false,Ui.isDark(growth));
            LinearLayout monthSlots = monthly.findViewWithTag("growth:day-stamps");
            test.check(monthSlots.getChildCount() == 14 && Stable319UiNativeTest.text(monthly).contains("13 / 14"),
                    "Monthly gold shows fourteen distinct day slots");
            test.check(Stable319UiNativeTest.text(monthly).contains(BetaMedalUi.grade(growth,3)),
                    "Monthly gold is labeled independently of color");
        } catch (JSONException e) {
            throw new AssertionError(e);
        }
    }

    private static BetaGrowthModel model(long now) throws JSONException {
        return new BetaGrowthModel(new JSONObject().put("enabled", true).put("last_response", now),
                BetaGrowthModel.Status.COMPLETE);
    }

    /** Called off the UI thread by the isolated instrumentation runner. */
    static void capture(Stable319UiNativeTest test, GrowthActivity growth, String suffix) throws Exception {
        long now = System.currentTimeMillis();
        LocalDate date = BetaV4.day(now);
        // One more recorded date is feasible even on the final day of a period.
        int week = Math.min(3,date.getDayOfWeek().getValue()-1);
        boolean stampedToday = week > 0 && date.getDayOfWeek().getValue() < 7;
        JSONObject medals = new JSONObject().put("week",week).put("month",Math.min(week,date.getDayOfMonth()))
                .put("date",date.toString());
        if (stampedToday) medals.put("today",new JSONObject().put("active",true).put("grade",1));
        JSONObject state = new JSONObject().put("level",31);
        BetaGrowthModel model = model(now);
        LinearLayout parent = growth.findViewById(R.id.dashboard_content);
        LinearLayout[] preview = new LinearLayout[1];
        Map<View,Integer> visibility = new IdentityHashMap<>();
        try {
            test.main(() -> {
                for (int i=0; i<parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    visibility.put(child,child.getVisibility());
                    child.setVisibility(View.GONE);
                }
                preview[0] = new LinearLayout(growth);
                preview[0].setOrientation(LinearLayout.VERTICAL);
                parent.addView(preview[0],new LinearLayout.LayoutParams(-1,-2));
                BetaCompactGrowthUi.overview(growth,preview[0],state,model,medals,false,Ui.isDark(growth));
            });
            test.waitForIdleSync();
            test.main(() -> {
                test.check(preview[0].isShown() && preview[0].findViewWithTag("growth:medal-goal") != null,
                        "Synthetic preview renders the native goal");
                test.bounds(preview[0]);
            });
            test.shot(test.getTargetContext(),"growth-goal-example-"+suffix);
        } finally {
            test.main(() -> {
                if (preview[0] != null) parent.removeView(preview[0]);
                for (Map.Entry<View,Integer> entry : visibility.entrySet()) entry.getKey().setVisibility(entry.getValue());
            });
        }
    }
}
