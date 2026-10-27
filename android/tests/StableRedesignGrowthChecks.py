"""Run focused production calendar navigation checks without changing historical fixtures."""
from pathlib import Path
import os
import subprocess

root = Path(__file__).resolve().parents[1]
out = root / 'build/stable-redesign/growth-jvm'
out.mkdir(parents=True, exist_ok=True)
app = root / 'app/src/main/java/dev/bennett/codexmeter'
jar = root / 'build/test-libs/json-20250517.jar'
if not jar.is_file():
    raise SystemExit('Existing org.json test jar required: ' + str(jar))
jdk = Path(os.environ['JAVA_HOME']) / 'bin'
suffix = '.exe' if os.name == 'nt' else ''
files = [app/'HudUiModel.java', app/'GrowthCalendarNavigation.java', root/'tests/StableRedesignGrowthSelfTest.java']
subprocess.run([str(jdk/('javac'+suffix)), '-encoding', 'UTF-8', '-cp', str(jar), '-d', str(out), *map(str, files)], check=True)
subprocess.run([str(jdk/('java'+suffix)), '-ea', '-cp', os.pathsep.join(map(str, [out, jar])), 'dev.bennett.codexmeter.StableRedesignGrowthSelfTest'], check=True)
