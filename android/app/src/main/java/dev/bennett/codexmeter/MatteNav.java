package dev.bennett.codexmeter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

/** Two visible roots; old analysis intents still route into records. */
final class MatteNav {
    private MatteNav(){}
    static void install(AppCompatActivity activity,int selected){
        installSettings(activity);
        FrameLayout container=activity.findViewById(R.id.matte_bottom);if(container==null)return;
        boolean dark=Ui.isDark(activity);container.setVisibility(View.VISIBLE);container.setBackgroundColor(Ui.background(activity,dark));
        LinearLayout bar=new LinearLayout(activity);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setPadding(Ui.dp(activity,8),Ui.dp(activity,4),Ui.dp(activity,8),Ui.dp(activity,4));bar.setBackground(LedgerUi.shape(activity,Ui.cardColor(activity,dark),18));
        int[] labels={R.string.matte_records,R.string.matte_home,R.string.rpg3_growth};int[] icons={R.drawable.matte_records,R.drawable.matte_home,R.drawable.rpg_growth};
        int[] roots={2,1,3};selected=selected==0?2:selected;final int active=selected;
        for(int i=0;i<3;i++){final int index=roots[i];Button button=Ui.button(activity,activity.getString(labels[i]),false,dark);
            button.setTextSize(12);button.setSingleLine(false);button.setIncludeFontPadding(true);button.setLineSpacing(0,1.35f);button.setMinHeight(Ui.dp(activity,64));button.setMinimumHeight(Ui.dp(activity,64));button.setPadding(Ui.dp(activity,6),Ui.dp(activity,8),Ui.dp(activity,6),Ui.dp(activity,8));button.setSelected(index==active);
            button.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf((0x33000000|(Ui.accent(activity,dark)&0x00ffffff))),index==active?LedgerUi.shape(activity,Ui.controlSurface(activity,dark),12):null,null));
            button.setTextColor(index==active?Ui.accent(activity,dark):Ui.secondaryText(dark));button.setCompoundDrawablesRelativeWithIntrinsicBounds(0,icons[i],0,0);button.setCompoundDrawablePadding(Ui.dp(activity,4));
            androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(button,ColorStateList.valueOf(index==active?Ui.accent(activity,dark):Ui.secondaryText(dark)));
            button.setContentDescription(activity.getString(labels[i]));if(index==active)androidx.core.view.ViewCompat.setStateDescription(button,activity.getString(R.string.matte_selected));button.setOnClickListener(v->{if(index!=active)open(activity,index);});
            bar.addView(button,new LinearLayout.LayoutParams(0,-2,1));
        }container.removeAllViews();container.addView(bar);
        activity.getOnBackPressedDispatcher().addCallback(activity,new OnBackPressedCallback(true){public void handleOnBackPressed(){if(active==1)activity.moveTaskToBack(true);else open(activity,1);}});
    }
    private static void installSettings(AppCompatActivity activity){
        dev.oneuiproject.oneui.layout.ToolbarLayout layout=activity.findViewById(R.id.toolbar_layout);
        if(layout==null||layout.getToolbar().findViewWithTag("beta9915:settings")!=null)return;
        android.widget.ImageButton button=new android.widget.ImageButton(activity);
        button.setTag("beta9915:settings");button.setContentDescription(activity.getString(R.string.matte_settings));
        button.setTooltipText(activity.getString(R.string.matte_settings));
        button.setImageResource(R.drawable.hud_settings);button.setImageTintList(ColorStateList.valueOf(Ui.mainText(Ui.isDark(activity))));
        button.setPadding(Ui.dp(activity,12),Ui.dp(activity,12),Ui.dp(activity,12),Ui.dp(activity,12));
        android.util.TypedValue ripple=new android.util.TypedValue();activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,ripple,true);
        if(ripple.resourceId!=0)button.setBackgroundResource(ripple.resourceId);
        button.setOnClickListener(v->activity.startActivity(new Intent(activity,MeterSettingsActivity.class)));
        layout.getToolbar().addView(button,new androidx.appcompat.widget.Toolbar.LayoutParams(Ui.dp(activity,48),Ui.dp(activity,48),android.view.Gravity.END|android.view.Gravity.CENTER_VERTICAL));
    }
    static void open(AppCompatActivity activity,int index){
        Class<?> target=index==3?GrowthActivity.class:index==0||index==2?RecordsActivity.class:MainActivity.class;
        activity.startActivity(new Intent(activity,target).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }
    static void homeAction(AppCompatActivity activity,String action){activity.startActivity(new Intent(activity,MainActivity.class).putExtra("matte_action",action).putExtra("matte_return",activity instanceof RecordsActivity||activity instanceof MeterSettingsActivity?2:1).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP));}
}
