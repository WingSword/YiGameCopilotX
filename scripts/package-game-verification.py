"""Package locally verified binaries and the review report, without runtime data or credentials."""
import argparse,hashlib,json,shutil,subprocess,sys,zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--suite',choices=['one-night','cloud-expansion','cloud-six-web','room-invites','cloud-live'],default='one-night')
suite=parser.parse_args().suite
live=suite=='cloud-live'
invites=suite=='room-invites' or live
six=suite in ['cloud-six-web','room-invites'] or live
expanded=suite in ['cloud-expansion','cloud-six-web','room-invites'] or live
evidence='cloud-live-20260916' if live else suite if expanded else 'one-night-20260910'
out=root/'artifacts'/evidence/'release'
out.mkdir(parents=True,exist_ok=True)
def log(name):
    data=(root/'artifacts'/name).read_bytes()
    return data.decode('utf-16' if data.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf-8-sig')
six_names={'android-build':'android-web-build' if invites else 'android-web-final-build','harmony-build':'harmony-build','server-tests':'server-tests','client-tests':'client-contracts','offline-tests':'offline-tests','cross-platform':'cross-platform'}
logs={name:log((('room-invites' if live else suite)+'/'+six_names[name] if six else 'cloud-expansion/'+name if expanded else 'one-night-'+legacy)+'.log') for name,legacy in
      [('android-build','android-build'),('harmony-build','harmony-build'),('server-tests','server-build'),
       ('client-tests','client-tests'),('offline-tests','offline-tests'),('cross-platform','cross-platform')]}
if live:
    # Unchanged server/offline engines retain the previous full rule suite evidence;
    # changed clients, UI and answer book require this round's own verification.
    for name, filename in {'android-build':'android-web-final-build', 'harmony-build':'harmony-final-build',
                           'client-tests':'compatibility-client-tests-final', 'cross-platform':'cross-platform'}.items():
        logs[name]=log(evidence+'/'+filename+'.log')
    assert 'PASS' in log(evidence+'/answer-book-tests.log')
    remote=json.loads((root/'artifacts'/evidence/'api-final/live-cloud-results.json').read_text(encoding='utf-8'))
    assert {r['game'] for r in remote['results'] if r['status']=='PASS'} == {'spy','werewolf','hunt','ledger','avalon','drawing'}
    assert not remote['remainingOwnedRooms'] and all(r['status']=='deleted' for r in remote['cleanup'])
assert 'BUILD SUCCESSFUL' in logs['android-build'] and 'BUILD SUCCESSFUL' in logs['harmony-build']
assert '128630 one-night checks' in logs['server-tests'] and 'Built dist/yigame-room-server.jar' in logs['server-tests']
assert 'PASS live one-night' in logs['client-tests']
assert 'PASS Kotlin offline' in logs['offline-tests'] and 'PASS Harmony offline' in logs['offline-tests']
assert 'no unreviewed source changes' in logs['cross-platform']
if expanded:
    assert '378675 cloud expansion checks' in logs['server-tests']
    for kind in ['ledger','hunt']:
        for host in ['Kotlin','Harmony']:assert 'PASS live '+kind+' '+host+' host' in logs['client-tests']
    for platform in ['Kotlin','Harmony']:assert 'PASS '+platform+' ledger' in logs['offline-tests']
if six:
    assert '98056 six-game checks' in logs['server-tests']
    assert ':composeApp:wasmJsBrowserDistribution' in logs['android-build']
    for kind in ['avalon','drawing']:
        for host in ['Kotlin','Harmony']:assert 'PASS live '+kind+' '+host+' host' in logs['client-tests']
if invites:
    assert '207 room invitation checks' in logs['server-tests']
    assert 'PASS Kotlin invitations/view policy: 69 checks' in logs['client-tests']
artifacts={
    'YiGameCopilotX-Android-direct-debug.apk':root/'composeApp/build/outputs/apk/direct/debug/composeApp-direct-debug.apk',
    'YiGameCopilotX-Harmony-debug.hap':root.parent/'YiGameCopilotX-Harmony/entry/build/default/outputs/default/entry-default-signed.hap',
    'yigame-room-server.jar':root/'server/dist/yigame-room-server.jar',
    'GAME_QA_REPORT.md':root/'docs'/('CLOUD_LIVE_UI_QA.md' if live else 'ROOM_INVITATIONS_QA.md' if invites else 'SIX_CLOUD_WEB_QA.md' if six else 'CLOUD_LEDGER_HUNT_QA.md' if expanded else 'ONE_NIGHT_CLOUD_AND_OFFLINE_QA.md'),
    'SERVER_README.md':root/'server/README.md'
}
if live:
    artifacts['KMP_HARMONY_MAINLINE.md']=root/'docs/KMP_HARMONY_MAINLINE.md'
for name,source in artifacts.items():
    shutil.copy2(source,out/name)
    if source.suffix in ['.apk','.hap','.jar']:
        with zipfile.ZipFile(source) as archive:assert archive.testzip() is None
subprocess.run([sys.executable,str(root/'scripts/package-room-server.py'),'--output-dir',str(out)],check=True)
names=list(artifacts)+['yigame-room-server.zip']
if six:
    web=root/'composeApp/build/dist/wasmJs/productionExecutable'
    assert (web/'index.html').is_file() and (web/'composeApp.js').is_file()
    assert list(web.glob('*.wasm'))
    assert all('node_modules' not in p.parts for p in web.rglob('*'))
    with zipfile.ZipFile(out/'YiGameCopilotX-Web.zip','w',zipfile.ZIP_DEFLATED) as archive:
        for p in sorted(web.rglob('*')):
            if p.is_file():archive.write(p,p.relative_to(web).as_posix())
    shutil.copy2(root/'docs/WEB_AND_SIX_CLOUD_GAMES.md',out/'WEB_DEPLOYMENT.md')
    names += ['YiGameCopilotX-Web.zip','WEB_DEPLOYMENT.md']
checks=[{'file':name,'bytes':(out/name).stat().st_size,'sha256':hashlib.sha256((out/name).read_bytes()).hexdigest()} for name in names]
(out/'SHA256SUMS.txt').write_text(''.join(f"{c['sha256']}  {c['file']}\n" for c in checks),encoding='utf-8')
manifest={'suite':suite,'verification':'local Java service, production clients, Android/Harmony emulators' + (', production Wasm browser UI' if six else ''),
    'sourceReviewSha256':hashlib.sha256((root/'cross-platform/review-state.json').read_bytes()).hexdigest(),'files':checks}
if live:
    manifest['deployedApi']={'server':remote['server'],'checks':remote['checks'],'requests':remote['requests'],'capabilities':remote['deployment']}
    manifest['limitations']=['Deployed server lacks invite and hostview; new feature UI verified against local latest Java service.',
                            'Unchanged server and offline engine rule evidence retained from room-invites suite.']
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(checks,ensure_ascii=False,indent=2))
