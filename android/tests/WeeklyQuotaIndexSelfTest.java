package dev.bennett.codexmeter;

import org.json.JSONObject;

public final class WeeklyQuotaIndexSelfTest {
    private static int checks;

    private static void check(boolean passes, String description) {
        checks++;
        if (!passes) throw new AssertionError(description);
    }

    private static void expect(String plan, double percent, boolean doubled,
            int total, int used, int remaining) {
        WeeklyQuotaIndex result = WeeklyQuotaIndex.estimate(plan, percent, doubled);
        check(result != null && result.total == total && result.used == used
                        && result.remaining == remaining,
                plan + " used=" + percent + "% double=" + doubled + " expected "
                        + used + " used and " + remaining + " left out of " + total);
    }

    private static void verifyProviderPlan(String plan, String key, int total) throws Exception {
        JSONObject window = new JSONObject().put("used_percent", 27.5).put("limit_window_seconds", 604800);
        JSONObject response = new JSONObject().put("plan_type", plan)
                .put("rate_limit", new JSONObject().put("primary_window", window));
        UsageSnapshot fresh = UsageParser.parse(response.toString(), 1000L);
        UsageSnapshot cached = UsageSnapshot.fromJson(new JSONObject(fresh.toJson().toString()));
        for (UsageSnapshot snapshot : new UsageSnapshot[]{fresh, cached}) {
            check(plan.equals(snapshot.planType), "Preserved original plan identifier " + plan);
            String resolved = WeeklyQuotaIndex.planFor(snapshot);
            check(key.equals(resolved), "Canonical plan " + plan);
            expect(resolved, snapshot.weekly.preciseUsedPercent, true, total * 2,
                    total * 55 / 100, total * 145 / 100);
        }
    }


    private static void verifyWholePercentLabels(java.util.Locale locale, String less, String more) {
        java.util.Locale.setDefault(locale);
        double[] inputs = {0, Double.MIN_VALUE, 0.01, 0.5, 0.99, 1, 27, 58.5, 99, 99.1, 99.51, 99.99, 100};
        String[] used = {"0%", less, less, less, less, "1%", "27%", "59%", "99%", more, more, more, "100%"};
        String[] remaining = {"100%", more, more, more, more, "99%", "73%", "41%", "1%", less, less, less, "0%"};
        for (int i = 0; i < inputs.length; i++) {
            UsageWindow window = new UsageWindow(inputs[i], 604800, 0, 0);
            check(used[i].equals(UsagePrecision.used(window, 0)), "Used whole percent at " + inputs[i]);
            check(remaining[i].equals(UsagePrecision.remaining(window, 0)), "Remaining whole percent at " + inputs[i]);
        }
        UsageWindow nearlySpent = new UsageWindow(99.51, 604800, 0, 0);
        check("<1".equals(UsagePrecision.percent(nearlySpent, false, 0, false)), "Symbol-free widget near zero");
        check(">99".equals(UsagePrecision.percent(nearlySpent, true, 0, false)), "Symbol-free widget near full");
        check("0.49%".equals(UsagePrecision.remaining(nearlySpent, 2)), "Explicit decimal precision retained");
        check("—".equals(UsagePrecision.remaining(null, 0)), "Missing quota stays unknown");
    }

    public static void main(String[] args) throws Exception {
        java.util.Locale savedLocale = java.util.Locale.getDefault();
        try {
            verifyWholePercentLabels(java.util.Locale.KOREAN, "1% 미만", "99% 초과");
            verifyWholePercentLabels(java.util.Locale.ENGLISH, "<1%", ">99%");
        } finally {
            java.util.Locale.setDefault(savedLocale);
        }
        String[] plans = {"Plus", "Pro 100", "Pro 200", "Pro 500"};
        int[] scales = {100, 1_000, 2_000, 5_000};
        for (int i = 0; i < plans.length; i++) {
            for (boolean doubled : new boolean[]{false, true}) {
                int total = doubled ? scales[i] * 2 : scales[i];
                expect(plans[i], 0, doubled, total, 0, total);
                expect(plans[i], 50, doubled, total, total / 2, total / 2);
                expect(plans[i], 27, doubled, total, total * 27 / 100, total * 73 / 100);
                expect(plans[i], 100, doubled, total, total, 0);
            }
        }

        for (String plan : new String[]{"PRO100", "pro-100", "pro_100", " ProLite ", "pro_lite",
                "PRO-LITE", "pro_light", "Pro5x", "PRO_5X"}) {
            expect(plan, 27, true, 2_000, 540, 1_460);
        }
        for (String plan : new String[]{"pro200", "PRO_200", "PRO20X", "pro-20x"}) {
            expect(plan, 27, false, 2_000, 540, 1_460);
        }
        expect("pro_500", 3.75, false, 5_000, 188, 4_812);
        expect("pro500", 3.75, true, 10_000, 375, 9_625);
        expect("promax", 50, false, 5_000, 2_500, 2_500);
        expect("prolite", 0.01, false, 1_000, 1, 999);
        expect("plus", 99.99, false, 100, 99, 1);
        for (String plan : new String[]{null, "", " ", "pro", "go", "free", "enterprise",
                "business", "self_serve_business_prolite", "unknown", "pro___100", "p-lus",
                "Pro 1 00", "pro100-trial"}) {
            check(WeeklyQuotaIndex.baseTotal(plan) == 0, "Unverified band: " + plan);
            check(WeeklyQuotaIndex.estimate(plan, 50, true) == null, "No estimate: " + plan);
        }
        for (double invalid : new double[]{-0.001, 100.01, Double.NaN,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            check(WeeklyQuotaIndex.estimate("plus", invalid, false) == null,
                    "Invalid observation: " + invalid);
        }
        verifyProviderPlan("plus", "plus", 100);
        verifyProviderPlan("prolite", "pro100", 1_000);
        verifyProviderPlan("PRO_5X", "pro100", 1_000);
        verifyProviderPlan("pro", "pro200", 2_000);
        verifyProviderPlan("pro_20x", "pro200", 2_000);
        verifyProviderPlan("promax", "pro500", 5_000);
        verifyProviderPlan("Pro 500", "pro500", 5_000);
        check(WeeklyQuotaIndex.planFor(null) == null, "Null snapshot");
        JSONObject legacy = new JSONObject().put("plan_type", "pro").put("fetched_at", 1000L);
        check(WeeklyQuotaIndex.planFor(UsageSnapshot.fromJson(legacy)) == null, "Legacy Pro stays unknown");
        check(WeeklyQuotaIndex.planFor(UsageParser.parse("{\"plan_type\":\"Pro\"}", 1000L)) == null,
                "Generic display label in place of exact wire enum");
        legacy.put("provider_pro_tier_confirmed", "true");
        check(WeeklyQuotaIndex.planFor(UsageSnapshot.fromJson(legacy)) == null, "Malformed cache marker");
        legacy.put("provider_pro_tier_confirmed", true).put("plan_type", "enterprise");
        check(WeeklyQuotaIndex.planFor(UsageSnapshot.fromJson(legacy)) == null, "Enterprise cannot become Pro");
        System.out.println("Weekly quota index: " + checks + " checks passed");
    }
}
