"""Stable redesign: isolated native checks; historical runner remains unchanged."""
from pathlib import Path
from zipfile import ZipFile
import argparse, os, subprocess, hashlib, json, time

p=argparse.ArgumentParser()
p.add_argument('--serial',required=True)
p.add_argument('--locale',default='ko',choices=['ko','en'])
p.add_argument('--theme',default='dark',choices=['dark','light'])
p.add_argument('--phase',default='verify',choices=['seed','before','verify'])
p.add_argument('--long-name',action='store_true')
p.add_argument('--coach-example',action='store_true',help='Capture a synthetic linked coach tip from the actual Korean home')
p.add_argument('--output-dir',type=Path,help='Separate results directory for local follow-up checks')
p.add_argument('--class-name',default='Stable319UiNativeTest',choices=['Stable319UiNativeTest','Stable319ContinuityNativeTest','Stable319PublicUpdateNativeTest','StableAutomaticNoticeNativeTest','HomeQuotaScheduleNativeTest','HomeDesignPreviewNativeTest'])
p.add_argument('--upgrade-to', choices=['3.1.9','3.1.10','3.1.11','3.1.12'], default='3.1.9')
a=p.parse_args()
if a.coach_example and (a.class_name!='HomeDesignPreviewNativeTest' or a.locale!='ko'):
    p.error('--coach-example requires --class-name HomeDesignPreviewNativeTest --locale ko')
from_name, from_code, to_code = {'3.1.9': ('3.1.8', 95, 96), '3.1.10': ('3.1.9', 96, 97),
                                 '3.1.11': ('3.1.10', 97, 98), '3.1.12': ('3.1.11', 98, 99)}[a.upgrade_to]
to_name = a.upgrade_to
root=Path(__file__).resolve().parents[1]
out=a.output_dir or root/'build/stable319/native'
out.mkdir(parents=True,exist_ok=True)
sdk=Path(os.environ['ANDROID_HOME']);jdk=Path(os.environ['JAVA_HOME'])/'bin'
bt=sdk/'build-tools/36.0.0';adb=sdk/'platform-tools/adb.exe';android=sdk/'platforms/android-36/android.jar'
run=lambda args:subprocess.run(list(map(str,args)),check=True)
shell=lambda *args:subprocess.check_output([str(adb),'-s',a.serial,'shell',*map(str,args)],encoding='utf-8',timeout=180)
assert a.serial.startswith('emulator-')
assert shell('getprop','ro.kernel.qemu').strip()=='1'
assert shell('getprop','ro.boot.qemu.avd_name').strip() in {'gpthud_stable319','gpthud_stable319_preview','gpthud_stable3110_upgrade','gpthud_stable3111_upgrade','gpthud_stable3112_upgrade'}
target='dev.bennett.codexmeter';package='dev.bennett.codexmeter.stable319test'
assert 'package:'+target in shell('pm','list','packages',target).splitlines()
deps=[];cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
for aar in [*cache.rglob('*.aar'),*(root/'vendor').rglob('*.aar')]:
    if not aar.is_relative_to(root/'vendor') and not any(str(aar).split(os.sep)[-5].startswith(g) for g in ['androidx.','io.github.oneuiproject']):continue
    with ZipFile(aar) as z:
        if 'classes.jar' in z.namelist():
            dest=out/(hashlib.sha256(aar.read_bytes()).hexdigest()[:16]+'.jar')
            if not dest.exists():dest.write_bytes(z.read('classes.jar'))
            deps.append(dest)
deps+=list(cache.glob('androidx.*/*/*/*/*.jar'))
deps+=list(cache.glob('org.jetbrains.kotlin/kotlin-stdlib/*/*/*.jar'))
cp=[android,root/'app/build/intermediates/javac/release/compileReleaseJavaWithJavac/classes',root/'app/build/intermediates/compile_r_class_jar/release/generateReleaseRFile/R.jar',root/'shared/build/classes/java/main',*deps]
classes=out/'classes';classes.mkdir(exist_ok=True)
quote=lambda s:'"'+str(s).replace('\\','/')+'"'
sources=[root/'tests'/(a.class_name+'.java')]
if a.class_name=='Stable319UiNativeTest':
    sources += [root/'tests'/name for name in ['RecordsReviewNativeChecks.java','WeeklyLedgerBoundaryChecks.java',
                                               'GrowthDesignNativeChecks.java','WeeklyDesignNativeChecks.java']]
