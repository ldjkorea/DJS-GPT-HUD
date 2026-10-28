package dev.bennett.codexmeter;
import android.content.Intent;import android.widget.*;import org.json.*;

/** One recommended goal, compact period summary, collection entry points. Details are opt-in. */
final class BetaCompactGrowthUi {
    static boolean render(GrowthActivity a,LinearLayout parent,JSONObject journal,boolean dark){
        JSONObject state=journal==null?null:journal.optJSONObject("beta_v4");BetaGrowthModel model=a.betaGrowthModel;
        if(state==null||model==null||model.summary==null)return BetaGrowthUi.renderDetails(a,parent,journal,dark);
        String account=SubscriptionStore.accountKey(a);JSONObject goal=model.summary.optJSONObject("next_goal");
        JSONObject medals=BetaMedalStore.peek(a),periodGoal=null;try{periodGoal=BetaMedalModel.nextPeriod(medals,System.currentTimeMillis());}catch(Exception ignored){}final JSONObject recommended=periodGoal;
        LinearLayout next=BetaUi.group(parent,BetaMedalUi.t(a,"다음 추천 목표","Recommended next goal"),dark);next.setTag("beta9911:next-goal");
        observationStatus(a,next,model,dark);
        String id=goal==null?"":goal.optString("id");
        if(recommended!=null){
            String label=BetaMedalUi.t(a,recommended.optBoolean("month")?"이번 달 %s":"이번 주 %s",recommended.optBoolean("month")?"This month · %s":"This week · %s",BetaMedalUi.grade(a,recommended.optInt("grade")));
            next.addView(BetaUi.title(a,label,dark));
            next.addView(BetaUi.body(a,BetaMedalUi.t(a,"확인된 활동이 %d일 더 쌓이면 %s을 받을 수 있어요.","Record %d more verified activity days to earn %s.",HudUiModel.remaining(recommended.optInt("current"),recommended.optInt("target")),label),dark));ProgressBar bar=Ui.progress(a,dark);bar.setMax(recommended.optInt("target"));bar.setProgress(recommended.optInt("current"));bar.setContentDescription(label+" "+recommended.optInt("current")+" / "+recommended.optInt("target"));next.addView(bar);
            next.addView(BetaUi.note(a,BetaMedalUi.t(a,"기록 %d / %d일 · %d일 더 쌓이면 달성","Recorded %d / %d days · %d more days to go",recommended.optInt("current"),recommended.optInt("target"),HudUiModel.remaining(recommended.optInt("current"),recommended.optInt("target"))),dark));next.addView(BetaUi.row(a,BetaMedalUi.t(a,"달성 조건","Conditions"),"",dark,()->BetaMedalUi.periods(a,dark)));
        }
        else if(id.isEmpty())next.addView(BetaUi.body(a,BetaCopy.t(a,"complete_all"),dark));
        else{
            next.addView(BetaUi.title(a,BetaCatalog.name(id,BetaIdentity.name(a),BetaCopy.ko(a)),dark));
            int target=Math.max(1,goal.optInt("target")),value=Math.min(target,goal.optInt("current"));ProgressBar bar=Ui.progress(a,dark);bar.setMax(target);bar.setProgress(value);bar.setContentDescription(value+" / "+target);next.addView(bar);
            String[] entry=BetaCatalog.find(id);
            if(entry!=null)next.addView(BetaUi.body(a,entry[BetaCopy.ko(a)?3:4],dark));
            String metric=goal.optString("metric");
            String unit=metric.equals("days")||metric.equals("week_now")?BetaMedalUi.t(a,"일","days"):metric.equals("weeks")||metric.equals("perfect")?BetaMedalUi.t(a,"주","weeks"):metric.equals("bronze")||metric.equals("silver")||metric.equals("gold")?BetaMedalUi.t(a,"개월","months"):"";
            next.addView(BetaUi.note(a,value+" / "+target+(unit.isEmpty()?"":" "+unit),dark));next.addView(BetaUi.row(a,BetaMedalUi.t(a,"달성 조건","Conditions"),"",dark,()->BetaGrowthUi.detail(a,state,id,dark,account)));
        }
        JSONObject today=medals==null?null:medals.optJSONObject("today");
        LinearLayout summary=BetaUi.group(parent,BetaMedalUi.t(a,"나의 메달","My medals"),dark);summary.setTag("beta9911:medals");
        int week=medals==null?0:medals.optInt("week"),month=medals==null?0:medals.optInt("month");
        LinearLayout tiles=new LinearLayout(a);boolean stack=a.getResources().getConfiguration().fontScale>1.3f||a.getResources().getConfiguration().screenWidthDp<360;tiles.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);summary.addView(tiles);
        tile(a,tiles,BetaMedalUi.t(a,"오늘","Today"),BetaMedalUi.status(a,today),today==null?0:today.optInt("grade"),today!=null&&today.optBoolean("active"),dark,stack);
        tile(a,tiles,BetaMedalUi.t(a,"이번 주","This week"),medals==null?BetaMedalUi.t(a,"확인 대기","Pending"):BetaMedalUi.t(a,"%d일","%d days",week),BetaMedalModel.grade(week,false),BetaMedalModel.grade(week,false)>0,dark,stack);
        tile(a,tiles,BetaMedalUi.t(a,"이번 달","This month"),medals==null?BetaMedalUi.t(a,"확인 대기","Pending"):BetaMedalUi.t(a,"%d일","%d days",month),BetaMedalModel.grade(month,true),BetaMedalModel.grade(month,true)>0,dark,stack);
        android.view.View records=BetaUi.row(a,BetaMedalUi.t(a,"메달 기록 보기","View medal records"),"",dark,()->a.startActivity(new Intent(a,RecordsActivity.class).putExtra("medal_current_month",true).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        records.setTag("beta9915:medal-records");summary.addView(records);
        if(TrialDemo.active(a))summary.addView(BetaUi.note(a,BetaCopy.t(a,"demo"),dark));
        if(BetaMedalStore.failed(a))summary.addView(BetaUi.note(a,BetaMedalUi.t(a,"메달 기록 확인 필요 · 마지막 기록을 유지합니다","Medal records need another check · saved records retained"),dark));
        LinearLayout collection=BetaUi.group(parent,BetaCopy.t(a,"collection"),dark);collection.setTag("beta9911:collections");
        collection.addView(BetaUi.row(a,BetaCopy.t(a,"honors"),"",dark,()->a.startActivity(new Intent(a,BetaCatalogActivity.class))));
        collection.addView(BetaUi.row(a,BetaCopy.t(a,"titles"),"",dark,()->a.startActivity(new Intent(a,BetaCatalogActivity.class).putExtra("titles",true))));
        collection.addView(BetaUi.row(a,BetaMedalUi.t(a,"성장 상세","Growth details"),"",dark,()->a.startActivity(new Intent(a,GrowthActivity.class).putExtra("beta_details",true))));
        return true;
    }
    static void observationStatus(GrowthActivity a,LinearLayout parent,BetaGrowthModel model,boolean dark){
        boolean paused=!model.collectionEnabled;
        boolean needsCheck=model.status==BetaGrowthModel.Status.ERROR||model.status==BetaGrowthModel.Status.STALE||model.status==BetaGrowthModel.Status.BASELINE;
        String label=paused?BetaCopy.t(a,"paused"):BetaCopy.t(a,model.key());
        if(model.lastObserved>0&&model.status!=BetaGrowthModel.Status.DEMO)label+=" · "+BetaGrowthUi.time(a,model.lastObserved);
        TextView status=BetaUi.note(a,label,dark);status.setTag("stable-redesign:growth-status");
        if(paused||needsCheck)status.setTextColor(BetaUi.warning(dark));parent.addView(status);Ui.addSpacer(parent,8);
        if(paused)parent.addView(BetaUi.row(a,BetaMedalUi.t(a,"수집 설정 확인","Check collection settings"),"",dark,()->a.startActivity(new Intent(a,MeterSettingsActivity.class))));
        else if(needsCheck)parent.addView(BetaUi.row(a,BetaCopy.t(a,"check_home"),"",dark,()->MatteNav.open(a,1)));
    }
    static void tile(GrowthActivity a,LinearLayout parent,String label,String progress,int grade,boolean active,boolean dark,boolean stack){
        LinearLayout column=new LinearLayout(a);column.setOrientation(LinearLayout.VERTICAL);column.setGravity(android.view.Gravity.CENTER);column.setPadding(Ui.dp(a,4),Ui.dp(a,8),Ui.dp(a,4),Ui.dp(a,8));
        parent.addView(column,new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1));ImageView medal=new ImageView(a);medal.setImageDrawable(new BetaMedalUi.Mark(a,grade,active,dark));medal.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);column.addView(medal,new LinearLayout.LayoutParams(Ui.dp(a,44),Ui.dp(a,44)));
        TextView name=BetaUi.body(a,label,dark),note=BetaUi.note(a,progress,dark);name.setGravity(android.view.Gravity.CENTER);note.setGravity(android.view.Gravity.CENTER);column.addView(name);column.addView(note);column.setContentDescription(label+" · "+progress+(active?" · "+BetaMedalUi.grade(a,grade):""));
    }
}
