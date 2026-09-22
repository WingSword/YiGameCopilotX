"""Two local HTTP peers for native Android + Harmony room acceptance testing.

Usage: python scripts/qa-cloud-peers.py ROOM_CODE ROOM_KEY
Only contacts the local test service; never uses an administrator credential.
"""
import json, secrets, sys, time, urllib.request, urllib.error
from pathlib import Path

OUT = Path(__file__).resolve().parents[1] / 'artifacts/refinement-20260906'
code, key = sys.argv[1:3]
base = 'http://127.0.0.1:18080/api/v1/rooms/' + code

def request(path='', body=None, token=''):
    headers = {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token}
    data = None if body is None else json.dumps(body).encode()
    with urllib.request.urlopen(urllib.request.Request(base+path, data=data, headers=headers), timeout=5) as r:
        return json.load(r)

peers = [request('/join', {'nickname': name, 'roomKey': key, 'requestId': secrets.token_urlsafe(36)})
         for name in ['PeerQA1', 'PeerQA2']]
last = ''
for _ in range(600):
    try:
        views = [request(token=p['token']) for p in peers]
        for peer, view in zip(peers, views):
            assert all(set(p) == {'id','nickname','ready','connected'} for p in view['players']), 'Public roster exposes private data'
            if view['status'] == 'WAITING':
                myself = next(p for p in view['players'] if p['id'] == view['selfId'])
                if not myself['ready']: request('/ready', {'ready': True}, peer['token'])
        status = views[0]['status']
        result = {'roomId': code, 'status': status, 'players': len(views[0]['players']),
                  'roundNumber': views[0]['roundNumber'], 'winner': views[0]['winner'],
                  'privateViews': [{'selfId': v['selfId'], 'identity': v['identity']} for v in views]}
        (OUT/'cloud-native-peer-result.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
        if status != last:
            print(json.dumps(result, ensure_ascii=False), flush=True); last = status
        time.sleep(2)
    except urllib.error.HTTPError as e:
        if e.code in (401,404,410):
            print('Room closed or membership revoked; peers stopped.', flush=True); break
        raise
