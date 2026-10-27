package dev.bennett.codexmeter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

/** Home reads only account-scoped display caches. No DB/network/award/whole-history work. */
final class BetaHomeUi {
    static LinearLayout card(AppCompatActivity a,boolean dark){
        return card(a,dark,true);
    }
    static LinearLayout card(AppCompatActivity a,boolean dark,boolean framed){
        int rank=TierTheme.tier(a);JSONObject growth=BetaGrowthStore.peek(a);
        LinearLayout card=framed?BetaUi.card(a,dark):new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);card.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        if(framed){TierTheme.frame(card,rank,dark);card.setPadding(Ui.dp(a,18),Ui.dp(a,20),Ui.dp(a,18),Ui.dp(a,18));}card.setTag("beta996:identity");
        boolean stack=a.getResources().getConfiguration().fontScale>1.3f||a.getResources().getConfiguration().screenWidthDp<300;
        LinearLayout hero=new LinearLayout(a);hero.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);hero.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));hero.setGravity(stack?Gravity.CENTER_HORIZONTAL:Gravity.CENTER_VERTICAL);
        TierCompanionView badge=new TierCompanionView(a,rank);badge.setTag("beta996:tier-badge");int size=Ui.dp(a,framed?(stack?112:92):(stack?80:64));android.widget.FrameLayout emblem=TierTheme.companion(badge,rank);hero.addView(emblem,new LinearLayout.LayoutParams(size,size));
        LinearLayout identity=new LinearLayout(a);identity.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams words=new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1);if(stack)words.topMargin=Ui.dp(a,12);else words.setMarginStart(Ui.dp(a,12));hero.addView(identity,words);
        String grade=rank<0?a.getString(R.string.fun_placing):a.getString(TierPresentation.NAMES[rank]);
        TextView tier=BetaUi.text(a,grade,14,Ui.accent(a,dark),true);tier.setTag("beta996:tier-name");
        TextView name=BetaUi.title(a,BetaIdentity.name(a),dark);name.setTag("beta996:name");
        LinearLayout rankRow=new LinearLayout(a);rankRow.setGravity(Gravity.CENTER_VERTICAL);
        rankRow.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        if(framed){identity.addView(tier);Ui.addSpacer(identity,6);identity.addView(name);}
        else{
            identity.addView(name);Ui.addSpacer(identity,2);
            rankRow.addView(tier,new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1));
            identity.addView(rankRow,new LinearLayout.LayoutParams(-1,-2));
        }
        Ui.addSpacer(identity,4);
        String equipped=growth==null?"":growth.optString("equipped");TextView title=BetaUi.body(a,BetaMedalUi.equippedTitle(a,equipped.isEmpty()?BetaCopy.t(a,"no_title"):BetaIdentity.title(a,equipped)),dark);title.setTextColor(BetaMedalUi.titleColor(a,equipped,dark));title.setTag("beta996:title");identity.addView(title);
        if(stack){tier.setGravity(Gravity.CENTER);name.setGravity(Gravity.CENTER);title.setGravity(Gravity.CENTER);}card.addView(hero);
        if(framed)card.addView(BetaUi.row(a,BetaCopy.ko(a)?"활동 등급 · 대한민국":"Activity grade · Korea",BetaCopy.ko(a)?"고정 기준 등급 · 산정 기준 보기":"Fixed criteria · View how grades work",dark,()->BetaTierDialog.show(a)));
        else{hero.setMinimumHeight(Ui.dp(a,64));hero.setFocusable(true);hero.setClickable(true);hero.setContentDescription(grade+" · "+BetaIdentity.name(a)+" · "+title.getText()+" · "+BetaExperienceUi.t(a,"티어 산정 기준 보기","View tier criteria"));hero.setOnClickListener(v->BetaTierDialog.show(a));}
        if(growth!=null){if(framed)Ui.addSpacer(card,12);TextView level=BetaUi.text(a,"Lv. "+growth.optInt("level"),framed?24:14,Ui.mainText(dark),true);level.setTag("beta996:level");
            if(framed)card.addView(level);
            else{LinearLayout.LayoutParams levelParams=new LinearLayout.LayoutParams(stack?-1:-2,-2);if(!stack)levelParams.setMarginStart(Ui.dp(a,12));if(stack)level.setGravity(Gravity.CENTER);rankRow.addView(level,levelParams);}
            ProgressBar progress=Ui.progress(a,dark);progress.setMax((int)growth.optLong("next_cost",1));progress.setProgress((int)growth.optLong("progress"));progress.setProgressTintList(ColorStateList.valueOf(Ui.accent(a,dark)));progress.setVisibility(framed?android.view.View.VISIBLE:android.view.View.GONE);progress.setContentDescription(BetaCopy.t(a,"xp")+" "+growth.optLong("progress")+" / "+growth.optLong("next_cost"));card.addView(progress);
            if(framed)card.addView(BetaUi.note(a,LedgerUi.number(growth.optLong("progress"))+" / "+LedgerUi.number(growth.optLong("next_cost"))+" "+BetaCopy.t(a,"xp"),dark));
        }else{JSONObject old=RpgProgressStore.progress(a),mxp=MxpEconomy.state(old);if(old!=null)card.addView(BetaUi.body(a,BetaCopy.t(a,"archive")+" · Lv. "+(mxp==null?old.optInt("level",1):mxp.optInt("level",1)),dark));card.addView(BetaUi.note(a,BetaCopy.t(a,BetaGrowthStore.displayError(a)?"record_error":"pending_growth"),dark));}
        if(BetaGrowthStore.displayError(a))card.addView(BetaUi.row(a,BetaCopy.t(a,"retry_record"),BetaCopy.t(a,"record_error"),dark,()->BetaGrowthStore.refreshDisplay(a)));
        return card;
    }
    static void growthLinks(AppCompatActivity a,LinearLayout card,JSONObject growth,boolean dark){
        JSONObject goal=growth==null?null:growth.optJSONObject("next_goal");
        if(goal!=null&&!goal.optString("id").isEmpty()){
            android.view.View row=BetaUi.row(a,BetaCopy.t(a,"next_focus"),BetaCatalog.name(goal.optString("id"),BetaIdentity.name(a),BetaCopy.ko(a))+" · "+Math.min(goal.optInt("current"),goal.optInt("target"))+" / "+goal.optInt("target"),dark,()->a.startActivity(new Intent(a,GrowthActivity.class)));
            row.setTag("beta998:goal-link");card.addView(row);
        }
        android.view.View row=BetaUi.row(a,BetaCopy.ko(a)?"성장과 다음 목표":"Growth and next goal",growth==null?"":BetaCopy.t(a,"counts",growth.optJSONObject("honors").length(),growth.optJSONObject("titles").length()),dark,()->a.startActivity(new Intent(a,GrowthActivity.class)));
        row.setTag("beta998:growth-link");card.addView(row);
    }
}
