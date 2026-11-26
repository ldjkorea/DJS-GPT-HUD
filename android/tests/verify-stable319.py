"""Core and redesign regressions, published releases and weekly quota index checks."""
from pathlib import Path
import argparse,os,subprocess,sys
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('historical_feed', nargs='?', type=Path, help='3.1.10 release feed for historical checks')
parser.add_argument('--release-3111-json', type=Path, help='Actual 3.1.11 public update response')
parser.add_argument('--release-3112-json', type=Path)
args = parser.parse_args()
r=Path(__file__).resolve().parents[2];a=r/'android';tests=a/'tests'
for script in ['verify-stable315.py','StableRedesignGrowthChecks.py','StableRedesignSettingsTest.py']:
 subprocess.run([sys.executable,str(tests/script)],cwd=r,check=True)
out=a/'build/stable319/jvm';out.mkdir(parents=True,exist_ok=True);jar=a/'build/test-libs/json-20250517.jar';old=a/'build/stable315/jvm';jdk=Path(os.environ['JAVA_HOME'])/'bin';ext='.exe' if os.name=='nt' else ''
classes = ['Stable319ReleaseSelfTest', 'Stable319To3110ReleaseTest', 'Stable3110To3111ReleaseTest',
           'Stable3111To3112ReleaseTest', 'WeeklyQuotaIndexSelfTest']
app = a/'app/src/main/java/dev/bennett/codexmeter'
subprocess.run([jdk/('javac'+ext), '-encoding', 'UTF-8', '-cp', os.pathsep.join(map(str,[jar,old])),
                '-d', str(out), str(app/'WeeklyQuotaIndex.java'), str(app/'UsageParser.java'),
                *(str(tests/(name+'.java')) for name in classes)], check=True)
feeds = {'Stable3111To3112ReleaseTest': args.release_3112_json, 'Stable319To3110ReleaseTest': args.historical_feed, 'Stable3110To3111ReleaseTest': args.release_3111_json}
for name in classes:
    live_response = [feeds[name]] if feeds.get(name) else []
    subprocess.run([jdk/('java'+ext), '-ea', '-cp', os.pathsep.join(map(str,[out,old,jar])),
                    'dev.bennett.codexmeter.'+name, *live_response], check=True)
