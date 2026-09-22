from pathlib import Path
import subprocess
import os
root = Path(__file__).resolve().parents[1]
out = root / 'artifacts/refinement-20260906'
startup = subprocess.STARTUPINFO()
startup.dwFlags |= subprocess.STARTF_USESHOWWINDOW
startup.wShowWindow = subprocess.SW_HIDE
p = subprocess.Popen([r'D:\Develop\Android\SDK\emulator\emulator.exe', '-avd', 'YiGameReference',
    '-no-snapshot-load', '-no-boot-anim', '-no-window', '-no-audio'],
    stdout=(out/'emulator.log').open('w'), stderr=(out/'emulator-error.log').open('w'),
    startupinfo=startup, creationflags=subprocess.CREATE_NO_WINDOW,
    env=dict(os.environ, ANDROID_AVD_HOME=r'C:\Users\Yves\.android\avd', ANDROID_HOME=r'D:\Develop\Android\SDK'))
print('QA emulator process', p.pid)
