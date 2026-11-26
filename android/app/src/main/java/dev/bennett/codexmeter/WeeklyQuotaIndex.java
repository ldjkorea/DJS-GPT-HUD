package dev.bennett.codexmeter;

import java.util.Locale;

/** Display-only weekly allowance comparison, with an estimated Plus week indexed to 100. */
final class WeeklyQuotaIndex {
    final int total;
    final int used;
    final int remaining;

    private WeeklyQuotaIndex(int total, int used) {
        this.total = total;
        this.used = used;
        this.remaining = total - used;
    }

    private enum Plan {
        PLUS("plus", 100), PRO_100("pro100", 1_000), PRO_200("pro200", 2_000), PRO_500("pro500", 5_000);

        final String key;
        final int scale;

        Plan(String key, int scale) {
            this.key = key;
            this.scale = scale;
        }
    }

    private static Plan knownPlan(String planType) {
        if (planType == null) return null;
        String plan = planType.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        switch (plan) {
            case "plus":
                return Plan.PLUS;
            case "pro100":
            case "pro_100":
            case "prolite":
            case "pro_lite":
            case "pro_light":
            case "pro5x":
            case "pro_5x":
                return Plan.PRO_100;
            case "pro200":
            case "pro_200":
            case "pro20x":
            case "pro_20x":
                return Plan.PRO_200;
            case "pro500":
            case "pro_500":
            case "promax":
                return Plan.PRO_500;
            default:
                return null;
        }
    }

    /** Zero means this plan's weekly comparison scale is unknown. */
    static int baseTotal(String planType) {
        Plan plan = knownPlan(planType);
        return plan == null ? 0 : plan.scale;
    }

    /** Stable setting key across wire/display aliases; distinguish the wire ID "pro" from a legacy label. */
    static String planFor(UsageSnapshot snapshot) {
        if (snapshot == null) return null;
        Plan plan = snapshot.providerProTierConfirmed ? Plan.PRO_200 : knownPlan(snapshot.planType);
        return plan == null ? null : plan.key;
    }

    /** Use the observed weekly window's precise percentage. Manual events scale only the index. */
    static WeeklyQuotaIndex estimate(String planType, double usedPercent, boolean doubleEvent) {
        int base = baseTotal(planType);
        if (base == 0 || !Double.isFinite(usedPercent) || usedPercent < 0 || usedPercent > 100) {
            return null;
        }
        int total = doubleEvent ? 2 * base : base;
        int used = (int) Math.round(usedPercent * (total / 100.0));
        // Reserve the exact endpoints for a truly unused or exhausted observed window.
        if (usedPercent > 0 && usedPercent < 100) {
            used = Math.max(1, Math.min(total - 1, used));
        }
        return new WeeklyQuotaIndex(total, used);
    }
}
