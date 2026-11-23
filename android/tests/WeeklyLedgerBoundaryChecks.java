package dev.bennett.codexmeter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.io.File;
import java.time.Instant;

/** Simulates two Plus accounts with the same reset window using an isolated ledger. */
final class WeeklyLedgerBoundaryChecks {
    static void run(Stable319UiNativeTest test, MainActivity activity) throws Exception {
        Context app = test.getTargetContext();
        File file = File.createTempFile("weekly-account-boundary-", ".db", app.getCacheDir());
        String prefsName = file.getName();
        SharedPreferences state = app.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        boolean[] clearDuringRead = {false};
        Context isolated = new ContextWrapper(app) {
            @Override public File getDatabasePath(String name) {
                if (clearDuringRead[0]) {
                    clearDuringRead[0] = false;
                    if (!state.edit().putString("clear_pending", "1").commit())
                        throw new AssertionError("Failed to stage a pending clear");
                }
                return file;
            }

            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return "codex_next_ledger_state".equals(name) ? state : super.getSharedPreferences(name, mode);
            }
        };
        long now = Instant.parse("2026-10-28T03:05:00Z").toEpochMilli();
        long reset = now + 86400000L;
        UsageWindow week = new UsageWindow(22, 604800, 0, reset / 1000);
        UsageSnapshot snapshot = new UsageSnapshot("plus", true, false, null, week, now);
        try {
            try (SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file, null)) {
                db.execSQL(UsageLedgerDatabase.CREATE_OBSERVATIONS);
                db.execSQL("INSERT INTO observations(meter,plan,policy,observed_at,used,used_decimal,"
                        + "reset_at,window_seconds,source,manual) "
                        + "VALUES('weekly','plus','weekly|plus|604800',?,22,'22',?,604800,'api_rounded',0)",
                        new Object[]{now, reset});
            }
            test.check(BetaWeeklyReader.read(isolated, snapshot, now).recordedDays == 1,
                    "A matching observation is visible before an account clear");
            test.check(state.edit().putString("clear_pending", "1").commit(), "Stage persisted clear");
            assertBlocked(test, isolated, snapshot, now);
            test.check("1".equals(state.getString("clear_pending", "")),
                    "The read-only graph cannot finish the deferred clear");
            test.check(state.edit().remove("clear_pending").commit(), "Prepare in-flight clear");
            clearDuringRead[0] = true;
            assertBlocked(test, isolated, snapshot, now);
            // Model successful recovery by the separate writer before retrying the same snapshot.
            try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                test.check(db.delete("observations", null, null) == 1,
                        "Blocked reads preserved the old row for the clear owner");
            }
            test.check(state.edit().remove("clear_pending").commit(), "Finish synthetic clear");
            test.check(BetaWeeklyReader.read(isolated, snapshot, now).recordedDays == 0,
                    "The same plan and snapshot can retry without the prior account's day");
        } finally {
            try {
                app.deleteSharedPreferences(prefsName);
            } finally {
                SQLiteDatabase.deleteDatabase(file);
            }
        }
        checkInFlightReload(test, activity);
    }

    private static void checkInFlightReload(Stable319UiNativeTest test, MainActivity activity) throws Exception {
        // Invalidate the same request stamp as a clear without clearing the synthetic app's ledger.
        SharedPreferences prefs = activity.getSharedPreferences("codex_subscription_cost", Context.MODE_PRIVATE);
        String key = "qa_weekly_boundary_" + System.nanoTime();
        LinearLayout[] graph = new LinearLayout[1];
        try {
            test.main(() -> {
                graph[0] = new LinearLayout(activity);
                activity.addContentView(graph[0], new ViewGroup.LayoutParams(1, 1));
                BetaWeeklyUi.load(activity, graph[0], AppPreferences.loadSnapshot(activity), true);
                // Its UI callback cannot run until this main-thread action finishes.
                test.check(prefs.edit().putString(key, "invalidate").commit(), "Invalidate in-flight request");
            });
            boolean[] loaded = {false};
            for (int pass = 0; pass < 10 && !loaded[0]; pass++) {
                test.workers();
                test.main(() -> loaded[0] = graph[0].findViewWithTag("home:weekly-legend") != null
                        || graph[0].findViewWithTag("beta999:weekly-missing") != null);
            }
            test.check(loaded[0], "Still-visible same-account graph reloads after request invalidation");
        } finally {
            test.main(() -> {
                if (graph[0] != null && graph[0].getParent() instanceof ViewGroup)
                    ((ViewGroup) graph[0].getParent()).removeView(graph[0]);
                test.check(prefs.edit().remove(key).commit(), "Restore synthetic settings");
            });
        }
    }

    private static void assertBlocked(Stable319UiNativeTest test, Context context,
            UsageSnapshot snapshot, long now) throws Exception {
        try {
            BetaWeeklyReader.read(context, snapshot, now);
            throw new AssertionError("A pending clear exposed the previous account's observations");
        } catch (IllegalStateException expected) {
            test.check("History clear pending".equals(expected.getMessage()), "Weekly read fails closed");
        }
    }
}
