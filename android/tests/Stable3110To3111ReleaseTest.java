package dev.bennett.codexmeter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Exercise the same release parser, channel and archive identity checks shipped in 3.1.10. */
public final class Stable3110To3111ReleaseTest {
    private static final String VERSION = "3.1.11";
    private static final String APK = "CodexMeter-3.1.11-ko.apk";
    private static final String DOWNLOAD = GitHubReleaseSource.REPOSITORY_URL + "/releases/download/v3.1.11/";
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void discover(String json) throws Exception {
        List<GitHubRelease> releases = GitHubReleaseParser.parse(json);
        for (String channel : new String[]{UpdateChannel.STABLE, UpdateChannel.ALPHA}) {
            GitHubRelease update = UpdateChannel.selectUpdate(releases, "3.1.10", channel);
            check(update != null && VERSION.equals(update.version), "3.1.10 must discover 3.1.11");
            check(!update.prerelease && "v3.1.11".equals(update.tag), "Expected a stable release tag");
            check(APK.equals(update.apkName) && update.apkSize > 0, "Expected the phone APK");
            check((DOWNLOAD + APK).equals(update.apkUrl), "Unexpected APK download URL");
            check((DOWNLOAD + "SHA256SUMS.txt").equals(update.checksumUrl), "Unexpected checksum URL");
            check(UpdateChannel.selectUpdate(releases, VERSION, channel) == null, "Update offered twice");
            check(UpdateChannel.selectUpdate(releases, "3.1.12", channel) == null, "Downgrade offered");
        }
    }

    public static void main(String[] args) throws Exception {
        JSONObject current = Stable319ReleaseSelfTest.release("v3.1.10", "CodexMeter-3.1.10-ko.apk");
        JSONObject next = Stable319ReleaseSelfTest.release("v3.1.11", APK);
        discover(new JSONArray().put(current).put(next).toString());
        discover(new JSONArray().put(next).put(current).toString());

        JSONObject draft = new JSONObject(next.toString()).put("draft", true);
        JSONObject incomplete = new JSONObject(next.toString());
        incomplete.getJSONArray("assets").remove(1);
        for (JSONObject unavailable : new JSONObject[]{draft, incomplete}) {
            List<GitHubRelease> releases = GitHubReleaseParser.parse(new JSONArray().put(current).put(unavailable).toString());
            check(UpdateChannel.selectUpdate(releases, "3.1.10", UpdateChannel.STABLE) == null,
                    "Drafts and releases without checksum assets must not be offered");
        }
        String phone = "dev.bennett.codexmeter";
        String cert = KoreanUpdateTrust.CERTIFICATE;
        check(KoreanUpdateTrust.compatible(phone, cert, 97, 98), "Expected signed phone code 97 to 98");
        check(!KoreanUpdateTrust.compatible(phone, cert, 98, 98), "Same code accepted");
        check(!KoreanUpdateTrust.compatible(phone, cert, 98, 97), "Older code accepted");
        check(!KoreanUpdateTrust.compatible(phone, "unknown", 97, 98), "Wrong signer accepted");
        check(!KoreanUpdateTrust.compatible(phone + ".trial", cert, 97, 98), "Wrong package accepted");

        if (args.length == 1) discover(Files.readString(Path.of(args[0])));
        System.out.println("3.1.10 to 3.1.11 release discovery: " + checks + " checks passed"
                + (args.length == 1 ? " (including supplied release feed)" : " (fixtures)"));
    }
}
