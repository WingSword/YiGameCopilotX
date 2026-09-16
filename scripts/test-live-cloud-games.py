"""Bounded live API regression using only disposable QA rooms and their member tokens.

No administrator credentials, service restart, or access to existing rooms. Each
created room is closed in finally. Logs deliberately exclude member/invite tokens,
passwords and private identities. Run with --probe for deployment capabilities only.
"""
import argparse
import json
from pathlib import Path
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid


def uid():
    return str(uuid.uuid4())


class LiveSuite:
    def __init__(self, base, output):
        self.base = base.rstrip('/')
        self.output = output
        self.output.mkdir(parents=True, exist_ok=True)
        self.report = {'server': self.base, 'started': time.strftime('%Y-%m-%d %H:%M:%S'),
                       'checks': 0, 'requests': 0, 'results': [], 'cleanup': [], 'deployment': {}}
        self.owned = {}
        self.members = []
        self.code = ''
        self.round_id = ''
        self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        self.run_tag = 'QA' + uuid.uuid4().hex[:6]

    def log(self, text):
        print(text, flush=True)
        with (self.output / 'live-cloud.log').open('a', encoding='utf-8') as stream:
            stream.write(text + '\n')

    def check(self, condition, message):
        self.report['checks'] += 1
        if not condition:
            raise AssertionError(message)

    def request(self, method, path, body=None, token='', expected=200):
        headers = {'Content-Type': 'application/json', 'User-Agent': 'YiGame-QA/1'}
        if token:
            headers['Authorization'] = 'Bearer ' + token
        data = None if body is None else json.dumps(body, ensure_ascii=False).encode('utf-8')
        request = urllib.request.Request(self.base + path, data=data, headers=headers, method=method)
        for attempt in range(2):
            self.report['requests'] += 1
            try:
                with self.opener.open(request, timeout=25) as response:
                    status, raw, response_headers = response.status, response.read(), response.headers
            except urllib.error.HTTPError as error:
                status, raw, response_headers = error.code, error.read(), error.headers
            if status == 429 and attempt == 0:
                delay = min(65, max(1, int(response_headers.get('Retry-After', '60'))))
                self.log('Rate limit: respecting Retry-After before retrying the same request.')
                time.sleep(delay)
                continue
            break
        value = json.loads(raw)
        statuses = expected if isinstance(expected, tuple) else (expected,)
        self.check(status in statuses, f'{method} {path.rsplit("/", 1)[-1]}: expected {statuses}, got {status} ({value.get("code", "unknown")})')
        return value

    def room(self, index=0):
        return self.request('GET', '/api/v1/rooms/' + self.code, token=self.members[index]['token'])

    def action(self, action, body, index=0, expected=200):
        return self.request('POST', '/api/v1/rooms/' + self.code + '/' + action,
                            body, self.members[index]['token'], expected)

    def create(self, game, count):
        body = dict(nickname=self.run_tag + '-host', gameType=game, maxPlayers=count,
                    roomKey=uuid.uuid4().hex[:16], requestId=uid())
        if game == 'ledger':
            body.update(ledgerPreset='chips', initialBalance=1000)
        if game == 'hunt':
            body['witchCount'] = 1
        if game == 'werewolf':
            body['roles'] = ['WEREWOLF', 'SEER', 'ROBBER', 'VILLAGER', 'VILLAGER', 'VILLAGER']
        joined = self.request('POST', '/api/v1/rooms', body)
        self.code = joined['room']['roomId']
        self.owned[self.code] = joined['token']
        self.members = [joined]
        self.check(self.request('POST', '/api/v1/rooms', body)['token'] == joined['token'], 'create retry must restore the same membership')
        return body

    def invitation(self, configured_only=False):
        view = self.action('invite', {'serverUrl': self.base}, expected=(200, 409, 404))
        if view.get('code') == 'NOT_FOUND':
            self.report['deployment']['invite'] = 'MISSING_ENDPOINT'
            self.report['deployment']['publicWebUrl'] = 'UNKNOWN_MISSING_INVITE_ENDPOINT'
            self.log('Deployment gap: POST /invite returns 404 NOT_FOUND; PUBLIC_WEB_URL cannot be verified.')
            return None
        self.report['deployment']['invite'] = 'present'
        if 'code' in view:
            self.check(view['code'] == 'WEB_NOT_CONFIGURED', 'unexpected invitation deployment error')
            self.report['deployment']['publicWebUrl'] = 'NOT_CONFIGURED'
            self.log('Deployment: invitation endpoint present; PUBLIC_WEB_URL is not configured.')
            if configured_only:
                return None
            view = self.action('invite', {'serverUrl': self.base, 'webUrl': self.base + '/'})
        else:
            parsed = urllib.parse.urlsplit(view['url'])
            self.report['deployment']['publicWebUrl'] = urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, parsed.path, '', ''))
            self.log('Deployment: PUBLIC_WEB_URL=' + self.report['deployment']['publicWebUrl'])
        fields = urllib.parse.parse_qs(urllib.parse.urlsplit(view['url']).fragment)
        self.check(set(fields) == {'room', 'invite', 'server'}, 'invitation must only contain join data')
        self.check(self.members[0]['token'] not in view['url'], 'invitation must not disclose host token')
        rows = view['modules']
        self.check(len(rows) >= 21 and len(rows) % 4 == 1 and all(len(row) == len(rows) and set(row) <= {'0', '1'} for row in rows), 'QR matrix invalid')
        self.check(fields['room'][0] == self.code, 'invitation room mismatch')
        invitation = fields['invite'][0]
        self.check(invitation not in json.dumps(self.room()), 'public snapshots must not include invitation')
        self.request('GET', '/api/v1/rooms/' + self.code, token=invitation, expected=401)
        return invitation

    def populate(self, count, body, invitation):
        for index in range(1, count):
            join = {'nickname': self.run_tag + '-' + str(index), 'requestId': uid()}
            join.update({'inviteToken': invitation} if index == 1 and invitation else {'roomKey': body['roomKey']})
            joined = self.request('POST', '/api/v1/rooms/' + self.code + '/join', join)
            self.members.append(joined)
            retry = self.request('POST', '/api/v1/rooms/' + self.code + '/join', join)
            self.check(retry['token'] == joined['token'], 'join retry must reuse membership even when full')
        if self.report['deployment'].get('hostview') == 'present':
            self.action('hostview', {'roundId': '', 'revision': 0, 'hostView': 'player'}, 1, 403)
        if invitation:
            self.action('invite', {'serverUrl': self.base, 'webUrl': self.base + '/'}, 1, 403)
        self.request('DELETE', '/api/v1/rooms/' + self.code, token=self.members[1]['token'], expected=403)

    def mode_checks(self):
        if self.report['deployment'].get('hostview') != 'present':
            return
        before = self.room()
        members_before = [(player['id'], player['nickname']) for player in before['players']]
        old = dict(roundId=before['roundId'], revision=before['hostViewRevision'], hostView='player')
        player = self.action('hostview', old)
        body = dict(roundId=player['roundId'], revision=player['hostViewRevision'], hostView='admin')
        admin = self.action('hostview', body)
        retry = self.action('hostview', body)
        self.check(admin['hostViewRevision'] == retry['hostViewRevision'], 'same mode retry must not notify twice')
        self.action('hostview', old, expected=409)
        for index in range(len(self.members)):
            view = self.room(index)
            self.check(view['hostViewChanges'] == admin['hostViewChanges'] and view['hostView'] == 'admin', 'members must receive the same mode notifications')
            self.check(members_before == [(p['id'], p['nickname']) for p in view['players']], 'mode switch must preserve seats')
        self.check(before['identity'] == admin['identity'], 'mode switch must preserve host identity')

    def start(self):
        for index in range(len(self.members)):
            self.action('ready', {'ready': True}, index)
        number = self.room()['roundNumber']
        self.action('start', {'roundNumber': number}, 1, 403)
        room = self.action('start', {'roundNumber': number})
        self.round_id = room['roundId']
        if self.report['deployment'].get('hostview') == 'present':
            self.check(room['hostView'] == 'player', 'starting must switch host to participant view')
        self.action('start', {'roundNumber': number}, expected=409)
        self.check(self.room()['roundId'] == self.round_id, 'duplicate start must not redeal')
        for index in range(len(self.members)):
            view = self.room(index)
            self.check(all(set(p) <= {'id', 'nickname', 'ready', 'connected'} for p in view['players']), 'members must not expose identities/tokens')
            self.check(view['selfId'] == self.members[index]['room']['selfId'], 'same member identity after reconnect')
            self.check(view['identity'] == self.room(index)['identity'], 'reconnect must preserve identity')
        self.mode_checks()

    def close(self):
        if self.code not in self.owned:
            return
        self.request('DELETE', '/api/v1/rooms/' + self.code, token=self.owned[self.code])
        self.request('GET', '/api/v1/rooms/' + self.code, token=self.owned[self.code], expected=404)
        self.report['cleanup'].append({'room': self.code, 'status': 'deleted'})
        del self.owned[self.code]

    def probe(self):
        self.report['deployment']['health'] = self.request('GET', '/health')
        self.create('ledger', 2)
        try:
            self.invitation(configured_only=True)
            room = self.room()
            response = self.action('hostview', {'roundId': room['roundId'], 'revision': room.get('hostViewRevision', 0), 'hostView': 'player'}, expected=(200, 404))
            self.report['deployment']['hostview'] = 'MISSING_ENDPOINT' if response.get('code') == 'NOT_FOUND' else 'present'
            self.log('Deployment: hostview=' + self.report['deployment']['hostview'])
        finally:
            self.close()

    def game_round(self, kind):
        phases = []
        witch = next((m['room']['selfId'] for i, m in enumerate(self.members)
                      if kind == 'hunt' and self.room(i)['identity']['role'] == '女巫'), '')
        for step in range(140):
            acted = False
            for index in range(len(self.members)):
                room = self.room(index)
                game = room['game']
                if game['phase'] not in phases:
                    phases.append(game['phase'])
                if room['status'] == 'FINISHED':
                    if kind == 'hunt':
                        self.check(all(p['role'] for p in game['roster']), 'hunt must reveal final identities')
                        self.check(room['winner'] == '村民阵营胜利', 'hunt directed execution must remove the witch')
                    else:
                        self.check(len(game['revealed']) == len(self.members), 'result must reveal all identities')
                    return phases
                self.check(not game.get('revealed'), 'unrevealed identities must remain private')
                if kind == 'werewolf':
                    self.check(not game['center'], 'center cards must remain private before result')
                if kind == 'hunt':
                    self.check(all(not p['role'] for p in game['roster'] if p['alive']), 'living roles must remain private')
                if not game['options']:
                    continue
                choice = game['options'][0]['id']
                if kind == 'hunt' and game['phase'] == 'NIGHT':
                    choice = 'skip'
                if kind == 'hunt' and game['phase'] == 'VOTE':
                    choice = 'player:' + witch if room['selfId'] != witch else 'skip'
                body = dict(roundId=self.round_id, stepId=game['stepId'], choice=choice)
                if choice == 'team':
                    body['targets'] = [p['id'] for p in room['players'][:game['avalon']['teamSize']]]
                response = self.action('game', body, index)
                retry = self.action('game', body, index)
                self.check(response['game'] == retry['game'], 'same game action must be idempotent')
                acted = True
                break
            self.check(acted, kind + ' action flow stalled')
        raise AssertionError(kind + ' exceeded bounded full-round action count')

    def ledger(self, operation, index=0, expected=200, **fields):
        room = self.room(index)
        body = dict(roundId=self.round_id, requestId=uid(), revision=room['ledger']['revision'], operation=operation, **fields)
        response = self.action('ledger', body, index, expected)
        if expected == 200:
            retry = self.action('ledger', body, index)
            self.check(response['ledger'] == retry['ledger'], 'ledger retry must not duplicate debit or history')
        return response, body

    def ledger_round(self):
        ids = [m['room']['selfId'] for m in self.members]
        balance = lambda r, p: next(a['balance'] for a in r['ledger']['accounts'] if a['id'] == p)
        paid, old = self.ledger('transfer', 1, **{'from': ids[1], 'to': ids[0], 'amount': 30, 'memo': 'QA transfer'})
        self.check(balance(paid, ids[1]) == 970 and balance(paid, ids[0]) == 1030, 'player transfer balances')
        self.action('ledger', dict(old, requestId=uid()), 1, 409)
        self.action('ledger', dict(old, amount=31), 1, 409)
        self.ledger('transfer', 1, 409, **{'from': ids[0], 'to': ids[1], 'amount': 1})
        self.ledger('grant', 1, 409, targets=ids, amount=1)
        requested, _ = self.ledger('request', 1, **{'from': ids[0], 'to': ids[1], 'amount': 5})
        entry = requested['ledger']['pending'][0]['id']
        self.check(not self.room(2)['ledger']['pending'], 'unrelated member cannot see private collection request')
        self.ledger('approve', 1, 409, entryId=entry)
        approved, _ = self.ledger('approve', entryId=entry)
        self.check(balance(approved, ids[1]) == 975, 'collection must debit only after payer approves')
        requested, _ = self.ledger('request', 1, **{'from': ids[0], 'to': ids[1], 'amount': 2})
        self.ledger('reject', entryId=requested['ledger']['pending'][0]['id'])
        requested, _ = self.ledger('request', 1, **{'from': ids[0], 'to': ids[1], 'amount': 2})
        self.ledger('cancel', 1, entryId=requested['ledger']['pending'][0]['id'])
        self.ledger('grant', targets=ids, amount=10)
        collected, _ = self.ledger('collect', targets=ids, amount=30)
        self.check(balance(collected, 'pot') == 90, 'all-player collection')
        split, _ = self.ledger('split', targets=list(reversed(ids)), amount=32)
        self.check(balance(split, 'pot') == 58, 'split preserves remainder in pot')
        self.check([balance(split, p) - balance(collected, p) for p in ids] == [11, 11, 10], 'split remainder follows seat order')
        undone, _ = self.ledger('undo', entryId=split['ledger']['undoId'])
        self.check(balance(undone, 'pot') == 90, 'batch undo restores pot')
        self.ledger('transfer', 1, 409, **{'from': ids[1], 'to': ids[0], 'amount': 999999})
        body = {'roundId': self.round_id}
        final = self.action('finish', body)
        self.action('finish', body)
        self.check(final['winner'] == '记账结束', 'ledger ends with locked result')
        self.ledger('grant', expected=409, targets=ids, amount=1)
        return ['PLAYING', 'FINISHED']

    def draw(self, index, operation, expected=200, **fields):
        drawing = self.room(index)['drawing']
        body = dict(roundId=self.round_id, stepId=drawing['stepId'], requestId=uid(), operation=operation, **fields)
        result = self.action('draw', body, index, expected)
        if expected == 200:
            retry = self.action('draw', body, index)
            # Countdown is computed per response; only the authoritative state is idempotent.
            stable = lambda view: {key: value for key, value in view.items() if key != 'remainingSeconds'}
            self.check(stable(result['drawing']) == stable(retry['drawing']), 'drawing action must be idempotent')
        return result, body

    def drawing_round(self):
        ids = [m['room']['selfId'] for m in self.members]
        for turn in range(len(ids)):
            drawing = self.room()['drawing']
            painter = ids.index(drawing['painterId'])
            word = self.room(painter)['drawing']['word']
            self.check(bool(word), 'painter receives secret word')
            for index in range(len(ids)):
                if index != painter:
                    self.check(not self.room(index)['drawing']['word'], 'guesser cannot read secret word')
            self.draw(painter, 'start')
            drawing = self.room(painter)['drawing']
            stroke = dict(revision=drawing['revision'], color='#E53935', width=8, points=[[0, 0], [500, 500], [1000, 200]])
            self.draw((painter + 1) % len(ids), 'stroke', 409, **stroke)
            painted, body = self.draw(painter, 'stroke', **stroke)
            self.check(len(painted['drawing']['strokes']) == 1, 'single submitted stroke')
            for index in range(len(ids)):
                self.check(len(self.room(index)['drawing']['strokes']) == 1, 'stroke synchronized to all members')
            self.action('draw', dict(body, requestId=uid()), painter, 409)
            undone, _ = self.draw(painter, 'undo', revision=painted['drawing']['revision'])
            self.check(not undone['drawing']['strokes'], 'painter can undo')
            repainted, _ = self.draw(painter, 'stroke', **dict(stroke, revision=undone['drawing']['revision']))
            cleared, _ = self.draw(painter, 'clear', revision=repainted['drawing']['revision'])
            self.check(not cleared['drawing']['strokes'], 'painter can clear')
            for index in range(len(ids)):
                if index != painter:
                    self.draw(index, 'guess', guess=word)
            self.check(self.room()['drawing']['phase'] == 'TURN_RESULT', 'all guesses complete turn')
            self.draw(0, 'next')
        final = self.room()
        self.check(final['status'] == 'FINISHED', 'drawing full rotation completes game')
        self.check(all(p['score'] == 30 for p in final['drawing']['scores']), 'drawing score totals')
        return ['DRAW_READY', 'DRAWING', 'TURN_RESULT', 'RESULT']

    def next_and_transfer(self):
        final = self.room()
        self.check(final['status'] == 'FINISHED' and bool(final['winner']), 'authoritative final result')
        for index in range(len(self.members)):
            self.check(self.room(index)['winner'] == final['winner'], 'all members receive same result on reconnect')
        waiting = self.action('next', {'roundId': self.round_id})
        self.check(waiting['status'] == 'WAITING' and waiting['identity'] is None and waiting['game'] is None and waiting.get('ledger') is None and waiting.get('drawing') is None, 'next round clears previous private data')
        self.action('start', {'roundNumber': waiting['roundNumber'] - 1}, expected=409)
        for index in range(len(self.members)):
            self.action('ready', {'ready': True}, index)
        again = self.action('start', {'roundNumber': waiting['roundNumber']})
        self.check(again['roundId'] != self.round_id, 'new round must have a new identity')
        kind = again['gameType']
        if kind in ('spy', 'ledger'):
            self.action('finish', {'roundId': self.round_id, 'winner': '平民'}, expected=409)
        elif kind == 'drawing':
            self.action('draw', dict(roundId=self.round_id, stepId=again['drawing']['stepId'], requestId=uid(), operation='next'), expected=409)
        else:
            self.action('game', dict(roundId=self.round_id, stepId=again['game']['stepId'], choice='confirm'), expected=409)
        self.check(self.room()['roundId'] == again['roundId'] and self.room()['status'] == 'PLAYING', 'old round request must leave current game intact')
        old_token = self.members[0]['token']
        self.action('leave', {})
        self.owned[self.code] = self.members[1]['token']
        new = self.room(1)
        self.check(new['hostId'] == new['selfId'] and len(new['players']) == len(self.members) - 1, 'host transfer keeps next member participating')
        if kind != 'spy':
            self.check(new['status'] == 'FINISHED', 'member departure must safely end this game')
        if kind in ('werewolf', 'hunt', 'avalon'):
            self.check(new['game']['phase'] == 'ABORTED', 'departure must mark incomplete rule game aborted')
        if kind == 'werewolf':
            self.check(not new['game']['revealed'] and not new['game']['center'], 'aborted one-night game must not leak cards')
        if kind == 'drawing':
            self.check(new['drawing']['phase'] == 'ABORTED' and not new['drawing']['word'], 'departure must stop painting and conceal answer')
        self.request('GET', '/api/v1/rooms/' + self.code, token=old_token, expected=401)
        if self.report['deployment'].get('hostview') == 'present':
            switched = self.action('hostview', dict(roundId=new['roundId'], revision=new['hostViewRevision'], hostView='player'), 1)
            self.check(switched['hostView'] == 'player', 'new host can switch mode')

    def run_games(self):
        for kind, count in [('spy', 4), ('werewolf', 3), ('hunt', 4), ('ledger', 3), ('avalon', 5), ('drawing', 3)]:
            started = time.monotonic()
            result = {'game': kind}
            try:
                body = self.create(kind, count)
                invitation = self.invitation() if self.report['deployment'].get('invite') == 'present' else None
                self.populate(count, body, invitation)
                self.mode_checks()
                self.start()
                if kind == 'spy':
                    roles = [self.room(i)['identity']['role'] for i in range(count)]
                    self.check(roles.count('卧底') == 1 and roles.count('平民') == 3, 'spy role distribution')
                    finish = dict(roundId=self.round_id, winner='平民')
                    self.action('finish', finish, 1, 403)
                    self.action('finish', finish)
                    self.action('finish', finish)
                    self.action('finish', dict(finish, winner='卧底'), expected=409)
                    phases = ['PLAYING', 'FINISHED']
                elif kind == 'ledger':
                    phases = self.ledger_round()
                elif kind == 'drawing':
                    phases = self.drawing_round()
                else:
                    self.action('finish', {'roundId': self.round_id, 'winner': '未判定'}, expected=409)
                    phases = self.game_round(kind)
                self.next_and_transfer()
                result.update(status='PASS', phases=phases)
                self.log('PASS ' + kind + ': complete round, privacy, retries, reconnect, next round, stale-round rejection and active host transfer.')
            except Exception as error:
                result.update(status='FAIL', error=str(error))
                self.log('FAIL ' + kind + ': ' + str(error))
            finally:
                self.close()
                result['seconds'] = round(time.monotonic() - started, 2)
                self.report['results'].append(result)
                self.persist()
        self.check(all(r['status'] == 'PASS' for r in self.report['results']), 'one or more cloud games failed')
        self.log('PASS all six cloud game flows; deployment gaps are reported separately.')

    def persist(self):
        self.report['ended'] = time.strftime('%Y-%m-%d %H:%M:%S')
        self.report['remainingOwnedRooms'] = list(self.owned)
        (self.output / 'live-cloud-results.json').write_text(json.dumps(self.report, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server', default='http://8.133.216.39:8080')
    parser.add_argument('--output', type=Path, default=Path('artifacts/cloud-live-20260916/api'))
    parser.add_argument('--probe', action='store_true')
    args = parser.parse_args()
    suite = LiveSuite(args.server, args.output)
    try:
        suite.probe()
        if not args.probe:
            suite.run_games()
    finally:
        for room, token in list(suite.owned.items()):
            suite.code = room
            try:
                suite.close()
            except Exception as error:
                suite.log('Cleanup failed for QA room ' + room + ': ' + type(error).__name__)
        suite.persist()


if __name__ == '__main__':
    main()
