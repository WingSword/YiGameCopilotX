const assert = require('node:assert/strict');
const fs = require('node:fs');
const http = require('node:http');
const path = require('node:path');
const vm = require('node:vm');
const { randomUUID } = require('node:crypto');
const { spawn } = require('node:child_process');
const readline = require('node:readline');
const [java, classpath, temp, harmony, typescript] = process.argv.slice(2);
const ts = require(typescript);

class KotlinClient {
  async restart() {
    await this.stop();
    this.child = spawn(java, ['-cp', classpath, 'ClientBridgeKt'], {
      env: { ...process.env, CLOUD_TEST_STORE: path.join(temp, 'kotlin.properties') }, windowsHide: true
    });
    this.pending = [];
    this.errors = '';
    this.child.stderr.on('data', value => { this.errors += value.toString(); });
    readline.createInterface({ input: this.child.stdout }).on('line', line => {
      const task = this.pending.shift();
      if (task) { try { task.resolve(JSON.parse(line)); } catch (e) { task.reject(e); } }
    });
    this.child.on('exit', code => {
      for (const task of this.pending.splice(0)) task.reject(new Error(`Kotlin exited ${code}: ${this.errors}`));
    });
  }
  call(command) {
    return new Promise((resolve, reject) => {
      this.pending.push({ resolve, reject });
      this.child.stdin.write(JSON.stringify(command) + '\n');
    });
  }
  async stop() {
    if (!this.child) return;
    const child = this.child;
    this.child = null;
    if (child.exitCode !== null) return;
    await new Promise(resolve => { child.once('exit', resolve); child.kill(); });
  }
}

class HarmonyClient {
  async restart() {
    const storeFile = path.join(temp, 'harmony.json');
    const preferences = fs.existsSync(storeFile) ? JSON.parse(fs.readFileSync(storeFile)) : {};
    const prefs = { async get(key, fallback) { return preferences[key] ?? fallback; },
      async put(key, value) { preferences[key] = value; fs.writeFileSync(storeFile, JSON.stringify(preferences)); } };
    const network = { RequestMethod: { GET: 'GET', POST: 'POST', DELETE: 'DELETE' }, HttpDataType: { STRING: 0 },
      createHttp() { return { destroy() {}, async request(url, options) {
        // Native ArkUI rejects even extraData: '' for bodyless requests.
        if (options.method === 'GET' || options.method === 'DELETE') {
          assert.ok(!Object.prototype.hasOwnProperty.call(options, 'extraData'), 'Harmony GET/DELETE must omit extraData entirely');
        }
        const response = await fetch(url, { method: options.method, headers: options.header,
          body: options.extraData });
        return { result: await response.text(), responseCode: response.status,
          header: Object.fromEntries(response.headers.entries()) };
      } }; } };
    function load(relative, imports) {
      const source = fs.readFileSync(path.join(harmony, 'entry/src/main/ets', relative), 'utf8');
      const context = { exports: {}, require(name) {
        assert.ok(name in imports, `Unexpected import: ${name}`);
        return imports[name];
      }, Error, Date };
      vm.runInNewContext(ts.transpileModule(source, { compilerOptions: {
        module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2021
      } }).outputText, context, { filename: relative });
      return context.exports;
    }
    const models = load('model/online/CloudRoomModels.ets', {});
    this.models = models;
    const distribution = load('distribution/AppDistribution.ets', { BuildProfile: { default: { DISTRIBUTION_CHANNEL: this.channel || 'direct' } } });
    this.distribution = distribution.AppDistribution;
    const module = load('store/CloudRoomStore.ets', {
      '@ohos.net.http': { default: network }, '@ohos.util': { default: { generateRandomUUID: () => randomUUID() } },
      '../distribution/AppDistribution': distribution,
      '../utils/PreferencesUtils': { PreferencesUtils: prefs }, '../model/online/CloudRoomModels': models
    });
    this.store = module.CloudRoomStore.getInstance();
  }
  async call(command) {
    const s = this.store;
    let result = true;
    if (command.op === 'enter') result = await s.enter(command.server, command.code || '', command.create,
      { maxPlayers: 12, spyCount: 1, blankCount: 0, gameType: 'spy', roles: [], ledgerPreset: 'electronic', initialBalance: 1500000, witchCount: 1, ...command.body });
    else if (command.op === 'ledger') result = await s.ledgerAction(command.body ? { requestId: '', revision: 0, operation: '', from: '', to: '', amount: 0, memo: '', targets: [], entryId: '', ...command.body } : null);
    else if (command.op === 'draw') result = await s.drawAction(command.body ? { requestId: '', revision: 0, color: '#111111', width: 8, points: [], guess: '', ...command.body } : null);
    else if (command.op === 'action') result = await s.action(command.name, command.body || {});
    else if (command.op === 'close') result = await s.closeRoom();
    else if (command.op === 'refresh') await s.refresh();
    else if (command.op === 'invite') result = true;
    else await s.restore();
    const invitation = command.op === 'invite' ? await s.invitation(command.webUrl || '') : null;
    return JSON.parse(JSON.stringify({ invitation, result, joined: s.hasSession(), error: s.error, pendingLedger: s.pendingLedger, pendingDraw: s.pendingDraw,
      compatibilityMessage: s.compatibilityMessage, supportsHostView: this.models.supportsHostView(s.room),
      server: s.serverAddress, delay: s.pollDelayMillis(), key: s.key(), room: s.room }));
  }
  async stop() {}
}

