package dev.bennett.codexmeter;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

/** Exercises the real records page using only the runner's isolated synthetic account. */
final class RecordsReviewNativeChecks {
    static void verify(Stable319UiNativeTest test, RecordsActivity records) throws Exception {
        LocalDate today = BetaV4.day(System.currentTimeMillis());
        LocalDate past = today.minusDays(1);
        LocalDate future = today.plusDays(1);
        JSONObject first = new JSONObject().put("measured", false).put("points", 0).put("reference", 0);
        JSONObject flat = new JSONObject().put("measured", true).put("points", 0).put("reference", 3.5);
        JSONObject older = new JSONObject().put("active", true).put("points", 4);
        boolean korean = BetaCopy.ko(records);
        test.ready(records,"records:change-period");
        test.main(() -> {
            test.check(!records.monthView, "The records tab opens on this week");
            LocalDate monday = today.minusDays(today.getDayOfWeek().getValue()-1);
            for (int i=0; i<7; i++) {
                View date = records.body.findViewWithTag("beta9911:day:" + monday.plusDays(i));
                test.check(date != null, "All seven real dates are available in the week");
            }
            View todayTile = records.body.findViewWithTag("beta9911:day:" + today);
            test.check(todayTile.getContentDescription().toString().contains(korean ? "오늘" : "Today"), "Today is named in the calendar");
            View largeList = records.body.findViewWithTag("beta9911:calendar-list");
            if (records.getResources().getConfiguration().fontScale > 1.3f)
                test.check(largeList != null, "Large fonts get fully spelled-out date and status rows");
            else
                test.check(records.body.findViewWithTag("records:week-strip") != null || largeList != null,
                        "A weekly day selector is present");
            LocalDate acrossMonths = LocalDate.of(2026,10,26);
            records.month = YearMonth.from(acrossMonths); records.anchor = acrossMonths; records.render();
            test.check(records.body.findViewWithTag("beta9911:day:2026-11-01") != null, "Sunday in the next month belongs to its Monday week");
            records.body.findViewWithTag("records:next-week").performClick();
            test.check(records.anchor.equals(acrossMonths.plusWeeks(1)), "Weekly arrow navigates one complete week");
            records.body.findViewWithTag("records:previous-week").performClick();
            test.check(records.anchor.equals(acrossMonths), "Weekly arrows are reversible");
            records.body.findViewWithTag("records:change-period").performClick();
            test.check(records.monthView && records.body.findViewWithTag("records:next-month") != null,
                    "The month control opens navigable monthly records");
            records.getOnBackPressedDispatcher().onBackPressed();
            test.check(!records.monthView && records.body.findViewWithTag("records:next-month") == null,
                    "Back returns to the week without leaving records");
            test.check(BetaMedalUi.recordStatus(records, past, today, null).equals(korean ? "관측 자료 없음" : "No observations"), "Past missing observations have a final status");
            test.check(BetaMedalUi.recordStatus(records, today, today, null).equals(BetaMedalUi.status(records, null)), "Today can still receive observations");
            test.check(BetaMedalUi.recordStatus(records, past, today, first).equals(korean ? "비교 자료 부족" : "Too few comparable observations"), "One old sample cannot imply ongoing collection");
            test.check(!BetaMedalUi.observedIncrease(records, first).contains("0"), "One sample does not imply zero observed usage");
            test.check(BetaMedalUi.observedIncrease(records, flat).contains("0"), "A measured flat interval still shows zero");
            test.check(BetaMedalUi.observedIncrease(records, older).contains("4"), "A retained positive increase is visible without a measurement flag");
            test.check(!BetaMedalUi.personalReference(records, first).contains("0"), "Missing reference is not a zero threshold");
            test.check(BetaMedalUi.personalReference(records, flat).contains("3.5"), "Established personal reference retains its value");
            records.month = YearMonth.from(future);
            records.anchor = future;
            records.render();
            View next = records.body.findViewWithTag("beta9911:day:" + future);
            test.check(next != null && !next.isClickable() && !next.performClick(), "Upcoming dates cannot open an activity detail");
            // Saving instance state stops lifecycle-bound back callbacks; exercise Back while resumed.
            records.body.findViewWithTag("records:change-period").performClick();
            Bundle saved = new Bundle();
            records.onSaveInstanceState(saved);
            test.check(saved.getBoolean("records_show_month"), "Month mode is saved for activity recreation");
            test.check(saved.getString("medal_anchor").equals(future.toString()) && saved.getString("medal_month").equals(records.month.toString()), "Calendar position saved for activity recreation");
        });

        Instrumentation.ActivityMonitor monitor = test.addMonitor(LedgerAnalyticsActivity.class.getName(), null, true);
        try {
            test.main(() -> records.onNewIntent(new Intent(records, RecordsActivity.class).putExtra("selected_date", past.toString())));
            test.check(monitor.getHits() == 1, "An already-open calendar forwards a requested day to usage details");
        } finally {
            test.removeMonitor(monitor);
        }

        test.workers();
        CountDownLatch release = new CountDownLatch(1);
        holdMedalReader(release);
        long[] firstGeneration = new long[1];
        try {
            test.main(() -> {
                records.load();
                firstGeneration[0] = records.generation;
                records.updated.onReceive(records, new Intent(AppConstants.ACTION_USAGE_UPDATED));
                records.updated.onReceive(records, new Intent(AppConstants.ACTION_USAGE_UPDATED));
                test.check(records.generation == firstGeneration[0], "Overlapping updates do not queue duplicate reads");
            });
        } finally {
            release.countDown();
        }
        test.workers();
        test.workers();
        test.main(() -> test.check(!records.loading && records.generation == firstGeneration[0] + 1, "Overlapping updates receive exactly one fresh read"));

        AuthTokens original = SecureTokenStore.load(records);
        test.check(original != null && "stable316-qa".equals(original.accountId), "Only the runner's synthetic account may be switched");
        String account = SubscriptionStore.accountKey(records);
        try {
            CountDownLatch switched = new CountDownLatch(1);
            holdMedalReader(switched);
            try {
                test.main(records::load);
                synchronized (UsageApi.NETWORK_LOCK) {
                    SecureTokenStore.save(records, new AuthTokens("SYNTHETIC_RECORDS_REVIEW", "SYNTHETIC_REFRESH", "",
                            System.currentTimeMillis() + 86400000, "records-review-empty", ""));
                }
            } finally { switched.countDown(); }
            String other = SubscriptionStore.accountKey(records);
            test.workers();
            test.workers();
            test.main(() -> test.check(other != null && other.equals(records.account) && !records.loading
                    && records.state == null, "An account change during a read discards old data and finishes the new read"));
        } finally {
            synchronized (UsageApi.NETWORK_LOCK) { SecureTokenStore.save(records, original); }
            test.main(records::load);
            test.workers();
            test.workers();
        }
        test.main(() -> test.check(account.equals(records.account) && records.state != null && !records.loading,
                "Returning to the original account recovers its records"));
    }

    private static void holdMedalReader(CountDownLatch release) {
        BetaMedalStore.WORKER.execute(() -> {
            try { release.await(30, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }
}
