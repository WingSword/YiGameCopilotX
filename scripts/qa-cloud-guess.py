"""Enter a drawing answer through the currently visible emulator UI (local QA only)."""
import runpy,sys,re,time
from pathlib import Path
q=runpy.run_path(str(Path(__file__).with_name('qa-game-flows.py')))
for attempt in range(5):
    rows=q['tree']()
    fields=[n for n in rows if n.get('class')=='android.widget.EditText' or n.get('type')=='TextInput']
    if fields:break
    q['scroll'](rows)
assert fields, 'Guess field is not visible'
b=list(map(int,re.findall(r'\d+',fields[0]['bounds'])));x,y=(b[0]+b[2])//2,(b[1]+b[3])//2
run=q['run']
if q['HARMONY']:
    run('shell','uitest','uiInput','click',x,y)
    run('shell','uitest','uiInput','keyEvent',2072,2017)
    run('shell','uitest','uiInput','keyEvent',2055)
    run('shell','uitest','uiInput','text',sys.argv[1])
    run('shell','uitest','uiInput','keyEvent',2070)
else:
    assert sys.argv[1].isascii(), 'adb input text supports ASCII only; use an IME for Chinese'
    run('shell','input','tap',x,y);run('shell','input','text',sys.argv[1]);run('shell','input','keyevent','4')
q['tap']('提交答案');time.sleep(1);q['shot']('guess-submitted-'+('harmony' if q['HARMONY'] else 'android'))
print('\n'.join(q['labels'](q['tree']())))
