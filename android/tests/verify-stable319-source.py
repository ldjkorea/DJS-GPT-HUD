"""Verify the two public release snapshots and the subsequent phone design changes."""
import json
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[2]
previous = '83e2cf0b6a9f2b67bc19fdb7349c63d4ad9b3505'
published = 'eff2300c0e3bf7ef590fcf9710b6a54451816115'
stable = 'bfec707836b5d81a46585617ad78a77d9a9821a7'
app = 'android/app/src/main/java/dev/bennett/codexmeter/'
history = 'android/app/src/main/assets/release-history.json'


def git(*args):
    return subprocess.check_output(['git', *args], cwd=root)


def paths(*args):
    return set(git(*args).decode('utf-8').splitlines())


def production(changes):
    return {p for p in changes if p.startswith(('android/app/', 'android/shared/', 'android/wear/',
                                                'windows-widget/', 'ios/'))
            or p in {'android/build.sh', 'android/build.gradle.kts'}}


release = paths('diff', '--name-only', previous, published)
release_scope = {app + n + '.java' for n in ('BetaWeeklyUi', 'BetaQuotaUi', 'BetaSettingsUi',
                                             'DashboardReorderActivity', 'BetaCompactGrowthUi', 'AppConstants')}
release_scope.update(('android/app/build.gradle.kts', 'android/build.sh', history))
assert production(release) == release_scope, production(release) ^ release_scope
old_tests = paths('ls-tree', '-r', '--name-only', previous, 'android/tests', 'android/trial')
assert not release & old_tests, release & old_tests
for name in ('MainActivity', 'BetaHomeSummaryUi', 'FunHome', 'CoachUi', 'BetaWeeklyReader', 'BetaNextGoal'):
    path = app + name + '.java'
    assert git('show', published + ':' + path) == git('show', previous + ':' + path), name
rows = json.loads(git('show', published + ':' + history))
assert rows[0]['version'] == '3.1.9' and rows[1:] == json.loads(git('show', previous + ':' + history))
assert b'versionCode = 96' in git('show', published + ':android/app/build.gradle.kts')

review = paths('diff', '--name-only', published, stable)
review_scope = {app + n + '.java' for n in ('RecordsActivity', 'BetaMedalUi', 'BetaCompactGrowthUi',
                                            'BetaMedalStore', 'BetaWeeklyUi', 'BetaWeeklyReader',
                                            'UsageLedgerDatabase', 'GrowthActivity', 'BetaGrowthUi',
                                            'BetaGrowthStore', 'BetaHomeUi', 'BetaQuotaUi', 'CoachUi',
                                            'AppConstants')}
review_scope.update(('android/app/build.gradle.kts', 'android/build.sh', history))
assert production(review) <= review_scope, production(review) - review_scope
assert not paths('diff', '--diff-filter=D', '--name-only', published, stable)
published_tests = paths('ls-tree', '-r', '--name-only', published, 'android/tests', 'android/trial')
updated_tests = {'android/tests/Stable319UiNativeTest.java', 'android/tests/run-stable319-native.py',
                 'android/tests/Stable319ContinuityNativeTest.java', 'android/tests/Stable319PublicUpdateNativeTest.java',
                 'android/tests/verify-stable319-source.py', 'android/tests/verify-stable319.py'}
assert not (review & published_tests) - updated_tests, (review & published_tests) - updated_tests
latest = json.loads(git('show', stable + ':' + history))
assert latest[1:] == rows, 'Earlier release history changed'
assert latest[0]['version'] == '3.1.10'
assert latest[0]['source'] == 'https://github.com/ldjkorea/DJS-GPT-HUD/releases/tag/v3.1.10'
for file, values in {
    'android/app/build.gradle.kts': ('versionCode = 97', 'versionName = "3.1.10"'),
    app + 'AppConstants.java': ('VERSION_CODE = 97;', 'VERSION_NAME = "3.1.10";'),
    'android/build.sh': ('VERSION_NAME="3.1.10"',),
}.items():
    contents = git('show', stable + ':' + file).decode('utf-8')
    assert all(value in contents for value in values), file
current = paths('diff', '--name-only', stable) | paths('ls-files', '-o', '--exclude-standard')
visual_scope = {app + name + '.java' for name in (
    'BetaCompactGrowthUi', 'BetaHomeUi', 'BetaHomeSummaryUi', 'BetaMedalUi', 'BetaQuotaUi',
    'BetaSettingsUi', 'BetaTierDialog', 'BetaUi', 'BetaWeeklyUi', 'CoachUi', 'FunHome', 'LedgerUi',
    'MainActivity', 'MatteNav', 'RecordsActivity', 'TierCompanionView', 'TierTheme', 'Ui')}
res = 'android/app/src/main/res/'
visual_scope.update(res + folder + '/' + file for folder in ('values', 'values-night')
                    for file in ('colors.xml', 'styles.xml'))
visual_scope.update(res + folder + '/ui_v3_strings.xml' for folder in ('values', 'values-ko'))
visual_scope.update(res + 'drawable/' + file + '.xml' for file in (
    'samjogo_mark', 'gpt_hud_launcher_background', 'gpt_hud_launcher_foreground', 'ic_launcher_monochrome',
    'about_gradient_bg'))
visual_scope.update('android/app/src/main/assets/' + f for f in ('coach-ko-v2.json', 'coach-ko-medals-v1.json'))
assert production(current) <= visual_scope, production(current) - visual_scope
assert not paths('diff', '--diff-filter=D', '--name-only', stable)
subprocess.run([sys.executable, str(root / 'android/tests/verify-coach-design.py')], check=True)
print('Public 3.1.9 and 3.1.10 contracts, current design scope and historical fixtures passed.')
