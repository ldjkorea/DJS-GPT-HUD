"""Verify the published phone releases and the scope of the weekly quota comparison."""
import json
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[2]
previous = '83e2cf0b6a9f2b67bc19fdb7349c63d4ad9b3505'
published = 'eff2300c0e3bf7ef590fcf9710b6a54451816115'
stable = 'bfec707836b5d81a46585617ad78a77d9a9821a7'
design = 'eb813db9553941f914d83beba66cd0a2e4e3afbf'
released_3111 = 'c04f1d33703ef97476eae021627697bb82aa81cd'
released_3112 = '794a5ac37415bc5fd80e104ae2989b0aca7acfc6'
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
design_changes = paths('diff', '--name-only', stable, design)
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
assert production(design_changes) <= visual_scope, production(design_changes) - visual_scope
assert not paths('diff', '--diff-filter=D', '--name-only', stable, design)

# An installed 3.1.10 already knows how to receive this exact phone asset. Keep the accepted
# screens, package ID, existing updater, authentication and storage byte-for-byte at release.
metadata = {
    'android/app/build.gradle.kts': [('versionCode = 97', 'versionCode = 98'),
                                     ('versionName = "3.1.10"', 'versionName = "3.1.11"')],
    app + 'AppConstants.java': [('VERSION_CODE = 97;', 'VERSION_CODE = 98;'),
                                ('VERSION_NAME = "3.1.10";', 'VERSION_NAME = "3.1.11";')],
    'android/build.sh': [('VERSION_NAME="3.1.10"', 'VERSION_NAME="3.1.11"')],
}
release_3111 = paths('diff', '--name-only', design, released_3111)
assert production(release_3111) == set(metadata) | {history}, production(release_3111) ^ (set(metadata) | {history})
for file, replacements in metadata.items():
    expected = git('show', design + ':' + file).decode('utf-8')
    for before, after in replacements:
        assert expected.count(before) == 1, (file, before)
        expected = expected.replace(before, after)
    assert git('show', released_3111 + ':' + file) == expected.encode('utf-8'), file
new_history = json.loads(git('show', released_3111 + ':' + history))
first = new_history[0]
assert new_history[1:] == latest and first['version'] == '3.1.11', 'Earlier release history changed'
assert first['source'] == 'https://github.com/ldjkorea/DJS-GPT-HUD/releases/tag/v3.1.11'
assert all(first[lang]['title'] and first[lang]['changes'] for lang in ('ko', 'en'))
assert not paths('diff', '--diff-filter=D', '--name-only', stable, released_3111)

# Check the source and metadata actually shipped for 3.1.12.
quota_scope = {app + name + '.java' for name in (
    'BetaHomeDetailsUi', 'BetaQuotaUi', 'MainActivity', 'UsageParser',
    'WeeklyQuotaIndex', 'WeeklyQuotaIndexUi')}
quota_scope.add('android/shared/src/main/java/dev/bennett/codexmeter/UsageSnapshot.java')
quota_scope.update(res + folder + '/quota_index_strings.xml' for folder in ('values', 'values-ko'))
quota_changes = paths('diff', '--name-only', released_3111, released_3112)
for file in metadata:
    prior = git('show', released_3111 + ':' + file).decode('utf-8')
    assert git('show', released_3112 + ':' + file).decode('utf-8') == prior.replace('3.1.11', '3.1.12').replace('= 98', '= 99'), file
current_history = json.loads(git('show', released_3112 + ':' + history))
assert current_history[1:] == new_history and current_history[0]['version'] == '3.1.12'
assert current_history[0]['source'] == 'https://github.com/ldjkorea/DJS-GPT-HUD/releases/tag/v3.1.12'
assert all(current_history[0][lang]['title'] and current_history[0][lang]['changes'] for lang in ('ko', 'en'))
quota_scope.update(set(metadata) | {history})
assert production(quota_changes) == quota_scope, production(quota_changes) ^ quota_scope
assert not paths('diff', '--diff-filter=D', '--name-only', released_3111, released_3112)

# 3.1.13 changes the accepted presentation and phone release metadata.
# The parser, account storage, updater and companion applications stay protected.
presentation_scope = {app + name + '.java' for name in (
    'BetaHomeDetailsUi', 'BetaQuotaUi', 'BetaSettingsUi', 'BetaWeeklyUi', 'WeeklyQuotaIndexUi',
    'UsageInsightDisplay', 'CoachUi', 'SpicyQuotes')}
shared = 'android/shared/src/main/java/dev/bennett/codexmeter/'
presentation_scope.update(shared + name + '.java' for name in ('UsagePrecision', 'CoachCatalog'))
presentation_scope.update(res + folder + '/quota_index_strings.xml' for folder in ('values', 'values-ko'))
for file in metadata:
    prior = git('show', released_3112 + ':' + file).decode('utf-8')
    expected = prior.replace('3.1.12', '3.1.13').replace('= 99', '= 100')
    assert expected != prior and (root / file).read_text('utf-8') == expected, file
candidate_history = json.loads((root / history).read_text('utf-8'))
assert candidate_history[1:] == current_history, 'Prior release notes changed'
candidate = candidate_history[0]
assert candidate['version'] == '3.1.13'
assert candidate['source'] == 'https://github.com/ldjkorea/DJS-GPT-HUD/releases/tag/v3.1.13'
assert all(candidate[lang]['title'] and candidate[lang]['changes'] for lang in ('ko', 'en'))
presentation_scope.update(set(metadata) | {history})
working = paths('diff', '--name-only', released_3112) | paths('ls-files', '-o', '--exclude-standard')
assert production(working) == presentation_scope, production(working) ^ presentation_scope
assert not paths('diff', '--diff-filter=D', '--name-only', released_3112)
subprocess.run([sys.executable, str(root / 'android/tests/verify-coach-design.py')], check=True)
print('Historical releases through 3.1.12 and 3.1.13/code100 metadata, history and presentation scope passed.')
