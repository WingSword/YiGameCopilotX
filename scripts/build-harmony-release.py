"""Build and verify an AppGallery package using the installed Release SDK."""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output-dir', type=Path, default=root / 'artifacts/internal-test-20260908')
parser.add_argument('--channel', choices=('domestic',), default='domestic',
                    help='AppGallery packages always disable rooms; use the device builder for full testing.')
args = parser.parse_args()
project = root.parent / 'YiGameCopilotX-Harmony'
toolchain = project / '.release-tools/command-line-tools'
sdk = toolchain / 'sdk/default'
out = args.output_dir.resolve()
out.mkdir(parents=True, exist_ok=True)
metadata = json.loads((sdk / 'sdk-pkg.json').read_text(encoding='utf-8'))['data']
assert metadata['releaseType'] == 'Release', 'Publishing requires a Release SDK'
for package in ['openharmony/ets/oh-uni-package.json', 'openharmony/toolchains/oh-uni-package.json', 'hms/ets/uni-package.json']:
    assert json.loads((sdk / package).read_text(encoding='utf-8'))['releaseType'] == 'Release'
config = json.loads((project / 'build-profile.json5').read_text(encoding='utf-8'))
assert config['app']['products'][0]['signingConfig'] == 'release'
version = json.loads((project / 'AppScope/app.json5').read_text(encoding='utf-8'))['app']
node = Path('C:/Program Files/Huawei/DevEco Studio/tools/node/node.exe')
java = Path('C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe')
env = os.environ.copy()
env['YIGAME_DISTRIBUTION'] = args.channel
env['DEVECO_SDK_HOME'] = str(toolchain / 'sdk')
env['NODE_HOME'] = str(node.parent)
env['JAVA_HOME'] = str(java.parent.parent)
with (out / 'harmony-release-build.log').open('w', encoding='utf-8') as log:
    subprocess.run([str(node), str(toolchain / 'hvigor/bin/hvigorw.js'), '--mode', 'project',
                    '-p', 'product=default', '-p', 'buildMode=release', 'clean', 'assembleApp', '--no-daemon'],
                   cwd=project, env=env, stdout=log, stderr=subprocess.STDOUT, check=True)
generated = (project / 'entry/build/default/generated/profile/default/BuildProfile.ets').read_text(encoding='utf-8')
assert "DISTRIBUTION_CHANNEL = 'domestic'" in generated, 'Refusing to publish a full build to AppGallery'
app = out / f"YiGameCopilotX-Harmony-{version['versionName']}-{version['versionCode']}-domestic-appgallery-release.app"
shutil.copy2(project / 'build/outputs/default/YiGameCopilotX-Harmony-default-signed.app', app)
with zipfile.ZipFile(app) as archive:
    assert archive.testzip() is None
    pack = json.loads(archive.read('pack.info'))
    assert pack['summary']['app']['version'] == {'code': version['versionCode'], 'name': version['versionName']}
    api = [module['apiVersion'] for module in pack['summary']['modules']]
    assert all(item['releaseType'] == 'Release' for item in api), api
    for name in archive.namelist():
        if name.endswith('.hap'):
            with zipfile.ZipFile(io.BytesIO(archive.read(name))) as hap:
                assert hap.testzip() is None
                manifest = json.loads(hap.read('module.json'))
                assert manifest['app']['versionCode'] == version['versionCode']
                assert manifest['app']['versionName'] == version['versionName']
                assert not manifest['app'].get('debug', False)
signer = sdk / 'openharmony/toolchains/lib/hap-sign-tool.jar'
with (out / 'release-signature-check.log').open('w', encoding='utf-8') as log:
    subprocess.run([str(java), '-jar', str(signer), 'verify-app', '-inFile', str(app), '-inForm', 'zip',
                    '-outCertChain', str(out / 'release-verified-chain.cer'),
                    '-outProfile', str(out / 'release-verified-profile.p7b')], stdout=log, stderr=subprocess.STDOUT, check=True)
assert 'verify-app success' in (out / 'release-signature-check.log').read_text(encoding='utf-8')
with (out / 'release-profile-check.log').open('w', encoding='utf-8') as log:
    subprocess.run([str(java), '-jar', str(signer), 'verify-profile', '-inFile', str(out / 'release-verified-profile.p7b'),
                    '-outFile', str(out / 'release-profile-verification.json')], stdout=log, stderr=subprocess.STDOUT, check=True)
verified = json.loads((out / 'release-profile-verification.json').read_text(encoding='utf-8'))
assert verified['verifiedPassed']
profile = verified['content']
profile = json.loads(profile) if isinstance(profile, str) else profile
assert profile['type'] == 'release'
assert profile['app-distribution-type'] == 'app_gallery'
digest = hashlib.sha256(app.read_bytes()).hexdigest()
result = {'distributionChannel': args.channel, 'roomsEnabled': False, 'bundleName': version['bundleName'], 'versionName': version['versionName'],
          'versionCode': version['versionCode'], 'appBytes': app.stat().st_size,
          'sdkVersion': metadata['version'], 'apiVersion': api, 'signatureVerified': True,
          'profileType': 'release/app_gallery', 'sha256': digest}
(out / 'release-package-check.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
(out / 'SHA256SUMS.txt').write_text(f'{digest}  {app.name}\n', encoding='utf-8')
print(json.dumps(result, ensure_ascii=False))
