package dev.bennett.codexmeter;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inspector.WindowInspector;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
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
        JSONObject expected = BetaMedalModel.nextPeriod(medals,now);
        test.check(expected != null,"Synthetic date has an attainable period medal");
        int grade = expected.getInt("grade");
        GrowthActivity[] foreground = new GrowthActivity[1];
        long deadline = SystemClock.uptimeMillis()+8000;
        do {
            test.main(() -> foreground[0] = focusedGrowth(growth));
            if (foreground[0] == null) SystemClock.sleep(80);
        } while (foreground[0] == null && SystemClock.uptimeMillis() < deadline);
        test.check(foreground[0] != null,"A real focused growth window is available on the emulator");
        GrowthActivity front = foreground[0];
        PopupWindow[] window = new PopupWindow[1];
        LinearLayout[] preview = new LinearLayout[1];
        String fileName = "growth-goal-example-" + suffix;
        boolean dark = Ui.isDark(front);
        try {
            test.main(() -> {
                // A resumed Activity can still be a different instance after a theme or
                // locale change. Target the focused app token and cover only its scroll
                // viewport, keeping the production toolbar, navigation and system bars.
                View viewport = front.findViewById(R.id.dashboard_scroll);
                LinearLayout live = front.findViewById(R.id.dashboard_content);
                Rect visible = new Rect();
                test.check(viewport.getGlobalVisibleRect(visible) && front.hasWindowFocus(),"Focused growth viewport is visible");
                int[] position = new int[2];
                viewport.getLocationOnScreen(position);
                ScrollView scroll = new ScrollView(front);
                scroll.setFillViewport(true);
                scroll.setVerticalScrollBarEnabled(false);
                preview[0] = new LinearLayout(front);
                preview[0].setOrientation(LinearLayout.VERTICAL);
                preview[0].setPadding(live.getPaddingLeft(),live.getPaddingTop(),live.getPaddingRight(),live.getPaddingBottom());
                scroll.addView(preview[0]);
                BetaCompactGrowthUi.overview(front,preview[0],state,model,medals,false,dark);
                PopupWindow popup = new PopupWindow(scroll,viewport.getWidth(),viewport.getHeight(),true);
                window[0] = popup;
                popup.setBackgroundDrawable(new ColorDrawable(Ui.background(front,dark)));
                popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                popup.setIsLaidOutInScreen(true);
                popup.setElevation(0);
                popup.setAnimationStyle(0);
                popup.showAtLocation(viewport,Gravity.TOP|Gravity.LEFT,position[0],position[1]);
            });
            Rect badge = new Rect();
            long until = SystemClock.uptimeMillis()+8000;
            int metal = BetaMedalUi.color(grade,dark);
            String medalLabel = BetaMedalUi.grade(front,grade);
            do {
                test.main(() -> {
                    badge.setEmpty();
                    TextView heading = preview[0].findViewWithTag("growth:medal-goal");
                    if (!preview[0].hasWindowFocus() || heading == null || !heading.getGlobalVisibleRect(new Rect())
                            || !heading.getText().toString().contains(medalLabel)) return;
                    ImageView mark = findMedal(preview[0].findViewWithTag("beta9911:next-goal"));
                    if (mark == null || !mark.getGlobalVisibleRect(new Rect())) return;
                    int[] pos = new int[2];
                    mark.getLocationOnScreen(pos);
                    badge.set(pos[0],pos[1],pos[0]+mark.getWidth(),pos[1]+mark.getHeight());
                });
                if (!badge.isEmpty()) {
                    Bitmap frame = test.getUiAutomation().takeScreenshot();
                    if (frame != null) try {
                        if (containsMedal(frame,badge,metal)) {
                            test.main(() -> test.bounds(preview[0]));
                            File png = new File(test.getTargetContext().getExternalFilesDir(null),"stable319/"+fileName+".png");
                            try (FileOutputStream out = new FileOutputStream(png)) {
                                test.check(frame.compress(Bitmap.CompressFormat.PNG,100,out),
                                        "Saved the displayed OS frame containing the expected target medal");
                            }
                            return;
                        }
                    } finally { frame.recycle(); }
                }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() < until);
            throw new AssertionError("The focused goal preview never appeared in the emulator screenshot at " + badge);
        } finally {
            test.main(() -> { if (window[0] != null) window[0].dismiss(); });
        }
    }

    private static GrowthActivity focusedGrowth(GrowthActivity started) {
        for (View root : WindowInspector.getGlobalWindowViews()) {
            if (!root.isShown() || !root.hasWindowFocus()) continue;
            if (root == started.getWindow().getDecorView()) return started;
            View content = root.findViewById(R.id.dashboard_content);
            if (content == null) continue;
            Context context = content.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof GrowthActivity) return (GrowthActivity)context;
                Context base = ((ContextWrapper)context).getBaseContext();
                if (context == base) break;
                context = base;
            }
        }
        return null;
    }

    private static ImageView findMedal(View view) {
        if (view instanceof ImageView && ((ImageView)view).getDrawable() instanceof BetaMedalUi.Mark) return (ImageView)view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i=0; i<group.getChildCount(); i++) {
                ImageView found = findMedal(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    private static boolean containsMedal(Bitmap photo, Rect bounds, int expected) {
        Rect area = new Rect(bounds);
        if (!area.intersect(0,0,photo.getWidth(),photo.getHeight())) return false;
        int matched = 0;
        for (int y=area.top; y<area.bottom; y++) for (int x=area.left; x<area.right; x++) {
            int p = photo.getPixel(x,y);
            if (Math.abs(Color.red(p)-Color.red(expected)) <= 12 && Math.abs(Color.green(p)-Color.green(expected)) <= 12
                    && Math.abs(Color.blue(p)-Color.blue(expected)) <= 12) matched++;
        }
        return matched >= Math.max(6,area.width()*area.height()/50);
    }
}
