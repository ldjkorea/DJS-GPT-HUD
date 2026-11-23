package dev.bennett.codexmeter;
import android.content.*;
import android.view.*;
import android.widget.*;
import org.json.*;
final class BetaGrowthUi {
    static boolean render(GrowthActivity a,LinearLayout parent,JSONObject journal,boolean dark){return a.getIntent().getBooleanExtra("beta_details",false)?renderDetails(a,parent,journal,dark):BetaCompactGrowthUi.render(a,parent,journal,dark);}
    static boolean renderDetails(GrowthActivity a,LinearLayout parent,JSONObject journal,boolean dark){
        String account=SubscriptionStore.accountKey(a);BetaGrowthModel model=a.betaGrowthModel;
        if(account==null||model==null)return false;
        JSONObject state=journal==null?null:journal.optJSONObject("beta_v4"),s=model.summary;
        if(state!=null&&s!=null){try{s.put("next_goal",BetaNextGoal.choose(state,System.currentTimeMillis()));}catch(Exception ignored){}BetaNextGoalUi.render(a,parent,state,model,dark,account);BetaExperienceUi.shortcuts(a,parent,dark);}
        LinearLayout hero=BetaUi.group(parent,"",dark);hero.setTag("beta:hero");
        String equipped=s==null?"":s.optString("equipped");hero.addView(BetaUi.title(a,BetaIdentity.title(a,equipped),dark));Ui.addSpacer(hero,8);
        if(state==null){
            JSONObject old=journal==null?null:journal.optJSONObject("rpg"),mxp=MxpEconomy.state(old);
            hero.addView(BetaUi.note(a,BetaCopy.t(a,"archive")+(mxp==null?"":" · Lv. "+mxp.optInt("level")),dark));Ui.addSpacer(hero,8);
            hero.addView(BetaUi.body(a,BetaCopy.t(a,"pending_growth"),dark));hero.addView(BetaUi.action(a,BetaCopy.t(a,"new_growth"),true,dark,()->BetaUi.start(a,account,()->a.load(true))));
            LinearLayout help=BetaUi.group(parent,BetaCopy.t(a,"app_info"),dark);help.addView(BetaUi.row(a,BetaCopy.t(a,"check_home"),"",dark,()->MatteNav.open(a,1)));help.addView(BetaUi.row(a,BetaCopy.t(a,"advanced"),"",dark,()->a.startActivity(new Intent(a,MeterSettingsActivity.class))));return true;
        }
        if(s==null){hero.addView(BetaUi.note(a,BetaCopy.t(a,"error"),dark));hero.addView(BetaUi.action(a,BetaCopy.t(a,"backup"),false,dark,()->a.startActivity(new Intent(a,BetaBackupActivity.class))));return true;}
        hero.addView(BetaUi.text(a,"Lv. "+s.optInt("level"),28,Ui.mainText(dark),true));
        hero.addView(BetaUi.note(a,BetaCopy.t(a,"to_level",LedgerUi.number(Math.max(0,s.optLong("next_cost")-s.optLong("progress")))),dark));Ui.addSpacer(hero,10);
        ProgressBar xp=Ui.progress(a,dark);xp.setMax((int)s.optLong("next_cost"));xp.setProgress((int)s.optLong("progress"));xp.setProgressTintList(android.content.res.ColorStateList.valueOf(BetaUi.accent(a,dark)));xp.setContentDescription(BetaCopy.t(a,"xp")+" "+s.optLong("progress")+" / "+s.optLong("next_cost"));hero.addView(xp);
        hero.addView(BetaUi.note(a,LedgerUi.number(s.optLong("progress"))+" / "+LedgerUi.number(s.optLong("next_cost"))+" "+BetaCopy.t(a,"xp"),dark));
        rewardNotice(a, hero, state, account, dark);
        LinearLayout missions=BetaUi.group(parent,BetaCopy.t(a,"missions"),dark);missions.setTag("beta:challenges");
        int period=a.getIntent().getIntExtra("beta_period",0);LinearLayout tabs=new LinearLayout(a);boolean vertical=a.getResources().getConfiguration().fontScale>1.6f;tabs.setOrientation(vertical?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        String[] names={"daily","weekly","monthly"};for(int i=0;i<3;i++){final int index=i;Button tab=BetaUi.action(a,BetaCopy.t(a,names[i]),i==period,dark,()->{a.getIntent().putExtra("beta_period",index);a.load(true);});tab.setPadding(Ui.dp(a,8),Ui.dp(a,10),Ui.dp(a,8),Ui.dp(a,10));tab.setTag("beta995:period:"+i);tab.setSelected(i==period);androidx.core.view.ViewCompat.setStateDescription(tab,i==period?a.getString(R.string.matte_selected):"");LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(vertical?-1:0,-2,vertical?0:1);if(i>0){if(vertical)lp.topMargin=Ui.dp(a,6);else lp.setMarginStart(Ui.dp(a,6));}tabs.addView(tab,lp);}missions.addView(tabs);Ui.addSpacer(missions,12);
        java.time.LocalDate date=java.time.LocalDate.parse(s.optString("as_of_day"));java.time.LocalDate from=period==1?java.time.LocalDate.parse(BetaV4.week(date)):period==2?date.withDayOfMonth(1):date;java.time.LocalDate to=period==1?from.plusDays(6):period==2?date.withDayOfMonth(date.lengthOfMonth()):date;
        java.time.format.DateTimeFormatter format=java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(a.getResources().getConfiguration().getLocales().get(0));missions.addView(BetaUi.note(a,from.format(format)+(period==0?"":" – "+to.format(format)),dark));
        TextView status=BetaUi.note(a,BetaCopy.t(a,model.key()),dark);status.setTag("beta:status");missions.addView(status);if(model.status==BetaGrowthModel.Status.DEMO&&!s.optBoolean("enabled"))missions.addView(BetaUi.note(a,BetaCopy.t(a,"paused"),dark));Ui.addSpacer(missions,8);
        missions.addView(BetaUi.body(a,BetaCopy.t(a,period==0?"check_today":period==1?"weekly_condition":"monthly_condition"),dark));
        if(period==0)missions.addView(BetaUi.note(a,BetaCopy.t(a,"day_condition"),dark));
        int count=s.optInt(period==0?"day":period==1?"week":"month");int[] targets=BetaV4.targets(period);
        for(int target:targets){Ui.addSpacer(missions,10);LinearLayout step=new LinearLayout(a);step.setOrientation(LinearLayout.VERTICAL);step.addView(BetaUi.body(a,BetaCopy.t(a,"days",Math.min(count,target),target),dark));
            ProgressBar bar=Ui.progress(a,dark);bar.setMax(target);bar.setProgress(Math.min(count,target));bar.setProgressTintList(android.content.res.ColorStateList.valueOf(BetaUi.accent(a,dark)));bar.setContentDescription(BetaCopy.t(a,"days",Math.min(count,target),target));step.addView(bar);
            int reward=BetaV4.reward(period,target);step.addView(BetaUi.note(a,BetaCopy.t(a,"stage_reward",reward)+" · "+(count>=target?BetaCopy.t(a,"auto_paid"):BetaCopy.t(a,"remaining",target-count)),dark));missions.addView(step);}
        if(model.lastObserved>0)missions.addView(BetaUi.note(a,BetaCopy.t(a,"last_check",time(a,model.lastObserved)),dark));
        missions.addView(BetaUi.action(a,BetaCopy.t(a,"check_home"),true,dark,()->MatteNav.open(a,1)));
        LinearLayout collection=BetaUi.group(parent,BetaCopy.t(a,"collection"),dark);collection.addView(BetaUi.note(a,BetaCopy.t(a,"counts",s.optJSONObject("honors").length(),s.optJSONObject("titles").length()),dark));
        View medals=BetaUi.row(a,BetaCopy.t(a,"honors"),"",dark,()->catalog(a,state,dark,false,0,account));medals.setTag("beta:honors");collection.addView(medals);
        View titles=BetaUi.row(a,BetaCopy.t(a,"titles"),equipped.isEmpty()?BetaCopy.t(a,"no_title"):BetaIdentity.title(a,equipped),dark,()->catalog(a,state,dark,true,0,account));titles.setTag("beta:wardrobe");collection.addView(titles);
        collection.addView(BetaUi.row(a,BetaCopy.t(a,"rules"),"",dark,()->rules(a,state,dark,account)));collection.addView(BetaUi.row(a,BetaCopy.t(a,"tier"),"",dark,()->tier(a,journal,account)));collection.addView(BetaUi.row(a,BetaCopy.t(a,"share"),"",dark,()->a.startActivity(new Intent(a,BetaProfileActivity.class))));return true;
    }
    private static void rewardNotice(GrowthActivity a, LinearLayout hero, JSONObject state, String account, boolean dark) {
        long displayedTotal = state.optLong("total");
        long fresh = displayedTotal - state.optLong("seen_total");
        if (fresh <= 0) return;
        TextView notice = BetaUi.note(a, BetaCopy.t(a, "reward_summary", LedgerUi.number(fresh)), dark);
        notice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        notice.setTag("growth:recent-rewards");
        class Receipt implements ViewTreeObserver.OnDrawListener, View.OnAttachStateChangeListener {
            final android.graphics.Rect visible = new android.graphics.Rect();
            boolean submitted;
            @Override public void onViewAttachedToWindow(View v) {
                v.getViewTreeObserver().addOnDrawListener(this);
            }
            @Override public void onViewDetachedFromWindow(View v) {
                v.getViewTreeObserver().removeOnDrawListener(this);
            }
            @Override public void onDraw() {
                if (submitted || !notice.hasWindowFocus() || !notice.isShown() || !notice.getGlobalVisibleRect(visible)
                        || visible.height() < notice.getHeight()) return;
                submitted = true;
                notice.post(() -> {
                    if (notice.isAttachedToWindow()) notice.getViewTreeObserver().removeOnDrawListener(this);
                    notice.removeOnAttachStateChangeListener(this);
                    Context context = a.getApplicationContext();
                    BetaGrowthStore.WORKER.execute(() -> {
                        try { BetaGrowthStore.acknowledge(context, account, displayedTotal); }
                        catch (Exception unavailable) { /* An unread receipt will be shown again on the next visit. */ }
                    });
                });
            }
        }
        notice.addOnAttachStateChangeListener(new Receipt());
        hero.addView(notice);
    }
    static View link(GrowthActivity a,String label,boolean dark,Runnable run){return BetaUi.row(a,label,"",dark,run);}
    static String time(android.content.Context c,long at){return java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.SHORT).withLocale(c.getResources().getConfiguration().getLocales().get(0)).withZone(BetaV4.ZONE).format(java.time.Instant.ofEpochMilli(at));}
    static void catalog(GrowthActivity a,JSONObject state,boolean dark,boolean title,int filter,String account){if(BetaUi.current(a,account))a.startActivity(new Intent(a,BetaCatalogActivity.class).putExtra("titles",title));}
    static void detail(GrowthActivity a,JSONObject state,String id,boolean dark,String account){if(!BetaUi.current(a,account))return;String[] entry=BetaCatalog.find(id);if(entry==null)return;int[] p=BetaCatalog.progress(state,id);if(a.detailDialog!=null)a.detailDialog.dismiss();a.detailDialog=new androidx.appcompat.app.AlertDialog.Builder(a).setTitle(BetaCatalog.name(id,BetaIdentity.name(a),BetaCopy.ko(a))).setMessage(entry[BetaCopy.ko(a)?3:4]+"\n"+Math.min(p[0],p[1])+" / "+p[1]+"\n"+BetaCopy.t(a,"decorative")).setPositiveButton(BetaCopy.t(a,"close"),null).create();a.detailDialog.show();}
    static void rules(GrowthActivity a,JSONObject state,boolean dark,String account){if(!BetaUi.current(a,account))return;StringBuilder text=new StringBuilder(BetaCopy.t(a,"mission_note")).append("\n\n").append(BetaCopy.t(a,"period_note")).append("\n\n").append(BetaCopy.t(a,"day_condition")).append("\n").append(BetaCopy.t(a,"weekly_condition")).append("\n").append(BetaCopy.t(a,"monthly_condition")).append("\n\n").append(BetaCopy.t(a,"total",LedgerUi.number(state.optLong("total"))));
        java.util.ArrayList<String> keys=new java.util.ArrayList<>();state.optJSONObject("rewards").keys().forEachRemaining(keys::add);keys.sort((x,y)->Long.compare(state.optJSONObject("rewards").optJSONObject(y).optLong("at"),state.optJSONObject("rewards").optJSONObject(x).optLong("at")));for(String key:keys.subList(0,Math.min(30,keys.size()))){JSONObject r=state.optJSONObject("rewards").optJSONObject(key);text.append("\n").append(time(a,r.optLong("at"))).append(" · +").append(r.optLong("xp")).append(" ").append(BetaCopy.t(a,"xp"));}
        if(a.detailDialog!=null)a.detailDialog.dismiss();TextView body=BetaUi.body(a,text.toString(),dark);body.setPadding(Ui.dp(a,20),Ui.dp(a,16),Ui.dp(a,20),Ui.dp(a,16));ScrollView scroll=new ScrollView(a);scroll.addView(body);a.detailDialog=new androidx.appcompat.app.AlertDialog.Builder(a).setTitle(BetaCopy.t(a,"rules")).setView(scroll).setPositiveButton(BetaCopy.t(a,"close"),null).create();a.detailDialog.show();}
    static void tier(GrowthActivity a,JSONObject journal,String account){if(!BetaUi.current(a,account))return;JSONObject state=journal.optJSONObject("rpg");StringBuilder text=new StringBuilder(BetaCopy.t(a,"tier_note"));for(String region:new String[]{"KR","WORLD"}){JSONObject fact=state==null?null:state.optJSONObject(region);int rank=fact==null?-1:fact.optInt("tier",-1);text.append("\n\n").append(region.equals("KR")?a.getString(R.string.rpg3_korea):a.getString(R.string.rpg3_world)).append(" · ").append(rank<0?BetaCopy.t(a,"grade_pending"):RpgGrowthUi.rank(a,rank));}if(a.detailDialog!=null)a.detailDialog.dismiss();a.detailDialog=new androidx.appcompat.app.AlertDialog.Builder(a).setTitle(BetaCopy.t(a,"tier")).setMessage(text.toString()).setPositiveButton(BetaCopy.t(a,"close"),null).create();a.detailDialog.show();}
}
