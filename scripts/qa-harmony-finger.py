"""Capture two-finger waves, selection, and release using bounds from the native UI tree."""
import contextlib, io, json, re, runpy, subprocess, sys, time
from pathlib import Path

sys.argv = ['qa-native-ui.py', 'harmony', 'tree']
with contextlib.redirect_stdout(io.StringIO()):
    qa = runpy.run_path(str(Path(__file__).with_name('qa-native-ui.py')))
rows = qa['nodes']()
stage = next(n for n in rows if n.get('id') == 'finger_spinner_stage')
l,t,r,b = map(int,re.findall(r'-?\d+',stage['bounds']))
x1,x2,y = l+(r-l)//4,l+3*(r-l)//4,t+(b-t)//2
command=qa['PREFIX']+['shell','uinput','-T','-m',str(x1),str(y),str(x1+1),str(y),
    str(x2),str(y),str(x2+1),str(y),'-k','20000','100']
p=subprocess.Popen(command,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
evidence=[]
def capture(name):
    qa['run']('shell','uitest','screenCap','-p','/data/local/tmp/finger-wave.png')
    qa['run']('file','recv','/data/local/tmp/finger-wave.png',qa['OUT']/(name+'.png'))
    labels=[n.get('text') for n in qa['nodes']() if n.get('text')]
    evidence.append({'frame':name,'labels':labels})
try:
    time.sleep(.6); capture('harmony-finger-wave-1')
    time.sleep(.35); capture('harmony-finger-wave-2')
    time.sleep(4); capture('harmony-finger-winner')
finally:
    p.communicate(timeout=15)
time.sleep(.5); capture('harmony-finger-released')
(qa['OUT']/'finger-evidence.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf-8')
assert any('已检测 2 个手指' in e['labels'] for e in evidence[:2]), evidence
assert '已选中，松开全部手指后重置' in evidence[2]['labels'], evidence
assert '已检测 0 个手指' in evidence[3]['labels'], evidence
print('PASS: two pointers, countdown, selection, release; four native frames captured.')
