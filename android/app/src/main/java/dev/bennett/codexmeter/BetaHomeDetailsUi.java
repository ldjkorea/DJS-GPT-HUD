package dev.bennett.codexmeter;
import android.content.Intent;import android.widget.*;import org.json.JSONObject;

final class BetaHomeDetailsUi {
    static void show(MainActivity a,UsageSnapshot snapshot,boolean dark,String expected){
        if(!java.util.Objects.equals(expected,SubscriptionStore.accountKey(a)))return;
        LinearLayout body=BetaMedalUi.content(a);JSONObject growth=BetaGrowthStore.peek(a);
        if(growth!=null)body.addView(BetaUi.body(a,"Lv. "+growth.optInt("level")+" · "+LedgerUi.number(growth.optLong("progress"))+" / "+LedgerUi.number(growth.optLong("next_cost"))+" "+BetaCopy.t(a,"xp"),dark));
        BetaQuotaUi.details(a,body,dark,expected);
        body.addView(BetaUi.row(a,BetaMedalUi.t(a,"구독·결제 분석","Subscription and billing analysis"),"",dark,()->a.startActivity(new Intent(a,FunActivity.class))));
        body.addView(BetaUi.row(a,BetaMedalUi.t(a,"저장된 인용","Saved excerpt"),"",dark,()->{LinearLayout quote=BetaMedalUi.content(a);TiboQuote.add(a,quote,dark);BetaMedalUi.show(a,BetaMedalUi.t(a,"저장된 인용","Saved excerpt"),quote);}));
        if(TaskStatusStore.enabled(a))body.addView(BetaUi.row(a,BetaMedalUi.t(a,"연결된 작업 상태","Connected task status"),TaskStatusStore.homeText(a),dark,()->a.startActivity(new Intent(a,TaskStatusActivity.class))));
        body.addView(BetaUi.row(a,BetaCopy.t(a,"tier"),"",dark,()->BetaTierDialog.show(a)));
        body.addView(BetaUi.row(a,BetaMedalUi.t(a,"연결·업데이트 복구","Connection and update recovery"),"",dark,()->BetaExperienceUi.open(a,"health")));
        body.addView(BetaUi.row(a,BetaMedalUi.t(a,"설정","Settings"),"",dark,()->a.startActivity(new Intent(a,MeterSettingsActivity.class))));
        BetaMedalUi.show(a,BetaMedalUi.t(a,"내 상태 상세","My status details"),body,expected);
    }
}
