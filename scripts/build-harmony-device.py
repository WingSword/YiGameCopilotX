"""Build with the existing AGC device debug credentials, restoring the project signing configuration."""
import argparse, json, os, shutil, subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
project = root.parent / 'YiGameCopilotX-Harmony'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--channel', choices=('domestic', 'direct'), default='domestic')
parser.add_argument('--output-dir', type=Path, default=root / 'artifacts/harmony-device')
args = parser.parse_args()
out = args.output_dir.resolve() / args.channel
out.mkdir(parents=True, exist_ok=True)
profile = project / 'build-profile.json5'
original = profile.read_bytes()
config = json.loads(original.decode('utf-8-sig'))
material = config['app']['signingConfigs'][0]['material']
for key, name in [('profile','agc-debug-profile.p7b'),('certpath','agc-device-debug-chain.cer')]:
    credential = project / 'signing' / name
    if not credential.is_file(): raise FileNotFoundError(name)
    material[key] = str(credential)
temporary = (json.dumps(config,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
deveco = Path('C:/Program Files/Huawei/DevEco Studio')
environment = os.environ.copy()
environment['YIGAME_DISTRIBUTION'] = args.channel
environment['DEVECO_SDK_HOME'] = str(deveco/'sdk')
environment['NODE_HOME'] = str(deveco/'tools/node')
profile.write_bytes(temporary)
try:
    with (out/'harmony-device-build.log').open('w',encoding='utf-8') as log:
        subprocess.run([str(deveco/'tools/node/node.exe'),str(deveco/'tools/hvigor/bin/hvigorw.js'),
            '--mode','module','-p','product=default','-p','module=entry@default','clean','assembleHap','--no-daemon'],
            cwd=project,env=environment,stdout=log,stderr=subprocess.STDOUT,check=True)
    generated = (project/'entry/build/default/generated/profile/default/BuildProfile.ets').read_text(encoding='utf-8')
    assert f"DISTRIBUTION_CHANNEL = '{args.channel}'" in generated, 'Unexpected build channel'
    target=out/f'yigame-harmony-{args.channel}-device-debug.hap'
    shutil.copy2(project/'entry/build/default/outputs/default/entry-default-signed.hap',target)
    print('Device debug package: '+str(target))
finally:
    if profile.read_bytes() == temporary:
        profile.write_bytes(original)
    else:
        raise RuntimeError('Signing configuration was edited during the build; current contents preserved.')
