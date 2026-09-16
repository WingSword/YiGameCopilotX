"""Validate the AppGallery APP signature and its contained module manifest."""
import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

root = Path(__file__).resolve().parents[1]
out = root / 'artifacts/roundtable-20260906'
app = out / 'YiGameCopilotX-Harmony-1.5-8-appgallery-release.app'
with zipfile.ZipFile(app) as archive:
    assert archive.testzip() is None
    pack = json.loads(archive.read('pack.info'))
    assert pack['summary']['app']['bundleName'] == 'org.walks.gamecopilot'
    assert pack['summary']['app']['version'] == {'code': 8, 'name': '1.5'}
    haps = [name for name in archive.namelist() if name.endswith('.hap')]
    assert len(haps) == 1
    hap = out / 'yigame-harmony-release-module.hap'
    hap.write_bytes(archive.read(haps[0]))
with zipfile.ZipFile(hap) as archive:
    assert archive.testzip() is None
    manifest = json.loads(archive.read('module.json'))
    assert manifest['app']['versionCode'] == 8
    assert manifest['app']['versionName'] == '1.5'
    assert manifest['app'].get('debug', False) is False
java = Path('C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe')
signer = Path('C:/Program Files/Huawei/DevEco Studio/sdk/default/openharmony/toolchains/lib/hap-sign-tool.jar')
with (out / 'release-signature-check.log').open('w', encoding='utf-8') as log:
    subprocess.run([str(java), '-jar', str(signer), 'verify-app', '-inFile', str(app), '-inForm', 'zip',
                    '-outCertChain', str(out / 'release-verified-chain.cer'),
                    '-outProfile', str(out / 'release-verified-profile.p7b')],
                   stdout=log, stderr=subprocess.STDOUT, check=True)
log_text = (out / 'release-signature-check.log').read_text(encoding='utf-8')
assert 'verify-app success' in log_text
with (out / 'release-profile-check.log').open('w', encoding='utf-8') as log:
    subprocess.run([str(java), '-jar', str(signer), 'verify-profile',
                    '-inFile', str(out / 'release-verified-profile.p7b'),
                    '-outFile', str(out / 'release-profile-verification.json')],
                   stdout=log, stderr=subprocess.STDOUT, check=True)
profile_result = json.loads((out / 'release-profile-verification.json').read_text(encoding='utf-8'))
assert profile_result['verifiedPassed'] is True
profile = profile_result['content']
if isinstance(profile, str):
    profile = json.loads(profile)
assert profile['type'] == 'release'
assert profile['app-distribution-type'] == 'app_gallery'
artifacts = [app, out / 'yigame-android-debug.apk', out / 'yigame-harmony-device-debug.hap']
(out / 'SHA256SUMS.txt').write_text(''.join(
    f'{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}\n' for path in artifacts), encoding='utf-8')
(out / 'release-package-check.json').write_text(json.dumps({
    'bundleName': manifest['app']['bundleName'],
    'versionName': manifest['app']['versionName'],
    'versionCode': manifest['app']['versionCode'],
    'debug': manifest['app'].get('debug', False),
    'appBytes': app.stat().st_size,
    'apiVersion': pack['summary']['modules'][0]['apiVersion'],
    'signature': 'verify-app success; release profile',
}, ensure_ascii=False, indent=2), encoding='utf-8')
print('PASS: release APP 1.5 (8), non-debug HAP, archive integrity, release signature and artifact hashes')
