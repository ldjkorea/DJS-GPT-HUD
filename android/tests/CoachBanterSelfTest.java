package dev.bennett.codexmeter;

import java.nio.file.*;
import java.util.*;
import org.json.*;

/** Local presentation regressions on synthetic quota values; no Android/account I/O. */
public final class CoachBanterSelfTest {
    private static final long NOW = 1800000000000L;
    private static int checks;
    private static void check(boolean ok, String label) {
        checks++;
        if (!ok) throw new AssertionError(label);
    }
    private static UsageWindow window(double used, long seconds) {
        return new UsageWindow(used, seconds, 0, (NOW + seconds * 500) / 1000);
    }
    private static UsageSnapshot sample(String plan, double shortUsed, double weeklyUsed) {
        return new UsageSnapshot(plan, true, false, window(shortUsed, 18000), window(weeklyUsed, 604800), NOW);
    }
    private static String group(UsageSnapshot snapshot) {
        return CoachCatalog.group(snapshot, true, false, false, 0, NOW);
    }
    public static void main(String[] args) throws Exception {
        JSONObject raw = new JSONObject(Files.readString(Path.of(args[0])));
        JSONArray medals = new JSONObject(Files.readString(Path.of(args[1]))).getJSONArray("quotes");
        for (int i = 0; i < medals.length(); i++) raw.getJSONArray("quotes").put(medals.getJSONObject(i));
        CoachCatalog catalog = new CoachCatalog(raw);
        check(catalog.schemaVersion == 2 && (catalog.contentVersion == 5 || catalog.contentVersion == 6),
                "Banter and redesign catalogs share the same storage schema");
        check(catalog.quotes.size() == 624 && catalog.tips.size() == 40, "All content retained");
        Set<String> texts = new HashSet<>();
        int paid = 0;
        for (CoachCatalog.Quote q : catalog.quotes) {
            check(texts.add(q.text), "Unique text " + q.id);
            check(!q.text.contains("\n") && q.text.length() <= 80, "Compact paragraph " + q.id);
            if (q.group.equals("missing")) {
                check(q.text.contains("한도"), "Fact-first unclassified quota copy " + q.id);
                check(!q.text.contains("가려") && !q.text.contains("못 읽") && !q.text.contains("업데이트"), "No invented collection failure " + q.id);
            }
            if (q.group.equals("network")) check(q.text.startsWith("이번 ") && q.text.contains("실패"), "Lookup failure, no invented cause " + q.id);
            if (q.group.equals("stale")) check((q.text.contains("확인") || q.text.contains("다시 봐")) && !q.text.contains("오래됐"), "Reconfirm time, including future/expired observations " + q.id);
            if (q.paidText.isEmpty()) continue;
            paid++;
            double use = q.group.equals("zero") ? 0 : q.group.equals("tiny") ? 0.1 : 5;
            for (String plan : new String[]{"free", "unknown", "go", "team", "enterprise", "pro500", ""}) {
                check(CoachCatalog.displayText(q, sample(plan, use, use), true, false, false, 0, NOW).equals(q.text), "Unverified plan neutral " + plan);
            }
            for (String plan : new String[]{"plus", "pro", "pro20x", "pro5x", "prolite"}) {
                UsageSnapshot s = sample(plan, use, use);
                check(!CoachCatalog.displayText(q, s, true, false, false, 0, NOW).equals(q.text), "Verified plan only " + plan);
                check(CoachCatalog.displayText(q, s, false, false, false, 0, NOW).equals(q.text), "Stale never paid joke");
                check(CoachCatalog.displayText(q, s, true, true, false, 0, NOW).equals(q.text), "Error never paid joke");
                check(CoachCatalog.displayText(q, sample(plan, 95, 0), true, false, false, 0, NOW).equals(q.text), "Wrong group never paid joke");
            }
        }
        check(paid == 4, "Four guarded variants unchanged");
        // Complete fresh regular observations cannot take the missing-state joke path.
        for (String plan : new String[]{"plus", "pro", "pro20x", "pro5x", "unknown"})
            for (double a : new double[]{0, 0.0001, 0.1, 5, 25, 50, 89.9, 90, 100})
                for (double b : new double[]{0, 0.1, 5, 25, 50, 89.9, 90, 100})
                    check(!group(sample(plan, a, b)).equals("missing"), "Complete normal quota not missing " + plan + "/" + a + "/" + b);
        check(group(null).equals("missing"), "No snapshot");
        check(group(new UsageSnapshot("plus", true, false, null, window(5, 604800), NOW)).equals("missing"), "Only five-hour quota absent, not total read failure");
        UsageLimit extra = new UsageLimit("independent", "Extra", "feature", true, false, window(5, 18000), null);
        UsageSnapshot mixed = new UsageSnapshot("plus", true, false, window(0, 18000), window(0, 604800), Collections.singletonList(extra), null, -1, NOW);
        check(group(mixed).equals("missing"), "Complete independent positive quota: cannot invent combined percentage");
        check(CoachCatalog.group(sample("plus", 10, 10), true, true, false, 0, NOW).equals("network"), "Error wins");
        check(CoachCatalog.group(sample("plus", 95, 0), true, false, true, 25, NOW).equals("critical"), "Constraint wins");
        check(CoachCatalog.group(sample("plus", 0, 0), true, false, false, 0, NOW - 1).equals("stale"), "Future observation needs reconfirmation");
        check(CoachCatalog.group(sample("plus", 0, 0), true, false, false, 0, NOW + 900001).equals("stale"), "Expired observation needs reconfirmation");
        Random random = new Random(241024);
        JSONObject state = catalog.migrate(new JSONObject());
        CoachCatalog.Quote first = catalog.select(state, "low", "spicy", "", "same", false, random);
        String saved = state.toString();
        for (int i = 0; i < 100; i++) {
            check(catalog.select(state, "low", "spicy", "", "same", false, random) == first, "Reading stability");
            check(saved.equals(state.toString()), "Reading does not change history");
        }
        ArrayDeque<String> recent = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            CoachCatalog.Quote q = catalog.select(state, "low", "spicy", "", "new:" + i, false, random);
            check(!recent.contains(q.id), "Priority cannot defeat quote cooldown");
            recent.addLast(q.id); if (recent.size() > 20) recent.removeFirst();
            seen.add(q.id);
            check(state.getJSONArray("recent").length() <= 20 && state.getJSONArray("tips").length() <= 5, "History bounds");
        }
        check(seen.size() == 80, "No low quote starved by weighting");
        check(catalog.select(state, "low", "off", "", "off", true, random) == null, "OFF unchanged");
        for (String g : new String[]{"critical", "missing", "stale", "network"}) {
            for (int i = 0; i < 80; i++) {
                CoachCatalog.Quote q = catalog.select(state, g, "spicy", "developer", "guard:" + i, true, random);
                check(q != null && q.group.equals(g) && q.tipId.isEmpty(), "Unknown/urgent group cannot gain tip");
            }
        }
        JSONArray hidden = new JSONArray();
        for (CoachCatalog.Quote q : catalog.quotes) if (q.selectionPriority == 1) hidden.put(q.id);
        state.put("hidden", hidden);
        check(catalog.select(state, "low", "spicy", "", "hidden-curated", true, random).selectionPriority == 0, "Hidden curated rows fall back without force");
        for (CoachCatalog.Quote q : catalog.quotes) hidden.put(q.id);
        check(catalog.select(state, "low", "spicy", "", "hidden-all", true, random) == null, "All hidden terminates");
        // Older bundled rows have priority zero; malformed priority fails closed.
        JSONObject oldRow = new JSONObject(raw.getJSONArray("quotes").getJSONObject(0).toString());
        oldRow.remove("selectionPriority");
        JSONObject one = new JSONObject(raw.toString()).put("legacyIds", new JSONObject()).put("quotes", new JSONArray().put(oldRow));
        check(new CoachCatalog(one).quotes.get(0).selectionPriority == 0, "Old row backward compatible");
        for (int invalid : new int[]{-1, 2, 1000}) {
            oldRow.put("selectionPriority", invalid);
            boolean rejected = false;
            try { new CoachCatalog(one); } catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "Priority bounded " + invalid);
        }
        // Interest and recent-tip eligibility stay ahead of weighting.
        CoachCatalog.Tip interest = catalog.tips.values().iterator().next();
        CoachCatalog.Quote linked = null;
        for (CoachCatalog.Quote q : catalog.quotes) if (q.tipId.equals(interest.id)) { linked = q; break; }
        check(linked != null, "Interest fixture exists");
        JSONObject neutral = new JSONObject().put("id", "test.neutral").put("situation", linked.group).put("tone", "spicy").put("text", "neutral").put("selectionPriority", 1);
        JSONObject matched = new JSONObject().put("id", "test.matched").put("situation", linked.group).put("tone", "spicy").put("text", "matched").put("tipId", interest.id);
        CoachCatalog pair = new CoachCatalog(new JSONObject(raw.toString()).put("legacyIds", new JSONObject()).put("quotes", new JSONArray().put(neutral).put(matched)));
        JSONObject pairState = pair.migrate(new JSONObject());
        check(pair.select(pairState, linked.group, "spicy", interest.interest, "interest", true, random).id.equals("test.matched"), "Matching interest beats curated weight");
        pairState = pair.migrate(new JSONObject()).put("tips", new JSONArray().put(interest.id));
        check(pair.select(pairState, linked.group, "spicy", interest.interest, "tip-recent", true, random).id.equals("test.neutral"), "Tip cooldown beats interest and weighting");
        System.out.println("Coach banter: " + checks + " assertions passed. Synthetic JVM policy, not live collection/device proof.");
    }
}
