from pathlib import Path
import subprocess,json
r=Path(__file__).resolve().parents[2];base='83e2cf0b6a9f2b67bc19fdb7349c63d4ad9b3505'
git=lambda *a:subprocess.check_output(['git',*a],cwd=r)
norm=lambda b:b.replace(b'\r\n',b'\n')
app='android/app/src/main/java/dev/bennett/codexmeter/'
allowed={app+n for n in ['BetaWeeklyUi.java','BetaQuotaUi.java','BetaSettingsUi.java','DashboardReorderActivity.java','BetaCompactGrowthUi.java','AppConstants.java']}|{'android/app/build.gradle.kts','android/build.sh','android/app/src/main/assets/release-history.json'}
paths=set(git('diff','--name-only',base).decode().splitlines())|set(git('ls-files','-o','--exclude-standard').decode().splitlines())
production={p for p in paths if p.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or p in {'android/build.sh','android/build.gradle.kts'}}
assert production==allowed,production^allowed
historical=set(git('ls-tree','-r','--name-only',base,'android/tests','android/trial').decode().splitlines());assert not paths&historical,paths&historical
for name in ['MainActivity.java','BetaHomeSummaryUi.java','FunHome.java','CoachUi.java','BetaWeeklyReader.java','BetaNextGoal.java']:
 assert norm((r/app/name).read_bytes())==norm(git('show',base+':'+app+name)),name
rows=json.loads((r/'android/app/src/main/assets/release-history.json').read_text('utf-8'));assert rows[0]['version']=='3.1.9' and rows[1:]==json.loads(git('show',base+':android/app/src/main/assets/release-history.json'))
assert 'versionCode = 96' in (r/'android/app/build.gradle.kts').read_text()
print('Stable319 source scope passed: home layout, data/auth/economy/update/coach/signing and historical tests unchanged.')
