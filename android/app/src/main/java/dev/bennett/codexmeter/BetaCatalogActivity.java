package dev.bennett.codexmeter;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import androidx.appcompat.app.*;
public final class BetaCatalogActivity extends AppCompatActivity {
    LinearLayout body;JSONObject state;boolean dark,titles;int filter=1,category=-1;String account;long generation;AlertDialog dialog;boolean registered,loading,loadFailed,medalLoadFailed,saving;
    final BroadcastReceiver changed=new BroadcastReceiver(){public void onReceive(Context c,Intent i){load();}};
    @Override protected void onCreate(Bundle saved){Ui.applySelectedTheme(this);super.onCreate(saved);dark=Ui.isDark(this);titles=getIntent().getBooleanExtra("titles",false);if(saved!=null){filter=saved.getInt("filter",1);category=saved.getInt("category",-1);}body=Ui.installPage(this,BetaCopy.t(this,titles?"titles":"honors"),true).content;androidx.swiperefreshlayout.widget.SwipeRefreshLayout refresh=findViewById(R.id.dashboard_refresh);refresh.setEnabled(false);refresh.setRefreshing(false);}
    @Override protected void onResume(){super.onResume();if(!registered){androidx.core.content.ContextCompat.registerReceiver(this,changed,new IntentFilter(AppConstants.ACTION_USAGE_UPDATED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;}load();}
    @Override protected void onPause(){if(registered){unregisterReceiver(changed);registered=false;}super.onPause();}
    @Override protected void onDestroy(){generation++;if(dialog!=null)dialog.dismiss();super.onDestroy();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("filter",filter);out.putInt("category",category);}
    void load(){
        final String expected=SubscriptionStore.accountKey(this);
        if(loading&&java.util.Objects.equals(expected,account))return;
        if(!java.util.Objects.equals(expected,account)){account=expected;state=null;loadFailed=false;medalLoadFailed=false;saving=false;if(dialog!=null)dialog.dismiss();}
        final long ticket=++generation;
        if(expected==null){loading=false;render();return;}
        loading=true;if(state==null||loadFailed||medalLoadFailed)render();
        BetaGrowthStore.WORKER.execute(()->{
            JSONObject loaded=null;boolean growthError=false,medalError=false;
            try{loaded=BetaGrowthStore.load(getApplicationContext(),expected);}catch(Exception unavailable){growthError=true;}
            try{BetaMedalStore.load(getApplicationContext(),expected);}catch(Exception unavailable){medalError=true;}
            final JSONObject value=loaded;final boolean failedGrowth=growthError,failedMedal=medalError;
            runOnUiThread(()->{if(ticket!=generation||!BetaUi.current(this,expected))return;loading=false;loadFailed=failedGrowth;medalLoadFailed=failedMedal;if(!failedGrowth)state=value;render();});
        });
    }
    String t(String ko,String en,Object... args){return BetaMedalUi.t(this,ko,en,args);}
    boolean stacked(){return getResources().getConfiguration().fontScale>1.3f||getResources().getConfiguration().screenWidthDp<360;}
    void render(){
        body.removeAllViews();
        if(account==null){body.addView(BetaUi.note(this,BetaCopy.t(this,"no_account"),dark));body.addView(BetaUi.row(this,BetaCopy.t(this,"account"),"",dark,()->MatteNav.open(this,1)));return;}
        if(loadFailed||medalLoadFailed){
            LinearLayout error=BetaUi.group(body,t("수집 기록을 확인하지 못했어요","Could not read your collection"),dark);error.setTag("stable-redesign:catalog-error");
            TextView note=BetaUi.note(this,state==null?t("획득한 항목이 없다는 뜻은 아니에요. 원본 기록은 보존됩니다.","This does not mean your collection is empty. Original records are retained."):t("읽을 수 있는 마지막 기록을 표시합니다. 원본 기록은 보존됩니다.","Showing the last available records. Original records are retained."),dark);note.setTextColor(BetaUi.warning(dark));error.addView(note);
            if(medalLoadFailed)error.addView(BetaUi.note(this,t("활동 메달 기록 확인 필요","Activity medal records need another check"),dark));
            Button retry=BetaUi.action(this,loading?t("확인 중…","Checking…"):t("다시 시도","Retry"),true,dark,this::load);retry.setEnabled(!loading);error.addView(retry);if(state==null&&loadFailed)return;
        }
        if(loading&&state==null){body.addView(BetaUi.note(this,getString(R.string.next_loading),dark));return;}
        if(state==null){body.addView(BetaUi.note(this,BetaCopy.t(this,"pending_growth"),dark));body.addView(BetaUi.row(this,t("성장 화면 보기","Open Growth"),"",dark,()->MatteNav.open(this,3)));return;}
        JSONObject ownedFacts=state.optJSONObject(titles?"titles":"honors"),medals=BetaMedalStore.peek(this),medalFacts=medals==null?null:medals.optJSONObject(titles?"titles":"honors");
        String[] medalIds=titles?BetaMedalModel.TITLES:BetaMedalModel.HONORS;int ownedCount=HudUiModel.visibleMedals(medalFacts,medalIds,1);
        for(String[] entry:BetaCatalog.ALL)if(BetaCatalog.title(entry[0])==titles&&ownedFacts!=null&&ownedFacts.has(entry[0]))ownedCount++;
        LinearLayout summary=BetaUi.group(body,t(titles?"나의 칭호":"나의 명예",titles?"My titles":"My honors"),dark);summary.setTag("stable-redesign:collection-summary");summary.addView(BetaUi.title(this,t("보유 %d / %d","Owned %d / %d",ownedCount,BetaCatalog.count(titles)+medalIds.length),dark));
        if(saving)summary.addView(BetaUi.note(this,t("장착 상태를 저장하고 있어요…","Saving your title…"),dark));
        if(titles){LinearLayout preview=BetaUi.group(body,BetaCopy.t(this,"preview"),dark);preview.addView(BetaUi.title(this,BetaMedalUi.equippedTitle(this,BetaIdentity.title(this,state.optString("equipped"))),dark));preview.addView(BetaUi.note(this,BetaCopy.t(this,"name_note"),dark));}
        String[] filters={"all","owned","progress"};LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(stacked()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);for(int i=0;i<3;i++){final int index=i;Button button=BetaUi.action(this,BetaCopy.t(this,filters[i]),filter==i,dark,()->{filter=index;render();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(stacked()?-1:0,-2,stacked()?0:1);button.setTag("stable-redesign:catalog-filter:"+i);button.setSelected(filter==i);androidx.core.view.ViewCompat.setStateDescription(button,filter==i?getString(R.string.matte_selected):"");button.setPadding(Ui.dp(this,6),Ui.dp(this,10),Ui.dp(this,6),Ui.dp(this,10));lp.topMargin=Ui.dp(this,8);if(i>0&&!(stacked()))lp.setMarginStart(Ui.dp(this,6));tabs.addView(button,lp);}body.addView(tabs);Ui.addSpacer(body,12);
        if(!titles){String[] categories={"all","start","routine","period","long"};Spinner chooser=new Spinner(this);String[] labels=new String[5];for(int i=0;i<labels.length;i++)labels[i]=BetaCopy.t(this,categories[i]);android.widget.ArrayAdapter<String> adapter=new android.widget.ArrayAdapter<>(this,android.R.layout.simple_spinner_item,labels);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);chooser.setAdapter(adapter);chooser.setSelection(category+1);chooser.setMinimumHeight(Ui.dp(this,48));chooser.setContentDescription(BetaCopy.t(this,"honors"));chooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int position,long id){if(category!=position-1){category=position-1;render();}}public void onNothingSelected(AdapterView<?> p){}});body.addView(chooser);}
        JSONObject facts=state.optJSONObject(titles?"titles":"honors");LinearLayout row=null;int shown=0;boolean stack=stacked();
        for(String[] e:BetaCatalog.ALL){if(BetaCatalog.title(e[0])!=titles)continue;boolean owned=facts!=null&&facts.has(e[0]);if(filter==1&&!owned||filter==2&&owned||!titles&&category>=0&&Integer.parseInt(e[5])!=category)continue;
            if(stack||shown%2==0){row=new LinearLayout(this);row.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);body.addView(row);Ui.addSpacer(body,12);}LinearLayout card=BetaUi.card(this,dark);card.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));ImageView emblem=new ImageView(this);emblem.setImageDrawable(new BetaEmblem(e[0],owned,dark));emblem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);card.addView(emblem,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
            String label=BetaCatalog.name(e[0],BetaIdentity.name(this),BetaCopy.ko(this));card.addView(BetaUi.body(this,label,dark));TextView rarity=BetaUi.note(this,BetaMedalUi.rarityName(this,e[6])+" · "+BetaMedalUi.t(this,"성장 기록","Growth record"),dark);rarity.setTextColor(BetaMedalUi.textColor(e[6],dark));card.addView(rarity);if(owned)BetaMedalUi.frame(card,BetaMedalUi.rarity(e[6]),dark);int[] p=BetaCatalog.progress(state,e[0]);if(!owned){ProgressBar bar=Ui.progress(this,dark);bar.setMax(Math.max(1,p[1]));bar.setProgress(Math.max(0,Math.min(p[0],p[1])));bar.setProgressTintList(android.content.res.ColorStateList.valueOf(BetaMedalUi.textColor(e[6],dark)));bar.setContentDescription(Math.max(0,Math.min(p[0],p[1]))+" / "+p[1]);card.addView(bar);}card.addView(BetaUi.note(this,Math.min(p[0],p[1])+" / "+p[1]+" · "+BetaCopy.t(this,owned?state.optString("equipped").equals(e[0])?"equipped":"earned":"locked"),dark));card.setContentDescription(label+", "+e[BetaCopy.ko(this)?3:4]+", "+BetaCopy.t(this,owned?"earned":"locked"));card.setTag("beta995:collectible:"+e[0]);card.setFocusable(true);card.setClickable(true);card.setOnClickListener(v->show(e[0]));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1);if(!stack&&shown%2==1)lp.setMarginStart(Ui.dp(this,12));row.addView(card,lp);shown++;
        }int medalShown=(titles||category<0||category==2)?BetaMedalUi.collection(this,body,dark,titles,filter):0;if(shown+medalShown==0){LinearLayout empty=BetaUi.group(body,t(filter==1?"아직 모으는 중이에요":"조건에 맞는 항목이 없어요",filter==1?"Your collection starts here":"No matching items"),dark);empty.setTag("stable-redesign:collection-empty");empty.addView(BetaUi.note(this,t("획득 가능한 항목과 달성 조건을 살펴보세요.","Explore available items and their requirements."),dark));empty.addView(BetaUi.action(this,t("전체 항목 보기","View all items"),true,dark,()->{filter=0;category=-1;render();}));}if(!stack&&shown%2==1&&row!=null)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
    }
    void show(String id){final String expectedAccount=account;if(saving||state==null||!BetaUi.current(this,expectedAccount))return;String[] e=BetaCatalog.find(id);if(e==null)return;JSONObject facts=state.optJSONObject(BetaCatalog.title(id)?"titles":"honors");boolean owned=facts!=null&&facts.has(id);int[] p=BetaCatalog.progress(state,id);String text=e[BetaCopy.ko(this)?3:4]+"\n"+Math.min(p[0],p[1])+" / "+p[1]+"\n"+BetaCopy.t(this,"decorative")+(owned?"\n"+BetaCopy.t(this,"earned_at",BetaGrowthUi.time(this,facts.optLong(id))):"");
        if(dialog!=null)dialog.dismiss();AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle(BetaCatalog.name(id,BetaIdentity.name(this),BetaCopy.ko(this))).setMessage(text).setNegativeButton(BetaCopy.t(this,"close"),null);
        if(owned&&BetaCatalog.title(id)){boolean equipped=id.equals(state.optString("equipped"));builder.setPositiveButton(BetaCopy.t(this,equipped?"unequip":"equip"),(d,w)->{if(saving||!BetaUi.current(this,expectedAccount))return;saving=true;render();BetaUi.task(this,expectedAccount,()->{boolean saved=BetaGrowthStore.equip(this,expectedAccount,equipped?"":id);if(saved)BetaMedalStore.equip(this,expectedAccount,"");return saved;},()->{saving=false;load();});});}dialog=builder.create();dialog.show();
    }
}
