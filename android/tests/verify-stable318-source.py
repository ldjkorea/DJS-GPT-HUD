"""Bound the release to home ordering and version metadata; retain all published tests."""
from pathlib import Path
import json,subprocess
r=Path(__file__).resolve().parents[2];base='6ca8d4965f100ffe4ae4eff9238fea6111220136'
git=lambda *a:subprocess.check_output(['git',*a],cwd=r)
norm=lambda b:b.replace(b'\r\n',b'\n')
read=lambda p:norm((r/p).read_bytes())
old=lambda p:norm(git('show',base+':'+p))
app='android/app/src/main/java/dev/bennett/codexmeter/'
main=app+'MainActivity.java'
before=b'        BetaWeeklyUi.add(this,content,snapshot,dark,quotaClockUpdates);\n        FunHome.add(this,content,dark,this::restoreHomeScroll,crestSlot);'
after=b'        FunHome.add(this,content,dark,this::restoreHomeScroll,crestSlot);\n        BetaWeeklyUi.add(this,content,snapshot,dark,quotaClockUpdates);'
assert before in old(main) and read(main)==old(main).replace(before,after),'Only relocate the coach above weekly usage'
gradle='android/app/build.gradle.kts';assert read(gradle)==old(gradle).replace(b'versionCode = 94',b'versionCode = 95').replace(b'versionName = "3.1.7"',b'versionName = "3.1.8"')
constants=app+'AppConstants.java';assert read(constants)==old(constants).replace(b'VERSION_CODE = 94',b'VERSION_CODE = 95').replace(b'3.1.7',b'3.1.8')
build='android/build.sh';assert read(build)==old(build).replace(b'VERSION_NAME="3.1.7"',b'VERSION_NAME="3.1.8"')
history='android/app/src/main/assets/release-history.json';rows=json.loads(read(history));assert rows[0]['version']=='3.1.8' and rows[1:]==json.loads(old(history))
allowed={main,gradle,constants,build,history}
paths=set(git('diff','--name-only',base).decode().splitlines())|set(git('ls-files','-o','--exclude-standard').decode().splitlines())
production={p for p in paths if p.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or p in {'android/build.sh','android/build.gradle.kts'}}
assert production==allowed,sorted(production^allowed)
historical=set(git('ls-tree','-r','--name-only',base,'android/tests','android/trial').decode().splitlines());assert not paths.intersection(historical),sorted(paths.intersection(historical))
for p in [app+'BetaQuotaUi.java','android/wear/build.gradle.kts','windows-widget/src/AssemblyInfo.cs','android/app/src/main/AndroidManifest.xml','android/app/lint-baseline.xml']:
 assert read(p)==old(p),p
text=read(main).decode();assert text.index('LinearLayout crestSlot=BetaHomeSummaryUi.add')<text.index('FunHome.add(this,content')<text.index('BetaWeeklyUi.add(this,content')
print('Stable318 source: coach relocation only + version95/history; exact reset schedule, auth/stores/coach policy/catalogs/settings/signing/update trust/Wear/Windows and historical tests preserved.')
