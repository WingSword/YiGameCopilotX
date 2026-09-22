"""Start an isolated loopback-only Java service for emulator QA; never touches deployment data."""
import json,os,secrets,shutil,socket,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
out=Path(os.environ.get('YIGAME_QA_OUT',str(root/'artifacts/cloud-expansion')))
out.mkdir(parents=True,exist_ok=True)
with socket.socket() as check: check.bind(('127.0.0.1',18084))
jar=out/'native-qa-server.jar'
shutil.copy2(root/'server/dist/yigame-room-server.jar',jar)
startup=subprocess.STARTUPINFO();startup.dwFlags|=subprocess.STARTF_USESHOWWINDOW;startup.wShowWindow=subprocess.SW_HIDE
p=subprocess.Popen([r'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe','-jar',str(jar)],
    env=dict(os.environ,HOST='127.0.0.1',PORT='18084',DATA_DIR=str(out/'native-server-data'),ADMIN_TOKEN=secrets.token_urlsafe(32),WEB_ORIGINS='http://127.0.0.1:18085,http://localhost:18085'),
    stdout=(out/'native-server.log').open('w'),stderr=(out/'native-server-error.log').open('w'),startupinfo=startup,creationflags=subprocess.CREATE_NO_WINDOW)
(out/'native-server-control.json').write_text(json.dumps({'pid':p.pid,'port':18084}),encoding='utf-8')
print('Isolated QA service started on loopback port 18084, pid',p.pid)
