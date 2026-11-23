package dev.bennett.codexmeter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class Stable319To3110ReleaseTest {
    private static final String TARGET = "3.1.10";
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void checkDiscovery(List<GitHubRelease> releases) {
        for (String channel : new String[]{UpdateChannel.STABLE, UpdateChannel.ALPHA}) {
            GitHubRelease update = UpdateChannel.selectUpdate(releases, "3.1.9", channel);
            check(update != null && TARGET.equals(update.version), "3.1.9 must discover " + TARGET);
            check("CodexMeter-3.1.10-ko.apk".equals(update.apkName), "Unexpected phone asset");
            check(!update.prerelease, "Expected a stable release");
            check(UpdateChannel.selectUpdate(releases, TARGET, channel) == null, "Current release reoffered");
            check(UpdateChannel.selectUpdate(releases, "3.1.11", channel) == null, "Offered downgrade");
        }
    }

    public static void main(String[] args) throws Exception {
        JSONObject previous = Stable319ReleaseSelfTest.release("v3.1.9", "CodexMeter-3.1.9-ko.apk");
        JSONObject current = Stable319ReleaseSelfTest.release("v3.1.10", "CodexMeter-3.1.10-ko.apk");
        checkDiscovery(GitHubReleaseParser.parse(new JSONArray().put(previous).put(current).toString()));
        checkDiscovery(GitHubReleaseParser.parse(new JSONArray().put(current).put(previous).toString()));
        check(KoreanUpdateTrust.compatible("dev.bennett.codexmeter", KoreanUpdateTrust.CERTIFICATE, 96, 97),
                "Existing 3.1.9 package and signer must advance");
        check(!KoreanUpdateTrust.compatible("dev.bennett.codexmeter", KoreanUpdateTrust.CERTIFICATE, 97, 96),
                "Older code was accepted");
        if (args.length == 1) {
            checkDiscovery(GitHubReleaseParser.parse(Files.readString(Path.of(args[0]))));
        }
        System.out.println("3.1.9 to 3.1.10 release discovery: " + checks + " checks passed");
    }
}
