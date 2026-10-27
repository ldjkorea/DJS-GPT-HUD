"""Bounded current redesign and preservation contracts; historical release fixtures stay intact."""
from pathlib import Path
import subprocess, json

r=Path(__file__).resolve().parents[2]
base='9a7da3cdd39692cd39fc153385a5aa85a0a78b25'
app='android/app/src/main/java/dev/bennett/codexmeter/'
allowed={app+n+'.java' for n in ['AccountSession','BetaBackupActivity','BetaCatalogActivity','BetaCompactGrowthUi','BetaHomeUi','BetaQuotaUi','BetaSettingsUi','BetaUi','BetaWeeklyUi','LedgerAnalyticsActivity','MainActivity','RecordsActivity','Ui','UsageHistoryActivity','GrowthCalendarNavigation','AppConstants']}|{'android/app/build.gradle.kts','android/app/src/main/assets/release-history.json','android/build.sh'}
git=lambda *a:subprocess.check_output(['git',*a],cwd=r)
paths=set(git('diff','--name-only',base).decode().splitlines())|set(git('ls-files','-o','--exclude-standard').decode().splitlines())
production={p for p in paths if p.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or p in {'android/build.sh','android/build.gradle.kts'}}
assert production<=allowed,sorted(production-allowed)
assert app+'GrowthCalendarNavigation.java' in production
protected=['BetaGrowthStore','BetaMedalStore','BetaBundle','SecureTokenStore','OAuthClient','OAuthAttempt','UsageApi','BetaV4','BetaCatalog','BetaMedalModel','TierTheme','BetaIdentity','GitHubReleaseParser','UpdateChannel','ForegroundUpdateNotice','ReleaseUpdateClient','UpdatePreferences','AppPreferences','UsageLedgerDatabase','RpgProgressStore']
normalize=lambda b:b.replace(b'\r\n',b'\n')
for name in protected:
    p=app+name+'.java'
    assert normalize((r/p).read_bytes())==normalize(git('show',base+':'+p)),name
for p in ['android/app/src/main/AndroidManifest.xml','android/shared/src/main/java/dev/bennett/codexmeter/KoreanUpdateTrust.java','android/shared/src/main/java/dev/bennett/codexmeter/ForegroundUpdatePolicy.java','android/app/src/main/assets/coach-ko-v2.json','android/app/src/main/assets/coach-ko-medals-v1.json','android/app/lint-baseline.xml']:
    assert normalize((r/p).read_bytes())==normalize(git('show',base+':'+p)),p
historical=set(git('ls-tree','-r','--name-only',base,'android/tests','android/trial').decode().splitlines())
assert not paths.intersection(historical),sorted(paths.intersection(historical))
main=(r/(app+'MainActivity.java')).read_text(encoding='utf-8')
assert main.index('LinearLayout crestSlot=BetaHomeSummaryUi.add')<main.index('BetaWeeklyUi.add(this,content')<main.index('FunHome.add(this,content')
print(json.dumps({'base':base,'production_files':len(production),'protected_sources':len(protected),'unchanged':'stores/economy/IDs/catalogs/auth/package/signing config/update trust/manifest/lint baseline/historical tests','public_version':'3.1.6/code93; Wear3.1.0/Windows3.1.1 unchanged'},ensure_ascii=False))

def read(p):return (r/p).read_text(encoding='utf-8')
def before(p):return git('show',base+':'+p).decode('utf-8').replace('\r\n','\n')
gradle='android/app/build.gradle.kts';assert read(gradle)==before(gradle).replace('versionCode = 92','versionCode = 93').replace('versionName = "3.1.5"','versionName = "3.1.6"')
constants=app+'AppConstants.java';assert read(constants)==before(constants).replace('VERSION_CODE = 92','VERSION_CODE = 93').replace('VERSION_NAME = "3.1.5"','VERSION_NAME = "3.1.6"').replace('codex-meter-android/3.1.5','codex-meter-android/3.1.6')
assert read('android/build.sh')==before('android/build.sh').replace('VERSION_NAME="3.1.5"','VERSION_NAME="3.1.6"')
history=json.loads(read('android/app/src/main/assets/release-history.json'));assert history[0]['version']=='3.1.6' and history[1:]==json.loads(before('android/app/src/main/assets/release-history.json'))
for p in ['android/wear/build.gradle.kts','windows-widget/src/AssemblyInfo.cs']:assert read(p)==before(p),p
assert 'versionName = "3.1.0"' in read('android/wear/build.gradle.kts')
print('Stable316 version/code/history and unchanged Wear/Windows: passed')
