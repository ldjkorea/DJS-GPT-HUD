package dev.bennett.codexmeter;

import android.content.*;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.*;
import org.json.JSONObject;

/** Retained growth root. Full journals are read on a worker only. */
public final class GrowthActivity extends AppCompatActivity {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    private LinearLayout content; private boolean busy,dark; private String signature="",loadedAccount="",requestedAccount;
    androidx.appcompat.app.AlertDialog detailDialog;
    BetaGrowthModel betaGrowthModel;
    private int scroll; private long generation; private boolean receiverRegistered;
    private final BroadcastReceiver updates=new BroadcastReceiver(){public void onReceive(Context c,Intent i){load(false);}};
    @Override protected void onCreate(Bundle state){Ui.applySelectedTheme(this);super.onCreate(state);dark=Ui.isDark(this);
        content=Ui.installPage(this,getString(R.string.rpg3_growth),false).content;MatteNav.install(this,3);
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout refresh=findViewById(R.id.dashboard_refresh);refresh.setRefreshing(false);refresh.setEnabled(false);
        if(state!=null){scroll=state.getInt("growth_scroll");getIntent().putExtra("catalog",false).putExtra("growth_mode",state.getString("growth_mode","overview"));getIntent().putExtra("beta_period",state.getInt("beta_period",0));}
        String destination=mode();if(!destination.equals("overview")){getIntent().putExtra("catalog",false).putExtra("growth_mode","overview");content.post(()->RpgCollectionActivity.open(this,destination));}
        getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true){public void handleOnBackPressed(){if(!mode().equals("overview")){open("overview");}else MatteNav.open(GrowthActivity.this,1);}});
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);String destination=mode();getIntent().putExtra("catalog",false).putExtra("growth_mode","overview");if(!destination.equals("overview"))RpgCollectionActivity.open(this,destination);signature="";load(true);}
    @Override protected void onResume(){super.onResume();
        if(!receiverRegistered){androidx.core.content.ContextCompat.registerReceiver(this,updates,new IntentFilter(AppConstants.ACTION_USAGE_UPDATED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);receiverRegistered=true;}load(false);
    }
    @Override protected void onPause(){if(receiverRegistered){unregisterReceiver(updates);receiverRegistered=false;}super.onPause();}
    @Override protected void onDestroy(){generation++;if(detailDialog!=null)detailDialog.dismiss();super.onDestroy();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);android.view.View view=findViewById(R.id.dashboard_scroll);state.putInt("growth_scroll",view==null?scroll:view.getScrollY());state.putString("growth_mode",mode());state.putInt("beta_period",getIntent().getIntExtra("beta_period",0));}
    void load(boolean force){
        if(content==null)return;BetaMedalStore.refresh(this);String account=SubscriptionStore.accountKey(this);
        // Clear the old profile immediately, even while its worker is still running.
        if(!java.util.Objects.equals(account,requestedAccount)){
            generation++;busy=false;signature="";loadedAccount="";requestedAccount=account;scroll=0;
            if(detailDialog!=null)detailDialog.dismiss();betaGrowthModel=null;content.removeAllViews();
        }
        if(busy)return;
        String baseSignature=account+":"+mode()+":"+getSharedPreferences("codex_rpg_showcase_v1",0).getAll().hashCode()+":"+getSharedPreferences("codex_rpg_display_v1",0).getAll().hashCode()+":"+LiveUsageStore.signature(this,AppPreferences.loadSnapshot(this))+":"+AppPreferences.getLastError(this)+":"+AppPreferences.getAutomaticRefresh(this)+":"+MxpEconomy.date(System.currentTimeMillis());
        String next=baseSignature+":"+getSharedPreferences("beta_identity_v1",0).getAll().hashCode()+":"+getSharedPreferences("beta_growth_display_v4",0).getAll().hashCode()+":"+getSharedPreferences(BetaMedalStore.CACHE,0).getAll().hashCode();
        if(!force&&next.equals(signature)&&content.getChildCount()>0)return;
        if(account==null){if(detailDialog!=null)detailDialog.dismiss();loadedAccount="";generation++;signature=next;content.removeAllViews();content.addView(LedgerUi.caption(this,BetaCopy.t(this,"no_account"),dark));content.addView(BetaGrowthUi.link(this,BetaCopy.t(this,"account"),dark,()->MatteNav.open(this,1)));return;}
        busy=true;long ticket=++generation;
        if(content.getChildCount()==0)content.addView(LedgerUi.caption(this,getString(R.string.next_loading),dark));
        WORKER.execute(()->{JSONObject journal=null;java.util.List<String> unlocks=java.util.Collections.emptyList();boolean failed=false;
            try{journal=RpgProgressStore.export(getApplicationContext(),account);unlocks=RpgShowcase.observe(getApplicationContext(),account,journal==null?null:journal.optJSONObject("rpg"));}catch(Exception e){failed=true;}
            try{JSONObject beta=BetaGrowthStore.load(this,account);if(beta!=null){if(journal==null)journal=new JSONObject();journal.put("beta_v4",beta);}}catch(Exception unavailable){failed=true;}
            final BetaGrowthModel presentation=failed?null:BetaGrowthModel.create(journal,account,System.currentTimeMillis(),AppPreferences.getAutomaticRefresh(this),!AppPreferences.getLastError(this).isEmpty()||getSharedPreferences("beta_growth_display_v4",0).getBoolean(account+":error",false),TrialDemo.active(this));
            final JSONObject result=journal;final boolean error=failed;final java.util.List<String> earned=unlocks;
            runOnUiThread(()->{if(isDestroyed()||isFinishing()||ticket!=generation)return;busy=false;
                if(!account.equals(SubscriptionStore.accountKey(this))){if(detailDialog!=null)detailDialog.dismiss();signature="";load(true);return;}
                android.view.View view=findViewById(R.id.dashboard_scroll);if(content.getChildCount()>1&&view!=null)scroll=view.getScrollY();
                loadedAccount=account;betaGrowthModel=presentation;signature=next;content.removeAllViews();
                if(error){content.addView(LedgerUi.caption(this,BetaCopy.t(this,"error"),dark));content.addView(BetaGrowthUi.link(this,BetaCopy.t(this,"check_home"),dark,()->load(true)));}
                else {
                    if(!earned.isEmpty()){
                        android.widget.TextView banner=LedgerUi.badge(this,getString(R.string.growth_new_unlock,earned.size()),dark,false);banner.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);banner.setOnClickListener(v->open("achievements"));content.addView(banner);Ui.addSpacer(content,12);
                        if(android.provider.Settings.Global.getFloat(getContentResolver(),android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1)>0){banner.setAlpha(.4f);banner.animate().alpha(1).setDuration(450).start();}
                    }
                    RpgGrowthUi.render(this,content,result,dark,mode());
                }
                if(view!=null)view.post(()->view.scrollTo(0,scroll));
            });
        });
    }
    void betaCollections(JSONObject journal,LinearLayout target){LinearLayout saved=content;try{content=target;collections(journal);}finally{content=saved;}}
    void collections(JSONObject journal){
        JSONObject state=journal==null?null:journal.optJSONObject("rpg");
        LinearLayout savedContent=content;content=Ui.card(this,dark);LinearLayout settings=content;settings.setVisibility(android.view.View.GONE);
        savedContent.addView(LedgerUi.action(this,"수집 설정 · 보관함",false,dark,()->settings.setVisibility(settings.getVisibility()==android.view.View.GONE?android.view.View.VISIBLE:android.view.View.GONE)));savedContent.addView(settings);
        LedgerUi.section(content,getString(R.string.reborn_collections),dark);
        collectionSwitch(state,false);collectionSwitch(state,true);
        JSONObject journey=AchievementJourney.journey(state);if(journey!=null&&journey.optJSONObject("legacy")!=null){android.widget.Button button=LedgerUi.action(this,getString(R.string.reborn_archive),false,dark,()->archive(state));button.setTag("growth:archive");content.addView(button);}
        content=savedContent;
    }
    private void collectionSwitch(JSONObject state,boolean titles){
        JSONObject j=titles?TitleJourney.journey(state):AchievementJourney.journey(state);boolean enabled=titles?TitleJourney.enabled(state):AchievementJourney.enabled(state);long started=j==null?0:j.optLong("first_started_at");String expected=loadedAccount;
        androidx.appcompat.widget.SwitchCompat toggle=new androidx.appcompat.widget.SwitchCompat(this);toggle.setText(titles?R.string.reborn_title_switch:R.string.journey_switch);toggle.setTextColor(Ui.mainText(dark));toggle.setTextSize(16);toggle.setMinHeight(Ui.dp(this,48));toggle.setChecked(enabled);toggle.setTag(titles?"titles:switch":"journey:switch");content.addView(toggle);
        if(started>0&&!enabled)content.addView(LedgerUi.caption(this,getString(R.string.journey_paused),dark));
        if(MxpEconomy.state(state)==null&&enabled&&j!=null&&!j.optBoolean("official_armed"))content.addView(LedgerUi.caption(this,getString(R.string.reborn_baseline_pending),dark));
        toggle.setOnCheckedChangeListener((button,on)->{button.setOnCheckedChangeListener(null);button.setChecked(enabled);if(on){detailDialog=new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(started>0?R.string.journey_resume_question:titles?R.string.reborn_title_start:R.string.journey_start_question).setMessage(started>0?R.string.journey_resume_note:titles?R.string.reborn_title_note:R.string.journey_start_note).setNegativeButton(android.R.string.cancel,(d,w)->load(true)).setPositiveButton(R.string.journey_confirm,(d,w)->setCollectionEnabled(true,expected,titles)).setOnCancelListener(d->load(true)).create();detailDialog.show();}else setCollectionEnabled(false,expected,titles);});
    }
    private void setCollectionEnabled(boolean enabled,String account,boolean titles){if(busy||account.isEmpty()||!account.equals(SubscriptionStore.accountKey(this)))return;busy=true;WORKER.execute(()->{boolean ok=false;try{ok=titles?RpgProgressStore.setTitlesEnabled(getApplicationContext(),account,enabled):RpgProgressStore.setAchievementsEnabled(getApplicationContext(),account,enabled);}catch(Exception ignored){}final boolean saved=ok;runOnUiThread(()->{busy=false;if(isDestroyed()||!account.equals(SubscriptionStore.accountKey(this)))return;if(saved)sendBroadcast(new Intent(AppConstants.ACTION_USAGE_UPDATED).setPackage(getPackageName()));else android.widget.Toast.makeText(this,R.string.rpg3_failed,android.widget.Toast.LENGTH_SHORT).show();signature="";load(true);});});}
    private void archive(JSONObject state){if(state==null)return;try{JSONObject j=AchievementJourney.journey(state),facts=j.getJSONObject("legacy").getJSONObject("achievements"),old=new JSONObject(state.toString()).put("achievements",facts);android.widget.ScrollView scroll=new android.widget.ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(Ui.dp(this,20),Ui.dp(this,12),Ui.dp(this,20),Ui.dp(this,20));scroll.addView(body);body.addView(LedgerUi.caption(this,getString(R.string.reborn_archive_note),dark));int count=0;for(RpgCatalog.Entry e:RpgCatalog.ALL)if(facts.optJSONObject(e.id)!=null&&facts.optJSONObject(e.id).optBoolean("unlocked")){count++;LinearLayout row=RpgGrowthUi.compact(this,e,dark);row.setOnClickListener(v->RpgMedalUi.show(this,old,e,dark,false,null));body.addView(row);Ui.addSpacer(body,10);}if(count==0)body.addView(LedgerUi.caption(this,getString(R.string.growth_showcase_empty),dark));detailDialog=new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.reborn_archive).setView(scroll).setPositiveButton(R.string.growth_close,null).create();detailDialog.show();scroll.post(()->{int max=(int)(getResources().getDisplayMetrics().heightPixels*.7);if(scroll.getHeight()>max){scroll.getLayoutParams().height=max;scroll.requestLayout();}});}catch(Exception e){android.widget.Toast.makeText(this,R.string.rpg3_failed,android.widget.Toast.LENGTH_SHORT).show();}}
    String mode(){String value=getIntent().getStringExtra("growth_mode");return "titles".equals(value)?"titles":"achievements".equals(value)||getIntent().getBooleanExtra("catalog",false)?"achievements":"overview";}
    void open(String mode){if(busy)return;if(!mode.equals("overview")){RpgCollectionActivity.open(this,mode);return;}if(detailDialog!=null)detailDialog.dismiss();getIntent().putExtra("catalog",false).putExtra("growth_mode",mode);scroll=0;android.view.View view=findViewById(R.id.dashboard_scroll);if(view!=null)view.scrollTo(0,0);load(true);}
    void detail(String kind,JSONObject state){if(isFinishing()||isDestroyed()||!loadedAccount.equals(SubscriptionStore.accountKey(this)))return;if(detailDialog!=null)detailDialog.dismiss();detailDialog=RpgDetails.show(this,kind,state,dark);}
    void medalDetail(JSONObject state,RpgCatalog.Entry e){if(isFinishing()||isDestroyed()||!loadedAccount.equals(SubscriptionStore.accountKey(this)))return;if(detailDialog!=null)detailDialog.dismiss();detailDialog=RpgMedalUi.show(this,state,e,dark,false,null);}
    void chooseShowcase(JSONObject state){if(!busy&&loadedAccount.equals(SubscriptionStore.accountKey(this)))RpgCollectionActivity.open(this,"showcase");}
    void equip(String title){String account=SubscriptionStore.accountKey(this);if(account==null||busy)return;busy=true;
        WORKER.execute(()->{boolean saved=false;try{saved=RpgProgressStore.equip(getApplicationContext(),account,title);}catch(Exception ignored){}final boolean ok=saved;
            runOnUiThread(()->{busy=false;if(!isDestroyed()&&account.equals(SubscriptionStore.accountKey(this))){android.widget.Toast.makeText(this,ok?R.string.rpg3_saved:R.string.rpg3_failed,android.widget.Toast.LENGTH_SHORT).show();load(true);}});
        });
    }
    void recommend(){String account=SubscriptionStore.accountKey(this);if(account==null||busy)return;busy=true;
        WORKER.execute(()->{try{RpgProgressStore.recommend(getApplicationContext(),account);}catch(Exception ignored){}runOnUiThread(()->{busy=false;if(!isDestroyed()&&account.equals(SubscriptionStore.accountKey(this)))load(true);});});
    }
}
