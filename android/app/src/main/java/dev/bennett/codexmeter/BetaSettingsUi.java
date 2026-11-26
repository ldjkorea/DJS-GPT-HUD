package dev.bennett.codexmeter;

import android.content.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.function.Consumer;

/** Settings grouped by what the user wants to change. */
final class BetaSettingsUi {
    static String t(android.content.Context c,String ko,String en){return BetaExperienceUi.t(c,ko,en);}
    static void hub(AppCompatActivity a,LinearLayout parent,boolean dark,Consumer<Boolean> export,Runnable clear,Runnable details){
        LinearLayout account=BetaUi.group(parent,t(a,"내 프로필","My profile"),dark);
        account.addView(BetaUi.row(a,BetaCopy.t(a,"account"),"",dark,()->MatteNav.homeAction(a,"account")));
        account.addView(BetaUi.row(a,BetaCopy.t(a,"name"),BetaIdentity.name(a),dark,()->BetaIdentity.edit(a,a::recreate)));
        JSONObject cached=BetaGrowthStore.peek(a);
        account.addView(BetaUi.row(a,BetaCopy.t(a,"title"),BetaMedalUi.equippedTitle(a,cached==null||cached.optString("equipped").isEmpty()?BetaCopy.t(a,"no_title"):BetaIdentity.title(a,cached.optString("equipped"))),dark,()->a.startActivity(new Intent(a,BetaCatalogActivity.class).putExtra("titles",true))));
        if(SubscriptionStore.key(a,AppPreferences.loadSnapshot(a))!=null)account.addView(BetaUi.row(a,t(a,"구독·결제 기간","Subscription and billing period"),"",dark,()->SubscriptionUi.edit(a,AppPreferences.loadSnapshot(a),dark,a::recreate)));

        LinearLayout display=BetaUi.group(parent,t(a,"화면·한마디·알림","Display, coach and notifications"),dark);
        CoachUi.settings(a,display,dark);
        display.addView(BetaUi.row(a,t(a,"저장된 인용","Saved excerpt"),"",dark,()->{
            LinearLayout excerpt=BetaMedalUi.content(a);
            TiboQuote.add(a,excerpt,dark);
            BetaMedalUi.show(a,t(a,"저장된 인용","Saved excerpt"),excerpt);
        }));
        display.addView(BetaUi.row(a,BetaCopy.t(a,"language"),AppLanguage.currentName(a),dark,()->AppLanguage.show(a,false)));
        String theme=AppPreferences.getAppTheme(a);
        String themeLabel=WidgetOptions.THEME_DARK.equals(theme)?t(a,"어둡게","Dark")
                :WidgetOptions.THEME_LIGHT.equals(theme)?t(a,"밝게","Light"):a.getString(R.string.ui_system_default_9d8d38);
        display.addView(BetaUi.row(a,BetaCopy.t(a,"theme"),themeLabel,dark,()->page(a,"appearance")));
        display.addView(BetaUi.row(a,t(a,"상세 한도 표시 설정","Detailed limit display"),t(a,"상세 화면의 순서·표시만 변경","Order and visibility in the detail screen only"),dark,()->a.startActivity(new Intent(a,DashboardReorderActivity.class))));
        display.addView(BetaUi.row(a,BetaCopy.t(a,"notifications"),"",dark,()->page(a,"notifications")));

        LinearLayout connections=BetaUi.group(parent,t(a,"새로고침·기기 연결","Refresh and devices"),dark);
        connections.addView(BetaUi.row(a,BetaCopy.t(a,"automatic"),"",dark,()->page(a,"refresh_usage")));
        connections.addView(BetaUi.row(a,t(a,"연결·업데이트 복구","Connection and update recovery"),"",dark,()->BetaExperienceUi.open(a,"health")));
        connections.addView(BetaUi.row(a,BetaCopy.t(a,"devices"),"",dark,()->a.startActivity(new Intent(a,WidgetGuideActivity.class))));

        LinearLayout data=BetaUi.group(parent,t(a,"데이터·백업","Data and backup"),dark);
        View backup=BetaUi.row(a,t(a,"백업·복원","Backup and restore"),t(a,"성장·날짜별 메달·설정 · 로그인 제외","Growth, dated medals and settings · no credentials"),dark,()->a.startActivity(new Intent(a,BetaBackupActivity.class)));
        backup.setTag("beta9915:backup");data.addView(backup);
        data.addView(BetaUi.row(a,BetaCopy.t(a,"archive"),BetaCopy.t(a,"readonly"),dark,()->a.startActivity(new Intent(a,BetaArchiveActivity.class))));
        String key=SubscriptionStore.accountKey(a);
        if(cached!=null){
            androidx.appcompat.widget.SwitchCompat on=new androidx.appcompat.widget.SwitchCompat(a);
            on.setText(BetaCopy.t(a,"growth_enabled"));on.setTextColor(Ui.mainText(dark));on.setTextSize(15);on.setMinHeight(Ui.dp(a,48));on.setChecked(cached.optBoolean("enabled"));
            on.setOnCheckedChangeListener((b,checked)->{on.setOnCheckedChangeListener(null);BetaUi.task(a,key,()->BetaGrowthStore.enabled(a,key,checked),a::recreate);});data.addView(on);
        }else if(key!=null&&BetaGrowthStore.displayError(a))data.addView(BetaUi.row(a,BetaCopy.t(a,"retry_record"),BetaCopy.t(a,"record_error"),dark,()->BetaGrowthStore.refreshDisplay(a)));
        else if(key!=null)data.addView(BetaUi.action(a,BetaCopy.t(a,"new_growth"),true,dark,()->BetaUi.start(a,key,a::recreate)));

        LinearLayout app=BetaUi.group(parent,BetaCopy.t(a,"app_info"),dark);
        app.addView(BetaUi.note(a,"GPT HUD · "+AppConstants.VERSION_NAME,dark));
        app.addView(BetaUi.row(a,BetaCopy.t(a,"updates"),"",dark,()->a.startActivity(new Intent(a,UpdateActivity.class))));
        String automatic=UpdatePreferences.automaticChecks(a)?t(a,"켜짐", "On")+" · "+DisplayMessages.localize(a,UpdateCheckFrequency.label(UpdatePreferences.checkIntervalHours(a))):t(a,"꺼짐", "Off");
        View updateSettings=BetaUi.row(a,t(a,"자동 업데이트 설정","Automatic update settings"),automatic,dark,()->page(a,"updates"));
        updateSettings.setTag("stable-redesign:update-settings");app.addView(updateSettings);
        app.addView(BetaUi.row(a,BetaCopy.t(a,"help"),"",dark,()->a.startActivity(new Intent(a,AboutActivity.class))));
        app.addView(BetaUi.row(a,BetaCopy.t(a,"diagnostics"),"",dark,()->BetaDiagnostics.show(a)));

        LinearLayout advanced=BetaUi.group(parent,"",dark),legacy=new LinearLayout(a);
        legacy.setOrientation(LinearLayout.VERTICAL);legacy.setVisibility(View.GONE);
        View toggle=BetaUi.row(a,t(a,"고급·이전 형식 도구","Advanced and legacy tools"),t(a,"전체 이력 내보내기·이전 백업 형식·연결 설정","Full history export, legacy backups and connection settings"),dark,()->{
            if(legacy.getChildCount()==0){
                RecordSettings.legacyHub(a,legacy,dark,export,clear,details);
            }
            legacy.setVisibility(legacy.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
            View control=((LinearLayout)legacy.getParent()).getChildAt(0);
            androidx.core.view.ViewCompat.setStateDescription(control,t(a,legacy.getVisibility()==View.VISIBLE?"펼쳐짐":"접힘",legacy.getVisibility()==View.VISIBLE?"Expanded":"Collapsed"));
        });
        toggle.setTag("beta995:advanced");androidx.core.view.ViewCompat.setStateDescription(toggle,t(a,"접힘","Collapsed"));advanced.addView(toggle);advanced.addView(legacy);
    }
    static void page(AppCompatActivity a,String page){a.startActivity(new Intent(a,SettingsActivity.class).putExtra("settings_page",page));}
}
