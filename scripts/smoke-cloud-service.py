"""Exercise a disposable four-player room on an explicitly selected service.

Uses only player APIs. Creates one completed QA round in server statistics and
deletes its own room on exit; does not inspect or modify existing rooms.
"""
import argparse
import json
import urllib.error
import urllib.request
import uuid

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--server', required=True)
args = parser.parse_args()
base = args.server.rstrip('/')


def request(method, path, body=None, token=''):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    payload = json.dumps(body, ensure_ascii=False).encode() if body is not None else None
    req = urllib.request.Request(base + path, data=payload, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=15) as response:
        return json.load(response)


checks = 0


def check(condition):
    global checks
    assert condition
    checks += 1


room_path = ''
host_token = ''
try:
    check(request('GET', '/health') == {'status': 'ok', 'protocol': 1})
    create = {'nickname': '接口联调房主', 'roomKey': '', 'requestId': uuid.uuid4().hex,
              'gameType': 'spy', 'maxPlayers': 4, 'spyCount': 1, 'blankCount': 0}
    host = request('POST', '/api/v1/rooms', create)
    room_path = '/api/v1/rooms/' + host['room']['roomId']
    host_token = host['token']
    check(len(host['room']['roomId']) == 6 and host['room']['roomId'].isdigit())
    check(host['room']['identity'] is None and host['room']['gameType'] == 'spy')
    repeated = request('POST', '/api/v1/rooms', create)
    check(repeated['token'] == host_token and repeated['room']['selfId'] == host['room']['selfId'])
    peers = []
    for number in range(1, 4):
        body = {'nickname': f'接口联调成员{number}', 'roomKey': host['roomKey'], 'requestId': uuid.uuid4().hex}
        member = request('POST', room_path + '/join', body)
        peers.append(member)
        check(member['roomKey'] == '' and member['room']['identity'] is None)
        ready = request('POST', room_path + '/ready', {'ready': True}, member['token'])
        check(next(p for p in ready['players'] if p['id'] == ready['selfId'])['ready'])
    room = request('GET', room_path, token=host_token)
    check(len(room['players']) == 4 and all(p['ready'] and p['connected'] for p in room['players']))
    room = request('POST', room_path + '/start', {'roundNumber': room['roundNumber']}, host_token)
    check(room['status'] == 'PLAYING' and room['roundNumber'] == 1 and bool(room['roundId']))
    identities = [room['identity']]
    for member in peers:
        view = request('GET', room_path, token=member['token'])
        check(view['selfId'] == member['room']['selfId'] and view['roundId'] == room['roundId'])
        check(all(set(p) == {'id', 'nickname', 'ready', 'connected'} for p in view['players']))
        identities.append(view['identity'])
    check(sum(i['role'] == '卧底' for i in identities) == 1)
    finish = {'roundId': room['roundId'], 'winner': '未判定'}
    check(request('POST', room_path + '/finish', finish, host_token)['status'] == 'FINISHED')
    check(request('POST', room_path + '/finish', finish, host_token)['winner'] == '未判定')
    waiting = request('POST', room_path + '/next', {'roundId': room['roundId']}, host_token)
    check(waiting['status'] == 'WAITING' and waiting['identity'] is None and waiting['roundNumber'] == 1)
    kicked = request('POST', room_path + '/kick', {'playerId': peers[-1]['room']['selfId']}, host_token)
    check(len(kicked['players']) == 3)
    check(request('POST', room_path + '/leave', {}, host_token)['ok'])
    host_token = peers[0]['token']
    transferred = request('GET', room_path, token=host_token)
    check(transferred['hostId'] == transferred['selfId'])
    check(request('DELETE', room_path, token=host_token)['ok'])
    room_path = ''
    print(f'PASS: {checks} live checks; 4 players, durable membership, private identities, finish/next, kick, host transfer and room cleanup.')
finally:
    if room_path and host_token:
        request('DELETE', room_path, token=host_token)
