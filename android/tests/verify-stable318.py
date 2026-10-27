"""Current318 release checks plus unchanged core and focused redesign regressions."""
from pathlib import Path
import os,subprocess,sys
r=Path(__file__).resolve().parents[2];a=r/'android';tests=a/'tests'
for script in ['verify-stable315.py','StableRedesignGrowthChecks.py','StableRedesignSettingsTest.py']:
 subprocess.run([sys.executable,str(tests/script)],cwd=r,check=True)
out=a/'build/stable318/jvm';out.mkdir(parents=True,exist_ok=True);jar=a/'build/test-libs/json-20250517.jar';old=a/'build/stable315/jvm';jdk=Path(os.environ['JAVA_HOME'])/'bin';ext='.exe' if os.name=='nt' else ''
subprocess.run([jdk/('javac'+ext),'-encoding','UTF-8','-cp',os.pathsep.join(map(str,[jar,old])),'-d',str(out),str(tests/'Stable318ReleaseSelfTest.java')],check=True)
subprocess.run([jdk/('java'+ext),'-ea','-cp',os.pathsep.join(map(str,[out,old,jar])),'dev.bennett.codexmeter.Stable318ReleaseSelfTest',*sys.argv[1:]],check=True)
