package dev.bennett.codexmeter;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
final class BetaUi {
    static int warning(boolean dark){return dark?0xffffbd7a:0xff8c3f00;}
    static LinearLayout linkedGroup(LinearLayout parent,String heading,String label,boolean dark,Runnable run){
        Context c=parent.getContext();LinearLayout card=group(parent,"",dark),header=new LinearLayout(c);
        boolean stack=c.getResources().getConfiguration().fontScale>1.3f;
        header.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=title(c,heading,dark);androidx.core.view.ViewCompat.setAccessibilityHeading(title,true);
        header.addView(title,new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1));
        Button action=action(c,label,false,dark,run);action.setMinWidth(Ui.dp(c,48));action.setMinimumWidth(Ui.dp(c,48));action.setTextSize(14);
        android.util.TypedValue ripple=new android.util.TypedValue();c.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);if(ripple.resourceId!=0)action.setBackgroundResource(ripple.resourceId);
        header.addView(action,new LinearLayout.LayoutParams(stack?-1:-2,-2));card.addView(header);return card;
    }
    static int accent(boolean dark){return dark?0xff8eb8ff:0xff2458a8;}
    static int accent(Context context,boolean dark){return TierTheme.accent(context,dark);}
    static TextView text(Context c,String label,int size,int color,boolean bold){TextView t=Ui.text(c,label,size,color);t.setSingleLine(false);t.setIncludeFontPadding(true);t.setLineSpacing(0,1.2f);if(bold)t.setTypeface(PretendardFont.semibold(c));return t;}
    static TextView title(Context c,String label,boolean dark){return text(c,label,20,Ui.mainText(dark),true);}
    static TextView body(Context c,String label,boolean dark){return text(c,label,15,Ui.mainText(dark),false);}
    static TextView note(Context c,String label,boolean dark){return text(c,label,14,LedgerUi.muted(dark),false);}
    static LinearLayout card(Context c,boolean dark){LinearLayout p=new LinearLayout(c);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(Ui.dp(c,18),Ui.dp(c,16),Ui.dp(c,18),Ui.dp(c,16));p.setBackground(LedgerUi.shape(c,Ui.cardColor(c,dark),18));p.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));return p;}
    static LinearLayout group(LinearLayout parent,String heading,boolean dark){if(parent.getChildCount()>0)Ui.addSpacer(parent,16);LinearLayout card=card(parent.getContext(),dark);if(!heading.isEmpty()){TextView title=title(parent.getContext(),heading,dark);androidx.core.view.ViewCompat.setAccessibilityHeading(title,true);card.addView(title);Ui.addSpacer(card,8);}parent.addView(card);return card;}
    static Button action(Context c,String label,boolean primary,boolean dark,Runnable run){Button b=Ui.button(c,label,primary,dark);b.setSingleLine(false);b.setTextSize(15);b.setIncludeFontPadding(true);b.setLineSpacing(0,1.35f);b.setMinHeight(Ui.dp(c,48));b.setMinimumHeight(Ui.dp(c,48));b.setPadding(Ui.dp(c,16),Ui.dp(c,12),Ui.dp(c,16),Ui.dp(c,12));b.setCompoundDrawables(null,null,null,null);b.setOnClickListener(v->run.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=Ui.dp(c,16);b.setLayoutParams(lp);return b;}
    static View row(Context c,String label,String value,boolean dark,Runnable click){LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,Ui.dp(c,10),0,Ui.dp(c,10));row.setMinimumHeight(Ui.dp(c,56));LinearLayout words=new LinearLayout(c);words.setOrientation(LinearLayout.VERTICAL);words.addView(body(c,label,dark));if(value!=null&&!value.isEmpty()){Ui.addSpacer(words,6);words.addView(note(c,value,dark));}row.addView(words,new LinearLayout.LayoutParams(0,-2,1));ImageView icon=new ImageView(c);icon.setImageDrawable(new Chevron(LedgerUi.muted(dark)));icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(c,24),Ui.dp(c,24)));words.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);row.setFocusable(true);row.setClickable(true);row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});row.setContentDescription(label+(value==null||value.isEmpty()?"":", "+value));android.util.TypedValue ripple=new android.util.TypedValue();c.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);if(ripple.resourceId!=0)row.setBackgroundResource(ripple.resourceId);row.setOnClickListener(v->click.run());return row;}
    static boolean current(android.app.Activity a,String key){return !a.isDestroyed()&&!a.isFinishing()&&BetaGrowthStore.current(a,key);}
    static void start(androidx.appcompat.app.AppCompatActivity a,String key,Runnable after){if(!current(a,key))return;new androidx.appcompat.app.AlertDialog.Builder(a).setTitle(BetaCopy.t(a,"transition")).setMessage(BetaCopy.t(a,"transition_note")).setNegativeButton(BetaCopy.t(a,"skip"),null).setPositiveButton(BetaCopy.t(a,"new_growth"),(d,w)->task(a,key,()->BetaGrowthStore.start(a,key)||BetaGrowthStore.activeWorker(a,key),after)).show();}
    interface Work {boolean run()throws Exception;}
    static void task(androidx.appcompat.app.AppCompatActivity a,String key,Work work,Runnable after){BetaGrowthStore.WORKER.execute(()->{boolean ok=false;try{if(BetaGrowthStore.current(a,key))ok=work.run();}catch(Exception ignored){}final boolean saved=ok;a.runOnUiThread(()->{if(!current(a,key))return;Toast.makeText(a,BetaCopy.t(a,saved?"saved":"failed"),Toast.LENGTH_SHORT).show();after.run();});});}
    static final class Chevron extends Drawable {final Paint p=new Paint(3);Chevron(int color){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);}public void draw(Canvas c){Rect b=getBounds();Path path=new Path();path.moveTo(b.left+b.width()*.35f,b.top+b.height()*.25f);path.lineTo(b.left+b.width()*.65f,b.top+b.height()*.5f);path.lineTo(b.left+b.width()*.35f,b.top+b.height()*.75f);c.drawPath(path,p);}public void setAlpha(int a){p.setAlpha(a);}public void setColorFilter(ColorFilter f){p.setColorFilter(f);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}}
}
