"""Fill a visible native cloud entry form; only uses local test server port 18084."""
import runpy,re,sys,time
from pathlib import Path
q=runpy.run_path(str(Path(__file__).with_name('qa-game-flows.py')))
tap,tree,labels,run,shot=[q[k] for k in ['tap','tree','labels','run','shot']]
mode=sys.argv[1]
if '自定义服务器' in labels(tree()):tap('自定义服务器')
def fill(index,value):
    rows=tree(); fields=[n for n in rows if n.get('class')=='android.widget.EditText' or n.get('type')=='TextInput']
    assert len(fields)>index,(index,fields)
    n=fields[index];b=list(map(int,re.findall(r'\d+',n['bounds'])));x,y=(b[0]+b[2])//2,(b[1]+b[3])//2
    dock=[list(map(int,re.findall(r'\d+',item['bounds']))) for item in rows if str(item.get('id','')).startswith('main_tab_')]
    if any(d[0]<=x<=d[2] and d[1]<=y<=d[3] for d in dock):
        points=[(b[0]+(b[2]-b[0])//10,y),(b[2]-(b[2]-b[0])//10,y)]
        x,y=next((px,py) for px,py in points if not any(d[0]<=px<=d[2] and d[1]<=py<=d[3] for d in dock))
    if q['HARMONY']:
        run('shell','uitest','uiInput','click',x,y)
        run('shell','uitest','uiInput','keyEvent',2072,2017)
        run('shell','uitest','uiInput','keyEvent',2055)
        run('shell','uitest','uiInput','text',value)
        run('shell','uitest','uiInput','keyEvent',2070) # Close IME without popping app navigation.
    else:
        run('shell','input','tap',x,y);run('shell','input','keyevent','123',*(['67']*len(n.get('text',''))));run('shell','input','text',value);run('shell','input','keyevent','4')
    time.sleep(.5)
if mode=='join':tap('加入房间')
fill(0,'http://127.0.0.1:18084');fill(1,'HarmonyNative' if q['HARMONY'] else 'AndroidNative')
if mode=='join':fill(2,sys.argv[2]);fill(3,'Native88')
else:
    fill(2,'Native88')
    if mode=='hunt':tap('猎巫镇' if not q['HARMONY'] else '猎巫镇 · 简化版');tap('减少人数');tap('减少人数');tap('减少女巫')
    elif mode in ['avalon','drawing']:tap('阿瓦隆' if mode=='avalon' else '你画我猜')
    else:tap('积分赛')
# Scroll to the real submit button; the segmented create/join selector has the same title.
for _ in range(2):
    rows=tree()
    if any(n.get('scrollable')=='true' for n in rows):q['scroll'](rows)
shot('entry-filled-'+mode)
label='加入房间' if mode=='join' else '创建房间'
rows=tree();matches=[n for n in rows if n.get('text')==label]
assert matches,'Submit button absent from fresh UI tree: '+str(labels(rows))
tap(label,len(matches)-1)
time.sleep(2);shot('entry-result-'+mode)
print('\n'.join(labels(tree())),flush=True)