const queue = [];
const errors = [];
let requests = 0;
const server = http.createServer(async (req, res) => {
  requests++;
  try {
    const expected = queue.shift();
    assert.ok(expected, `Unexpected HTTP ${req.method} ${req.url}`);
    assert.equal(req.method, expected.method);
    assert.equal(req.url, '/api/v1/rooms' + expected.path);
    if (expected.token) assert.equal(req.headers.authorization, 'Bearer ' + expected.token);
    const chunks = [];
    for await (const chunk of req) chunks.push(chunk);
    const raw = Buffer.concat(chunks).toString();
    if (req.method === 'POST') assert.match(req.headers['content-type'], /^application\/json/);
    if (expected.inspect) expected.inspect(raw ? JSON.parse(raw) : null, raw);
    res.writeHead(expected.status || 200, { 'Content-Type': 'application/json', ...expected.headers });
    res.end(typeof expected.response === 'string' ? expected.response : JSON.stringify(expected.response));
  } catch (e) { errors.push(e); res.writeHead(500); res.end('{}'); }
});

function expect(method, path, response, options = {}) { queue.push({ method, path, response, ...options }); }
const fixture = (fields = {}) => ({ roomId: '028922', gameType: 'spy', status: 'WAITING', hostId: 'host',
  selfId: 'host', maxPlayers: 12, spyCount: 1, blankCount: 0, roundId: '', roundNumber: 0, winner: '',
  players: [{ id: 'host', nickname: '房主', ready: true, connected: true }], identity: null, ...fields });
const entry = (room = fixture(), roomKey = 'secret1') => ({ token: 'member-token', roomKey, room });
const error = code => ({ code, message: code });
const auth = { token: 'member-token' };
const equalBody = body => ({ ...auth, inspect: actual => assert.deepEqual(actual, body) });

async function domesticSuite(address) {
  const storeFile = path.join(temp, 'harmony.json');
  const saved = {
    cloud_room_session_v1: JSON.stringify({ server: address, token: 'member-token', roomId: '028922', roomKey: 'secret1' }),
    cloud_ledger_pending_v1: 'pending-ledger', cloud_draw_pending_v1: 'pending-drawing'
  };
  fs.writeFileSync(storeFile, JSON.stringify(saved));
  const client = new HarmonyClient(); client.channel = 'domestic'; await client.restart();
  const before = requests;
  try {
    assert.equal(client.distribution.roomsEnabled, false);
    for (const route of ['MultiplayerPage', 'RoomPage', 'LedgerCloudEntryPage', 'LANRoomDiscoveryPage', 'LANCreateRoomPage', 'LANRoomLobbyPage']) {
      assert.ok(client.distribution.isRoomRoute(route));
    }
    for (const route of ['HomePage', 'MonopolyMoneyPage', 'RandomPage', 'WerewolfGamePage']) assert.ok(!client.distribution.isRoomRoute(route));
    await client.call({ op: 'restore' }); await client.call({ op: 'refresh' });
    for (const create of [true, false]) {
      const result = await client.call({ op: 'enter', create, server: address, code: '028922', body: { nickname: 'host', roomKey: 'secret1' } });
      assert.equal(result.result, false); assert.equal(result.error, client.distribution.roomsUnavailable);
    }
    assert.equal((await client.call({ op: 'action', name: 'ready' })).result, false);
    assert.equal((await client.call({ op: 'close' })).result, false);
    assert.equal((await client.call({ op: 'invite' })).invitation, null);
    assert.equal((await client.call({ op: 'ledger' })).result, false);
    const result = await client.call({ op: 'draw' });
    assert.equal(result.result, false); assert.equal(result.joined, false);
    assert.equal(result.room, null); assert.equal(result.pendingLedger, ''); assert.equal(result.pendingDraw, '');
    assert.equal(requests, before, 'domestic client must never contact the server');
    assert.deepEqual(JSON.parse(fs.readFileSync(storeFile)), saved, 'saved full-edition data stays untouched');
    console.log('PASS Harmony domestic: disabled routes, saved session, enter/actions/invite rejected, zero HTTP');
  } finally { fs.rmSync(storeFile, { force: true }); await client.stop(); }
}

