package dev.bennett.codexmeter;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.*;

/** Compact labels and explicit local detail views. No database or reward work on the UI thread. */
final class BetaMedalUi {
    static String t(Context c,String ko,String en,Object... args){return BetaExperienceUi.t(c,ko,en,args);}
    static String grade(Context c,int grade){return grade==3?t(c,"금메달","Gold"):grade==2?t(c,"은메달","Silver"):grade==1?t(c,"동메달","Bronze"):t(c,"기록 메달","Recorded");}
    static int color(int grade,boolean dark){int color=grade==3?0xffe8ce91:grade==2?0xffd5e1eb:grade==1?0xffdfa778:0xffaee4ee;if(!dark){float[] hsv=new float[3];Color.colorToHSV(color,hsv);hsv[2]=.38f;color=Color.HSVToColor(hsv);}return color;}
    static String status(Context c,JSONObject today){return today==null||today.length()==0?t(c,"관측 대기","Waiting for observations"):today.optBoolean("active")?grade(c,today.optInt("grade")):today.optBoolean("measured")?t(c,"관측 증가 없음","No observed increase"):t(c,"기록 수집 중","Collecting observations");}
    static String name(Context c,String id){boolean ko=BetaCopy.ko(c);String user=BetaIdentity.name(c);
        switch(id){case "medal.days7":return t(c,"달력에 남긴 일곱 날","Seven recorded days");case "medal.month1":return t(c,"나의 첫 황금달","My first gold month");case "medal.month3":return t(c,"세 달에 새긴 금빛","Three gold months");case "medal.title1":return ko?"금빛 달력을 펼친 "+user:user+", Keeper of a Gold Calendar";case "medal.title3":return ko?"활동의 금빛을 석 달 모은 "+user:user+", Collector of Three Gold Activity Months";default:return "";}}
    static String equippedTitle(Context c,String fallback){JSONObject summary=BetaMedalStore.peek(c);String id=summary==null?"":summary.optString("equipped");return id.isEmpty()?fallback:name(c,id);}
    static int rarity(String rarity){switch(rarity){case "rare":return TierTheme.COLORS[6];case "epic":return TierTheme.COLORS[7];case "legendary":return TierTheme.COLORS[3];case "mythic":return TierTheme.COLORS[8];default:return TierTheme.COLORS[0];}}
    static int textColor(String rarity,boolean dark){int color=rarity(rarity);if(!dark){float[] hsv=new float[3];Color.colorToHSV(color,hsv);hsv[2]=.38f;color=Color.HSVToColor(hsv);}return color;}
    static int titleColor(Context c,String legacy,boolean dark){JSONObject summary=BetaMedalStore.peek(c);String selected=summary==null?"":summary.optString("equipped");if(!selected.isEmpty())return textColor(selected.endsWith("3")?"legendary":"epic",dark);String[] entry=BetaCatalog.find(legacy);return entry==null?Ui.mainText(dark):textColor(entry[6],dark);}
    static String rarityName(Context c,String rarity){switch(rarity){case "rare":return t(c,"희귀","Rare");case "epic":return t(c,"영웅","Epic");case "legendary":return t(c,"전설","Legendary");case "mythic":return t(c,"신화","Mythic");default:return t(c,"일반","Common");}}
    static void frame(View view,int color,boolean dark){GradientDrawable bg=new GradientDrawable();bg.setColor(dark?0xff1b1e24:0xfff8f9fc);bg.setCornerRadius(Ui.dp(view.getContext(),18));bg.setStroke(Ui.dp(view.getContext(),1),color);view.setBackground(bg);}
    static LinearLayout content(AppCompatActivity a){LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(Ui.dp(a,20),Ui.dp(a,16),Ui.dp(a,20),Ui.dp(a,16));return body;}
    static AlertDialog show(AppCompatActivity a,String title,LinearLayout body){return show(a,title,body,SubscriptionStore.accountKey(a));}
    static AlertDialog show(AppCompatActivity a,String title,LinearLayout body,String account){
        if(!java.util.Objects.equals(account,SubscriptionStore.accountKey(a))||a.isDestroyed()||a.isFinishing())return null;ScrollView scroll=new ScrollView(a);scroll.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle(title).setView(scroll).setPositiveButton(BetaCopy.t(a,"close"),null).create();
        BroadcastReceiver guard=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(!java.util.Objects.equals(account,SubscriptionStore.accountKey(a)))dialog.dismiss();}};
        androidx.core.content.ContextCompat.registerReceiver(a,guard,new IntentFilter(AppConstants.ACTION_USAGE_UPDATED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
        androidx.lifecycle.DefaultLifecycleObserver lifecycle=new androidx.lifecycle.DefaultLifecycleObserver(){public void onDestroy(androidx.lifecycle.LifecycleOwner owner){dialog.dismiss();}};
        a.getLifecycle().addObserver(lifecycle);dialog.setOnDismissListener(d->{a.unregisterReceiver(guard);a.getLifecycle().removeObserver(lifecycle);});dialog.show();return dialog;
    }
    static void day(AppCompatActivity a,String date,JSONObject row,boolean dark){day(a,date,row,dark,SubscriptionStore.accountKey(a));}
    static void day(AppCompatActivity a,String date,JSONObject row,boolean dark,String expected){
        if(!java.util.Objects.equals(expected,SubscriptionStore.accountKey(a)))return;
        LinearLayout body=content(a);body.addView(BetaUi.title(a,status(a,row),dark));
        if(TrialDemo.active(a))body.addView(BetaUi.note(a,BetaCopy.t(a,"demo"),dark));
        if(row!=null){
            body.addView(BetaUi.body(a,t(a,"관측 증가량 %s%%p","Observed increase %s percentage points",UsagePrecision.number(row.optDouble("points"))),dark));
            if(row.optBoolean("mixed"))body.addView(BetaUi.note(a,t(a,"한도 기준이 바뀐 날 · 기존 메달 보존, 추가 등급 계산 중지","Quota policy changed · earned medal retained; further grading paused today"),dark));
            if(row.optBoolean("uncertain"))body.addView(BetaUi.note(a,t(a,"관측 보정 감지 · 기존 메달 보존, 오늘 추가 평가 보류","Observation correction detected · earned medal retained; further grading paused today"),dark));
            body.addView(BetaUi.row(a,t(a,"기록 상세","Record details"),"",dark,()->{
                LinearLayout details=content(a);details.addView(BetaUi.body(a,t(a,"관측 %d회 · %s","%d observations · %s",row.optInt("samples"),row.optString("policy")),dark));
                details.addView(BetaUi.note(a,t(a,"본인의 과거 관측 기준 %s%%p · 최소7일 필요. 토큰 수·업무 성과·전체 사용량이 아닙니다.","Personal observed reference %s percentage points · at least 7 prior days required. Not token counts, work quality or complete usage.",UsagePrecision.number(row.optDouble("reference"))),dark));
                details.addView(BetaUi.note(a,t(a,"첫 관측 %s\n최근 관측 %s","First observed %s\nLast observed %s",V3Display.time(row.optLong("first")),V3Display.time(row.optLong("last"))),dark));
                if(row.optBoolean("restored"))details.addView(BetaUi.note(a,t(a,"백업에서 보존된 기록","Record retained from backup"),dark));show(a,date,details,expected);
            }));
        }else body.addView(BetaUi.note(a,t(a,"관측이 없다는 이유로 사용하지 않았다고 판단하지 않습니다.","Missing observations do not prove inactivity."),dark));
        show(a,date,body,expected);
    }
    static void legend(AppCompatActivity a,boolean dark){
        LinearLayout body=content(a);
        body.addView(BetaUi.body(a,t(a,"금·은·동: 획득한 메달\n기록 메달: 기준을 모으는 중에도 남기는 활동 기록\n—: 관측 자료 없음\n0: 관측된 구간의 증가 없음\n·: 기록 수집 중\n빈 날짜: 아직 오지 않은 날","Gold/silver/bronze: earned medal\nRecording medal: activity recorded while a personal reference is being built\n—: no observations\n0: no increase in the observed interval\n·: collecting observations\nBlank: upcoming date"),dark));
        body.addView(BetaUi.note(a,t(a,"관측 자료 없음은 사용하지 않았다는 뜻이 아닙니다.","Missing observations do not prove inactivity."),dark));
        body.addView(BetaUi.row(a,t(a,"주간·월간 메달 조건","Weekly and monthly conditions"),"",dark,()->periods(a,dark)));
        show(a,t(a,"기호·메달 기준","Symbols and medal rules"),body);
    }
    static void periods(AppCompatActivity a,boolean dark){JSONObject summary=BetaMedalStore.peek(a);LinearLayout body=content(a);
        body.addView(BetaUi.body(a,t(a,"주간3·4·5일 → 동·은·금\n월간7·10·14일 → 동·은·금","Weekly 3/4/5 days → bronze/silver/gold\nMonthly 7/10/14 days → bronze/silver/gold"),dark));
        body.addView(BetaUi.note(a,t(a,"양의 사용량 변화가 확인된 날짜를 셉니다. 초기 기록 메달도 포함하며 하루는 한 번만 집계합니다. 날짜는 한국시간, 주간은 월요일부터입니다. 한도의 초기화 주기와 별개입니다. 메달은 XP나 필수 승급 조건을 추가하지 않습니다.","Counts dates with positive observed usage changes, including ungraded recording medals. One count per day, Korea time, Monday weeks; separate from quota reset cycles. Medals add no XP or required promotion conditions."),dark));
        if(summary!=null)body.addView(BetaUi.body(a,t(a,"이번 주 %d일 · 이번 달 %d일","This week %d days · this month %d days",summary.optInt("week"),summary.optInt("month")),dark));
        show(a,t(a,"기간별 메달","Period medals"),body,summary==null?SubscriptionStore.accountKey(a):summary.optString("account"));
    }
    static int collection(AppCompatActivity a,LinearLayout parent,boolean dark,boolean titles,int filter){JSONObject summary=BetaMedalStore.peek(a);if(summary==null)return 0;
        String expected=summary.optString("account");JSONObject facts=summary.optJSONObject(titles?"titles":"honors");if(facts==null)return 0;int visible=HudUiModel.visibleMedals(facts,titles?BetaMedalModel.TITLES:BetaMedalModel.HONORS,filter);if(visible==0)return 0;LinearLayout group=BetaUi.group(parent,t(a,"활동 메달로 얻는 명예","Honors from activity medals"),dark);
        for(String id:titles?BetaMedalModel.TITLES:BetaMedalModel.HONORS){boolean owned=facts.has(id);if(filter==1&&!owned||filter==2&&owned)continue;
            String rule=t(a,"활동 메달 · ","Activity medals · ")+(id.endsWith("days7")?t(a,"사용량 증가 기록7일","7 dates with observed increases"):id.endsWith("3")?t(a,"월간 금메달3회","3 monthly gold medals"):t(a,"월간 금메달1회","1 monthly gold medal"));
            View row=BetaUi.row(a,name(a,id),rule+" · "+(owned?BetaCopy.t(a,"earned"):BetaCopy.t(a,"locked")),dark,()->{
                if(!BetaGrowthStore.current(a,expected))return;LinearLayout detail=content(a);detail.addView(BetaUi.body(a,rule,dark));if(owned)detail.addView(BetaUi.note(a,V3Display.time(facts.optJSONObject(id).optLong("first")),dark));
                if(titles&&owned)detail.addView(BetaUi.action(a,summary.optString("equipped").equals(id)?BetaCopy.t(a,"unequip"):BetaCopy.t(a,"equip"),true,dark,()->{
                    String account=expected;BetaMedalStore.WORKER.execute(()->{try{BetaMedalStore.equip(a,account,summary.optString("equipped").equals(id)?"":id);a.runOnUiThread(a::recreate);}catch(Exception failed){a.runOnUiThread(()->Toast.makeText(a,BetaCopy.t(a,"failed"),Toast.LENGTH_LONG).show());}});
                }));show(a,name(a,id),detail,expected);
            });row.setTag("beta9911:collectible:"+id);if(owned)frame(row,rarity(id.endsWith("days7")?"rare":id.endsWith("3")?"legendary":"epic"),dark);group.addView(row);
        }return visible;
    }
    static final class Mark extends Drawable {
        final Paint paint=new Paint(3);final int grade;final boolean active,dark;final String glyph;
        Mark(Context context,int grade,boolean active,boolean dark){this.grade=grade;this.active=active;this.dark=dark;glyph=grade==3?t(context,"금","G"):grade==2?t(context,"은","S"):grade==1?t(context,"동","B"):grade==-2?"0":"·";}
        public void draw(Canvas c){Rect b=getBounds();float cx=b.exactCenterX(),cy=b.exactCenterY(),r=Math.min(b.width(),b.height())*.30f;paint.setColor(active?color(grade,dark):0xff66717b);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,r*.12f));
            if(grade==-4)return;if(grade==-1){c.drawLine(cx-r*.4f,cy,cx+r*.4f,cy,paint);return;}
            if(active){Path ribbon=new Path();ribbon.moveTo(cx-r*.55f,cy+r*.65f);ribbon.lineTo(cx-r*.65f,cy+r*1.45f);ribbon.lineTo(cx,cy+r*1.15f);ribbon.lineTo(cx+r*.65f,cy+r*1.45f);ribbon.lineTo(cx+r*.55f,cy+r*.65f);c.drawPath(ribbon,paint);paint.setStyle(Paint.Style.FILL);paint.setAlpha(35);c.drawCircle(cx,cy,r,paint);paint.setAlpha(255);paint.setStyle(Paint.Style.STROKE);}
            c.drawCircle(cx,cy,r,paint);paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(r*1.2f);paint.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(glyph,cx,cy+r*.4f,paint);
        }
        public void setAlpha(int a){paint.setAlpha(a);}public void setColorFilter(ColorFilter f){paint.setColorFilter(f);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
