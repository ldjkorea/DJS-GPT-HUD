package dev.bennett.codexmeter;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import java.util.concurrent.*;
import org.json.*;

/** Task-owned synthetic emulator only. No authentication changes or real document provider writes. */
public final class StableRedesignBackupNativeTest extends Instrumentation {
    int checks;
    Throwable failure;
    BetaBackupActivity active;
    CountDownLatch blocked;
    void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    void main(Runnable action){runOnMainSync(()->{try{action.run();}catch(Throwable t){failure=t;}});if(failure!=null){Throwable t=failure;failure=null;throw new AssertionError(t);}}
    void workers()throws Exception{BetaGrowthStore.WORKER.submit(()->{}).get(20,TimeUnit.SECONDS);waitForIdleSync();}
    void blockWorker()throws Exception{
        CountDownLatch entered=new CountDownLatch(1);blocked=new CountDownLatch(1);CountDownLatch release=blocked;
        BetaGrowthStore.WORKER.execute(()->{entered.countDown();try{release.await(20,TimeUnit.SECONDS);}catch(InterruptedException error){Thread.currentThread().interrupt();}});
        check(entered.await(5,TimeUnit.SECONDS),"Worker gate entered");
    }
    void releaseWorker(){if(blocked!=null){blocked.countDown();blocked=null;}}
    BetaBackupActivity recreate(BetaBackupActivity old)throws Exception{
        ActivityMonitor monitor=addMonitor(BetaBackupActivity.class.getName(),null,false);
        try{main(old::recreate);Activity next=monitor.waitForActivityWithTimeout(5000);check(next instanceof BetaBackupActivity,"Actual backup Activity recreated");active=(BetaBackupActivity)next;waitForIdleSync();return active;}
        finally{removeMonitor(monitor);}
    }
    java.io.File cached(BetaBackupActivity activity,String bytes)throws Exception{
        java.io.File file=activity.cached(java.util.UUID.randomUUID()+".json");java.nio.file.Files.write(file.toPath(),bytes.getBytes(java.nio.charset.StandardCharsets.UTF_8));return file;
    }
    void idle(BetaBackupActivity activity){check(activity.operation==null,"No operation remains");check(activity.exportButton.isEnabled()&&activity.importButton.isEnabled(),"Both actions available again");check(activity.growth.isEnabled()&&activity.settings.isEnabled(),"Section controls available again");}
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle result=new Bundle();ActivityMonitor picker=null;try{
        Context c=getTargetContext();check(Build.MODEL.contains("sdk")&&c.getPackageName().equals("dev.bennett.codexmeter"),"Owned emulator stable package");
        check(SecureTokenStore.load(c)!=null&&"stable315-qa".equals(SecureTokenStore.load(c).accountId),"Synthetic fixture required");
        String account=SubscriptionStore.accountKey(c),growthBefore=BetaGrowthStore.load(c,account).toString(),medalsBefore=BetaMedalStore.export(c,account).toString(),credentialsBefore=SecureTokenStore.load(c).toJson().toString();
        String bundle=BetaBundle.collect(c,account,false,true).toString();
        active=(BetaBackupActivity)startActivitySync(new Intent(c,BetaBackupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();BetaBackupActivity first=active;
        main(()->{first.growth.setChecked(false);first.settings.setChecked(true);idle(first);});
        active=recreate(active);BetaBackupActivity selected=active;
        main(()->{check(!selected.growth.isChecked()&&selected.settings.isChecked(),"Chosen sections survive idle recreation");idle(selected);});

        // Actual export preparation stays single-flight while the Activity is recreated.
        IntentFilter pickerIntent=new IntentFilter(Intent.ACTION_CREATE_DOCUMENT);pickerIntent.addCategory(Intent.CATEGORY_OPENABLE);pickerIntent.addDataType("application/json");
        picker=addMonitor(pickerIntent,new ActivityResult(Activity.RESULT_CANCELED,null),true);
        blockWorker();main(selected::export);BetaBackupActivity.Operation exporting=selected.operation;
        main(()->{check(exporting!=null&&exporting.phase==BetaBackupActivity.Phase.PREPARING,"Export preparation visible");check(!selected.exportButton.isEnabled()&&!selected.importButton.isEnabled(),"Export/import cannot overlap");check(!selected.growth.isEnabled()&&!selected.settings.isEnabled(),"Export scope is locked");selected.export();selected.chooseImport();check(selected.operation==exporting,"Repeated actions keep the original operation");});
        active=recreate(active);BetaBackupActivity exportingActivity=active;
        main(()->{check(exportingActivity.operation==exporting,"In-flight export retained");check(!exporting.growth&&exporting.settings&&account.equals(exporting.account),"Export scope and account retained");});
        releaseWorker();workers();for(int i=0;i<40&&exportingActivity.operation!=null;i++){Thread.sleep(50);waitForIdleSync();}
        check(picker.getHits()==1,"Exactly one SAF save request");main(()->idle(exportingActivity));removeMonitor(picker);picker=null;

        // The cached bytes and account used by an already pending SAF callback survive recreation.
        java.io.File pending=cached(active,bundle);BetaBackupActivity beforeWrite=active;
        main(()->{BetaBackupActivity.Operation work=beforeWrite.begin(BetaBackupActivity.Phase.SAVE_LOCATION);work.pendingName=pending.getName();beforeWrite.syncOperation();});
        active=recreate(active);BetaBackupActivity writing=active;
        main(()->{check(account.equals(writing.account)&&pending.getName().equals(writing.pendingName),"Pending file/account retained");check(!writing.operation.growth&&writing.operation.settings,"Pending SAF scope retained");});
        java.io.File destination=new java.io.File(c.getExternalFilesDir(null),"backup-ux-"+java.util.UUID.randomUUID()+".json");
        main(()->writing.write(Uri.fromFile(destination)));workers();
        check(bundle.equals(new String(java.nio.file.Files.readAllBytes(destination.toPath()),java.nio.charset.StandardCharsets.UTF_8)),"SAF callback writes original cached bytes after recreation");main(()->idle(writing));

        // Actual import reading and its selected preview survive rotation without applying data.
        java.io.File source=cached(active,bundle);blockWorker();BetaBackupActivity beforeRead=active;
        main(()->{beforeRead.begin(BetaBackupActivity.Phase.OPEN_LOCATION);beforeRead.read(Uri.fromFile(source));check(beforeRead.operation.phase==BetaBackupActivity.Phase.READING,"Import reading phase visible");});
        BetaBackupActivity.Operation reading=beforeRead.operation;active=recreate(active);BetaBackupActivity preview=active;
        main(()->{check(preview.operation==reading,"In-flight read retained");check(!preview.exportButton.isEnabled()&&!preview.importButton.isEnabled(),"Reading blocks overlapping actions");});
        releaseWorker();workers();
        main(()->{check(preview.operation.phase==BetaBackupActivity.Phase.PREVIEW&&preview.dialog!=null&&preview.dialog.isShowing(),"Validated preview shown");String note=preview.previewNote(reading);check(note.contains(preview.t("복원할 항목: 설정","Sections to restore: Settings")),"Settings-only selected scope shown");check(!note.contains(" → ")&&!note.contains("Experience ")&&!note.contains("경험치 "),"Settings-only preview has no XP delta");check(note.contains(preview.t("성장·메달·장착·이전 보관함은 그대로 유지합니다.","Growth, medals, equipment and archive are retained.")),"Unselected growth is clearly retained");});
        active=recreate(active);BetaBackupActivity rotatedPreview=active;
        main(()->{check(rotatedPreview.operation==reading&&rotatedPreview.dialog.isShowing(),"Preview recreated with frozen file and scope");rotatedPreview.dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).performClick();});
        waitForIdleSync();main(()->{idle(rotatedPreview);check(!rotatedPreview.growth.isChecked()&&rotatedPreview.settings.isChecked(),"Cancel keeps chosen sections");});

        // Apply the same synthetic settings bundle, retaining the accepted restore across rotation.
        main(()->{rotatedPreview.begin(BetaBackupActivity.Phase.OPEN_LOCATION);rotatedPreview.read(Uri.fromFile(source));});workers();blockWorker();
        main(()->rotatedPreview.dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick());waitForIdleSync();
        BetaBackupActivity.Operation restoring=rotatedPreview.operation;
        main(()->{check(restoring!=null&&restoring.phase==BetaBackupActivity.Phase.RESTORING,"Accepted restore visibly busy");check(!rotatedPreview.exportButton.isEnabled()&&!rotatedPreview.importButton.isEnabled(),"Restore blocks other operations");});
        active=recreate(active);BetaBackupActivity restored=active;
        main(()->check(restored.operation==restoring,"Accepted restore retained through recreation"));releaseWorker();workers();
        main(()->{idle(restored);check(restored.dialog!=null&&restored.dialog.isShowing(),"Restore completion result shown");restored.dialog.dismiss();});
        check(BetaIdentity.name(c).equals(new JSONObject(bundle).getJSONObject("personal").getString("name")),"Selected settings restored from the file");

        main(()->{restored.begin(BetaBackupActivity.Phase.OPEN_LOCATION);restored.read(null);idle(restored);restored.begin(BetaBackupActivity.Phase.SAVE_LOCATION);restored.write(null);idle(restored);});
        main(()->{BetaBackupActivity.Operation foreign=new BetaBackupActivity.Operation("f".repeat(64),false,true,BetaBackupActivity.Phase.READING);foreign.owner=restored;restored.operation=foreign;restored.operationChanged();idle(restored);check(restored.status.getText().toString().equals(restored.t("계정이 바뀌어 작업을 멈췄습니다. 현재 계정에서 다시 시도하세요.","The account changed. Try again with the current account.")),"Foreign-account completion stops safely");});
        main(()->{restored.settings.setChecked(false);check(!restored.exportButton.isEnabled()&&!restored.importButton.isEnabled(),"No selection disables actions");restored.export();restored.chooseImport();check(restored.operation==null,"No selection cannot start work");});
        check(growthBefore.equals(BetaGrowthStore.load(c,account).toString()),"Growth document unchanged");check(medalsBefore.equals(BetaMedalStore.export(c,account).toString()),"Medal document unchanged");check(credentialsBefore.equals(SecureTokenStore.load(c).toJson().toString()),"Credentials unchanged");
        result.putString("stream","Stable redesign backup native: "+checks+" checks passed; busy / SAF cancellation / cached bytes / recreation / settings-only preview / account guard; synthetic only.\n");finish(-1,result);
    }catch(Throwable error){result.putString("stream",android.util.Log.getStackTraceString(error));finish(0,result);}finally{releaseWorker();if(picker!=null)removeMonitor(picker);if(active!=null){BetaBackupActivity last=active;main(last::finish);}}}
}