async function suite(label, client, address) {
  let checks = 0;
  const check = (value, message) => { assert.ok(value, label + ': ' + message); checks++; };
  const create = { op: 'enter', server: address, create: true, body: { nickname: '房主', roomKey: '' } };
  try {
    await client.restart();
    check((await client.call({ op: 'restore' })).server === 'http://8.133.216.39:8080', 'default service');
    let count = requests;
    check(!(await client.call({ ...create, body: { nickname: 'a\nb', roomKey: '' } })).result, 'control-character validation');
    check(!(await client.call({ ...create, body: { ...create.body, maxPlayers: 8, spyCount: 3, blankCount: 1 } })).result, 'half-capacity boundary');
    check(requests === count, 'invalid forms do not send HTTP');
    let firstBody;
    expect('POST', '', '{', { inspect: body => {
      assert.deepEqual(Object.keys(body).sort(), ['nickname','roomKey','requestId','gameType','maxPlayers','spyCount','blankCount'].sort());
      assert.equal(body.gameType, 'spy'); assert.ok(body.requestId.length >= 32 && body.requestId.length <= 100); firstBody = body;
    } });
    check(!(await client.call(create)).result, 'interrupted response is retryable');
    assert.ok(firstBody, `${label}: first entry request reached HTTP server`);
    await client.restart();
    expect('POST', '', entry(), { inspect: body => assert.deepEqual(body, firstBody) });
    check((await client.call(create)).joined, 'same request survives a process restart');
    check((await client.call({ op: 'restore' })).key === 'secret1', 'generated key is retained');
    await client.restart();
    check((await client.call({ op: 'restore' })).joined, 'membership token is durable');
    expect('GET', '/028922', fixture(), auth);
    check((await client.call({ op: 'refresh' })).room.selfId === 'host', 'restoration uses the original member');

    let legacy = await client.call({ op: 'restore' });
    check(!legacy.supportsHostView && legacy.compatibilityMessage.includes('服务器版本较旧'), 'missing legacy mode fields produce explicit incompatibility');
    expect('POST', '/028922/hostview', error('NOT_FOUND'), { ...equalBody({roundId:'',revision:0,hostView:'admin'}), status:404 });
    legacy = await client.call({op:'action',name:'hostview',body:{roundId:'',revision:0,hostView:'admin'}});
    check(!legacy.result && legacy.joined && legacy.error.includes('服务器版本较旧'), 'unsupported mode preserves membership and explains upgrade');
    check(legacy.room.hostView !== 'admin', 'unsupported mode never creates a local admin state');
    expect('POST', '/028922/invite', error('NOT_FOUND'), { ...equalBody({webUrl:'',serverUrl:address}), status:404 });
    legacy = await client.call({op:'invite'});
    check(legacy.invitation === null && legacy.joined && legacy.error.includes('服务器版本较旧'), 'unsupported invitation explains upgrade without dropping membership');
    expect('GET', '/028922', fixture(), auth);
    legacy = await client.call({op:'refresh'});
    check(legacy.compatibilityMessage.includes('服务器版本较旧'), 'successful legacy polling cannot erase compatibility warning');
    expect('GET', '/028922', fixture({hostView:'player'}), auth);
    check(!(await client.call({op:'refresh'})).supportsHostView, 'mode without server revision is not a supported broadcast protocol');
    expect('GET', '/028922', fixture({hostView:'admin',hostViewRevision:0,hostViewChanges:[]}), auth);
    const upgraded = await client.call({op:'refresh'});
    check(upgraded.supportsHostView && upgraded.compatibilityMessage === '', 'upgrade is recognized on refresh without new membership');

    const qr = { url: 'https://play.example/#room=028922&invite=' + 'A'.repeat(32), modules: ['101','010','101'] };
    expect('POST', '/028922/invite', qr, equalBody({ webUrl: 'https://play.example/', serverUrl: address }));
    check((await client.call({ op: 'invite', webUrl: 'https://play.example/' })).invitation.url === qr.url, 'invitation generated with authenticated production client');
    const managed = fixture({hostView:'admin',hostViewRevision:1,hostViewChanges:[{revision:1,mode:'admin',hostId:'host'}]});
    expect('POST', '/028922/hostview', managed, equalBody({roundId:'', revision:0, hostView:'admin'}));
    check((await client.call({op:'action',name:'hostview',body:{roundId:'',revision:0,hostView:'admin'}})).room.hostViewChanges.length === 1, 'mode and notification decode');

    expect('POST', '/028922/ready', fixture(), equalBody({ ready: true }));
    check((await client.call({ op: 'action', name: 'ready', body: { ready: true } })).result, 'ready body');
    const playing = fixture({ status: 'PLAYING', roundId: 'round-1', roundNumber: 1, identity: { role: '卧底', word: '月亮' } });
    expect('POST', '/028922/start', playing, equalBody({ roundNumber: 0 }));
    check((await client.call({ op: 'action', name: 'start', body: { roundNumber: 0 } })).room.identity.word === '月亮', 'private identity response');
    const finished = { ...playing, status: 'FINISHED', winner: '平民' };
    expect('POST', '/028922/finish', finished, equalBody({ roundId: 'round-1', winner: '平民' }));
    check((await client.call({ op: 'action', name: 'finish', body: { roundId: 'round-1', winner: '平民' } })).room.winner === '平民', 'finish body and result');
    expect('POST', '/028922/next', fixture({ roundNumber: 1 }), equalBody({ roundId: 'round-1' }));
    check((await client.call({ op: 'action', name: 'next', body: { roundId: 'round-1' } })).room.identity === null, 'next round clears identity');
    expect('POST', '/028922/start', error('STALE_ROUND'), { ...equalBody({ roundNumber: 1 }), status: 409 });
    expect('GET', '/028922', { ...playing, roundNumber: 2, roundId: 'round-2' }, auth);
    const stale = await client.call({ op: 'action', name: 'start', body: { roundNumber: 1 } });
    check(!stale.result && stale.room.roundNumber === 2, 'stale mutation refreshes state without replay');
    check(stale.error.includes('局次已更新'), 'stale result requests fresh user intent');
    expect('POST', '/028922/kick', fixture(), equalBody({ playerId: 'peer' }));
    check((await client.call({ op: 'action', name: 'kick', body: { playerId: 'peer' } })).result, 'kick body');
    expect('DELETE', '/028922', error('NOT_FOUND'), { ...auth, status: 404 });
    check((await client.call({ op: 'close' })).joined, 'unrelated 404 retains token');
    expect('POST', '/028922/ready', error('SESSION_EXPIRED'), { ...auth, status: 401 });
    check(!(await client.call({ op: 'action', name: 'ready', body: { ready: true } })).joined, 'mutation expiration clears token');
    await client.restart();
    check(!(await client.call({ op: 'restore' })).joined, 'expired membership remains cleared after restart');

    const join = { op: 'enter', server: address, code: '028922', create: false, body: { nickname: '成员', roomKey: 'wrong12' } };
    let wrongId;
    expect('POST', '/028922/join', error('WRONG_KEY'), { status: 403, inspect: body => {
      assert.deepEqual(Object.keys(body).sort(), ['nickname', 'roomKey', 'requestId'].sort()); wrongId = body.requestId;
    } });
    check((await client.call(join)).error === 'WRONG_KEY', 'server error message displayed');
    expect('POST', '/028922/join', entry(fixture({ selfId: 'peer' }), ''), { inspect: body => {
      assert.notEqual(body.requestId, wrongId); assert.equal(body.roomKey, 'secret1');
    } });
    check((await client.call({ ...join, body: { ...join.body, roomKey: 'secret1' } })).key === 'secret1', 'changed form rotates ID and keeps join key');
    expect('POST', '/028922/leave', { ok: true }, equalBody({}));
    check(!(await client.call({ op: 'action', name: 'leave' })).joined, 'leave sends empty object and clears member');
    expect('POST', '/028922/join', entry(fixture({selfId:'peer'}), ''), {inspect:body => {
      assert.equal(body.inviteToken, 'A'.repeat(32)); assert.equal(body.roomKey, '');
    }});
    check((await client.call({...join,body:{nickname:'扫码成员',roomKey:'',inviteToken:'A'.repeat(32)}})).key === '', 'join-only invitation needs no room key');
    expect('POST','/028922/leave',{ok:true},equalBody({})); await client.call({op:'action',name:'leave'});
    expect('POST', '', entry());
    await client.call(create);
    expect('DELETE', '/028922', error('ROOM_NOT_FOUND'), { ...auth, status: 404 });
    check(!(await client.call({ op: 'close' })).joined, 'missing room clears session on delete');
    expect('POST', '', entry());
    await client.call(create);
    expect('GET', '/028922', error('ROOM_EXPIRED'), { ...auth, status: 410 });
    check(!(await client.call({ op: 'refresh' })).joined, 'expired room clears session on poll');
    expect('POST', '', entry());
    await client.call(create);
    expect('GET', '/028922', error('RATE_LIMIT'), { ...auth, status: 429, headers: { 'Retry-After': '1' } });
    check((await client.call({ op: 'refresh' })).error.includes('请求过于频繁'), 'rate limit is distinguished from disconnect');
    count = requests;
    await client.call({ op: 'refresh' });
    await client.call({ op: 'action', name: 'ready', body: { ready: true } });
    check(requests === count, 'polls and actions respect cooldown');
    await new Promise(resolve => setTimeout(resolve, 1100));
    expect('GET', '/028922', fixture(), auth);
    check((await client.call({ op: 'refresh' })).error === '', 'requests resume after Retry-After');
    expect('DELETE', '/028922', { ok: true }, auth);
    check(!(await client.call({ op: 'close' })).joined, 'successful delete clears session');
    expect('POST', '', error('RATE_LIMIT'), { status: 429 });
    check((await client.call(create)).delay > 59000, 'missing Retry-After defaults to 60 seconds');
    check(queue.length === 0 && errors.length === 0, 'all expected HTTP exchanges validated');
    console.log(`PASS ${label}: ${checks} checks, real HTTP payloads, auth, durable retries, round refresh, expiration and backoff`);
  } finally { await client.stop(); }
}

