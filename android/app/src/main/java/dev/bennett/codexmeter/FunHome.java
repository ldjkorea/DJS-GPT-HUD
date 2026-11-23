package dev.bennett.codexmeter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Local home presentation. Results belong to an observation and account, never to an old view. */
final class FunHome {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();

    private static final class Input {
        final UsageSnapshot snapshot;
        final String account, planKey, stamp, coachKey;

        Input(Context context) {
            snapshot = AppPreferences.loadSnapshot(context);
            account = SubscriptionStore.accountKey(context);
            planKey = SubscriptionStore.key(context, snapshot);
            stamp = LiveUsageStore.signature(context, snapshot);
            coachKey = SpicyQuotes.cacheKey(context, account);
        }

        boolean sameReading(Input other) {
            return Objects.equals(account, other.account) && Objects.equals(planKey, other.planKey)
                    && stamp.equals(other.stamp);
        }

        boolean matches(Input other) {
            return sameReading(other) && coachKey.equals(other.coachKey);
        }
    }

    private static final class Reading {
        final Input input;
        final LiveUtilization.Result live;
        final double daily;

        Reading(Input input, LiveUtilization.Result live, double daily) {
            this.input = input;
            this.live = live;
            this.daily = daily;
        }
    }

    private static final class Ready {
        final Input input;
        final Reading reading;

        Ready(Input input, Reading reading) {
            this.input = input;
            this.reading = reading;
        }
    }

    // The single worker publishes independently of the lifetime of the initiating screen.
    private static Reading lastReading;
    private static volatile Ready lastReady;
    private final AppCompatActivity activity;
    private final LinearLayout target, crestTarget;
    private final boolean dark;
    private final Runnable finished;
    private volatile boolean detached;

    private FunHome(AppCompatActivity activity, LinearLayout target, boolean dark, Runnable finished,
                    LinearLayout crestTarget) {
        this.activity = activity;
        this.target = target;
        this.dark = dark;
        this.finished = finished;
        this.crestTarget = crestTarget;
    }

    static void add(AppCompatActivity activity, LinearLayout parent, boolean dark, Runnable finished) {
        add(activity, parent, dark, finished, null);
    }

