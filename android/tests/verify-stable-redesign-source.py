"""Bounded current redesign and preservation contracts; historical release fixtures stay intact."""
from pathlib import Path
import subprocess, json

r=Path(__file__).resolve().parents[2]
base='9a7da3cdd39692cd39fc153385a5aa85a0a78b25'
app='android/app/src/main/java/dev/bennett/codexmeter/'
allowed={app+n+'.java' for n in ['AccountSession','BetaBackupActivity','BetaCatalogActivity','BetaCompactGrowthUi','BetaHomeUi','BetaQuotaUi','BetaSettingsUi','BetaUi','BetaWeeklyUi','LedgerAnalyticsActivity','MainActivity','RecordsActivity','Ui','UsageHistoryActivity','GrowthCalendarNavigation']}
git=lambda *a:subprocess.check_output(['git',*a],cwd=r)
paths=set(git('diff','--name-only',base).decode().splitlines())|set(git('ls-files','-o','--exclude-standard').decode().splitlines())
production={p for p in paths if p.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or p in {'android/build.sh','android/build.gradle.kts'}}
assert production<=allowed,sorted(production-allowed)
assert app+'GrowthCalendarNavigation.java' in production
protected=['BetaGrowthStore','BetaMedalStore','BetaBundle','SecureTokenStore','OAuthClient','OAuthAttempt','UsageApi','BetaV4','BetaCatalog','BetaMedalModel','TierTheme','BetaIdentity','GitHubReleaseParser','UpdateChannel','ForegroundUpdateNotice','ReleaseUpdateClient','UpdatePreferences','AppPreferences','UsageLedgerDatabase','RpgProgressStore','AppConstants']
normalize=lambda b:b.replace(b'\r\n',b'\n')
for name in protected:
    p=app+name+'.java'
    assert normalize((r/p).read_bytes())==normalize(git('show',base+':'+p)),name
for p in ['android/app/src/main/AndroidManifest.xml','android/app/build.gradle.kts','android/shared/src/main/java/dev/bennett/codexmeter/KoreanUpdateTrust.java','android/shared/src/main/java/dev/bennett/codexmeter/ForegroundUpdatePolicy.java','android/app/src/main/assets/coach-ko-v2.json','android/app/src/main/assets/coach-ko-medals-v1.json','android/app/lint-baseline.xml']:
    assert normalize((r/p).read_bytes())==normalize(git('show',base+':'+p)),p
historical=set(git('ls-tree','-r','--name-only',base,'android/tests','android/trial').decode().splitlines())
assert not paths.intersection(historical),sorted(paths.intersection(historical))
main=(r/(app+'MainActivity.java')).read_text(encoding='utf-8')
assert main.index('LinearLayout crestSlot=BetaHomeSummaryUi.add')<main.index('BetaWeeklyUi.add(this,content')<main.index('FunHome.add(this,content')
print(json.dumps({'base':base,'production_files':len(production),'protected_sources':len(protected),'unchanged':'stores/economy/IDs/catalogs/auth/package/signing config/update trust/manifest/lint baseline/historical tests','public_version':'3.1.5/code92 unchanged'},ensure_ascii=False))