async function transportFailureSuite(address) {
  const client = new KotlinClient();
  let checks = 0;
  const check = (condition, message) => { assert.ok(condition, message); checks++; };
  const create = {op:'enter',server:address,create:true,body:{nickname:'网络恢复',roomKey:''}};
  const supported = fixture({hostView:'admin',hostViewRevision:0,hostViewChanges:[]});
  const qr = {url:'https://play.example/#room=028922',modules:['101','010','101']};
  try {
    await client.restart();
    const failedEntry = await client.call({...create,networkFailure:'error'});
    check(!failedEntry.result && !failedEntry.busy && failedEntry.error.includes('连接失败'), 'Wasm-style Error must release create button and show retry message');
    check(failedEntry.pendingEntryId.length >= 32, 'unknown create result keeps durable request ID');
    const cancelledEntry = await client.call({...create,networkFailure:'cancel'});
    check(cancelledEntry.cancelled && !cancelledEntry.busy && cancelledEntry.pendingEntryId === failedEntry.pendingEntryId, 'create cancellation propagates and keeps retry identity');
    expect('POST','',entry(supported), {inspect:body => assert.equal(body.requestId,failedEntry.pendingEntryId)});
    check((await client.call(create)).joined, 'create succeeds after Error and cancellation');

    const commands = [
      {command:{op:'refresh'},method:'GET',path:'/028922',response:supported},
      {command:{op:'action',name:'ready',body:{ready:true}},method:'POST',path:'/028922/ready',response:supported},
      {command:{op:'invite'},method:'POST',path:'/028922/invite',response:qr}
    ];
    for (const test of commands) {
      const failed = await client.call({...test.command,networkFailure:'error'});
      check(failed.joined && !failed.busy && failed.error.length > 0 && !failed.cancelled, test.command.op+' Error becomes a recoverable message');
      const cancelled = await client.call({...test.command,networkFailure:'cancel'});
      check(cancelled.cancelled && cancelled.joined && !cancelled.busy, test.command.op+' preserves structured cancellation');
      expect(test.method,test.path,test.response,auth);
      const recovered = await client.call(test.command);
      check(!recovered.busy && recovered.error === '' && (test.command.op !== 'invite' || recovered.invitation.url === qr.url), test.command.op+' retry succeeds after failure/cancel');
    }

    expect('POST','/028922/start',error('STALE_ROUND'),{...auth,status:409});
    const failedRefresh = await client.call({op:'action',name:'start',body:{roundNumber:0},networkFailure:'error',failureAfter:1});
    check(!failedRefresh.result && !failedRefresh.busy && failedRefresh.joined && failedRefresh.error.includes('连接中断'), 'Error during conflict refresh cannot escape or strand busy state');
    expect('GET','/028922',supported,auth);
    await client.call({op:'refresh'});

    for (const op of ['ledger','draw']) {
      const body = op === 'ledger' ? {roundId:'round-1',revision:0,operation:'transfer',from:'host',to:'peer',amount:1} :
        {roundId:'round-1',stepId:'draw:1',revision:0,operation:'stroke',color:'#111111',width:8,points:[[0,0],[1,1]]};
      const failed = await client.call({op,body,networkFailure:'error'});
      const pendingKey = op === 'ledger' ? 'pendingLedger' : 'pendingDraw';
      check(!failed.result && !failed.busy && failed[pendingKey], op+' retains durable command on network Error');
      const pending = JSON.parse(failed[pendingKey]);
      const cancelled = await client.call({op,networkFailure:'cancel'});
      check(cancelled.cancelled && !cancelled.busy && cancelled[pendingKey] === failed[pendingKey], op+' cancellation preserves exact pending command');
      expect('POST','/028922/'+op,supported,equalBody(pending));
      const recovered = await client.call({op});
      check(recovered.result && recovered[pendingKey] === '', op+' exact retry resolves pending state');
    }

    const failedClose = await client.call({op:'close',networkFailure:'error'});
    check(!failedClose.result && failedClose.joined && !failedClose.busy && failedClose.error.includes('解散未确认'), 'failed close retains session for retry');
    const cancelledClose = await client.call({op:'close',networkFailure:'cancel'});
    check(cancelledClose.cancelled && cancelledClose.joined && !cancelledClose.busy, 'close cancellation propagates without discarding session');
    expect('DELETE','/028922',{ok:true},auth);
    check(!(await client.call({op:'close'})).joined, 'close succeeds after Error/cancellation');
    check(queue.length === 0 && errors.length === 0, 'failure tests use expected real production HTTP retries');
    console.log(`PASS Kotlin transport failures: ${checks} checks; Error and cancellation across enter/refresh/action/invite/close, busy release, durable command retries and nested refresh`);
  } finally { await client.stop(); }
}

