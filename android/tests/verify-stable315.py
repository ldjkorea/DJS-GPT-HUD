"""Current stable UI/release contracts; historical fixtures remain untouched."""
from pathlib import Path
import os,subprocess,sys
r=Path(__file__).resolve().parents[2];a=r/'android';out=a/'build/stable315/jvm';out.mkdir(parents=True,exist_ok=True)
jar=a/'build/test-libs/json-20250517.jar'
if not jar.exists():
 import urllib.request
 jar.parent.mkdir(parents=True,exist_ok=True);urllib.request.urlretrieve('https://repo1.maven.org/maven2/org/json/json/20250517/json-20250517.jar',jar)
app=a/'app/src/main/java/dev/bennett/codexmeter';jdk=Path(os.environ['JAVA_HOME'])/'bin';ext='.exe' if os.name=='nt' else ''
files=list((a/'shared/src/main/java').rglob('*.java'))+[app/(n+'.java') for n in ['HudUiModel','BetaV4','BetaCatalog','BetaMedalModel','BetaMedalCoach','GitHubReleaseParser','GitHubRelease','GitHubReleaseSource','UpdateChannel','ReleaseVersion']]
tests=[('Stable315UiSelfTest',[]),('Stable315ReleaseSelfTest',sys.argv[1:]),('MxpEconomySelfTest',[]),('CoachV2SelfTest',['coach-ko-v2.json']),('CoachReliabilitySelfTest',['coach-ko-v2.json']),('CoachBanterSelfTest',['coach-ko-v2.json','coach-ko-medals-v1.json'])]
files += [a/'tests'/(name+'.java') for name,_ in tests]+[a/'trial/Beta9911SelfTest.java']
q=lambda p:'"'+str(p).replace('\\','/')+'"';args=out/'javac.args';args.write_text('-encoding UTF-8\n-cp '+q(jar)+'\n-d '+q(out)+'\n'+'\n'.join(map(q,files)),encoding='utf-8')
subprocess.run([jdk/('javac'+ext),'@'+str(args)],check=True)
tests.append(('Beta9911SelfTest',['coach-ko-v2.json','coach-ko-medals-v1.json']))
for name,extra in tests:
 extra=[str(a/'app/src/main/assets'/x) if x.startswith('coach-ko-') else x for x in extra]
 result=subprocess.check_output([jdk/('java'+ext),'-Xmx768m','-ea','-cp',os.pathsep.join(map(str,[out,jar])),'dev.bennett.codexmeter.'+name,*extra],encoding='utf-8');print(result.strip(),flush=True);(out/(name+'.txt')).write_text(result,encoding='utf-8')
