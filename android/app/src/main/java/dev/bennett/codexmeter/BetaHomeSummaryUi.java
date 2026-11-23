package dev.bennett.codexmeter;

import android.view.View;
import android.widget.LinearLayout;
import java.util.List;

/** One surface, independently replaceable identity and quota sections. */
final class BetaHomeSummaryUi {
    static LinearLayout add(MainActivity activity,LinearLayout parent,UsageSnapshot snapshot,
            boolean dark,List<Runnable> clockUpdates){
        LinearLayout card=BetaUi.group(parent,"",dark);card.setTag("beta999:summary");
        TierTheme.frame(card,TierTheme.tier(activity),dark);
        LinearLayout identity=new LinearLayout(activity);identity.setOrientation(LinearLayout.VERTICAL);
        identity.setTag("beta999:identity-slot");card.addView(identity,new LinearLayout.LayoutParams(-1,-2));
        identity.addView(BetaHomeUi.card(activity,dark,false));
        View divider=new View(activity);divider.setBackgroundColor(Ui.divider(dark));
        LinearLayout.LayoutParams rule=new LinearLayout.LayoutParams(-1,Ui.dp(activity,1));
        rule.topMargin=Ui.dp(activity,12);rule.bottomMargin=Ui.dp(activity,12);card.addView(divider,rule);
        BetaQuotaUi.addContent(activity,card,snapshot,dark,clockUpdates);
        return identity;
    }
}
