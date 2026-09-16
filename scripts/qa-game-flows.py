"""Native Android/Harmony interactions, using fresh UI-tree bounds for every tap and scroll."""
import argparse, json, os, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
HARMONY=os.environ.get('QA_PLATFORM')=='harmony'
OUT=ROOT/os.environ.get('YIGAME_QA_OUT', 'artifacts/one-night-20260910/harmony-ui' if HARMONY else 'artifacts/one-night-20260910/ui')
OUT.mkdir(parents=True,exist_ok=True)
ADB=[r'D:\Develop\Android\SDK\platform-tools\adb.exe','-s',os.environ.get('ANDROID_SERIAL','emulator-5554')]
if HARMONY: ADB=[r'C:\Program Files\Huawei\DevEco Studio\sdk\default\openharmony\toolchains\hdc.exe','-t',os.environ.get('HDC_TARGET','127.0.0.1:5557')]
def run(*args):
    return subprocess.run(ADB+list(map(str,args)),capture_output=True,check=True).stdout
def tree(tag='current'):
    if HARMONY:
        run('shell','uitest','dumpLayout','-p','/data/local/tmp/game-flow.json')
        path=OUT/(tag+'.json');run('file','recv','/data/local/tmp/game-flow.json',path)
        rows=[]
        def visit(n):
            rows.append(n.get('attributes',{}))
            for c in n.get('children',[]):visit(c)
        visit(json.loads(path.read_text(encoding='utf-8')))
        return rows
    remote='/sdcard/game-flow-qa.xml'
    for _ in range(4):
        result=run('shell','uiautomator','dump',remote)
        if b'dumped to' in result:break
        time.sleep(.5)
    else:raise RuntimeError('No fresh tree')
    raw=run('exec-out','cat',remote)
    (OUT/(tag+'.xml')).write_bytes(raw)
    rows=[dict(n.attrib) for n in ET.fromstring(raw).iter('node')]
    return rows
def labels(rows):return [n.get('text') or n.get('content-desc') or n.get('accessibilityText') for n in rows if n.get('text') or n.get('content-desc') or n.get('accessibilityText')]
def scroll(rows,down=False):
    boxes=[list(map(int,re.findall(r'\d+',n['bounds']))) for n in rows if n.get('scrollable')=='true']
    if not boxes:raise RuntimeError('No scroll region')
    b=max(boxes,key=lambda b:(b[2]-b[0])*(b[3]-b[1]))
    x=b[0]+(b[2]-b[0])*4//5;start=b[1]+(b[3]-b[1])*4//5;end=b[1]+(b[3]-b[1])//4
    if down:start,end=end,start
    run(*(['shell','uitest','uiInput','swipe'] if HARMONY else ['shell','input','swipe']),x,start,x,end,400);time.sleep(.3)
def tap(label,index=0,long=False):
    for attempt in range(5):
        rows=tree(); found=[n for n in rows if any(n.get(k)==label for k in ['text','content-desc','accessibilityText','id'])]
        if len(found)>index:break
        scroll(rows)
    else:raise RuntimeError('Missing '+label+' in '+str(labels(rows)))
    n=found[index]; assert n.get('enabled')=='true',(label,'disabled')
    b=list(map(int,re.findall(r'\d+',n['bounds'])));x=(b[0]+b[2])//2;y=(b[1]+b[3])//2
    if HARMONY:run('shell','uitest','uiInput','longClick' if long else 'click',x,y)
    else:run('shell','input','swipe',x,y,x,y,900) if long else run('shell','input','tap',x,y)
    time.sleep(.55)
def shot(tag):
    if HARMONY:
        run('shell','uitest','screenCap','-p','/data/local/tmp/game-flow.png');run('file','recv','/data/local/tmp/game-flow.png',OUT/(tag+'.png'))
    else:(OUT/(tag+'.png')).write_bytes(run('exec-out','screencap','-p'))
    tree(tag)
def commands(items):
    for action in items:
        if action=='back':
            run(*(['shell','uitest','uiInput','keyEvent','Back'] if HARMONY else ['shell','input','keyevent','4']));time.sleep(.6)
        elif action=='scroll':scroll(tree())
        elif action=='up':scroll(tree(),True)
        elif action in ['ink','board-scroll']:
            descriptions=['绘画区域','同步画板，在此作画']+(['同步画板'] if action=='board-scroll' else [])
            rows=tree();canvases=[n for n in rows if n.get('content-desc') in descriptions or n.get('type')=='Canvas']
            assert canvases,'No canvas in fresh layout'
            b=list(map(int,re.findall(r'\d+',canvases[0]['bounds'])));w=b[2]-b[0];h=b[3]-b[1]
            points=(b[0]+w//2,b[1]+h*4//5,b[0]+w//2,b[1]+h//5) if action=='board-scroll' else (b[0]+w//4,b[1]+h//3,b[0]+w*3//4,b[1]+h*2//3)
            run(*(['shell','uitest','uiInput','swipe'] if HARMONY else ['shell','input','swipe']),*points,600)
            time.sleep(.5)
        elif action=='last-clickable':
            rows=tree(); n=[n for n in rows if n.get('clickable')=='true' and n.get('enabled')=='true'][-1]
            b=list(map(int,re.findall(r'\d+',n['bounds'])));run('shell','input','tap',(b[0]+b[2])//2,(b[1]+b[3])//2);time.sleep(.6)
        elif action.startswith('shot:'):shot(action[5:])
        elif action.startswith('assert:'):assert action[7:] in labels(tree()),action
        else:
            spec=action.split('@');tap(spec[0],int(spec[1]) if len(spec)>1 else 0)
    print('\n'.join(labels(tree())),flush=True)
if __name__=='__main__':
    import sys
    commands(sys.argv[1:])
