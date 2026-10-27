"""Account-bound settings regression against production AccountSession. No fixture rewrites."""
from pathlib import Path
import os, subprocess
r=Path(__file__).resolve().parents[1]; app=r/'app/src/main/java/dev/bennett/codexmeter'
out=r/'build/stable-redesign/settings-jvm';out.mkdir(parents=True,exist_ok=True)
fixtures={
 'android/content/Context.java':'package android.content; public class Context {}',
 'dev/bennett/codexmeter/UsageApi.java':'package dev.bennett.codexmeter; class UsageApi {static final Object NETWORK_LOCK=new Object();}',
 'dev/bennett/codexmeter/SecureTokenStore.java':'''package dev.bennett.codexmeter; class SecureTokenStore {
 static AuthTokens current; static AuthTokens load(android.content.Context c){return current;}
 static void save(android.content.Context c,AuthTokens t){current=t;} static void clear(android.content.Context c){current=null;}}''',
 'dev/bennett/codexmeter/AppPreferences.java':'''package dev.bennett.codexmeter; class AppPreferences {
 static int history; static void clearSnapshot(android.content.Context c){history=0;}
 static void clearUsageHistory(android.content.Context c){history=0;}
 static void setOAuthPending(android.content.Context c,boolean b,String s){}}'''
}
for name in ['RefreshScheduler','ResetAlertScheduler','WidgetRenderer']:
 method='updateAll' if name=='WidgetRenderer' else 'cancelAll'
 fixtures[f'dev/bennett/codexmeter/{name}.java']=f'package dev.bennett.codexmeter; class {name} {{static void {method}(android.content.Context c){{}}}}'
files=[]
for name,body in fixtures.items():
 p=out/'fixtures'/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body,encoding='utf-8');files.append(p)
files += [app/(name+'.java') for name in ['AccountSession','AuthTokens','OAuthAttempt']]+[Path(__file__).with_name('StableRedesignSettingsSelfTest.java')]
jdk=Path(os.environ['JAVA_HOME'])/'bin';jar=r/'build/test-libs/json-20250517.jar'
classes=out/'classes';classes.mkdir(exist_ok=True)
subprocess.run([str(jdk/'javac'),'-encoding','UTF-8','-cp',str(jar),'-d',str(classes)]+list(map(str,files)),check=True)
subprocess.run([str(jdk/'java'),'-ea','-cp',str(classes)+os.pathsep+str(jar),'dev.bennett.codexmeter.StableRedesignSettingsSelfTest'],check=True)
# Integration guards supplement the concurrency test; they are not native UI proof.
ledger=(app/'LedgerAnalyticsActivity.java').read_text(encoding='utf-8')
assert 'if(settingsRoot()){loadedKey=requestedKey;controls();return;}' in ledger
assert 'state.putString("export_account",exportAccount)' in ledger
assert 'AccountSession.historyAccountCurrent(getApplicationContext(),expected)' in ledger
assert 'if(busy||exportPending)return;' in ledger
assert 'clearHistory(getApplicationContext(),expected)' in ledger
assert 'clearHistory(app,expected)' in (app/'UsageHistoryActivity.java').read_text(encoding='utf-8')
settings=(app/'BetaSettingsUi.java').read_text(encoding='utf-8')
assert 'if(legacy.getChildCount()==0)' in settings and settings.count('RecordSettings.legacyHub(')==1
assert 'stable-redesign:update-settings' in settings and '()->page(a,"updates")' in settings
print('Settings lazy loading, update navigation and SAF binding integration guards: 8 passed.')
