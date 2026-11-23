package dev.bennett.codexmeter;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;
import org.json.*;

/** Compact labels and explicit local detail views. No database or reward work on the UI thread. */
final class BetaMedalUi {
    static String t(Context c,String ko,String en,Object... args){return BetaExperienceUi.t(c,ko,en,args);}
    static String grade(Context c,int grade){return grade==3?t(c,"금메달","Gold"):grade==2?t(c,"은메달","Silver"):grade==1?t(c,"동메달","Bronze"):t(c,"기록 메달","Recorded");}
    static int color(int grade, boolean dark) {
        switch (grade) {
            case 3: return dark ? 0xffffd16f : 0xff785006;
            case 2: return dark ? 0xffbbd8f1 : 0xff43617b;
            case 1: return dark ? 0xfff3a783 : 0xff93482a;
            default: return dark ? 0xff8ddcc9 : 0xff186955;
        }
    }

    static int markGrade(JSONObject row, boolean future) {
        if (future) return -4;
        if (row == null) return -1;
        if (row.optBoolean("active")) return row.optInt("grade");
        return row.optBoolean("measured") ? -2 : -3;
    }

    static ImageView mark(Context c, int grade, boolean dark) {
        ImageView image = new ImageView(c);
        image.setImageDrawable(new Mark(c, grade, grade >= 0, dark));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return image;
    }
    static String status(Context c,JSONObject today){return today==null||today.length()==0?t(c,"관측 대기","Waiting for observations"):today.optBoolean("active")?grade(c,today.optInt("grade")):today.optBoolean("measured")?t(c,"관측 증가 없음","No observed increase"):t(c,"기록 수집 중","Collecting observations");}
    static String recordStatus(Context c, LocalDate date, LocalDate today, JSONObject row) {
        if (date.isAfter(today)) return t(c, "예정", "Upcoming");
        if (date.isBefore(today)) {
            if (row == null) return t(c, "관측 자료 없음", "No observations");
            if (!row.optBoolean("active") && !row.optBoolean("measured")) {
                return t(c, "비교 자료 부족", "Too few comparable observations");
            }
        }
        return status(c, row);
    }
    static String observedIncrease(Context c, JSONObject row) {
        double points = row.optDouble("points", 0);
        if (!row.optBoolean("measured") && points <= 0) {
            return t(c, "증가량을 계산할 비교 관측이 없어요.", "No comparable observations for an increase.");
        }
        return t(c, "관측 증가량 %s%%p", "Observed increase %s percentage points", UsagePrecision.number(points));
    }
    static String personalReference(Context c, JSONObject row) {
        double reference = row.optDouble("reference");
        if (reference > 0) {
            return t(c, "이 날의 개인 기준 %s%%p", "Personal reference for this date: %s percentage points", UsagePrecision.number(reference));
        }
        return t(c, "이 날은 개인 기준이 없어요. 최근 28일 중 같은 한도에서 증가가 확인된 날이 7일 필요해요.",
                "No personal reference for this date. At least 7 days with an observed increase under the same quota policy in the previous 28 days are required.");
    }
    static String name(Context c,String id){boolean ko=BetaCopy.ko(c);String user=BetaIdentity.name(c);
        switch(id){case "medal.days7":return t(c,"달력에 남긴 일곱 날","Seven recorded days");case "medal.month1":return t(c,"나의 첫 황금달","My first gold month");case "medal.month3":return t(c,"세 달에 새긴 금빛","Three gold months");case "medal.title1":return ko?"금빛 달력을 펼친 "+user:user+", Keeper of a Gold Calendar";case "medal.title3":return ko?"활동의 금빛을 석 달 모은 "+user:user+", Collector of Three Gold Activity Months";default:return "";}}
    static String equippedTitle(Context c,String fallback){JSONObject summary=BetaMedalStore.peek(c);String id=summary==null?"":summary.optString("equipped");return id.isEmpty()?fallback:name(c,id);}
    static int rarity(String rarity){switch(rarity){case "rare":return TierTheme.COLORS[6];case "epic":return TierTheme.COLORS[7];case "legendary":return TierTheme.COLORS[3];case "mythic":return TierTheme.COLORS[8];default:return TierTheme.COLORS[0];}}
    static int textColor(String rarity,boolean dark){int color=rarity(rarity);if(!dark){float[] hsv=new float[3];Color.colorToHSV(color,hsv);hsv[2]=.38f;color=Color.HSVToColor(hsv);}return color;}
    static int titleColor(Context c,String legacy,boolean dark){JSONObject summary=BetaMedalStore.peek(c);String selected=summary==null?"":summary.optString("equipped");if(!selected.isEmpty())return textColor(selected.endsWith("3")?"legendary":"epic",dark);String[] entry=BetaCatalog.find(legacy);return entry==null?Ui.mainText(dark):textColor(entry[6],dark);}
    static String rarityName(Context c,String rarity){switch(rarity){case "rare":return t(c,"희귀","Rare");case "epic":return t(c,"영웅","Epic");case "legendary":return t(c,"전설","Legendary");case "mythic":return t(c,"신화","Mythic");default:return t(c,"일반","Common");}}
    static void frame(View view,int color,boolean dark){GradientDrawable bg=LedgerUi.shape(view.getContext(),androidx.core.graphics.ColorUtils.blendARGB(Ui.cardColor(view.getContext(),dark),color,.07f),16);bg.setStroke(Ui.dp(view.getContext(),1),color);view.setBackground(bg);}
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
        LinearLayout body=content(a);
        LocalDate selected = LocalDate.parse(date), today = BetaV4.day(System.currentTimeMillis());
        symbolRow(body, markGrade(row, selected.isAfter(today)), recordStatus(a,selected,today,row), dark);
        if(TrialDemo.active(a))body.addView(BetaUi.note(a,BetaCopy.t(a,"demo"),dark));
        if(row!=null){
            body.addView(BetaUi.body(a,observedIncrease(a,row),dark));
            if(row.optBoolean("mixed"))body.addView(BetaUi.note(a,t(a,"한도 기준이 바뀐 날 · 기존 메달 보존, 추가 등급 계산 중지","Quota policy changed · earned medal retained; further grading paused for this date"),dark));
            if(row.optBoolean("uncertain"))body.addView(BetaUi.note(a,t(a,"관측 보정 감지 · 기존 메달 보존, 해당 날짜 추가 평가 보류","Observation correction detected · earned medal retained; further grading paused for this date"),dark));
            body.addView(BetaUi.row(a,t(a,"기록 상세","Record details"),"",dark,()->{
                LinearLayout details=content(a);details.addView(BetaUi.body(a,t(a,"관측 %d회 · %s","%d observations · %s",row.optInt("samples"),row.optString("policy")),dark));
                details.addView(BetaUi.note(a,personalReference(a,row),dark));
                details.addView(BetaUi.note(a,t(a,"첫 관측 %s\n최근 관측 %s","First observed %s\nLast observed %s",V3Display.time(row.optLong("first")),V3Display.time(row.optLong("last"))),dark));
                if(row.optBoolean("restored"))details.addView(BetaUi.note(a,t(a,"백업에서 보존된 기록","Record retained from backup"),dark));show(a,date,details,expected);
            }));
        }else body.addView(BetaUi.note(a,t(a,"관측이 없다는 이유로 사용하지 않았다고 판단하지 않습니다.","Missing observations do not prove inactivity."),dark));
        show(a,date,body,expected);
    }
    static void legend(AppCompatActivity a,boolean dark){
        LinearLayout body=content(a);
        for (int grade = 3; grade >= 1; grade--) symbolRow(body, grade, grade(a,grade), dark);
        symbolRow(body, 0, t(a,"기록 메달 · 개인 기준을 모으는 중","Recorded activity · building a personal baseline"), dark);
        symbolRow(body, -1, t(a,"관측 자료 없음","No observations"), dark);
        symbolRow(body, -2, t(a,"관측 구간에서 증가 없음","No increase in the observed interval"), dark);
        symbolRow(body, -3, t(a,"비교 관측이 더 필요해요","More observations needed"), dark);
        Ui.addSpacer(body, 8);
        body.addView(BetaUi.note(a,t(a,"일간 메달은 같은 한도에서 쌓은 내 기록을 기준으로 매겨요. 관측 자료가 없는 날의 실제 사용량은 알 수 없어요.","Daily medals use your personal history under the same quota policy. Usage is unknown on dates without observations."),dark));
        body.addView(BetaUi.row(a,t(a,"주간·월간 메달 조건","Weekly and monthly conditions"),"",dark,()->periods(a,dark)));
        show(a,t(a,"기호·메달 기준","Symbols and medal rules"),body);
    }
    static void periods(AppCompatActivity a,boolean dark){JSONObject summary=BetaMedalStore.peek(a);LinearLayout body=content(a);
        symbolRow(body, 1, t(a,"동메달 · 한 주 3일 또는 한 달 7일","Bronze · 3 days in a week or 7 in a month"),dark);
        symbolRow(body, 2, t(a,"은메달 · 한 주 4일 또는 한 달 10일","Silver · 4 days in a week or 10 in a month"),dark);
        symbolRow(body, 3, t(a,"금메달 · 한 주 5일 또는 한 달 14일","Gold · 5 days in a week or 14 in a month"),dark);
        Ui.addSpacer(body, 8);
        body.addView(BetaUi.note(a,t(a,"사용량 증가가 확인된 날마다 도장 한 칸이 채워져요. 기록 메달도 한 칸으로 셉니다.\n\n한국 시간 기준으로 한 주는 월요일, 한 달은 1일에 시작해요. 한도 초기화 일정과 별개이며 메달은 XP·승급 조건에 영향을 주지 않아요.","Each day with an observed increase fills one space, including days with a recording medal.\n\nWeeks begin on Monday and months on the first, Korea time. Quota reset dates follow their own schedule. These medals do not affect XP or level requirements."),dark));
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
    private static void symbolRow(LinearLayout body, int grade, String label, boolean dark) {
        Context c = body.getContext();
        LinearLayout row = new LinearLayout(c);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(Ui.dp(c,40));
        LinearLayout.LayoutParams icon = new LinearLayout.LayoutParams(Ui.dp(c,36),Ui.dp(c,36));
        icon.setMarginEnd(Ui.dp(c,10));
        row.addView(mark(c,grade,dark),icon);
        row.addView(BetaUi.body(c,label,dark),new LinearLayout.LayoutParams(0,-2,1));
        body.addView(row);
    }

    /** One seal for the date tile, its legend and the next-goal preview. */
    static final class Mark extends Drawable {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int grade, metal, ink, muted;
        final boolean active;
        final String glyph;
        final Path ribbons = new Path();
        int alpha = 255;
        Mark(Context c,int grade,boolean active,boolean dark) {
            this.grade=grade;
            this.active=active;
            metal = color(grade,dark);
            ink = dark ? 0xff193034 : 0xfffffbf5;
            muted = Ui.secondaryText(dark);
            glyph = grade==3?t(c,"금","G"):grade==2?t(c,"은","S"):grade==1?t(c,"동","B"):"";
            paint.setTypeface(PretendardFont.bold(c));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStrokeCap(Paint.Cap.ROUND);
            ribbons.moveTo(5,14); ribbons.lineTo(3,22); ribbons.lineTo(7.2f,20.5f); ribbons.lineTo(9.3f,23); ribbons.lineTo(12,16); ribbons.close();
            ribbons.moveTo(12,16); ribbons.lineTo(15.5f,22.5f); ribbons.lineTo(17,20); ribbons.lineTo(21,21.5f); ribbons.lineTo(18,14); ribbons.close();
        }
        @Override public void draw(Canvas c) {
            if (grade == -4) return;
            Rect b = getBounds();
            float scale = Math.min(b.width(),b.height()) / 24f;
            c.save();
            c.translate(b.exactCenterX()-12*scale,b.exactCenterY()-12*scale);
            c.scale(scale,scale);
            paint.setStyle(Paint.Style.FILL);
            if (active) {
                useColor(metal);
                c.drawPath(ribbons,paint);
                c.drawCircle(12,10,8.6f,paint);
                useColor(ink);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(.85f);
                c.drawCircle(12,10,6.8f,paint);
                paint.setStyle(Paint.Style.FILL);
                if (grade > 0) {
                    paint.setTextSize(9.5f);
                    c.drawText(glyph,12,10-(paint.ascent()+paint.descent())/2,paint);
                } else {
                    c.drawCircle(12,10,2.4f,paint);
                }
            } else {
                useColor(muted);
                paint.setStrokeWidth(1.5f);
                if (grade == -1) c.drawLine(9,12,15,12,paint);
                else if (grade == -2) {
                    paint.setTextSize(13);
                    c.drawText("0",12,12-(paint.ascent()+paint.descent())/2,paint);
                } else c.drawCircle(12,12,1.65f,paint);
            }
            c.restore();
        }
        @Override public int getIntrinsicWidth() { return 24; }
        @Override public int getIntrinsicHeight() { return 24; }
        private void useColor(int color) { paint.setColor(color); paint.setAlpha(alpha); }
        @Override public void setAlpha(int alpha) { this.alpha=alpha; invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
