"""Exact production delta from published316; preserve all historical tests and data/auth code."""
from pathlib import Path
import json,subprocess
r=Path(__file__).resolve().parents[2];base='59c6f2e0cf266165c3d84b48b11b2cabaff0ee20'
git=lambda *a:subprocess.check_output(['git',*a],cwd=r)
norm=lambda b:b.replace(b'\r\n',b'\n')
read=lambda p:norm((r/p).read_bytes())
old=lambda p:norm(git('show',base+':'+p))
app='android/app/src/main/java/dev/bennett/codexmeter/'
quota=app+'BetaQuotaUi.java'
expected=old(quota).replace(b'compact ? BetaExperienceUi.t(activity,"\xec\xb4\x88\xea\xb8\xb0\xed\x99\x94\xea\xb9\x8c\xec\xa7\x80 %s","Resets in %s",\n                        UsageFormat.compactDuration(activity, resetAt - now))\n                        : activity.getString',b'activity.getString')
assert read(quota)==expected and expected!=old(quota),'Only restore exact reset time in compact home'
gradle='android/app/build.gradle.kts';assert read(gradle)==old(gradle).replace(b'versionCode = 93',b'versionCode = 94').replace(b'versionName = "3.1.6"',b'versionName = "3.1.7"')
constants=app+'AppConstants.java';assert read(constants)==old(constants).replace(b'VERSION_CODE = 93',b'VERSION_CODE = 94').replace(b'3.1.6',b'3.1.7')
build='android/build.sh';assert read(build)==old(build).replace(b'VERSION_NAME="3.1.6"',b'VERSION_NAME="3.1.7"')
history='android/app/src/main/assets/release-history.json';rows=json.loads(read(history));assert rows[0]['version']=='3.1.7' and rows[1:]==json.loads(old(history))
allowed={quota,gradle,constants,build,history}
paths=set(git('diff','--name-only',base).decode().splitlines())|set(git('ls-files','-o','--exclude-standard').decode().splitlines())
production={p for p in paths if p.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or p in {'android/build.sh','android/build.gradle.kts'}}
assert production==allowed,sorted(production^allowed)
historical=set(git('ls-tree','-r','--name-only',base,'android/tests','android/trial').decode().splitlines())
assert not paths.intersection(historical),sorted(paths.intersection(historical))
for p in ['android/wear/build.gradle.kts','windows-widget/src/AssemblyInfo.cs','android/app/src/main/AndroidManifest.xml','android/app/lint-baseline.xml']:
 assert read(p)==old(p),p
assert read(quota).count(b'home_quota_reset_schedule')==1
assert 'versionCode = 94' in read(gradle).decode()
print('Stable317 source: exact home-reset-only production change + version94/history; all auth, stores, settings, catalogs, signing, trust, Wear/Windows and historical tests unchanged.')