args=out/'javac.args';args.write_text('-encoding UTF-8 -source 17 -target 17\n-cp '+quote(os.pathsep.join(map(str,cp)))+'\n-d '+quote(classes)+'\n'+'\n'.join(map(quote,sources)),encoding='utf-8')
run([jdk/'javac.exe','-proc:none','@'+str(args)])
manifest=out/'AndroidManifest.xml'
manifest.write_text(f'<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="{package}"><uses-sdk android:minSdkVersion="26" android:targetSdkVersion="36"/><application android:label="HUD UI synthetic verification"/><instrumentation android:name="dev.bennett.codexmeter.{a.class_name}" android:targetPackage="{target}"/></manifest>',encoding='utf-8')
run([bt/'aapt2.exe','link','-I',android,'--manifest',manifest,'-o',out/'base.apk'])
dex=out/'dex';dex.mkdir(exist_ok=True)
run([bt/'d8.bat','--lib',android,'--min-api','26','--output',dex,*classes.rglob('*.class')])
with ZipFile(out/'base.apk','a') as z:z.write(dex/'classes.dex','classes.dex')
signing=Path(os.environ['GPT_HUD_SIGNING_DIR'])
run([bt/'apksigner.bat','sign','--ks',signing/'codex-meter-local.p12','--ks-key-alias','codexmeter','--ks-pass','file:'+str(signing/'password'),'--out',out/'tests.apk',out/'base.apk'])
run([adb,'-s',a.serial,'install','-r',out/'tests.apk'])
if a.class_name in {'Stable319UiNativeTest','HomeQuotaScheduleNativeTest'}:
    run([adb,'-s',a.serial,'shell','cmd','locale','set-app-locales',target,'--user','0','--locales',a.locale])
    # Initialize AppCompat locale persistence before measuring the first fresh-install screen.
    run([adb,'-s',a.serial,'shell','am','start','-W','-n',target+'/.MainActivity'])
    run([adb,'-s',a.serial,'shell','am','force-stop',target])
    run([adb,'-s',a.serial,'shell','cmd','locale','set-app-locales',target,'--user','0','--locales',a.locale])
    for key in ['window_animation_scale','transition_animation_scale','animator_duration_scale']:
        run([adb,'-s',a.serial,'shell','settings','put','global',key,'0'])
result=shell('am','instrument','-r','-w','-e','phase',a.phase,'-e','locale',a.locale,'-e','theme',a.theme,'-e','long_name',str(a.long_name).lower(),
             '-e','coach_example',str(a.coach_example).lower(),
             '-e','from_name',from_name,'-e','from_code',str(from_code),
             '-e','to_name',to_name,'-e','to_code',str(to_code),package+'/dev.bennett.codexmeter.'+a.class_name)
font=shell('settings','get','system','font_scale').strip().replace('.','-')
size=shell('wm','size').strip().splitlines()[-1].split(':')[-1].strip()
case=a.class_name+('-coach-example' if a.coach_example else '')
(out/f'{case}-{a.phase}-{a.locale}-{a.theme}-font-{font}-{size}.txt').write_text(result,encoding='utf-8')
print(result,flush=True)
if a.class_name not in {'Stable319ContinuityNativeTest','StableAutomaticNoticeNativeTest'}:
    (out/'screens').mkdir(exist_ok=True)
    folder={'HomeDesignPreviewNativeTest':'home-design','HomeQuotaScheduleNativeTest':'stable316'}.get(a.class_name,'stable319')
    run([adb,'-s',a.serial,'pull',f'/sdcard/Android/data/{target}/files/'+folder,out/'screens'])
if a.class_name=='Stable319PublicUpdateNativeTest' and a.phase=='verify':
    for attempt in range(30):
        installed=shell('dumpsys','package',target)
        if f'versionName={to_name}\n' in installed and f'versionCode={to_code} ' in installed:break
        time.sleep(1)
    assert f'versionName={to_name}\n' in installed and f'versionCode={to_code} ' in installed,'Actual package replacement missing'
    assert 'installerPackageName='+target in installed,'App must be install source'
    receipt=json.loads((out/'screens/stable319/public-update.json').read_text(encoding='utf-8'))
    assert receipt['prepared'] and receipt['click_authorized'] and receipt['to']==to_name and receipt['from']==from_name
    assert receipt['system_button_package'] in {'com.google.android.packageinstaller','com.android.packageinstaller'}
    assert receipt['system_button_text'].lower() in {'update','업데이트'}
    path=shell('pm','path',target).splitlines()[0].removeprefix('package:').strip();apk=out/'public-system-installed.apk';run([adb,'-s',a.serial,'pull',path,apk])
    assert apk.stat().st_size==receipt['bytes'] and hashlib.sha256(apk.read_bytes()).hexdigest()==receipt['sha256']
    (out/'public-system-installed.json').write_text(json.dumps({'version':to_name,'code':to_code,'sha256':receipt['sha256'],'installer':target,'actual_system_update':True},indent=2),encoding='utf-8')
    print('Actual Android update + installed public artifact identity passed. Continuity is checked separately.')
else:assert 'checks passed' in result and 'INSTRUMENTATION_CODE: -1' in result
