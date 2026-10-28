package dev.bennett.codexmeter;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.*;
import java.util.*;
import org.json.*;

/** Explicitly authorized owned-AVD public download and Android confirmation exercise.
 * Replacement kills the instrumented old app. A host must separately verify the installed
 * APK hash/version and run Stable319ContinuityNativeTest; a click is not installation proof.
 */
public final class Stable319PublicUpdateNativeTest extends Instrumentation {
    String phase;
    int checks;
    void check(boolean value, String why) { checks++; if (!value) throw new AssertionError(why); }
    @Override public void onCreate(Bundle args) { super.onCreate(args); phase=args.getString("phase","before"); start(); }
    File receipt(Context c) { return new File(c.getExternalFilesDir(null),"stable319/public-update.json"); }
    void save(Context c, JSONObject value) throws Exception {
        File file=receipt(c); file.getParentFile().mkdirs();
        try (FileOutputStream stream=new FileOutputStream(file)) { stream.write(value.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8)); stream.getFD().sync(); }
    }
    AccessibilityNodeInfo confirmation(AccessibilityNodeInfo node) {
        if (node==null) return null;
        String pkg=String.valueOf(node.getPackageName()), text=String.valueOf(node.getText());
        if ((pkg.equals("com.google.android.packageinstaller") || pkg.equals("com.android.packageinstaller"))
                && "android:id/button1".equals(node.getViewIdResourceName()) && node.isEnabled()
                && (text.equalsIgnoreCase("Update") || text.equals("업데이트"))) return node;
        for (int i=0;i<node.getChildCount();i++) { AccessibilityNodeInfo found=confirmation(node.getChild(i)); if (found!=null) return found; }
        return null;
    }
    android.widget.Button downloadButton(android.view.View view,String label) {
        if(view instanceof android.widget.Button && view.isShown() && view.isEnabled()
                && label.contentEquals(((android.widget.Button)view).getText())) return (android.widget.Button)view;
        if(view instanceof android.view.ViewGroup) for(int i=0;i<((android.view.ViewGroup)view).getChildCount();i++) {
            android.widget.Button found=downloadButton(((android.view.ViewGroup)view).getChildAt(i),label);if(found!=null)return found;
        }
        return null;
    }
    @Override public void onStart() {
        Context c=getTargetContext(); Bundle out=new Bundle();
        try {
            check(Build.MODEL.contains("sdk") && c.getPackageName().equals("dev.bennett.codexmeter"),"Owned stable emulator only");
            check("stable316-qa".equals(SecureTokenStore.load(c).accountId),"Synthetic continuity account required");
            check(c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName.equals("3.1.8"),"Existing published 3.1.8 required");
            if (phase.equals("before")) {
                List<GitHubRelease> releases=ReleaseUpdateClient.check(c);
                GitHubRelease release=GitHubReleaseParser.findVersion(releases,"3.1.9");
                check(release!=null && !release.prerelease,"Live stable 3.1.9 discovered by production client");
                check(release.apkUrl.startsWith("https://github.com/ldjkorea/DJS-GPT-HUD/releases/download/v3.1.9/"),"Production release asset");
                UpdateInstaller.PreparedUpdate prepared=UpdateInstaller.prepare(c,release,(done,total)->{});
                check(prepared.versionCode==96 && prepared.versionName.equals("3.1.9"),"Downloaded package version and code validated");
                JSONObject receipt=new JSONObject().put("from","3.1.8").put("to","3.1.9").put("apk",release.apkName)
                        .put("sha256",ReleaseIntegrity.sha256(prepared.apk)).put("bytes",prepared.apk.length())
                        .put("url",release.apkUrl).put("prepared",true).put("synthetic_only",true);
                save(c,receipt);
                out.putString("stream","Stable319 public download: "+checks+" checks passed; production checksum, package, signing and version checks ran.\n");
                finish(-1,out);
            } else {
                check(phase.equals("verify"),"Only before/verify phases");
                check(receipt(c).isFile(),"Public preparation receipt required");
                check(c.getPackageManager().canRequestPackageInstalls(),"Owned fixture install-source permission must be explicitly granted");
                UiAutomation automation=getUiAutomation();
                android.accessibilityservice.AccessibilityServiceInfo service=automation.getServiceInfo();
                service.flags|=android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
                automation.setServiceInfo(service);
                Activity update=startActivitySync(new Intent(c,UpdateActivity.class).putExtra(UpdateActivity.EXTRA_VERSION,"3.1.9")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                waitForIdleSync();Thread.sleep(1000);
                final Throwable[] actionFailure={null};
                runOnMainSync(()->{try{
                    int id=update.getResources().getIdentifier("ui_download_and_install_0388ee","string",c.getPackageName());
                    android.widget.Button action=downloadButton(update.getWindow().getDecorView(),update.getString(id));
                    check(action!=null && action.performClick(),"Actual Download and install button clicked");
                }catch(Throwable error){actionFailure[0]=error;}});
                if(actionFailure[0]!=null)throw new AssertionError(actionFailure[0]);
                AccessibilityNodeInfo button=null;
                for (int i=0;i<180 && button==null;i++) { Thread.sleep(1000); button=confirmation(automation.getRootInActiveWindow()); }
                check(button!=null,"Actual Android Update confirmation visible");
                android.graphics.Bitmap image=automation.takeScreenshot();
                if(image!=null)try(FileOutputStream stream=new FileOutputStream(new File(receipt(c).getParentFile(),"android-update-confirmation.png"))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream);}finally{image.recycle();}
                JSONObject receipt=new JSONObject(new String(java.nio.file.Files.readAllBytes(receipt(c).toPath()),java.nio.charset.StandardCharsets.UTF_8));
                receipt.put("system_button_package",String.valueOf(button.getPackageName())).put("system_button_text",String.valueOf(button.getText()))
                        .put("confirmation_observed_at",System.currentTimeMillis()).put("click_authorized",true);
                save(c,receipt);
                check(button.performAction(AccessibilityNodeInfo.ACTION_CLICK),"Android Update confirmation accepted the authorized click");
                // Replacement normally terminates this process. A surviving process is not success.
                Thread.sleep(20000);
                throw new AssertionError("Host must inspect actual replacement; instrumented old app unexpectedly survived");
            }
        } catch(Throwable failure) { out.putString("stream",android.util.Log.getStackTraceString(failure)); finish(0,out); }
    }
}