    static void add(AppCompatActivity activity, LinearLayout parent, boolean dark, Runnable finished,
                    LinearLayout crestTarget) {
        LiveUsageStore.schedule(activity);
        BetaMedalStore.refresh(activity);
        LinearLayout target = new LinearLayout(activity);
        target.setOrientation(LinearLayout.VERTICAL);
        parent.addView(target);
        FunHome home = new FunHome(activity, target, dark, finished, crestTarget);
        target.addView(LedgerUi.caption(activity, activity.getString(R.string.next_loading), dark));
        // Always start with the current observation when this specific slot is attached.
        // Obsolete queue entries must not repeat journal work or select another line.
        target.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) {
                home.detached = false;
                target.post(() -> { if (home.active()) home.request(new Input(activity)); });
            }
            @Override public void onViewDetachedFromWindow(View view) { home.detached = true; }
        });
        if (home.active()) home.request(new Input(activity));
    }

    private boolean active() {
        return !activity.isDestroyed() && !activity.isFinishing() && target.isAttachedToWindow();
    }

    private void request(Input input) {
        if (input.planKey == null || input.account == null) {
            target.removeAllViews();
            target.addView(LedgerUi.caption(activity, activity.getString(R.string.fun_placing), dark));
            return;
        }
        Ready ready = lastReady;
        if (ready != null && ready.input.matches(input)) {
            render(input, ready.reading);
            return;
        }
        WORKER.execute(() -> prepare(input));
    }

    private void prepare(Input input) {
        if (detached) return;
        try {
            // Other home builds may have been queued before the first selection completed.
            Ready ready = lastReady;
            if (ready != null && ready.input.matches(input)) {
                deliver(input, ready.reading);
                return;
            }
            Context app = activity.getApplicationContext();
            Reading reading = lastReading;
            if (reading == null || !input.sameReading(reading.input)) {
                if (!input.sameReading(new Input(activity))) {
                    deliver(input, null);
                    return;
                }
                RpgProgressStore.backup(app);
                UsageLedgerDatabase.Data data = UsageLedgerDatabase.load(app);
                UsageWindow window = input.snapshot.longWindow();
                String policy = (input.snapshot.weekly != null ? "weekly" : "monthly") + "|"
                        + LedgerRecord.cleanPlan(input.snapshot.planType) + "|"
                        + (window == null ? 0 : window.windowSeconds);
                long now = System.currentTimeMillis();
                List<FunInsights.Window> completed = FunInsights.completed(data.records, policy, now);
                if (detached) return;
                if (!input.planKey.equals(SubscriptionStore.key(app, AppPreferences.loadSnapshot(app)))) {
                    deliver(input, null);
                    return;
                }
                if (!FunStore.settle(app, input.planKey, completed,
                        (tier, tone, variant) -> FunCoach.text(app, tone, tier, FunInsights.Situation.TIER, variant))) {
                    throw new IllegalStateException();
                }
                TierStore.evaluate(app, input.planKey, policy, completed);
                reading = new Reading(input, LiveUsageStore.calculate(app, input.snapshot, data),
                        LiveCards.daily(data, policy, now));
                if (!input.stamp.equals(LiveUsageStore.signature(app, AppPreferences.loadSnapshot(app)))) {
                    deliver(input, null);
                    return;
                }
                RpgProgressStore.reconcile(app, input.snapshot, data, input.account);
                lastReading = reading;
            }
            if (detached) return;
            if (!input.matches(new Input(activity))) {
                deliver(input, null);
                return;
            }
            long now = System.currentTimeMillis();
            SpicyQuotes.select(activity, input.account, input.snapshot, fresh(input.snapshot, now),
                    reading.live.bonus, reading.daily, now);
            // A completed selection can intentionally be empty (off, hidden, or a selection cap).
            lastReady = new Ready(input, reading);
            deliver(input, reading);
        } catch (Exception failed) {
            deliver(input, null);
        }
    }

    private void deliver(Input input, Reading reading) {
        activity.runOnUiThread(() -> {
            if (!active()) return;
            Input current = new Input(activity);
            if (!input.matches(current)) {
                // Only an actual change of account/observation/settings starts another request.
                request(current);
            } else if (reading == null) {
                target.removeAllViews();
                target.addView(LedgerUi.caption(activity, activity.getString(R.string.fun_load_failed), dark));
                finished.run();
            } else {
                render(current, reading);
            }
        });
    }

    private boolean fresh(UsageSnapshot snapshot, long now) {
        UsageWindow window = snapshot.longWindow();
        return window != null && now < window.effectiveResetAtMillis(snapshot.fetchedAtMillis)
                && UsageEvidence.state(snapshot, window, !AppPreferences.getLastError(activity).isEmpty(),
                        now, RefreshScheduler.effectiveRefreshMinutes(activity)) == UsageEvidence.State.CONFIRMED;
    }

    private void render(Input input, Reading reading) {
        target.removeAllViews();
        boolean merged = crestTarget != null && "beta999:identity-slot".equals(crestTarget.getTag());
        long now = System.currentTimeMillis();
        boolean fresh = fresh(input.snapshot, now);
        LinearLayout tier = merged ? BetaHomeUi.card(activity, dark, false)
                : LiveCards.crest(activity, dark, reading.live, fresh);
        if (crestTarget != null) {
            crestTarget.removeAllViews();
            crestTarget.addView(tier);
            if (merged) TierTheme.frame((View) crestTarget.getParent(), TierTheme.tier(activity), dark);
            else Ui.addSpacer(crestTarget, 10);
        } else {
            target.addView(tier);
        }
        target.addView(LiveCards.ai(activity, dark, reading.live, input.snapshot, fresh, reading.daily, now));
        finished.run();
    }
}
