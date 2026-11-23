package dev.bennett.codexmeter;
import android.app.Dialog;import android.os.Bundle;import android.content.*;import androidx.appcompat.app.*;import org.json.*;
/** Saved fragment, cached grade evidence, and account guard across dismissal/rotation. */
public final class BetaTierDialog extends AppCompatDialogFragment {
 String account;boolean registered;final BroadcastReceiver changed=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(!BetaGrowthStore.current(c,account))dismissAllowingStateLoss();}};
 static void show(AppCompatActivity a){if(a.getSupportFragmentManager().findFragmentByTag("beta-tier-detail")!=null)return;BetaTierDialog d=new BetaTierDialog();Bundle args=new Bundle();args.putString("account",SubscriptionStore.accountKey(a));d.setArguments(args);d.show(a.getSupportFragmentManager(),"beta-tier-detail");}
 @Override public Dialog onCreateDialog(Bundle saved){
     AppCompatActivity a=(AppCompatActivity)requireActivity();
     account=requireArguments().getString("account");
     boolean current=BetaGrowthStore.current(a,account);
     boolean dark=Ui.isDark(a);
     android.widget.LinearLayout body=BetaMedalUi.content(a);
     int tier=current?TierTheme.tier(a):-1;
     if(tier>=0&&tier<TierTheme.EMBLEMS.length){
         android.widget.ImageView crest=new android.widget.ImageView(a);
         crest.setImageResource(TierTheme.EMBLEMS[tier]);
         crest.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
         crest.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
         body.addView(crest,new android.widget.LinearLayout.LayoutParams(-1,Ui.dp(a,104)));
         android.widget.TextView name=BetaUi.text(a,a.getString(TierPresentation.NAMES[tier]),18,TierTheme.accent(tier,dark),true);
         name.setGravity(android.view.Gravity.CENTER);
         body.addView(name);
         Ui.addSpacer(body,12);
     }
     body.addView(BetaUi.note(a,BetaCopy.t(a,"tier_note"),dark));
     JSONObject doc=current?RpgProgressStore.peek(a):null;
     JSONObject rpg=doc==null?null:doc.optJSONObject("rpg");
     for(String region:new String[]{"KR","WORLD"}){
         JSONObject fact=rpg==null?null:rpg.optJSONObject(region);
         int rank=fact==null?-1:fact.optInt("tier",-1);
         String value=rank<0||rank>=TierPresentation.NAMES.length?BetaCopy.t(a,"grade_pending"):a.getString(TierPresentation.NAMES[rank]);
         Ui.addSpacer(body,10);
         body.addView(BetaUi.body(a,a.getString(region.equals("KR")?R.string.rpg3_korea:R.string.rpg3_world)+" · "+value,dark));
     }
     android.widget.ScrollView scroll=new android.widget.ScrollView(a);
     scroll.addView(body);
     return new AlertDialog.Builder(a).setTitle(BetaCopy.t(a,"tier")).setView(scroll)
             .setPositiveButton(BetaCopy.t(a,"close"),null).create();
 }
 @Override public void onResume(){super.onResume();if(!BetaGrowthStore.current(requireContext(),account)){dismissAllowingStateLoss();return;}androidx.core.content.ContextCompat.registerReceiver(requireContext(),changed,new IntentFilter(AppConstants.ACTION_USAGE_UPDATED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;}
 @Override public void onPause(){if(registered){requireContext().unregisterReceiver(changed);registered=false;}super.onPause();}
}
