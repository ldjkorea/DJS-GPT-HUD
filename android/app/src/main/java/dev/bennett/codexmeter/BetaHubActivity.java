package dev.bennett.codexmeter;

import android.content.*;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;
import java.time.*;

/** Local journey, recorded recap and actionable recovery. Data loads only on the worker. */
public final class BetaHubActivity extends AppCompatActivity {
    LinearLayout body;boolean dark,month,healthBusy,healthFailed;String page,account,healthResult="";long generation;LedgerPresentation.Period usagePeriod;
    String t(String ko,String en,Object... args){return BetaExperienceUi.t(this,ko,en,args);}
    @Override protected void onCreate(Bundle saved){Ui.applySelectedTheme(this);super.onCreate(saved);dark=Ui.isDark(this);page=getIntent().getStringExtra("page");if(page==null)page="health";month=saved!=null&&saved.getBoolean("month");Ui.Page layout=Ui.installPage(this,"",true);layout.toolbar.setTitle("");body=layout.content;findViewById(R.id.dashboard_refresh).setEnabled(false);}
    @Override protected void onResume(){super.onResume();load();}
    @Override protected void onSaveInstanceState(Bundle saved){saved.putBoolean("month",month);super.onSaveInstanceState(saved);}
    @Override protected void onDestroy(){generation++;super.onDestroy();}
    void heading(){TextView title=BetaUi.title(this,page.equals("journey")?t("성장 경로","Growth journey"):page.equals("recap")?t("기록 회고","Recorded recap"):t("연결·업데이트 복구","Connection and update recovery"),dark);androidx.core.view.ViewCompat.setAccessibilityHeading(title,true);body.addView(title);if(TrialDemo.active(this))body.addView(BetaUi.note(this,BetaCopy.t(this,"demo"),dark));}
    void load(){
        String currentAccount=SubscriptionStore.accountKey(this);if(!java.util.Objects.equals(account,currentAccount)){healthResult="";healthBusy=false;generation++;}account=currentAccount;body.removeAllViews();heading();
        if(page.equals("health")){health();return;}
        if(account==null){body.addView(BetaUi.body(this,BetaCopy.t(this,"no_account"),dark));return;}
        body.addView(BetaUi.note(this,t("기록 읽는 중…","Reading records…"),dark));String key=account;long ticket=++generation;
        boolean selectedMonth=month;
        BetaGrowthStore.WORKER.execute(()->{JSONObject state=null;LedgerPresentation.Period period=null;try{state=BetaGrowthStore.load(this,key);if(page.equals("recap"))synchronized(UsageApi.NETWORK_LOCK){if(BetaGrowthStore.current(this,key)){UsageSnapshot snapshot=AppPreferences.loadSnapshot(this);if(snapshot!=null&&snapshot.longWindow()!=null){UsageWindow window=snapshot.longWindow();String meter=snapshot.weekly!=null?"weekly":"monthly";String policy=meter+"|"+LedgerRecord.cleanPlan(snapshot.planType)+"|"+window.windowSeconds;JSONObject span=BetaExperience.recap(state,System.currentTimeMillis(),selectedMonth);period=LedgerPresentation.period(UsageLedgerDatabase.load(this).days,policy,BetaV4.day(System.currentTimeMillis()),span.getInt("elapsed"));}}}}catch(Exception ignored){}JSONObject data=state;LedgerPresentation.Period measured=period;runOnUiThread(()->{
            if(ticket!=generation||!BetaUi.current(this,key))return;body.removeAllViews();heading();usagePeriod=measured;if(data==null){body.addView(BetaUi.body(this,t("성장 기록을 읽지 못했어요. 원본은 보관됩니다.","Growth record unavailable. Original data is retained."),dark));body.addView(BetaUi.action(this,t("복구 센터","Recovery center"),true,dark,()->BetaExperienceUi.open(this,"health")));return;}
            try{if(page.equals("journey"))journey(data);else recap(data);}catch(Exception error){body.addView(BetaUi.note(this,t("표시를 준비하지 못했어요. 다시 열어 주세요.","Could not prepare this view. Please reopen it."),dark));}
        });});
    }
    void journey(JSONObject state)throws Exception {
        body.setTag("beta998:journey");String pinned="";
        body.addView(BetaUi.body(this,t("현재 기록에 맞춰 다음 목표를 자동 추천합니다.","The next goal is recommended from your records."),dark));
        String category="";LinearLayout group=null;
        for(String[] entry:BetaCatalog.ALL){if(BetaCatalog.title(entry[0]))continue;
            if(!category.equals(entry[5])){category=entry[5];group=BetaUi.group(body,pathName(category),dark);}
            String id=entry[0];boolean earned=state.getJSONObject("honors").has(id);int[] progress=BetaCatalog.progress(state,id,System.currentTimeMillis());
            String note=(earned?t("획득 완료","Earned"):progress[0]+" / "+progress[1])+(id.equals(pinned)?t(" · 고정됨"," · Pinned"):"");
            LinearLayout row=new LinearLayout(this);row.setGravity(android.view.Gravity.CENTER_VERTICAL);ImageView image=new ImageView(this);image.setImageDrawable(new BetaEmblem(id,earned,dark));image.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(image,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
            row.addView(BetaUi.row(this,BetaCatalog.name(id,BetaIdentity.name(this),BetaCopy.ko(this)),note,dark,()->goal(state,id)),new LinearLayout.LayoutParams(0,-2,1));group.addView(row);
        }
    }
    String pathName(String category){switch(category){case "0":return t("첫걸음","First steps");case "1":return t("꾸준한 확인","Steady checks");case "2":return t("달력에 쌓는 기록","Calendar milestones");default:return t("레벨과 장기 명예","Levels and lasting honors");}}
    void goal(JSONObject state,String id){
        String[] entry=BetaCatalog.find(id);boolean earned=state.optJSONObject("honors").has(id);
        androidx.appcompat.app.AlertDialog.Builder dialog=new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(BetaCatalog.name(id,BetaIdentity.name(this),BetaCopy.ko(this))).setMessage(entry[BetaCopy.ko(this)?3:4]).setNegativeButton(BetaCopy.t(this,"close"),null);
        dialog.show();
    }
    void recap(JSONObject state)throws Exception {
        body.setTag("beta998:recap");JSONObject summary=BetaExperience.recap(state,System.currentTimeMillis(),month);
        body.addView(BetaUi.action(this,month?t("이번 주로 보기","Show this week"):t("이번 달로 보기","Show this month"),false,dark,()->{month=!month;load();}));
        LinearLayout card=BetaUi.group(body,summary.getString("from")+" ~ "+summary.getString("to"),dark);
        card.addView(BetaUi.text(this,t("확인 %d일 · +%d XP","%d checked days · +%d XP",summary.getInt("days"),summary.getLong("xp")),24,Ui.mainText(dark),true));
        String coverage=t("경과 %d일 중 확인 %d일. 빈 날은 사용하지 않은 날을 뜻하지 않습니다.","%d days elapsed; %d checked. Blank days do not prove inactivity.",summary.getInt("elapsed"),summary.getInt("days"));card.addView(BetaUi.note(this,coverage,dark));
        card.addView(BetaUi.note(this,TrialDemo.active(this)?BetaCopy.t(this,"demo"):t("저장된 사용량 확인·보상 기록만 반영합니다. 대화 수나 업무 성과를 판단하지 않습니다.","Based only on recorded usage checks and awards. No conversation counts or work assessment."),dark));
        if(usagePeriod!=null){card.addView(BetaUi.body(this,usagePeriod.measuredDays==0?t("사용량 변화 · 측정 자료 부족","Usage change · insufficient measured data"):t("관측된 사용량 변화 %s%%p","Observed usage change %s percentage points",UsagePrecision.number(usagePeriod.points)),dark));card.addView(BetaUi.note(this,t("관측 자료 %d일 · 측정 구간 %d일. 전체 사용량·질문 수가 아닙니다.","%d days recorded · %d days measured. Not total usage or conversation counts.",usagePeriod.recordedDays,usagePeriod.measuredDays),dark));}else card.addView(BetaUi.note(this,t("사용량 관측 자료 부족 · 최신 사용량 창을 읽지 못했습니다. 확인 기록과 사용량은 별도입니다.","Insufficient usage evidence · no readable current usage window. Check days and usage are separate."),dark));
        String share=t("GPT HUD 기록 회고\n%s ~ %s\n확인 %d일 · +%d XP\n%s","GPT HUD recap\n%s ~ %s\n%d checked days · +%d XP\n%s",summary.getString("from"),summary.getString("to"),summary.getInt("days"),summary.getLong("xp"),coverage)+(TrialDemo.active(this)?"\n"+BetaCopy.t(this,"demo"):"");
        card.addView(BetaUi.action(this,t("요약 공유","Share summary"),false,dark,()->startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,share),BetaCopy.t(this,"share")))));
        card.addView(BetaUi.action(this,t("회고 이미지 공유","Share recap image"),false,dark,()->BetaRecapShare.share(this,account,share)));
        card.addView(BetaUi.row(this,t("사용량 캘린더·추세","Usage calendar and trends"),t("기존 관측 자료와 예측 보기","Existing observations and forecasts"),dark,()->startActivity(new Intent(this,LedgerAnalyticsActivity.class))));
        LinearLayout timeline=BetaUi.group(body,t("획득 타임라인","Award timeline"),dark);int count=0;
        for(String id:BetaExperience.timeline(state)){if(count++>=100)break;JSONObject facts=state.getJSONObject(BetaCatalog.title(id)?"titles":"honors");timeline.addView(BetaUi.body(this,BetaCatalog.name(id,BetaIdentity.name(this),BetaCopy.ko(this)),dark));timeline.addView(BetaUi.note(this,V3Display.time(facts.getLong(id)),dark));Ui.addSpacer(timeline,12);}
        if(count==0)timeline.addView(BetaUi.note(this,t("아직 획득한 기록이 없어요.","No awards recorded yet."),dark));
    }
    void health(){
        body.setTag("beta998:health");boolean signed=SecureTokenStore.isSignedIn(this),pending=AppPreferences.isOAuthPending(this);UsageSnapshot snapshot=AppPreferences.loadSnapshot(this);JSONObject growth=BetaGrowthStore.peek(this);
        LinearLayout accountCard=BetaUi.group(body,t("계정 연결","Account connection"),dark);
        accountCard.addView(BetaUi.body(this,TrialDemo.active(this)?BetaCopy.t(this,"demo"):pending?t("브라우저 인증 대기 중","Waiting for browser authorization"):signed?t("로그인 정보 저장됨 · 서버 연결 성공과는 별도입니다","Credentials stored · server connection is checked separately"):t("로그인이 필요해요","Sign-in needed"),dark));
        accountCard.addView(BetaUi.action(this,t("계정·로그인 열기","Open account and sign-in"),true,dark,()->MatteNav.homeAction(this,"account")));
        LinearLayout usage=BetaUi.group(body,t("사용량·수집","Usage and collection"),dark);usage.addView(BetaUi.body(this,snapshot==null?t("저장된 사용량 없음","No saved usage"):getString(R.string.next_observed_at,V3Display.time(snapshot.fetchedAtMillis)),dark));
        usage.addView(BetaUi.note(this,growth==null?t("성장 상태 확인 필요","Growth state needs checking"):growth.optBoolean("enabled")?t("성장 수집 켜짐","Growth collection on"):t("성장 수집 중지","Growth collection paused"),dark));
        String error=AppPreferences.getLastError(this);if(!error.isEmpty()){TextView warning=BetaUi.note(this,DiagnosticSanitizer.redact(error),dark);warning.setTextColor(BetaUi.warning(dark));usage.addView(warning);}
        if(!healthResult.isEmpty()){TextView result=BetaUi.note(this,healthResult,dark);result.setTag("beta9915:recheck-result");result.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);if(healthFailed)result.setTextColor(BetaUi.warning(dark));usage.addView(result);}
        Button recheck=BetaUi.action(this,healthBusy?t("확인 중…","Checking…"):t("저장 기록 재확인","Recheck stored record"),false,dark,this::recheckRecords);recheck.setEnabled(!healthBusy);usage.addView(recheck);
        LinearLayout update=BetaUi.group(body,t("앱 업데이트","App update"),dark);update.addView(BetaUi.body(this,AppConstants.VERSION_NAME+" · "+(getPackageManager().canRequestPackageInstalls()?t("설치 출처 허용됨","Install source allowed"):t("설치할 때 출처 허용 필요","Install source permission needed when installing")),dark));
        update.addView(BetaUi.action(this,t("업데이트 확인·재시도","Check or retry update"),true,dark,()->startActivity(new Intent(this,UpdateActivity.class))));
        body.addView(BetaUi.row(this,t("백업·복원","Backup and restore"),t("성장과 설정을 선택해 보관","Choose growth and settings"),dark,()->startActivity(new Intent(this,BetaBackupActivity.class))));
        body.addView(BetaUi.row(this,t("연결·알림 설정","Connections and notifications"),"",dark,()->startActivity(new Intent(this,MeterSettingsActivity.class))));
        body.addView(BetaUi.row(this,t("진단 정보 보기·복사","View or copy diagnostics"),t("토큰·인증 URL·대화 본문 제외","No tokens, authorization URLs or conversations"),dark,()->BetaDiagnostics.show(this)));
    }
    void recheckRecords(){
        if(healthBusy)return;String expected=account;long ticket=++generation;healthBusy=true;healthFailed=false;healthResult=t("저장 기록을 확인하고 있어요","Checking stored records");body.removeAllViews();heading();health();
        BetaGrowthStore.WORKER.execute(()->{
            String message;boolean failed=false;
            try{JSONObject growth=BetaGrowthStore.load(getApplicationContext(),expected);JSONObject medals=BetaMedalStore.load(getApplicationContext(),expected);message=growth==null&&medals==null?t("저장된 성장·메달 기록이 없습니다. 사용량 서버 조회는 별도입니다.","No stored growth or medal records. Usage server checks are separate."):t("저장 기록 확인 완료 · 사용량 서버 조회는 별도입니다.","Stored records checked · usage server checks are separate.");}
            catch(Exception unavailable){failed=true;message=t("저장 기록을 읽지 못했어요. 원본을 보존했습니다. 다시 시도해 주세요.","Could not read stored records. Originals are retained. Please retry.");}
            String outcome=message;boolean error=failed;runOnUiThread(()->{if(isDestroyed()||isFinishing()||ticket!=generation||!java.util.Objects.equals(expected,SubscriptionStore.accountKey(this)))return;healthBusy=false;healthFailed=error;healthResult=outcome;body.removeAllViews();heading();health();});
        });
    }
}
