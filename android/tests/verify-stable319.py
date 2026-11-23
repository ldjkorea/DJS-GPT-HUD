"""Core and redesign regressions, the historical 3.1.9 contract and the 3.1.10 update."""
from pathlib import Path
import os,subprocess,sys
r=Path(__file__).resolve().parents[2];a=r/'android';tests=a/'tests'
for script in ['verify-stable315.py','StableRedesignGrowthChecks.py','StableRedesignSettingsTest.py']:
 subprocess.run([sys.executable,str(tests/script)],cwd=r,check=True)
out=a/'build/stable319/jvm';out.mkdir(parents=True,exist_ok=True);jar=a/'build/test-libs/json-20250517.jar';old=a/'build/stable315/jvm';jdk=Path(os.environ['JAVA_HOME'])/'bin';ext='.exe' if os.name=='nt' else ''
classes = ['Stable319ReleaseSelfTest', 'Stable319To3110ReleaseTest']
subprocess.run([jdk/('javac'+ext), '-encoding', 'UTF-8', '-cp', os.pathsep.join(map(str,[jar,old])),
                '-d', str(out), *(str(tests/(name+'.java')) for name in classes)], check=True)
for name in classes:
    live_response = sys.argv[1:] if name == 'Stable319To3110ReleaseTest' else []
    subprocess.run([jdk/('java'+ext), '-ea', '-cp', os.pathsep.join(map(str,[out,old,jar])),
                    'dev.bennett.codexmeter.'+name, *live_response], check=True)
