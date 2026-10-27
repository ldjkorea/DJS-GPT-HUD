package dev.bennett.codexmeter;

import android.content.Context;

/** Serialize account boundaries with quota and reset-credit requests. Call off the UI thread. */
final class AccountSession {
    private AccountSession() { }
    private static final OAuthAttempt OAUTH=new OAuthAttempt();
    static long beginSignIn(){return OAUTH.begin();}
    static boolean signInCurrent(long attempt){return OAUTH.current(attempt);}
    static boolean signInActive(){return OAUTH.active();}
    static boolean sessionEpoch(long attempt){return OAUTH.epoch(attempt);}
    static void cancelSignIn(){OAUTH.cancel();}
    static boolean cancelSignIn(long attempt){return OAUTH.cancel(attempt);}
    static boolean finishSignIn(long attempt){return OAUTH.finish(attempt);}
    static boolean installFromOAuth(Context context,AuthTokens tokens,long attempt)throws Exception{
        synchronized(UsageApi.NETWORK_LOCK){return OAUTH.commit(attempt,()->installLocked(context,tokens,false));}
    }

    static AuthTokens signOut(Context context) {
        OAUTH.cancel();
        synchronized (UsageApi.NETWORK_LOCK) {
            AuthTokens previous;
            synchronized(OAUTH){OAUTH.cancel();previous=SecureTokenStore.load(context);SecureTokenStore.clear(context);
                AppPreferences.clearSnapshot(context);AppPreferences.setOAuthPending(context, false, "");}
            RefreshScheduler.cancelAll(context);
            ResetAlertScheduler.cancelAll(context);
            WidgetRenderer.updateAll(context);
            return previous;
        }
    }

    static void install(Context context, AuthTokens tokens, boolean imported) throws Exception {
        OAUTH.cancel();
        synchronized (UsageApi.NETWORK_LOCK) {synchronized(OAUTH){OAUTH.cancel();installLocked(context,tokens,imported);}}
    }
    private static void installLocked(Context context,AuthTokens tokens,boolean imported)throws Exception{
        AuthTokens previous=SecureTokenStore.load(context);
        boolean sameAccount=previous!=null&&!previous.accountId.isEmpty()&&previous.accountId.equals(tokens.accountId);
        if(imported||!sameAccount)AppPreferences.clearSnapshot(context);
        SecureTokenStore.save(context,tokens);
        AppPreferences.setOAuthPending(context,false,"");
    }

    /** Captured when the user starts a history action. Unknown identities cannot authorize it. */
    static String historyAccount(Context context) {
        AuthTokens tokens=SecureTokenStore.load(context);
        return tokens==null?"signed-out":tokens.accountId.isEmpty()?null:"account:"+tokens.accountId;
    }
    static boolean historyAccountCurrent(Context context,String expected) {
        return expected!=null&&expected.equals(historyAccount(context));
    }
    static void clearHistory(Context context) {
        synchronized (UsageApi.NETWORK_LOCK) {
            AppPreferences.clearUsageHistory(context);
        }
    }
    static boolean clearHistory(Context context,String expected) {
        synchronized (UsageApi.NETWORK_LOCK) {
            if(!historyAccountCurrent(context,expected))return false;
            AppPreferences.clearUsageHistory(context);
            return true;
        }
    }
}