async function liveOneNight() {
  const root = path.resolve(__dirname, '../..');
  for (const name of ['kotlin.properties', 'harmony.json']) {
    const file = path.join(temp, name); if (fs.existsSync(file)) fs.unlinkSync(file);
  }
  const socket = http.createServer();
  await new Promise(resolve => socket.listen(0, '127.0.0.1', resolve));
  const port = socket.address().port; await new Promise(resolve => socket.close(resolve));
  let service;
  const address = 'http://127.0.0.1:' + port;
  async function start() {
    service = spawn(java, ['-cp', path.join(root, 'server/build/classes'), 'org.walks.rooms.RoomServer'], {
      windowsHide: true, env: { ...process.env, HOST: '127.0.0.1', PORT: String(port),
        ADMIN_TOKEN: 'one-night-test-admin-only-0123456789', DATA_DIR: path.join(temp, 'live-server') }
    });
    await new Promise((resolve, reject) => { service.stdout.once('data', resolve); service.once('exit', c => reject(Error('Java service exited ' + c))); });
  }
  async function stop() { if (service && service.exitCode === null) await new Promise(resolve => { service.once('exit', resolve); service.kill(); }); }
  const clients = [new KotlinClient(), new HarmonyClient()];
  let hostToken, roomPath;
  async function request(method, route, body, token = hostToken) {
    const response = await fetch(address + route, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) }, body: body ? JSON.stringify(body) : undefined });
    const value = await response.json(); assert.equal(response.status, 200, JSON.stringify(value)); return value;
  }
  try {
    await start(); for (const c of clients) await c.restart();
    // Kotlin creates the real room, proving gameType/roles pass through production serialization.
    let joined = await clients[0].call({ op: 'enter', server: address, create: true, body: { nickname: '安卓', roomKey: 'Secret88', gameType: 'werewolf', maxPlayers: 3, roles: ['WEREWOLF','SEER','ROBBER','VILLAGER','VILLAGER','VILLAGER'] } });
    assert.equal(joined.result, true, joined.error); assert.equal(joined.room.gameType, 'werewolf');
    roomPath = '/api/v1/rooms/' + joined.room.roomId;
    const code = joined.room.roomId;
    joined = await clients[1].call({ op: 'enter', server: address, code, create: false, body: { nickname: '鸿蒙', roomKey: 'Secret88' } });
    assert.equal(joined.result, true, joined.error);
    const extra = await request('POST', roomPath + '/join', { nickname: '第三位', roomKey: 'Secret88', requestId: randomUUID() }, '');
    hostToken = extra.token;
    await request('POST', roomPath + '/ready', { ready: true });
    await clients[1].call({ op: 'action', name: 'ready', body: { ready: true } });
    joined = await clients[0].call({ op: 'action', name: 'start', body: { roundNumber: 0 } });
    assert.equal(joined.result, true, joined.error);
    const roundId = joined.room.roundId;
    const original = [];
    for (let i=0; i<3; i++) {
      const room = i<2 ? (await clients[i].call({ op: 'refresh' })).room : await request('GET', roomPath);
      original.push(room.identity);
      assert.equal(room.game.center.length, 0); assert.equal(room.game.revealed.length, 0);
      assert.ok(room.players.every(p => !('identity' in p) && !('initial' in p) && !('token' in p)));
    }
    for (let step=0; step<40; step++) {
      let acted = false, ended = false;
      for (let i=0; i<3; i++) {
        const room = i<2 ? (await clients[i].call({ op: 'refresh' })).room : await request('GET', roomPath);
        if (room.status === 'FINISHED') { ended = true; assert.equal(room.game.revealed.length, 3); break; }
        if (!room.game.options.length) continue;
        const body = { roundId, stepId: room.game.stepId, choice: room.game.options[0].id };
        const result = i<2 ? await clients[i].call({ op: 'action', name: 'game', body }) : { result: true, room: await request('POST', roomPath + '/game', body) };
        assert.ok(result.result, result.error);
        // Retry through the production clients must not repeat a swap/vote.
        if (i<2) assert.ok((await clients[i].call({ op: 'action', name: 'game', body })).result);
        else await request('POST', roomPath + '/game', body);
        acted = true; break;
      }
      if (ended) break;
      assert.ok(acted, 'A live cross-platform round stalled');
      if (step === 3) {
        await stop(); await start();
        for (let i=0;i<2;i++) { await clients[i].restart(); const restored = await clients[i].call({ op: 'refresh' }); assert.deepEqual(restored.room.identity, original[i]); assert.equal(restored.room.roundId, roundId); }
      }
    }
    const final = (await clients[0].call({ op: 'refresh' })).room;
    assert.equal(final.status, 'FINISHED'); assert.equal(final.game.center.length, 3);
    for (let i=0;i<2;i++) { await clients[i].restart(); const restored=await clients[i].call({op:'refresh'}); assert.equal(restored.room.winner, final.winner); }
    const next = await clients[0].call({op:'action',name:'next',body:{roundId}});
    assert.equal(next.room.identity,null); assert.equal(next.room.game,null);
    await clients[1].call({op:'action',name:'ready',body:{ready:true}});
    await request('POST',roomPath+'/ready',{ready:true});
    const again=await clients[0].call({op:'action',name:'start',body:{roundNumber:next.room.roundNumber}});
    assert.ok(again.result,again.error);
    assert.equal((await clients[1].call({op:'action',name:'game',body:{roundId,stepId:'deal',choice:'confirm'}})).result,false,'old round cannot operate new cards');
    assert.equal((await clients[1].call({op:'action',name:'game',body:{roundId:again.room.roundId,stepId:'deal',choice:'player:0'}})).result,false,'illegal action rejected');
    assert.equal((await clients[0].call({op:'action',name:'finish',body:{roundId:again.room.roundId,winner:'村民阵营胜利'}})).result,false,'host cannot bypass rules');
    await request('POST',roomPath+'/leave',{});
    const aborted=(await clients[0].call({op:'refresh'})).room;
    assert.equal(aborted.status,'FINISHED');assert.equal(aborted.game.phase,'ABORTED');
    assert.equal(aborted.game.revealed.length,0);
    assert.equal((await clients[0].call({op:'action',name:'next',body:{roundId:aborted.roundId}})).room.status,'WAITING');
    console.log('PASS live one-night: Java + production Kotlin + production ArkTS, full deal/night/vote/result, private identities, action retries, server/client restarts and next round');
  } finally { for (const c of clients) await c.stop(); await stop(); }
}

