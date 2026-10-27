package dev.bennett.codexmeter;
import android.content.Context;
import java.util.concurrent.*;
/** Real AccountSession code with isolated JVM persistence fixtures, not device evidence. */
public final class StableRedesignSettingsSelfTest {
    static int checks;
    static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    static AuthTokens token(String id){return new AuthTokens("fixture","fixture","",Long.MAX_VALUE,id,"");}
    public static void main(String[] ignored)throws Exception {
        Context c=new Context();ExecutorService worker=Executors.newSingleThreadExecutor();
        try {
            AccountSession.install(c,token("A"),false);AppPreferences.history=9;
            String expected=AccountSession.historyAccount(c);CountDownLatch entered=new CountDownLatch(1);
            Future<Boolean> pending;
            synchronized(UsageApi.NETWORK_LOCK){
                pending=worker.submit(()->{entered.countDown();return AccountSession.clearHistory(c,expected);});
                check(entered.await(2,TimeUnit.SECONDS),"Clear request queued");
                AccountSession.install(c,token("B"),false);AppPreferences.history=7;
            }
            check(!pending.get(2,TimeUnit.SECONDS),"A approval cannot delete B history after lock wait");
            check(AppPreferences.history==7,"B history preserved");
            check(SecureTokenStore.current.accountId.equals("B"),"B authentication preserved");
            check(!AccountSession.historyAccountCurrent(c,expected),"Export binding rejects switched account");
            String b=AccountSession.historyAccount(c);
            check(AccountSession.clearHistory(c,b),"Current account explicit clear accepted");
            check(AppPreferences.history==0,"Accepted clear removes history");
            AppPreferences.history=5;AccountSession.install(c,token("B"),false);
            check(AccountSession.historyAccountCurrent(c,b),"Same-account reauthentication keeps binding");
            check(AppPreferences.history==5,"Same-account reauthentication preserves history");
            AccountSession.signOut(c);AppPreferences.history=3;
            check(!AccountSession.clearHistory(c,b)&&AppPreferences.history==3,"Signed-out state rejects old account approval");
            String signedOut=AccountSession.historyAccount(c);
            check(AccountSession.clearHistory(c,signedOut),"Explicit signed-out local-history clear remains supported");
            AccountSession.install(c,token(""),false);AppPreferences.history=4;
            check(AccountSession.historyAccount(c)==null,"Unknown identity cannot bind a destructive action");
            check(!AccountSession.clearHistory(c,null)&&AppPreferences.history==4,"Missing binding preserves history");
            System.out.println("Stable redesign account-bound settings: "+checks+" assertions passed (JVM fixtures).");
        }finally{worker.shutdownNow();}
    }
}
