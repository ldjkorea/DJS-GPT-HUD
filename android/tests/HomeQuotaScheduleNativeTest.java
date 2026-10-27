package dev.bennett.codexmeter;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.util.*;

/** Real home rendering with in-memory quota cases on the owned synthetic preview AVD. */
public final class HomeQuotaScheduleNativeTest extends Instrumentation {
    int checks;
    Throwable failure;
    void check(boolean value, String why) { checks++; if (!value) throw new AssertionError(why); }
    void main(Runnable action) {
        runOnMainSync(() -> { try { action.run(); } catch (Throwable t) { failure=t; } });
        if (failure!=null) throw new AssertionError(failure);
    }
    TextView reset(View root, String key) { return root.findViewWithTag("beta998:quota:"+key+":reset"); }
    void fits(View view) {
        if (view instanceof TextView && view.isShown()) {
            TextView text=(TextView)view;
            if (text.length()>0 && text.getLayout()!=null) {
                android.text.Layout layout=text.getLayout();
                check(layout.getEllipsisCount(layout.getLineCount()-1)==0,"No ellipsis: "+text.getText());
                check(text.getHeight()+2>=text.getCompoundPaddingTop()+text.getCompoundPaddingBottom()
                        +layout.getLineBottom(layout.getLineCount()-1),"Full text height: "+text.getText());
            }
        }
        if (view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) fits(((ViewGroup)view).getChildAt(i));
    }
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Context c=getTargetContext(); Bundle result=new Bundle(); MainActivity home=null;
        UsageSnapshot original=null;
        try {
            check(Build.MODEL.contains("sdk") && c.getPackageName().equals("dev.bennett.codexmeter"),"Owned stable emulator only");
            check("stable316-qa".equals(SecureTokenStore.load(c).accountId),"Synthetic account required");
            String credentials=SecureTokenStore.load(c).toJson().toString();
            original=AppPreferences.loadSnapshot(c);
            long now=System.currentTimeMillis();
            long five=now+96*60000L, week=now+5*86400000L+21*3600000L;
            UsageSnapshot sample=new UsageSnapshot("plus",true,false,
                    new UsageWindow(100,18000,0,five/1000),new UsageWindow(19,604800,0,week/1000),now);
            check(AppPreferences.saveSnapshot(c,sample),"Temporary synthetic cache saved");
            home=(MainActivity)startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            MainActivity activity=home;
            waitForIdleSync(); Thread.sleep(800); waitForIdleSync();
            main(() -> {
                View body=activity.findViewById(R.id.dashboard_content);
                for(String key:new String[]{"five-hour","weekly"}) {
                    TextView text=reset(body,key);
                    check(text!=null && text.isShown(),"Home reset shown: "+key);
                    long at=key.equals("five-hour")?five:week;
                    check(text.getText().toString().contains(UsageFormat.absolute(activity,at,now)),"Home exact date/time: "+key);
                    check(text.getText().toString().contains("\n"),"Separate countdown line: "+key);
                    check(text.getLineCount()>=2,"Date and countdown rendered: "+key);
                }
                fits(body);
                // Build the production compact section without saving or refreshing any data.
                for(long offset:new long[]{-60000,0,60000,86400000L,7*86400000L}) {
                    long at=offset==0?0:now+offset;
                    UsageWindow window=new UsageWindow(19,604800,0,at/1000);
                    UsageSnapshot snapshot=new UsageSnapshot("plus",true,false,window,window,now);
                    LinearLayout probe=new LinearLayout(activity); probe.setOrientation(LinearLayout.VERTICAL);
                    BetaQuotaUi.addContent(activity,probe,snapshot,true,new ArrayList<>());
                    String text=reset(probe,"weekly").getText().toString();
                    if(offset>0) check(text.contains(UsageFormat.absolute(activity,at,now)) && text.contains("\n"),"Future exact date plus countdown: "+offset);
                    else check(text.equals(activity.getString(offset==0?R.string.ui_reset_time_unavailable_5c3030:R.string.pub_five_expired_short)),"Unknown/expired stays honest: "+offset);
                }
                LinearLayout empty=new LinearLayout(activity);
                BetaQuotaUi.addContent(activity,empty,null,true,new ArrayList<>());
                check(reset(empty,"five-hour").length()==0 && reset(empty,"weekly").length()==0,"No invented reset time without snapshot");
                UsageWindow relative=new UsageWindow(100,18000,7200,0);
                LinearLayout probe=new LinearLayout(activity);
                BetaQuotaUi.addContent(activity,probe,new UsageSnapshot("plus",true,false,relative,relative,now),true,new ArrayList<>());
                check(reset(probe,"five-hour").getText().toString().contains(UsageFormat.absolute(activity,now+7200000,now)),"Relative server reset resolved from observation time");
            });
            Bitmap screen=getUiAutomation().takeScreenshot();
            java.io.File folder=new java.io.File(c.getExternalFilesDir(null),"stable316"); folder.mkdirs();
            String config=c.getResources().getConfiguration().fontScale+"-"+c.getResources().getConfiguration().screenWidthDp;
            try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,"home-reset-restored-"+config+".png"))) { screen.compress(Bitmap.CompressFormat.PNG,100,out); }
            screen.recycle();
            check(credentials.equals(SecureTokenStore.load(c).toJson().toString()),"Encrypted synthetic login unchanged");
            result.putString("stream","Home quota exact reset: "+checks+" checks passed; synthetic only; "+config+"\n");
            finish(-1,result);
        } catch(Throwable t) { result.putString("stream",android.util.Log.getStackTraceString(t)); finish(0,result); }
        finally {
            if(original!=null) AppPreferences.saveSnapshot(c,original);
            if(home!=null) { MainActivity activity=home; runOnMainSync(activity::finish); }
        }
    }
}
