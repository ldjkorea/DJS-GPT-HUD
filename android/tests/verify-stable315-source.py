"""Verify stable identity and the bounded UI-only promotion from published 3.1.4."""
from pathlib import Path
import subprocess,json
r=Path(__file__).resolve().parents[2];base='fc5471e612f6e7d1b6a9cdbf50aaae1ae2bbf585'
app='android/app/src/main/java/dev/bennett/codexmeter/'
ui=['BetaCatalogActivity','BetaCompactGrowthUi','BetaHubActivity','BetaMedalUi','BetaQuotaUi','BetaSettingsUi','BetaUi','BetaWeeklyUi','CoachUi','HudUiModel','MainActivity','MatteNav','RecordSettings','RecordsActivity']
allowed={app+n+'.java' for n in ui+['AppConstants']}|{'android/app/build.gradle.kts','android/build.sh','android/app/src/main/res/drawable/hud_settings.xml','android/app/src/main/res/values/strings.xml','android/app/src/main/res/values-ko/strings.xml','android/app/src/main/assets/release-history.json'}
changed=set(subprocess.check_output(['git','diff','--name-only',base],cwd=r,text=True).splitlines())
changed.update(subprocess.check_output(['git','ls-files','-o','--exclude-standard'],cwd=r,text=True).splitlines())
production={x for x in changed if x.startswith(('android/app/','android/shared/','android/wear/','windows-widget/','ios/')) or x=='android/build.sh'}
assert production==allowed,(sorted(production-allowed),sorted(allowed-production))
before=lambda path:subprocess.check_output(['git','show',base+':'+path],cwd=r).decode('utf-8').replace('\r\n','\n')
read=lambda path:(r/path).read_text(encoding='utf-8')
protected=['BetaGrowthStore','BetaMedalStore','BetaBundle','SecureTokenStore','OAuthClient','OAuthAttempt','UsageApi','BetaV4','BetaCatalog','BetaMedalModel','TierTheme','BetaIdentity','GitHubReleaseParser','UpdateChannel','ForegroundUpdateNotice','ReleaseUpdateClient','UpdatePreferences']
for n in protected:assert read(app+n+'.java')==before(app+n+'.java'),n
assert read('android/shared/src/main/java/dev/bennett/codexmeter/KoreanUpdateTrust.java')==before('android/shared/src/main/java/dev/bennett/codexmeter/KoreanUpdateTrust.java')
assert read('android/app/src/main/AndroidManifest.xml')==before('android/app/src/main/AndroidManifest.xml'),'Stable permissions/launcher/callback/Wear remain identical'
gradle=read('android/app/build.gradle.kts');assert gradle==before('android/app/build.gradle.kts').replace('versionCode = 91','versionCode = 92').replace('versionName = "3.1.4"','versionName = "3.1.5"').replace('rootProject.file(".local-signing")','rootProject.file(providers.gradleProperty("releaseSigningDir").orNull ?: ".local-signing")')
constants=read(app+'AppConstants.java');assert constants==before(app+'AppConstants.java').replace('VERSION_CODE = 91','VERSION_CODE = 92').replace('VERSION_NAME = "3.1.4"','VERSION_NAME = "3.1.5"').replace('codex-meter-android/3.1.3','codex-meter-android/3.1.5')
main=read(app+'MainActivity.java');original=before(app+'MainActivity.java');start='    private LinearLayout buildUsageCard()';end='    private void addUsageRow('
assert main.split(start)[0]==original.split(start)[0] and main.split(end)[1]==original.split(end)[1],'Only account presentation changes in MainActivity'
history=json.loads(read('android/app/src/main/assets/release-history.json'));assert history[0]['version']=='3.1.5' and history[1:]==json.loads(before('android/app/src/main/assets/release-history.json'))
for n in ['coach-ko-v2.json','coach-ko-medals-v1.json']:assert read('android/app/src/main/assets/'+n)==before('android/app/src/main/assets/'+n)
assert 'GPT HUD BETA' not in read(app+'BetaSettingsUi.java')
assert not (r/'.github/workflows/beta-verify.yml').exists(),'Retired beta build entry point removed'
old_tests=set(subprocess.check_output(['git','ls-tree','-r','--name-only',base,'android/tests','android/trial'],cwd=r,text=True).splitlines())
assert not changed.intersection(old_tests),'Historical tests remain unchanged'
print(json.dumps({'base':base,'production_delta_paths':len(production),'protected_sources':len(protected),'stable_identity':'unchanged','permissions':'unchanged','coach_catalogs':'624 IDs and40 tips unchanged','beta_build_workflow':'retired'}))