async function liveExpansion() {
  const root = path.resolve(__dirname, '../..');
  for (const name of ['kotlin.properties', 'harmony.json']) { const file = path.join(temp, name); if (fs.existsSync(file)) fs.unlinkSync(file); }
  const socket = http.createServer(); await new Promise(r => socket.listen(0, '127.0.0.1', r));
  const port = socket.address().port; await new Promise(r => socket.close(r));
  let service, loseResponse = false;
  const proxy = http.createServer(async (req, res) => {
    try {
      const chunks = []; for await (const chunk of req) chunks.push(chunk);
      const response = await fetch('http://127.0.0.1:' + port + req.url, { method: req.method,
        headers: { 'Content-Type': 'application/json', Authorization: req.headers.authorization || '' },
        body: req.method === 'POST' ? Buffer.concat(chunks) : undefined });
      const body = await response.text();
      res.writeHead(response.status, { 'Content-Type': 'application/json' });
      if (loseResponse && (req.url.endsWith('/ledger') || req.url.endsWith('/draw')) && response.ok) { loseResponse = false; res.end('{'); }
      else res.end(body);
    } catch (error) { res.writeHead(503); res.end('{}'); }
  });
  await new Promise(r => proxy.listen(0, '127.0.0.1', r));
  const address = 'http://127.0.0.1:' + proxy.address().port;
  async function start() {
    service = spawn(java, ['-cp', path.join(root, 'server/build/classes'), 'org.walks.rooms.RoomServer'], {
      windowsHide: true, env: { ...process.env, HOST: '127.0.0.1', PORT: String(port), ADMIN_TOKEN: 'cloud-expansion-test-01234567890123456789', DATA_DIR: path.join(temp, 'expansion-server') } });
    await new Promise((resolve, reject) => { service.stdout.once('data', resolve); service.once('exit', c => reject(Error('Java service exited ' + c))); });
  }
  async function stop() { if (service?.exitCode === null) await new Promise(r => { service.once('exit', r); service.kill(); }); }
  const clients = [new KotlinClient(), new HarmonyClient()];
  async function callRaw(route, token, body) {
    const response = await fetch(address + '/api/v1/rooms/' + route, { method: body ? 'POST' : 'GET',
      headers: { 'Content-Type': 'application/json', Authorization: token ? 'Bearer ' + token : '' }, body: body ? JSON.stringify(body) : undefined });
    const result = await response.json(); assert.equal(response.status, 200, JSON.stringify(result)); return result;
  }
  try {
    await start(); for(const c of clients) await c.restart();
    for (let owner = 0; owner < 2; owner++) {
      const host = clients[owner], guest = clients[1-owner];
      let result = await host.call({ op: 'enter', create: true, server: address, body: { nickname: '银行员', roomKey: 'Secret88', gameType: 'ledger', maxPlayers: 20, ledgerPreset: owner ? 'score' : 'chips', initialBalance: 1000 } });
      assert.ok(result.result, result.error);
      const code = result.room.roomId, hostId = result.room.selfId;
      result = await guest.call({ op: 'enter', create: false, server: address, code, body: { nickname: '记账玩家', roomKey: 'Secret88' } });
      assert.ok(result.result, result.error); const guestId = result.room.selfId;
      await guest.call({ op: 'action', name: 'ready', body: { ready: true } });
      result = await host.call({ op: 'action', name: 'start', body: { roundNumber: 0 } }); assert.ok(result.result, result.error);
      const roundId = result.room.roundId;
      loseResponse = true;
      result = await host.call({ op: 'ledger', body: { roundId, revision: 0, operation: 'transfer', from: hostId, to: guestId, amount: 30, memo: '租金' } });
      assert.equal(result.result, false); assert.ok(result.pendingLedger.length > 0, 'uncertain command is durable');
      await host.restart(); await host.call({ op: 'restore' });
      result = await host.call({ op: 'ledger' }); assert.ok(result.result, result.error); assert.equal(result.pendingLedger, '');
      assert.equal(result.room.ledger.accounts.find(a => a.id === hostId).balance, 970, 'lost response never duplicates debit');
      result = await guest.call({ op: 'refresh' });
      result = await guest.call({ op: 'ledger', body: { roundId, revision: result.room.ledger.revision, operation: 'request', from: hostId, to: guestId, amount: 5, memo: '回合奖励' } }); assert.ok(result.result, result.error);
      const requestId = result.room.ledger.pending[0].id;
      result = await host.call({ op: 'refresh' });
      result = await host.call({ op: 'ledger', body: { roundId, revision: result.room.ledger.revision, operation: 'approve', entryId: requestId } }); assert.ok(result.result, result.error);
      const action = async (operation, fields) => {
        const snapshot = await host.call({ op: 'refresh' });
        const done = await host.call({ op: 'ledger', body: { roundId, revision: snapshot.room.ledger.revision, operation, ...fields } });
        assert.ok(done.result, done.error); return done.room;
      };
      await action('collect', { targets: [guestId, hostId], amount: 30 });
      const split = await action('split', { targets: [guestId, hostId], amount: 31 });
      assert.equal(split.ledger.accounts.find(a => a.id === 'pot').balance, 29);
      const undone = await action('undo', { entryId: split.ledger.undoId });
      assert.equal(undone.ledger.accounts.find(a => a.id === 'pot').balance, 60);
      await stop(); await start();
      for(const c of clients) { await c.restart(); const view = await c.call({ op: 'refresh' }); assert.equal(view.room.ledger.history.length, undone.ledger.history.length); }
      assert.ok((await host.call({ op: 'action', name: 'finish', body: { roundId } })).result);
      const next = await host.call({ op: 'action', name: 'next', body: { roundId } }); assert.equal(next.room.ledger, null);
      await host.call({ op: 'close' }); await guest.call({ op: 'refresh' });
      console.log(`PASS live ledger ${owner ? 'Harmony' : 'Kotlin'} host: scenes, durable unknown-response recovery, payment approval, collection, fair split, batch undo, server/client restart, next round`);
    }
    for (let owner = 0; owner < 2; owner++) {
      const host = clients[owner], guest = clients[1-owner];
      let result = await host.call({ op: 'enter', create: true, server: address, body: { nickname: '主持', roomKey: 'Secret88', gameType: 'hunt', maxPlayers: 4, witchCount: 1 } }); assert.ok(result.result, result.error);
      const code = result.room.roomId;
      result = await guest.call({ op: 'enter', create: false, server: address, code, body: { nickname: '猎巫玩家', roomKey: 'Secret88' } }); assert.ok(result.result, result.error);
      const extras = [];
      for(let i=0;i<2;i++) extras.push(await callRaw(code + '/join', '', { nickname: '牌友' + i, roomKey: 'Secret88', requestId: randomUUID() }));
      for(const extra of extras) await callRaw(code + '/ready', extra.token, { ready: true });
      await guest.call({ op: 'action', name: 'ready', body: { ready: true } });
      result = await host.call({ op: 'action', name: 'start', body: { roundNumber: 0 } }); assert.ok(result.result, result.error);
      const roundId = result.room.roundId;
      const participants = [host, guest, ...extras];
      const view = async i => i < 2 ? (await participants[i].call({ op: 'refresh' })).room : callRaw(code, participants[i].token);
      const identities = []; for(let i=0;i<4;i++) { const room=await view(i); identities.push({ id:room.selfId, role:room.identity.role }); }
      const witchId = identities.find(p => p.role === '女巫').id;
      let restarted = false, ended = false;
      for(let step=0;step<100 && !ended;step++) {
        let acted = false;
        for(let i=0;i<4;i++) {
          const room = await view(i); if(room.status === 'FINISHED') { ended = true; break; }
          assert.ok(room.game.roster.filter(p => p.alive).every(p => p.role === ''), 'public view never exposes living roles');
          if(!room.game.options.length) continue;
          let choice = room.game.options[0].id;
          if(room.game.phase === 'NIGHT') {
            choice = 'skip';
            if(owner === 1 && room.identity.role === '女巫') choice = 'player:' + room.game.roster.find(p => p.alive && p.id !== witchId).id;
          }
          if(room.game.phase === 'VOTE') choice = owner === 0 && room.selfId !== witchId ? 'player:' + witchId : 'skip';
          const body = { roundId, stepId:room.game.stepId, choice };
          if(i<2) { const response=await participants[i].call({op:'action',name:'game',body}); assert.ok(response.result,response.error); }
          else await callRaw(code + '/game', participants[i].token, body);
          acted = true;
          if(room.game.phase === 'NIGHT' && !restarted) {
            await stop(); await start(); for(const c of clients) await c.restart(); restarted=true;
          }
          break;
        }
        assert.ok(acted || ended, 'Hunt full client flow stalled');
      }
      assert.ok(ended,'hunt completed');
      const final=await view(0); assert.equal(final.winner,owner ? '女巫阵营胜利' : '村民阵营胜利');
      assert.ok(final.game.roster.every(p=>p.role.length>0),'final reveal');
      await host.call({op:'close'}); await guest.call({op:'refresh'});
      console.log(`PASS live hunt ${owner ? 'Harmony' : 'Kotlin'} host: complete private deal/night/discussion/secret vote/result, roles isolated, restart mid-night, ${final.winner}`);
    }
    for(const type of ['avalon', 'drawing']) for(let owner=0;owner<2;owner++) {
      const host=clients[owner], guest=clients[1-owner], count=type==='avalon'?5:3;
      let result=await host.call({op:'enter',create:true,server:address,body:{nickname:'主持',roomKey:'Secret88',gameType:type,maxPlayers:count}});
      assert.ok(result.result,result.error); const code=result.room.roomId;
      result=await guest.call({op:'enter',create:false,server:address,code,body:{nickname:'同伴',roomKey:'Secret88'}}); assert.ok(result.result,result.error);
      const extras=[];
      for(let i=2;i<count;i++) { const e=await callRaw(code+'/join','',{nickname:'玩家'+i,roomKey:'Secret88',requestId:randomUUID()}); extras.push(e); await callRaw(code+'/ready',e.token,{ready:true}); }
      await guest.call({op:'action',name:'ready',body:{ready:true}});
      result=await host.call({op:'action',name:'start',body:{roundNumber:0}}); assert.ok(result.result,result.error);
      const roundId=result.room.roundId, members=[host,guest,...extras];
      const view=async i=>i<2?(await members[i].call({op:'refresh'})).room:callRaw(code,members[i].token);
      const send=async(i,body,draw=false)=>{
        if(i<2) { const r=await members[i].call(draw?{op:'draw',body}:{op:'action',name:'game',body}); assert.ok(r.result,r.error); return r.room; }
        return callRaw(code+(draw?'/draw':'/game'),members[i].token,{...body,...(draw?{requestId:randomUUID()}:{})});
      };
      let restarted=false;
      if(type==='avalon') {
        let ended=false;
        for(let step=0;step<100 && !ended;step++) {
          let acted=false;
          for(let i=0;i<count;i++) {
            const room=await view(i);
            if(room.status==='FINISHED') { ended=true; break; }
            assert.equal(room.game.revealed.length,0,'no identities before assassination');
            if(!room.game.options.length) continue;
            const choice=room.game.options[0].id, body={roundId,stepId:room.game.stepId,choice};
            if(choice==='team') body.targets=room.players.slice(0,room.game.avalon.teamSize).map(p=>p.id);
            await send(i,body); acted=true;
            if(room.game.phase==='TEAM_VOTE' && !restarted) { await stop(); await start(); for(const c of clients) await c.restart(); restarted=true; }
            break;
          }
          assert.ok(acted||ended,'Avalon client flow stalled');
        }
        assert.ok(ended,'Avalon complete through assassination');
        assert.equal((await view(0)).game.revealed.length,count);
      } else {
        for(let turn=0;turn<count;turn++) {
          const painter=turn, room=await view(painter), word=room.drawing.word;
          assert.ok(word); assert.equal((await view((turn+1)%count)).drawing.word,'');
          await send(painter,{roundId,stepId:room.drawing.stepId,operation:'start'},true);
          const drawing=(await view(painter)).drawing;
          const body={roundId,stepId:drawing.stepId,operation:'stroke',revision:0,color:'#E53935',width:8,points:[[0,0],[500,500],[1000,200]]};
          if(painter<2) {
            loseResponse=true;
            const lost=await members[painter].call({op:'draw',body}); assert.equal(lost.result,false); assert.ok(lost.pendingDraw);
            await members[painter].restart(); await members[painter].call({op:'restore'});
            await stop(); await start();
            assert.ok((await members[painter].call({op:'draw'})).result,'durable retry after server and client restart');
          } else await send(painter,body,true);
          for(let i=0;i<count;i++) assert.equal((await view(i)).drawing.strokes.length,1,'exactly one shared stroke');
          for(let i=0;i<count;i++) if(i!==painter) {
            const d=(await view(i)).drawing;
            await send(i,{roundId,stepId:d.stepId,operation:'guess',guess:word},true);
          }
          const d=(await view(0)).drawing; assert.equal(d.phase,'TURN_RESULT');
          await send(0,{roundId,stepId:d.stepId,operation:'next'},true);
        }
        const final=await view(0); assert.equal(final.status,'FINISHED');
        assert.ok(final.drawing.scores.every(p=>p.score===30),'each painter earns 10 and each guesser earns 20');
      }
      const final=await view(0); const next=await host.call({op:'action',name:'next',body:{roundId}});
      assert.equal(next.room.status,'WAITING'); assert.equal(next.room.game,null); assert.equal(next.room.drawing,null);
      await host.call({op:'close'}); await guest.call({op:'refresh'});
      console.log(`PASS live ${type} ${owner?'Harmony':'Kotlin'} host: complete game, private state, authoritative result, server/client restarts, next round; ${final.winner}`);
    }
  } finally { for(const c of clients) await c.stop(); await stop(); proxy.closeAllConnections(); await new Promise(r=>proxy.close(r)); }
}

(async () => {
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  try {
    const address = 'http://127.0.0.1:' + server.address().port;
    await domesticSuite(address);
    await suite('Kotlin', new KotlinClient(), address);
    await suite('Harmony', new HarmonyClient(), address);
    await transportFailureSuite(address);
    await liveOneNight();
    await liveExpansion();
  } finally { server.closeAllConnections(); server.close(); }
})().catch(e => { console.error(e); for (const error of errors) console.error(error); process.exitCode = 1; });
