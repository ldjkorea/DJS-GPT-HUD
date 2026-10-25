package dev.bennett.codexmeter;

import android.app.AlertDialog;
import android.content.*;
import android.view.View;
import android.view.Gravity;
import android.text.Layout;
import android.graphics.text.LineBreaker;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

/** Explicit local actions only. Never reads clipboard, refreshes usage or reconciles growth. */
final class CoachUi {
    private static final java.util.concurrent.ExecutorService WORKER=java.util.concurrent.Executors.newSingleThreadExecutor();
    private static final Map<AppCompatActivity,AlertDialog> OPEN=new WeakHashMap<>();
    private static final Map<AppCompatActivity,String> OWNER=new WeakHashMap<>();
    private CoachUi(){}
    static LinearLayout card(AppCompatActivity a,boolean dark,UsageSnapshot snapshot,boolean fresh,boolean bonus,double daily){
        LinearLayout card=new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER_HORIZONTAL);card.setTag("beta9911:coach");
        String account=SubscriptionStore.accountKey(a),tone=CoachPolicy.tone(a),identity=SpicyQuotes.identity(a,account);
        SpicyQuotes.Selection selection=SpicyQuotes.peek(a,account);String group=CoachCatalog.group(snapshot,fresh,!AppPreferences.getLastError(a).isEmpty(),bonus,daily,System.currentTimeMillis());
        if(OPEN.containsKey(a)&&(!identity.equals(OWNER.get(a))||tone.equals("off")||CoachCatalog.constrained(group))){OPEN.get(a).dismiss();OPEN.remove(a);}
        if(tone.equals("off")){card.setVisibility(View.GONE);return card;}
        card.setPadding(Ui.dp(a,10),Ui.dp(a,14),Ui.dp(a,10),Ui.dp(a,14));
        TextView label=LedgerUi.caption(a,a.getString(R.string.live_ai_title),dark);
        label.setGravity(Gravity.CENTER);label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        card.addView(label,new LinearLayout.LayoutParams(-1,-2));Ui.addSpacer(card,8);
        boolean ko=RpgGrowthUi.korean(a);String text;
        if(ko&&tone.equals("spicy")){if(selection==null){card.setVisibility(View.GONE);return card;}text=selection.text;}
        else if(CoachCatalog.constrained(group))text=a.getString(R.string.coach_prepare_only);
        else text=FunCoach.current(a,tone,RpgProgressStore.tier(a),FunInsights.Situation.TIER,CoachMoment.Kind.NORMAL,"local",System.currentTimeMillis());
        TextView quote=LedgerUi.heading(a,"“"+text+"”",dark);quote.setTextSize(18);quote.setMinimumHeight(Ui.dp(a,48));quote.setSingleLine(false);quote.setIncludeFontPadding(true);quote.setLineSpacing(0,1.3f);
        // Android 8/9 retain their existing line-breaking behavior.
        if(android.os.Build.VERSION.SDK_INT>=29)quote.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
        quote.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);quote.setGravity(Gravity.CENTER);quote.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);quote.setTag("home:coach-text");quote.setContentDescription(text);quote.setOnClickListener(v->{if(!java.util.Objects.equals(account,SubscriptionStore.accountKey(a))||!identity.equals(SpicyQuotes.identity(a,account)))return;LinearLayout full=BetaMedalUi.content(a);full.addView(BetaUi.body(a,text,dark));BetaMedalUi.show(a,a.getString(R.string.live_ai_title),full,account);});card.addView(quote,new LinearLayout.LayoutParams(-1,-2));
        if(selection!=null&&selection.tip!=null&&!CoachCatalog.constrained(group)&&tone.equals("spicy")){
            Button action=LedgerUi.action(a,a.getString(R.string.coach_try),false,dark,()->tip(a,dark,account,selection));
            LinearLayout.LayoutParams actionParams=new LinearLayout.LayoutParams(-2,-2);actionParams.gravity=Gravity.CENTER_HORIZONTAL;actionParams.topMargin=Ui.dp(a,12);
            card.addView(action,actionParams);
        }
        return card;
    }
    private static void tip(AppCompatActivity a,boolean dark,String account,SpicyQuotes.Selection selection){
        if(!selection.identity.equals(SpicyQuotes.identity(a,account))||!account.equals(SubscriptionStore.accountKey(a)))return;
        CoachCatalog.Tip t=selection.tip;if(t==null)return;
        LinearLayout content=new LinearLayout(a);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(Ui.dp(a,20),Ui.dp(a,12),Ui.dp(a,20),Ui.dp(a,12));
        content.addView(BetaUi.body(a,selection.text,dark));Ui.addSpacer(content,12);
        content.addView(BetaUi.title(a,BetaExperienceUi.t(a,"준비할 내용","What to provide"),dark));
        content.addView(LedgerUi.caption(a,t.inputs+"\n\n"+t.fallback,dark));Ui.addSpacer(content,16);
        content.addView(BetaUi.title(a,BetaExperienceUi.t(a,"실전 프롬프트","Ready-to-use prompt"),dark));Ui.addSpacer(content,8);
        TextView prompt=BetaUi.body(a,t.prompt,dark);prompt.setTextIsSelectable(true);prompt.setPadding(Ui.dp(a,14),Ui.dp(a,14),Ui.dp(a,14),Ui.dp(a,14));prompt.setBackground(LedgerUi.shape(a,Ui.controlSurface(a,dark),12));content.addView(prompt);Ui.addSpacer(content,16);content.addView(LedgerUi.caption(a,t.check,dark));
        ScrollView scroll=new ScrollView(a);scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle(t.title).setView(scroll).setPositiveButton(R.string.coach_copy,null).setNegativeButton(R.string.coach_close,null).create();
        dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(!selection.identity.equals(SpicyQuotes.identity(a,account))||!account.equals(SubscriptionStore.accountKey(a))){dialog.dismiss();return;}
            boolean copied=copyPrompt((ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE),t.title,prompt.getText().toString());Toast.makeText(a,copied?R.string.coach_copied:R.string.coach_copy_failed,copied?Toast.LENGTH_SHORT:Toast.LENGTH_LONG).show();
        }));OPEN.put(a,dialog);OWNER.put(a,selection.identity);dialog.setOnDismissListener(x->{if(OPEN.get(a)==dialog){OPEN.remove(a);OWNER.remove(a);}});dialog.show();
    }
    static boolean copyPrompt(ClipboardManager clipboard,String title,String visiblePrompt){try{if(clipboard==null)return false;clipboard.setPrimaryClip(ClipData.newPlainText(title,visiblePrompt));return true;}catch(RuntimeException error){return false;}}
    static void settings(AppCompatActivity a,LinearLayout parent,boolean dark){
        android.view.View setting=BetaUi.row(a,a.getString(R.string.live_ai_title),BetaExperienceUi.t(a,CoachPolicy.tone(a).equals("off")?"꺼짐":"켜짐 · 앱이 자동 선택",CoachPolicy.tone(a).equals("off")?"Off":"On · chosen automatically"),dark,()->{
            String[] labels={BetaExperienceUi.t(a,"켜기 · 앱이 자동 선택","On · chosen automatically"),BetaExperienceUi.t(a,"끄기","Off")};
            new AlertDialog.Builder(a).setTitle(R.string.live_ai_title).setSingleChoiceItems(labels,CoachPolicy.tone(a).equals("off")?1:0,(d,i)->{d.dismiss();WORKER.execute(()->{FunStore.tone(a,i==1?"off":"spicy");a.runOnUiThread(a::recreate);});}).show();
        });setting.setTag("beta9915:coach-setting");parent.addView(setting);
    }
}
