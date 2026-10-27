package dev.bennett.codexmeter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import org.json.*;

/** Explicit SAF operations with account-bound pending files and retained in-flight work. */
public final class BetaBackupActivity extends AppCompatActivity {
    enum Phase { PREPARING, EXPORT_READY, SAVE_LOCATION, WRITING, OPEN_LOCATION, READING, PREVIEW, RESTORING, DONE }
    static final class Operation {
        final String account;
        final boolean growth,settings;
        volatile Phase phase;
        String pendingName,importUri,result;
        JSONObject candidate,before,after;
        BetaBackupActivity owner;
        boolean restoreResult;
        Operation(String account,boolean growth,boolean settings,Phase phase){this.account=account;this.growth=growth;this.settings=settings;this.phase=phase;}
    }
    String account,pendingName,importUri;
    LinearLayout body;
    AlertDialog dialog;
    CheckBox growth,settings;
    Button exportButton,importButton;
    TextView status;
    ProgressBar progress;
    Operation operation;
    String idleStatus;
    final ActivityResultLauncher<String> create=registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"),this::write);
    final ActivityResultLauncher<String[]> open=registerForActivityResult(new ActivityResultContracts.OpenDocument(),this::read);

    String t(String ko,String en,Object... args){return BetaExperienceUi.t(this,ko,en,args);}
    @Override protected void onCreate(Bundle saved){
        Ui.applySelectedTheme(this);super.onCreate(saved);boolean dark=Ui.isDark(this);
        Ui.Page layout=Ui.installPage(this,"",true);layout.toolbar.setTitle("");body=layout.content;
        body.addView(BetaUi.title(this,BetaCopy.t(this,"backup"),dark));
        if(TrialDemo.active(this))body.addView(BetaUi.note(this,BetaCopy.t(this,"demo"),dark));
        if(saved!=null){account=saved.getString("account");pendingName=saved.getString("pending");importUri=saved.getString("import_uri");idleStatus=saved.getString("backup_status");}
        body.addView(BetaUi.body(this,t("보관하거나 복원할 항목을 선택하세요","Choose sections to save or restore"),dark));
        growth=new CheckBox(this);growth.setText(t("성장·날짜별 메달·장착·이전 보관함","Growth, dated medals, equipment and archive"));growth.setTextColor(Ui.mainText(dark));growth.setMinHeight(Ui.dp(this,48));growth.setChecked(saved==null||saved.getBoolean("growth",true));body.addView(growth);
        settings=new CheckBox(this);settings.setText(t("이름·표시·알림·연결 설정 (로그인 제외)","Name, display, notifications and connection settings (no credentials)"));settings.setTextColor(Ui.mainText(dark));settings.setMinHeight(Ui.dp(this,48));settings.setChecked(saved==null||saved.getBoolean("settings",true));body.addView(settings);
        body.addView(BetaUi.note(this,t("성장은 병합합니다. 선택한 설정은 파일의 값으로 바뀝니다. 로그인 토큰·대화 내용·사용량 전체 이력은 포함하지 않습니다.","Growth is merged. Selected settings replace current values. No tokens, conversations or full usage history are included."),dark));
        exportButton=BetaUi.action(this,BetaCopy.t(this,"export"),true,dark,this::export);body.addView(exportButton);
        importButton=BetaUi.action(this,BetaCopy.t(this,"import"),false,dark,this::chooseImport);body.addView(importButton);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setIndeterminate(true);body.addView(progress,new LinearLayout.LayoutParams(-1,Ui.dp(this,4)));
        status=BetaUi.note(this,"",dark);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(status);
        growth.setOnCheckedChangeListener((button,checked)->{idleStatus=null;updateControls();});
        settings.setOnCheckedChangeListener((button,checked)->{idleStatus=null;updateControls();});
        findViewById(R.id.dashboard_refresh).setEnabled(false);
        Object retained=getLastCustomNonConfigurationInstance();boolean resumeRead=false;
        if(retained instanceof Operation)operation=(Operation)retained;
        else if(saved!=null){
            Phase phase=null;try{phase=Phase.valueOf(saved.getString("backup_phase",""));}catch(IllegalArgumentException ignored){}
            // Document pickers retain their own pending callback. Only reads may be retried after process death.
            if(phase==Phase.SAVE_LOCATION||phase==null&&pendingName!=null||phase==Phase.OPEN_LOCATION||importUri!=null&&(phase==Phase.READING||phase==Phase.PREVIEW)){
                operation=new Operation(account,saved.getBoolean("operation_growth",growth.isChecked()),saved.getBoolean("operation_settings",settings.isChecked()),phase==null?Phase.SAVE_LOCATION:phase);
                operation.pendingName=pendingName;operation.importUri=importUri;
                resumeRead=phase==Phase.READING||phase==Phase.PREVIEW;
            }else if(phase!=null){idleStatus=t("이전 작업이 중단되었습니다. 저장된 파일이나 현재 설정을 확인한 뒤 다시 시도하세요.","The previous operation was interrupted. Check the saved file or current settings before trying again.");pendingName=null;importUri=null;}
        }
        if(operation!=null){
            operation.owner=this;syncOperation();
            if(resumeRead){Operation resume=operation;body.post(()->readFile(resume));}
            else body.post(this::operationChanged);
        }
        updateControls();
    }
    @Override public Object onRetainCustomNonConfigurationInstance(){return operation;}
    @Override protected void onSaveInstanceState(Bundle saved){
        syncOperation();saved.putString("account",account);saved.putString("pending",pendingName);saved.putString("import_uri",importUri);saved.putBoolean("growth",growth.isChecked());saved.putBoolean("settings",settings.isChecked());saved.putString("backup_status",idleStatus);
        if(operation!=null){saved.putString("backup_phase",operation.phase.name());saved.putBoolean("operation_growth",operation.growth);saved.putBoolean("operation_settings",operation.settings);}
        super.onSaveInstanceState(saved);
    }
    @Override protected void onDestroy(){if(operation!=null&&operation.owner==this)operation.owner=null;if(dialog!=null)dialog.dismiss();super.onDestroy();}
    void message(String key){Toast.makeText(this,BetaCopy.t(this,key),Toast.LENGTH_LONG).show();}
    void syncOperation(){if(operation!=null){account=operation.account;pendingName=operation.pendingName;importUri=operation.importUri;}}
    void updateControls(){
        if(status==null)return;boolean busy=operation!=null,selected=growth.isChecked()||settings.isChecked();
        growth.setEnabled(!busy);settings.setEnabled(!busy);exportButton.setEnabled(!busy&&selected);importButton.setEnabled(!busy&&selected);
        progress.setVisibility(busy&&operation.phase!=Phase.SAVE_LOCATION&&operation.phase!=Phase.OPEN_LOCATION&&operation.phase!=Phase.PREVIEW?View.VISIBLE:View.GONE);
        String text=idleStatus;
        if(busy)switch(operation.phase){
            case PREPARING:case EXPORT_READY:text=t("백업 파일을 준비하고 있습니다…","Preparing the backup file…");break;
            case SAVE_LOCATION:text=t("파일을 저장할 위치를 선택하세요","Choose where to save the file");break;
            case WRITING:text=t("백업 파일을 저장하고 있습니다…","Saving the backup file…");break;
            case OPEN_LOCATION:text=t("복원할 백업 파일을 선택하세요","Choose a backup file to restore");break;
            case READING:text=t("파일과 선택한 항목을 확인하고 있습니다…","Checking the file and selected sections…");break;
            case PREVIEW:text=t("복원할 내용을 확인한 뒤 적용하세요","Review the selected sections before restoring");break;
            case RESTORING:text=t("선택한 항목을 복원하고 있습니다…","Restoring the selected sections…");break;
            default:break;
        }
        if(text==null||text.isEmpty())text=selected?t("선택한 항목만 보관하거나 복원합니다","Only the selected sections will be saved or restored"):t("항목을 하나 이상 선택하세요","Select at least one section");
        status.setText(text);
    }
    static java.io.File cached(Context c,String name)throws Exception {
        if(name==null||!name.matches("[a-f0-9-]{36}\\.json"))throw new IllegalArgumentException("Backup cache name");
        java.io.File folder=new java.io.File(c.getCacheDir(),"beta-backup");if(!folder.exists()&&!folder.mkdirs())throw new java.io.IOException();return new java.io.File(folder,name);
    }
    java.io.File cached(String name)throws Exception{return cached(this,name);}
    Operation begin(Phase phase){
        if(operation!=null||!growth.isChecked()&&!settings.isChecked())return null;
        operation=new Operation(SubscriptionStore.accountKey(this),growth.isChecked(),settings.isChecked(),phase);operation.owner=this;idleStatus=null;syncOperation();updateControls();return operation;
    }
    static void deliver(Operation work){new Handler(Looper.getMainLooper()).post(()->{if(work.owner!=null&&work.owner.operation==work)work.owner.operationChanged();});}
    static void completed(Operation work,String result,boolean restore){work.result=result;work.restoreResult=restore;work.phase=Phase.DONE;deliver(work);}
    void finishOperation(String text){
        if(operation!=null)operation.owner=null;operation=null;pendingName=null;importUri=null;idleStatus=text;updateControls();
    }
    void operationChanged(){
        Operation work=operation;if(work==null||isDestroyed()||isFinishing())return;syncOperation();
        if(!BetaUi.current(this,work.account)){finishOperation(t("계정이 바뀌어 작업을 멈췄습니다. 현재 계정에서 다시 시도하세요.","The account changed. Try again with the current account."));return;}
        updateControls();
        if(work.phase==Phase.EXPORT_READY){
            work.phase=Phase.SAVE_LOCATION;updateControls();try{create.launch("GPT-HUD-backup.json");}catch(RuntimeException error){finishOperation(BetaCopy.t(this,"failed"));message("failed");}
        }else if(work.phase==Phase.PREVIEW)showPreview(work);
        else if(work.phase==Phase.DONE){
            String result=work.result;boolean restore=work.restoreResult;finishOperation(result);
            if(restore){dialog=new AlertDialog.Builder(this).setTitle(BetaCopy.t(this,"restore")).setMessage(result).setPositiveButton(BetaCopy.t(this,"close"),null).create();dialog.show();}
            else Toast.makeText(this,result,Toast.LENGTH_LONG).show();
        }
    }
    void export(){
        Operation work=begin(Phase.PREPARING);if(work==null)return;Context c=getApplicationContext();
        BetaGrowthStore.WORKER.execute(()->{
            try{JSONObject bundle=BetaBundle.collect(c,work.account,work.growth,work.settings);String candidate=java.util.UUID.randomUUID()+".json";
                try(java.io.FileOutputStream out=new java.io.FileOutputStream(cached(c,candidate))){out.write(bundle.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));out.getFD().sync();}
                work.pendingName=candidate;work.phase=Phase.EXPORT_READY;deliver(work);
            }catch(Exception error){completed(work,BetaCopy.t(c,"failed"),false);}
        });
    }
    void write(Uri uri){
        Operation work=operation;
        if(work==null&&pendingName!=null){work=new Operation(account,growth.isChecked(),settings.isChecked(),Phase.SAVE_LOCATION);work.pendingName=pendingName;work.owner=this;operation=work;}
        if(work==null||work.phase!=Phase.SAVE_LOCATION)return;
        if(uri==null){finishOperation(t("저장을 취소했습니다","Save canceled"));return;}
        if(!BetaUi.current(this,work.account)){operationChanged();return;}
        Operation writing=work;String fileName=work.pendingName;work.phase=Phase.WRITING;updateControls();Context c=getApplicationContext();
        BetaGrowthStore.WORKER.execute(()->{
            boolean ok=false;try{if(!BetaGrowthStore.current(c,writing.account))throw new java.io.IOException();
                try(java.io.InputStream in=new java.io.FileInputStream(cached(c,fileName));java.io.OutputStream out=c.getContentResolver().openOutputStream(uri,"w")){
                    if(out==null)throw new java.io.IOException();byte[] buffer=new byte[8192];for(int n;(n=in.read(buffer))>=0;)out.write(buffer,0,n);out.flush();}ok=true;
            }catch(Exception ignored){}completed(writing,BetaCopy.t(c,ok?"saved":"failed"),false);
        });
    }
    void chooseImport(){
        Operation work=begin(Phase.OPEN_LOCATION);if(work==null)return;
        try{open.launch(new String[]{"application/json","text/plain"});}catch(RuntimeException error){finishOperation(BetaCopy.t(this,"failed"));message("failed");}
    }
    void read(Uri uri){
        Operation work=operation;if(work==null)return;if(work.phase!=Phase.OPEN_LOCATION)return;
        if(uri==null){finishOperation(t("복원을 취소했습니다","Restore canceled"));return;}
        work.importUri=uri.toString();syncOperation();readFile(work);
    }
    void readFile(Operation work){
        if(operation!=work||!BetaUi.current(this,work.account)){operationChanged();return;}
        work.phase=Phase.READING;updateControls();Context c=getApplicationContext();
        BetaGrowthStore.WORKER.execute(()->{
            try(java.io.InputStream in=c.getContentResolver().openInputStream(Uri.parse(work.importUri));java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
                if(in==null)throw new java.io.IOException();byte[] buffer=new byte[8192];for(int n;(n=in.read(buffer))>=0;){if(out.size()+n>8*1024*1024)throw new java.io.IOException();out.write(buffer,0,n);}
                JSONObject candidate=new JSONObject(out.toString("UTF-8")),after=BetaBundle.preview(c,work.account,candidate,work.growth,work.settings);
                work.candidate=candidate;work.after=after;work.before=work.growth?BetaGrowthStore.load(c,work.account):null;work.phase=Phase.PREVIEW;deliver(work);
            }catch(Exception error){completed(work,BetaCopy.t(c,"backup_invalid"),false);}
        });
    }
    String previewNote(Operation work){
        boolean bundle=BetaBundle.bundle(work.candidate),includeGrowth=!bundle||work.growth,includeSettings=bundle&&work.settings;
        StringBuilder note=new StringBuilder(t("복원할 항목: %s","Sections to restore: %s",includeGrowth&&includeSettings?t("성장과 설정","Growth and settings"):includeGrowth?t("성장","Growth"):t("설정","Settings")));
        if(includeGrowth){JSONObject before=work.before,after=work.after;note.append("\n\n").append(BetaCopy.t(this,"restore_note",LedgerUi.number(before==null?0:before.optLong("total")),LedgerUi.number(after.optLong("total")),count(before,"honors"),count(after,"honors"),count(before,"titles"),count(after,"titles")));
            JSONObject medals=work.candidate.optJSONObject("medals");if(medals!=null)note.append("\n").append(t("날짜별 메달 기록 %d일 · 기존 기록과 병합","Dated medal records: %d days · merge with existing",count(medals,"days")));
        }else note.append("\n\n").append(t("성장·메달·장착·이전 보관함은 그대로 유지합니다.","Growth, medals, equipment and archive are retained."));
        if(includeSettings)note.append("\n\n").append(t("이름·표시·알림·연결 설정을 파일의 값으로 바꿉니다.","Name, display, notifications and connection settings will use the file's values."));
        return note.append("\n").append(t("로그인 정보는 포함하거나 변경하지 않습니다.","Credentials are excluded and remain unchanged.")).toString();
    }
    static int count(JSONObject object,String key){JSONObject entries=object==null?null:object.optJSONObject(key);return entries==null?0:entries.length();}
    void showPreview(Operation work){
        if(dialog!=null&&dialog.isShowing())return;
        dialog=new AlertDialog.Builder(this).setTitle(BetaCopy.t(this,"restore_preview")).setMessage(previewNote(work))
                .setNegativeButton(BetaCopy.t(this,"cancel"),(d,w)->finishOperation(t("복원을 취소했습니다","Restore canceled")))
                .setOnCancelListener(d->finishOperation(t("복원을 취소했습니다","Restore canceled")))
                .setPositiveButton(BetaCopy.t(this,"restore"),(d,w)->restore(work)).create();dialog.show();
    }
    void restore(Operation work){
        if(operation!=work||work.phase!=Phase.PREVIEW)return;
        if(!BetaUi.current(this,work.account)){operationChanged();return;}
        work.phase=Phase.RESTORING;work.importUri=null;syncOperation();updateControls();Context c=getApplicationContext();
        BetaGrowthStore.WORKER.execute(()->{String outcome;try{outcome=BetaBundle.restore(c,work.account,work.candidate,work.growth,work.settings);}catch(Exception error){outcome=error.getMessage()==null?BetaCopy.t(c,"failed"):error.getMessage();}completed(work,outcome,true);});
    }
}
