"""Capture current trees; derive all interactions from node bounds on the selected emulator."""
import json, os, re, subprocess, sys, time, xml.etree.ElementTree as ET
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / os.environ.get('YIGAME_QA_OUT', 'artifacts/refinement-20260906')
OUT.mkdir(parents=True, exist_ok=True)
PLATFORM = sys.argv[1]
PREFIX = [r'D:\Develop\Android\SDK\platform-tools\adb.exe', '-s', os.environ.get('ANDROID_SERIAL', 'emulator-5556')] if PLATFORM == 'android' else [r'C:\Program Files\Huawei\DevEco Studio\sdk\default\openharmony\toolchains\hdc.exe', '-t', os.environ.get('HDC_TARGET', '127.0.0.1:5555')]
def run(*args):
    p = subprocess.run(PREFIX + list(map(str,args)), capture_output=True)
    if p.returncode: raise RuntimeError(p.stderr.decode('utf-8',errors='replace'))
    return p.stdout.decode('utf-8',errors='replace')
def nodes():
    if PLATFORM == 'android':
        remote='/sdcard/yigame-qa-'+str(time.time_ns())+'.xml'
        for attempt in range(3):
            result=run('shell','uiautomator','dump',remote)
            if 'dumped to' in result.lower():break
            time.sleep(.7)
        else:raise RuntimeError('Could not obtain a fresh Android UI tree: '+result)
        path=OUT/'android-ui.xml';run('pull',remote,path)
        run('shell','rm',remote)
        return [dict(n.attrib) for n in ET.parse(path).iter('node')]
    run('shell','uitest','dumpLayout','-p','/data/local/tmp/refinement-ui.json')
    path=OUT/'harmony-ui.json';run('file','recv','/data/local/tmp/refinement-ui.json',path)
    rows=[]
    def visit(n):
        rows.append(n.get('attributes',{}))
        for c in n.get('children',[]): visit(c)
    visit(json.loads(path.read_text(encoding='utf-8')))
    return rows
def find(label,index):
    rows=nodes(); matches=[n for n in rows if any(n.get(k)==label for k in ['text','content-desc','accessibilityText','id','hint'])]
    if len(matches)<=index: raise RuntimeError('Node missing: '+label)
    b=list(map(int,re.findall(r'-?\d+',matches[index]['bounds'])))
    x,y=(b[0]+b[2])//2,(b[1]+b[3])//2
    dock=[list(map(int,re.findall(r'-?\d+',n['bounds']))) for n in rows if str(n.get('id','')).startswith('main_tab_')]
    if dock and not str(matches[index].get('id','')).startswith('main_tab_'):
        points=[(x,y),(b[2]-(b[2]-b[0])//10,y),(b[0]+(b[2]-b[0])//10,y),(x,b[1]+(b[3]-b[1])//10)]
        free=[(px,py) for px,py in points if not any(d[0]<=px<=d[2] and d[1]<=py<=d[3] for d in dock)]
        if not free: raise RuntimeError('Node covered by dock; scroll before interaction: '+label)
        x,y=free[0]
    return x,y
cmd=sys.argv[2] if len(sys.argv)>2 else 'tree'
if cmd=='scrollleft':
    boxes=[list(map(int,re.findall(r'-?\d+',n['bounds']))) for n in nodes() if n.get('scrollable')=='true']
    boxes=[b for b in boxes if b[2]-b[0] > 2*(b[3]-b[1])]
    if not boxes: raise RuntimeError('No horizontal scroll region')
    b=max(boxes,key=lambda b:b[2]-b[0])
    run('shell','input','swipe',b[0]+(b[2]-b[0])*4//5,(b[1]+b[3])//2,b[0]+(b[2]-b[0])//5,(b[1]+b[3])//2,400)
    time.sleep(.6)
elif cmd=='scroll':
    rows=nodes()
    candidates=[n for n in rows if n.get('scrollable')=='true']
    if not candidates: raise RuntimeError('No scrollable node')
    boxes=[list(map(int,re.findall(r'-?\d+',n['bounds']))) for n in candidates]
    b=max(boxes,key=lambda a:(a[2]-a[0])*(a[3]-a[1]))
    x=b[0]+(b[2]-b[0])*4//5
    y1=b[1]+(b[3]-b[1])*3//4; y2=b[1]+(b[3]-b[1])//3
    if len(sys.argv)>3 and sys.argv[3]=='down': y1,y2=y2,y1
    if PLATFORM=='android':run('shell','input','swipe',x,y1,x,y2,400)
    else:run('shell','uitest','uiInput','swipe',x,y1,x,y2,800)
    time.sleep(0.6)
elif cmd in ('tap','fill','longtap'):
    x,y=find(sys.argv[3],int(sys.argv[5]) if len(sys.argv)>5 else 0)
    if PLATFORM=='android':
        if cmd=='longtap':run('shell','input','swipe',x,y,x,y,850)
        else:run('shell','input','tap',x,y)
        if cmd=='fill':run('shell','input','text',sys.argv[4])
    else:
        run('shell','uitest','uiInput','longClick' if cmd=='longtap' else 'click',x,y)
        if cmd=='fill':run('shell','uitest','uiInput','inputText',x,y,sys.argv[4])
    time.sleep(1.2)
elif cmd=='shot':
    if PLATFORM=='android':
        data=subprocess.run(PREFIX+['exec-out','screencap','-p'],capture_output=True,check=True).stdout
        (OUT/(sys.argv[3]+'.png')).write_bytes(data)
    else:
        run('shell','uitest','screenCap','-p','/data/local/tmp/refinement.png')
        run('file','recv','/data/local/tmp/refinement.png',OUT/(sys.argv[3]+'.png'))
    print(OUT/(sys.argv[3]+'.png'))
for n in nodes():
    label=n.get('text') or n.get('content-desc') or n.get('accessibilityText') or n.get('hint') or n.get('id')
    if PLATFORM == 'harmony' and not n.get('text') and not str(n.get('id','')).startswith('main_tab_') and not n.get('hint'):
        continue
    if label:print(n.get('class',n.get('type','')),repr(label),n.get('bounds'),n.get('enabled'))
