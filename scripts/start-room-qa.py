from pathlib import Path
import json, os, secrets, subprocess
R = Path(__file__).resolve().parents[1]
out = R/'artifacts/refinement-20260906'
admin = secrets.token_urlsafe(32)
startup = subprocess.STARTUPINFO(); startup.dwFlags |= subprocess.STARTF_USESHOWWINDOW; startup.wShowWindow = subprocess.SW_HIDE
p = subprocess.Popen([r'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe', '-jar', str(R/'server/dist/yigame-room-server.jar')],
    env=dict(os.environ, PORT='18080', HOST='127.0.0.1', DATA_DIR=str(out/'qa-server-data'), ADMIN_TOKEN=admin),
    stdout=(out/'qa-server.log').open('w'),stderr=(out/'qa-server-error.log').open('w'),
    startupinfo=startup,creationflags=subprocess.CREATE_NO_WINDOW)
(out/'qa-server-control.json').write_text(json.dumps(dict(pid=p.pid, admin=admin)),encoding='utf-8')
print('Local QA server started, pid',p.pid)
