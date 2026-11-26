package dev.bennett.codexmeter;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.inspector.WindowInspector;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.json.JSONObject;

/** Exercises the home and its real limit detail dialog on a dedicated synthetic emulator. */
public final class WeeklyQuotaIndexNativeTest extends Instrumentation {
    private static final String FIRST = "weekly-index-qa-a";
    private static final String SECOND = "weekly-index-qa-b";
    private static final String REMAINING = "weekly-index:remaining";
    private static final String USED = "weekly-index:used";
    private static final String EVENT = "weekly-index:event";
    private static final long DAY = 86_400_000L;
    private MainActivity home;
    private int checks;
    private boolean verifyColdStart;

    @Override public void onCreate(Bundle args) {
        super.onCreate(args);
        verifyColdStart = "cold".equals(args.getString("phase"));
        start();
    }

    private void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        checks++;
    }

    private <T> T ui(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        runOnMainSync(task);
        return task.get();
    }

    private void await(String description, Callable<Boolean> condition) throws Exception {
        long deadline = SystemClock.elapsedRealtime() + 12_000L;
        do {
            if (ui(condition)) {
                checks++;
                return;
            }
            SystemClock.sleep(100);
        } while (SystemClock.elapsedRealtime() < deadline);
        throw new AssertionError(description);
    }

    private View onHome(String tag) {
        View content = home.findViewById(R.id.dashboard_content);
        return content == null ? null : content.findViewWithTag(tag);
    }

    private View inDialog(String tag) {
        List<View> roots = WindowInspector.getGlobalWindowViews();
        for (View root : roots) {
            if (root == home.getWindow().getDecorView() || !root.isShown()) continue;
            View found = root.findViewWithTag(tag);
            if (found != null && found.isShown()) return found;
        }
        return null;
    }

    private static boolean says(View view, String expected) {
        if (!(view instanceof TextView) || !view.isShown()) return false;
        String text = ((TextView) view).getText().toString().replaceAll("[\\s,\\u00a0\\u202f]", "");
        return text.contains(expected);
    }

    private void homeShows(String ratio) throws Exception {
        await("Home should show the complete remaining index " + ratio, () -> {
            View view = onHome(REMAINING);
            if (!says(view, ratio)) return false;
            android.text.Layout layout = ((TextView) view).getLayout();
            if (layout == null || layout.getLineCount() == 0) return false;
            for (int line = 0; line < layout.getLineCount(); line++) {
                if (layout.getEllipsisCount(line) != 0) return false;
            }
            return true;
        });
    }

    private void openDetails() throws Exception {
        check(ui(() -> onHome("home:quota-details").performClick()), "Home details action");
    }

    private void closeDetails() throws Exception {
        await("Focused details window can be closed", () -> {
            for (View root : WindowInspector.getGlobalWindowViews()) {
                if (root == home.getWindow().getDecorView() || !root.hasWindowFocus()) continue;
                View close = root.findViewById(android.R.id.button1);
                if (close != null && close.isShown() && close.isEnabled()) return close.performClick();
            }
            return false;
        });
        await("Home regains focus", () -> home.hasWindowFocus());
    }

    private void observe(String plan, double used, long fetchedAt, long resetAt) throws Exception {
        UsageWindow week = new UsageWindow(used, 604800L, 0, resetAt / 1000);
        UsageSnapshot sample = new UsageSnapshot(plan, true, false, null, week, fetchedAt);
        showSample(sample);
    }

    private void observeServer(String plan, int used, long now) throws Exception {
        JSONObject week = new UsageWindow(used, 604800L, 0, (now + 3 * DAY) / 1000).toJson();
        JSONObject payload = new JSONObject().put("plan_type", plan)
                .put("rate_limit", new JSONObject().put("primary_window", week));
        showSample(UsageParser.parse(payload.toString(), now));
    }

    private void showSample(UsageSnapshot sample) throws Exception {
        check(AppPreferences.saveSnapshot(getTargetContext(), sample), "Observed fixture saved");
        if (home != null) ui(() -> { home.rebuild(); return null; });
    }

    private void signIn(String account) throws Exception {
        Context c = getTargetContext();
        SecureTokenStore.save(c, new AuthTokens("SYNTHETIC_INDEX_TEST", "SYNTHETIC_INDEX_TEST",
                "", System.currentTimeMillis() + 30 * DAY, account, ""));
        check(account.equals(SecureTokenStore.load(c).accountId), "Synthetic account selected");
    }

    private void openHome() throws Exception {
        home = (MainActivity) startActivitySync(new Intent(getTargetContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        await("Production quota card loaded", () -> home.hasWindowFocus() && onHome("home:quota-details") != null);
    }

    private void checkHomeAndDetails(String remaining, String used) throws Exception {
        homeShows(remaining);
        openDetails();
        await("Detail should show used " + used, () -> says(inDialog(USED), used));
        closeDetails();
    }

    private void setEvent(boolean enabled) throws Exception {
        openDetails();
        await("Event control available", () -> inDialog(EVENT) instanceof CompoundButton);
        check(ui(() -> {
            CompoundButton button = (CompoundButton) inDialog(EVENT);
            if (!button.isEnabled() || !button.isClickable()) return false;
            if (button.isChecked() != enabled) button.performClick();
            return true;
        }), "Event click accepted");
        await("Event state applied", () -> inDialog(EVENT) instanceof CompoundButton
                && ((CompoundButton) inDialog(EVENT)).isChecked() == enabled);
        await("Open detail reflects the new denominator without reopening", () ->
                says(inDialog(USED), enabled ? "/2000" : "/1000"));
        closeDetails();
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            Context c = getTargetContext();
            check(Build.MODEL.contains("sdk") && c.getPackageName().equals("dev.bennett.codexmeter"),
                    "Dedicated emulator required");
            AuthTokens prior = SecureTokenStore.load(c);
            check(prior == null || (prior.accessToken.equals("SYNTHETIC_INDEX_TEST")
                    && (FIRST.equals(prior.accountId) || SECOND.equals(prior.accountId))),
                    "Only empty installation or this test's own fixture");
            if (verifyColdStart) {
                check(prior != null && FIRST.equals(prior.accountId), "Existing synthetic session");
                openHome();
                checkHomeAndDetails("1460/2000", "540/2000");
                setEvent(false);
                homeShows("730/1000");
                result.putString("stream", checks + " cold-start quota index checks passed.\n");
                finish(-1, result);
                return;
            }
            signIn(FIRST);
            AppPreferences.completeOnboarding(c);
            AppPreferences.setAutomaticRefresh(c, false);
            AppPreferences.setRefreshOnLaunch(c, false);
            UpdatePreferences.setAutomaticChecks(c, false);
            UpdatePreferences.setNotifyUpdatesEnabled(c, false);
            BetaIdentity.prefs(c).edit().putBoolean("setup_done", true).commit();
            long now = System.currentTimeMillis();
            observe("prolite", 50, now, now + 3 * DAY);
            openHome();
            setEvent(false);
            checkHomeAndDetails("500/1000", "500/1000");
            openDetails();
            observe("prolite", 27, now, now + 3 * DAY);
            await("The open dialog follows a fresh observation", () -> says(inDialog(USED), "270/1000"));
            closeDetails();
            checkHomeAndDetails("730/1000", "270/1000");
            await("Single weekly observation gets the available card width", () -> {
                View card = onHome("beta998:quota");
                View value = onHome("beta998:quota:weekly:value");
                if (card == null || card.getWidth() == 0 || value == null) return false;
                View column = (View) value.getParent();
                return onHome("beta998:quota:five-hour:value") == null
                        && column.getWidth() * 4 > card.getWidth() * 3;
            });
            setEvent(true);
            checkHomeAndDetails("1460/2000", "540/2000");
            check(ui(() -> says(onHome("beta998:quota:weekly:value"), "73")), "Observed percentage kept");
            ui(() -> { home.finish(); return null; });
            waitForIdleSync();
            openHome();
            homeShows("1460/2000");

            // An already-open dialog must not apply account A's event to another account.
            openDetails();
            await("Original account setting visible", () -> inDialog(EVENT) instanceof CompoundButton);
            CompoundButton oldSetting = ui(() -> (CompoundButton) inDialog(EVENT));
            signIn(SECOND);
            observe("prolite", 27, now, now + 3 * DAY);
            ui(() -> { oldSetting.performClick(); return null; });
            if (ui(() -> !home.hasWindowFocus())) closeDetails();
            homeShows("730/1000");
            signIn(FIRST);
            observe("prolite", 27, now, now + 3 * DAY);
            homeShows("1460/2000");
            observe("plus", 27, now, now + 3 * DAY);
            checkHomeAndDetails("73/100", "27/100");
            UsageWindow five = new UsageWindow(42, 18000L, 0, (now + 7200000L) / 1000);
            UsageWindow weekly = new UsageWindow(27, 604800L, 0, (now + 3 * DAY) / 1000);
            showSample(new UsageSnapshot("plus", true, false, five, weekly, now));
            await("Two observed periods show separate remaining percentages on home", () ->
                    says(onHome("beta998:quota:five-hour:value"), "58%")
                    && says(onHome("beta998:quota:weekly:value"), "73%"));
            openDetails();
            await("Limit details report usage separately for each observed period", () ->
                    says(inDialog("beta998:quota:five-hour:value"), "42%")
                    && says(inDialog("beta998:quota:weekly:value"), "27%")
                    && says(inDialog(USED), "27/100"));
            closeDetails();
            observe("pro_100", 27, now, now + 3 * DAY);
            homeShows("1460/2000");
            setEvent(false);
            homeShows("730/1000");
            String[][] plans = {{"plus", "73/100", "27/100"}, {"pro_200","1460/2000","540/2000"},
                    {"pro_500","3650/5000","1350/5000"}};
            for (String[] plan : plans) {
                observe(plan[0], 27, now, now + 3 * DAY);
                checkHomeAndDetails(plan[1], plan[2]);
            }
            observeServer("pro", 27, now);
            checkHomeAndDetails("1460/2000", "540/2000");
            observeServer("promax", 27, now);
            checkHomeAndDetails("3650/5000", "1350/5000");

            for (String unknown : new String[]{"pro", "free", "go"}) {
                observe(unknown, 27, now, now + 3 * DAY);
                await("No unverified denominator for " + unknown, () ->
                        says(onHome("beta998:quota:weekly:value"), "73")
                        && (onHome(REMAINING) == null || !onHome(REMAINING).isShown()));
                openDetails();
                await("Unsupported tier still reports the observed usage", () ->
                        says(inDialog("beta998:quota:weekly:value"), "27%")
                        && inDialog(USED) == null && inDialog(EVENT) == null);
                closeDetails();
            }
            UsageWindow month = new UsageWindow(27, 30 * 86400L, 0, (now + 15 * DAY) / 1000);
            JSONObject monthly = new JSONObject().put("plan_type", "go").put("rate_limit",
                    new JSONObject().put("primary_window", month.toJson()));
            showSample(UsageParser.parse(monthly.toString(), now));
            await("Month-only account shows actual 73% and no weekly comparison", () ->
                    says(onHome("beta998:quota:monthly:value"), "73")
                    && onHome("beta998:quota:weekly:value") == null && onHome(REMAINING) == null);
            openDetails();
            await("Monthly details report actual use without an event or estimate", () ->
                    says(inDialog("beta998:quota:monthly:value"), "27%")
                    && inDialog("beta998:quota:weekly:value") == null
                    && inDialog(USED) == null && inDialog(EVENT) == null);
            closeDetails();
            observe("prolite", 27, now, now - DAY);
            await("Elapsed window gets no current estimate", () ->
                    onHome(REMAINING) == null || !onHome(REMAINING).isShown());
            observe("prolite", 27, now - 2 * DAY, now + DAY);
            await("Old observation gets no current estimate", () ->
                    onHome(REMAINING) == null || !onHome(REMAINING).isShown());
            observe("prolite", 0, now, now + 7 * DAY);
            checkHomeAndDetails("1000/1000", "0/1000");
            observe("prolite", 27, now, now + 3 * DAY);
            setEvent(true);
            result.putString("stream", checks + " weekly quota index native checks passed.\n");
            finish(-1, result);
        } catch (Throwable error) {
            result.putString("stream", android.util.Log.getStackTraceString(error));
            finish(0, result);
        }
    }
}
