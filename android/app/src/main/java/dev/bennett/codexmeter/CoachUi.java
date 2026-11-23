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
    static LinearLayout card(AppCompatActivity a, boolean dark, UsageSnapshot snapshot,
                             boolean fresh, boolean bonus, double daily) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setTag("beta9911:coach");

        String account = SubscriptionStore.accountKey(a);
        String tone = CoachPolicy.tone(a);
        String identity = SpicyQuotes.identity(a, account);
        SpicyQuotes.Selection selection = SpicyQuotes.peek(a, account);
        String group = CoachCatalog.group(snapshot, fresh, !AppPreferences.getLastError(a).isEmpty(),
                bonus, daily, System.currentTimeMillis());
        boolean constrained = CoachCatalog.constrained(group);
        AlertDialog open = OPEN.get(a);
        if (open != null && (!identity.equals(OWNER.get(a)) || tone.equals("off") || constrained)) {
            open.dismiss();
        }

        boolean localCatalog = RpgGrowthUi.korean(a) && tone.equals("spicy");
        if (tone.equals("off") || (localCatalog && selection == null)) {
            card.setVisibility(View.GONE);
            return card;
        }
        String text;
        if (localCatalog) text = selection.text;
        else if (constrained) text = a.getString(R.string.coach_prepare_only);
        else text = FunCoach.current(a, tone, RpgProgressStore.tier(a), FunInsights.Situation.TIER,
                    CoachMoment.Kind.NORMAL, "local", System.currentTimeMillis());

        int solar = Ui.solar(dark);
        int surface = androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(a, dark), solar, 0.07f);
        card.setBackground(LedgerUi.shape(a, surface, 16));
        card.setPadding(Ui.dp(a, 16), Ui.dp(a, 10), Ui.dp(a, 16), Ui.dp(a, 10));

        LinearLayout byline = new LinearLayout(a);
        byline.setGravity(Gravity.CENTER_VERTICAL);
        ImageView mark = new ImageView(a);
        mark.setImageResource(R.drawable.samjogo_mark);
        mark.setColorFilter(solar);
        mark.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        byline.addView(mark, new LinearLayout.LayoutParams(Ui.dp(a, 26), Ui.dp(a, 26)));
        TextView label = Ui.text(a, a.getString(R.string.live_ai_title), 14, solar);
        label.setTypeface(PretendardFont.semibold(a));
        androidx.core.view.ViewCompat.setAccessibilityHeading(label, true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, -2, 1);
        labelParams.setMarginStart(Ui.dp(a, 8));
        byline.addView(label, labelParams);
        card.addView(byline, new LinearLayout.LayoutParams(-1, -2));

        TextView quote = Ui.text(a, text, 18, Ui.mainText(dark));
        quote.setTypeface(PretendardFont.medium(a));
        quote.setTag("home:coach-text");
        quote.setSingleLine(false);
        quote.setMinimumHeight(Ui.dp(a, 48));
        quote.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        quote.setIncludeFontPadding(true);
        quote.setLineSpacing(Ui.dp(a, 2), 1f);
        wrapText(quote, localCatalog);
        quote.setOnClickListener(v -> {
            if (!java.util.Objects.equals(account, SubscriptionStore.accountKey(a))
                    || !identity.equals(SpicyQuotes.identity(a, account))) return;
            LinearLayout full = BetaMedalUi.content(a);
            full.addView(BetaUi.body(a, text, dark));
            BetaMedalUi.show(a, a.getString(R.string.live_ai_title), full, account);
        });
        card.addView(quote, new LinearLayout.LayoutParams(-1, -2));

        if (localCatalog && selection.tip != null && !constrained) {
            Button action = LedgerUi.action(a, a.getString(R.string.coach_try), false, dark,
                    () -> tip(a, dark, account, selection));
            action.setTextSize(14);
            action.setTextColor(Ui.accent(a, dark));
            action.setContentDescription(a.getString(R.string.coach_try) + " · " + selection.tip.title);
            boolean separateLine = LedgerUi.stacked(a);
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(separateLine ? -1 : -2, -2);
            (separateLine ? card : byline).addView(action, actionParams);
        }
        LinearLayout.LayoutParams placement = new LinearLayout.LayoutParams(-1, -2);
        placement.topMargin = Ui.dp(a, 6);
        placement.bottomMargin = Ui.dp(a, 6);
        card.setLayoutParams(placement);
        return card;
    }
    private static void wrapText(TextView view, boolean korean) {
        view.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        if (android.os.Build.VERSION.SDK_INT >= 29) view.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
        if (korean) {
            view.setTextLocale(Locale.KOREAN);
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                view.setLineBreakWordStyle(android.graphics.text.LineBreakConfig.LINE_BREAK_WORD_STYLE_PHRASE);
            }
        }
    }

    private static void tip(AppCompatActivity a, boolean dark, String account, SpicyQuotes.Selection selection) {
        if (!selection.identity.equals(SpicyQuotes.identity(a, account))
                || !account.equals(SubscriptionStore.accountKey(a))) return;
        CoachCatalog.Tip t = selection.tip;
        if (t == null) return;
        LinearLayout content = BetaMedalUi.content(a);
        TextView label = Ui.text(a, BetaExperienceUi.t(a, "실전 프롬프트", "Ready-to-use prompt"), 14, Ui.accent(a, dark));
        label.setTypeface(PretendardFont.semibold(a));
        androidx.core.view.ViewCompat.setAccessibilityHeading(label, true);
        content.addView(label);
        Ui.addSpacer(content, 6);
        TextView prompt = Ui.text(a, t.prompt, 17, Ui.mainText(dark));
        wrapText(prompt, true);
        prompt.setLineSpacing(Ui.dp(a, 3), 1f);
        prompt.setTextIsSelectable(true);
        prompt.setTag("coach:prompt");
        int inset = Ui.dp(a, 14);
        prompt.setPadding(inset, inset, inset, inset);
        prompt.setBackground(LedgerUi.shape(a, Ui.controlSurface(a, dark), 12));
        content.addView(prompt);
        addTipGuide(a, content, t, dark);
        ScrollView scroll = new ScrollView(a);
        scroll.addView(content);
        AlertDialog dialog = new AlertDialog.Builder(a).setTitle(t.title).setView(scroll)
                .setPositiveButton(R.string.coach_copy, null).setNegativeButton(R.string.coach_close, null).create();
        dialog.setOnShowListener(ignored -> {
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                android.graphics.drawable.Drawable surface = window.getDecorView().getBackground();
                if (surface != null) surface.mutate().setTint(Ui.cardColor(a, dark));
            }
            Button close = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            close.setTextColor(Ui.secondaryText(dark));
            close.setTypeface(PretendardFont.medium(a));
            Button copy = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            copy.setTextColor(Ui.accent(a, dark));
            copy.setTypeface(PretendardFont.semibold(a));
            copy.setOnClickListener(v -> {
                if (!selection.identity.equals(SpicyQuotes.identity(a, account))
                        || !account.equals(SubscriptionStore.accountKey(a))) { dialog.dismiss(); return; }
                boolean copied = copyPrompt((ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE),
                        t.title, prompt.getText().toString());
                // Android 13+ supplies its own clipboard confirmation.
                if (copied) copy.setText(BetaExperienceUi.t(a, "복사됨", "Copied"));
                if (!copied || android.os.Build.VERSION.SDK_INT < 33) {
                    Toast.makeText(a, copied ? R.string.coach_copied : R.string.coach_copy_failed,
                            copied ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                }
            });
        });
        OPEN.put(a, dialog);
        OWNER.put(a, selection.identity);
        dialog.setOnDismissListener(x -> {
            if (OPEN.get(a) == dialog) { OPEN.remove(a); OWNER.remove(a); }
        });
        dialog.show();
    }

    static void addTipGuide(AppCompatActivity a, LinearLayout content, CoachCatalog.Tip tip, boolean dark) {
        String expand = BetaExperienceUi.t(a, "사용 요령 보기", "Show guidance");
        String collapse = BetaExperienceUi.t(a, "사용 요령 접기", "Hide guidance");
        Button toggle = Ui.button(a, expand, false, dark);
        toggle.setTag("coach:guide-toggle");
        toggle.setMinHeight(Ui.dp(a, 48));
        toggle.setMinimumHeight(Ui.dp(a, 48));
        LinearLayout notes = new LinearLayout(a);
        notes.setOrientation(LinearLayout.VERTICAL);
        notes.setTag("coach:guide");
        Ui.addSpacer(notes, 8);
        notes.addView(BetaUi.title(a, BetaExperienceUi.t(a, "대괄호에 넣을 내용", "What goes in brackets"), dark));
        notes.addView(BetaUi.body(a, tip.inputs, dark));
        Ui.addSpacer(notes, 10);
        notes.addView(BetaUi.title(a, BetaExperienceUi.t(a, "결과 확인", "Check the result"), dark));
        notes.addView(BetaUi.body(a, tip.check, dark));
        Ui.addSpacer(notes, 8);
        notes.addView(BetaUi.note(a, tip.fallback, dark));
        notes.setVisibility(View.GONE);
        toggle.setOnClickListener(v -> {
            boolean opening = notes.getVisibility() != View.VISIBLE;
            notes.setVisibility(opening ? View.VISIBLE : View.GONE);
            toggle.setText(opening ? collapse : expand);
        });
        Ui.addSpacer(content, 8);
        content.addView(toggle);
        content.addView(notes);
    }
    static boolean copyPrompt(ClipboardManager clipboard,String title,String visiblePrompt){try{if(clipboard==null)return false;clipboard.setPrimaryClip(ClipData.newPlainText(title,visiblePrompt));return true;}catch(RuntimeException error){return false;}}
    static void settings(AppCompatActivity a,LinearLayout parent,boolean dark){
        android.view.View setting=BetaUi.row(a,a.getString(R.string.live_ai_title),BetaExperienceUi.t(a,CoachPolicy.tone(a).equals("off")?"꺼짐":"켜짐 · 앱이 자동 선택",CoachPolicy.tone(a).equals("off")?"Off":"On · chosen automatically"),dark,()->{
            String[] labels={BetaExperienceUi.t(a,"켜기 · 앱이 자동 선택","On · chosen automatically"),BetaExperienceUi.t(a,"끄기","Off")};
            new AlertDialog.Builder(a).setTitle(R.string.live_ai_title).setSingleChoiceItems(labels,CoachPolicy.tone(a).equals("off")?1:0,(d,i)->{d.dismiss();WORKER.execute(()->{FunStore.tone(a,i==1?"off":"spicy");a.runOnUiThread(a::recreate);});}).show();
        });setting.setTag("beta9915:coach-setting");parent.addView(setting);
    }
}
