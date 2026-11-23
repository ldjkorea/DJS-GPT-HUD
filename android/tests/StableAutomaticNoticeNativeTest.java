package dev.bennett.codexmeter;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

/** The old, publicly installed app must independently fetch and visibly offer the new release. */
public final class StableAutomaticNoticeNativeTest extends Instrumentation {
    private String fromVersion;
    private String toVersion;
    private int fromCode;
    private int checks;

    @Override public void onCreate(Bundle args) {
        super.onCreate(args);
        fromVersion = args.getString("from_name");
        toVersion = args.getString("to_name");
        fromCode = Integer.parseInt(args.getString("from_code"));
        start();
    }

    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private boolean visibleTitle(AccessibilityNodeInfo node, String title) {
        if (node == null) return false;
        if ("dev.bennett.codexmeter".contentEquals(String.valueOf(node.getPackageName()))
                && title.contentEquals(String.valueOf(node.getText())) && node.isVisibleToUser()) return true;
        for (int i = 0; i < node.getChildCount(); i++) {
            if (visibleTitle(node.getChild(i), title)) return true;
        }
        return false;
    }

    @Override public void onStart() {
        Context c = getTargetContext();
        String originalChannel = UpdatePreferences.channel(c);
        boolean originalAutomatic = UpdatePreferences.automaticChecks(c);
        Activity home = null;
        boolean changed = false;
        Bundle out = new Bundle();
        int result = 0;
        try {
            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            check("dev.bennett.codexmeter".equals(c.getPackageName())
                    && fromVersion.equals(info.versionName) && fromCode == info.getLongVersionCode(), "Expected existing official install");
            AuthTokens auth = SecureTokenStore.load(c);
            check(auth != null && "stable316-qa".equals(auth.accountId) && !originalAutomatic, "Synthetic fixture required");
            check(!UpdatePreferences.foregroundVersionShown(c, toVersion), "A previous notice would invalidate this case");
            GitHubRelease cached = UpdatePreferences.availableUpdate(c);
            check(cached == null || !toVersion.equals(cached.version), "Release must be discovered by opening home");
            changed = true;
            UpdatePreferences.setChannel(c, UpdateChannel.STABLE);
            UpdatePreferences.setAutomaticChecks(c, true);
            home = startActivitySync(new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            String title = home.getString(R.string.foreground_update_title, toVersion);
            long deadline = SystemClock.elapsedRealtime() + 90_000;
            boolean displayed;
            do {
                displayed = visibleTitle(getUiAutomation().getRootInActiveWindow(), title);
                if (!displayed) SystemClock.sleep(350);
            } while (!displayed && SystemClock.elapsedRealtime() < deadline);
            check(displayed, "Automatic foreground update dialog did not appear");
            check(UpdatePreferences.foregroundVersionShown(c, toVersion), "Displayed notice was not recorded");
            GitHubRelease available = UpdatePreferences.availableUpdate(c);
            check(available != null && toVersion.equals(available.version) && !available.prerelease, "Expected live stable release");
            out.putString("stream", "Automatic foreground update: " + checks + " checks passed; " + title + "; synthetic only.\n");
            result = -1;
        } catch (Throwable error) {
            out.putString("stream", android.util.Log.getStackTraceString(error));
        } finally {
            if (home != null) {
                Activity opened = home;
                runOnMainSync(opened::finish);
            }
            if (changed) {
                UpdatePreferences.setAutomaticChecks(c, originalAutomatic);
                UpdatePreferences.setChannel(c, originalChannel);
            }
        }
        finish(result, out);
    }
}
