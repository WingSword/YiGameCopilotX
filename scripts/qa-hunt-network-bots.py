"""Two local-test participants for a Hunt round played by both native emulator UIs.

Usage: qa-hunt-network-bots.py ROOM_CODE
Humans reveal/confirm, skip their night action and vote out the witch. The bots
keep their connections alive, confirm/skip and follow witch.txt in the QA folder.
This helper is restricted to the isolated loopback QA service, never production.
"""
import json, sys, time, urllib.request, uuid
from pathlib import Path

root=Path(__file__).resolve().parents[1]
out=root/'artifacts/cloud-expansion'
control=out/'native-hunt-witch.txt'
control.write_text('',encoding='utf-8')
code=sys.argv[1]
assert code.isdigit() and len(code)==6
base='http://127.0.0.1:18084/api/v1/rooms/'+code
def call(suffix='', token='', body=None):
    request=urllib.request.Request(base+suffix,data=json.dumps(body).encode() if body is not None else None,
        headers={'Content-Type':'application/json', **({'Authorization':'Bearer '+token} if token else {})})
    with urllib.request.urlopen(request,timeout=8) as response:return json.load(response)

bots=[]
for i in range(2):
    bot=call('/join',body={'nickname':'TestBot'+str(i+1),'roomKey':'Native88','requestId':str(uuid.uuid4())})
    bots.append(bot['token']);call('/ready',bot['token'],{'ready':True})
print('Two test participants joined and ready',flush=True)
seen=set()
for _ in range(900):
    for token in bots:
        room=call(token=token)
        if room['status']=='FINISHED':
            assert room['winner']=='村民阵营胜利',room['winner']
            (out/'native-hunt-result.json').write_text(json.dumps({'winner':room['winner'],'game':room['game']},ensure_ascii=False,indent=2),encoding='utf-8')
            print('PASS native cross-platform Hunt with two HTTP test participants: private deal, night, day, vote, village result',flush=True)
            sys.exit(0)
        if room['status']!='PLAYING':continue
        game=room['game']
        if room['selfId'] not in seen:
            print(next(p['nickname'] for p in room['players'] if p['id']==room['selfId'])+': '+room['identity']['role'],flush=True)
            seen.add(room['selfId'])
            if room['identity']['role']=='女巫':control.write_text(next(p['nickname'] for p in room['players'] if p['id']==room['selfId']),encoding='utf-8')
        choice=''
        if game['phase']=='DEAL':choice='confirm'
        elif game['phase']=='NIGHT':choice='skip'
        elif game['phase']=='VOTE':
            witch=control.read_text(encoding='utf-8').strip()
            target=next((p['id'] for p in room['players'] if p['nickname']==witch),'')
            if target:choice='skip' if target==room['selfId'] else 'player:'+target
        if any(o['id']==choice for o in game['options']):
            call('/game',token,{'roundId':room['roundId'],'stepId':game['stepId'],'choice':choice})
    time.sleep(2)
raise RuntimeError('Native Hunt QA timed out')
