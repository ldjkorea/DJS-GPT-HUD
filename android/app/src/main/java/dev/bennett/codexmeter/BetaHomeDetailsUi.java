package dev.bennett.codexmeter;

import android.widget.LinearLayout;
import java.util.Objects;

final class BetaHomeDetailsUi {
    static void show(MainActivity activity, boolean dark, String account) {
        if (!Objects.equals(account, SubscriptionStore.accountKey(activity))) return;
        LinearLayout body = BetaMedalUi.content(activity);
        BetaQuotaUi.details(activity, body, dark, account);
        BetaMedalUi.show(activity, activity.getString(R.string.quota_details_title), body, account);
    }
}
