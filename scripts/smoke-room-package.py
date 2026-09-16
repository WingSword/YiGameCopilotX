"""Extract and launch the delivered JAR in a fresh directory; no installed server dependencies."""
import argparse, json, os, secrets, subprocess, tempfile, time, urllib.request, zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output-dir',type=Path,default=root/'artifacts/refinement-20260906')
out=parser.parse_args().output_dir.resolve()
java=Path(os.environ.get('JAVA_HOME',r'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'))/'bin/java.exe'
with tempfile.TemporaryDirectory(prefix='package-smoke-',dir=out) as temp:
    directory=Path(temp)
    with zipfile.ZipFile(out/'yigame-room-server.zip') as archive:
        for n in archive.namelist(): assert (directory/n).resolve().is_relative_to(directory.resolve())
        archive.extractall(directory)
    package=directory/'yigame-room-server'
    env=dict(os.environ,HOST='127.0.0.1',PORT='18081',ADMIN_TOKEN=secrets.token_urlsafe(32),DATA_DIR=str(package/'data'),PUBLIC_WEB_URL='https://play.example.com/')
    startup=subprocess.STARTUPINFO();startup.dwFlags|=subprocess.STARTF_USESHOWWINDOW;startup.wShowWindow=subprocess.SW_HIDE
    with (out/'package-smoke-server.log').open('w') as log:
        proc=subprocess.Popen([str(java),'-jar',str(package/'yigame-room-server.jar')],cwd=package,env=env,
                              stdout=log,stderr=log,startupinfo=startup,creationflags=subprocess.CREATE_NO_WINDOW)
        try:
            for attempt in range(30):
                try:
                    with urllib.request.urlopen('http://127.0.0.1:18081/health',timeout=1) as r: health=json.load(r)
                    break
                except OSError:
                    if proc.poll() is not None: raise RuntimeError('Packaged JAR exited before health check')
                    time.sleep(.2)
            else: raise RuntimeError('Packaged JAR did not become healthy')
            assert health=={'status':'ok','protocol':1}
            body={'nickname':'PackageQA','roomKey':'','requestId':secrets.token_urlsafe(36),'gameType':'spy'}
            req=urllib.request.Request('http://127.0.0.1:18081/api/v1/rooms',json.dumps(body).encode(),{'Content-Type':'application/json'})
            with urllib.request.urlopen(req,timeout=5) as r: entry=json.load(r)
            assert len(entry['room']['roomId'])==6 and len(entry['roomKey'])>=6 and entry['room']['identity'] is None
            assert (package/'data/rooms.json').is_file()
            req=urllib.request.Request('http://127.0.0.1:18081/api/v1/rooms/'+entry['room']['roomId']+'/invite',
                json.dumps({'serverUrl':'http://8.133.216.39:8080'}).encode(),
                {'Content-Type':'application/json','Authorization':'Bearer '+entry['token']})
            with urllib.request.urlopen(req,timeout=5) as r: invitation=json.load(r)
            assert invitation['url'].startswith('https://play.example.com/#room=') and len(invitation['modules'])>=21
            with zipfile.ZipFile(package/'yigame-room-server.jar') as jar:
                assert 'META-INF/LICENSE-qrcodegen.txt' in jar.namelist()
            print('PASS: delivered ZIP extracted; standalone JAR health, room creation and durable data in a fresh directory')
        finally: proc.terminate();proc.wait(timeout=10)
