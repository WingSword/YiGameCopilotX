// Exercise the production AI store; substitute only OS HTTP/preferences adapters.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('C:/Program Files/Huawei/DevEco Studio/sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript');
const root = path.resolve(__dirname, '../../YiGameCopilotX-Harmony/entry/src/main/ets');
const values = new Map();
const cache = new Map();
let online = true;
let requests = 0;
const prefs = { get: async (key, fallback) => values.has(key) ? values.get(key) : fallback };
function load(file) {
  file = path.resolve(root, file);
  if (cache.has(file)) return cache.get(file);
  const exports = {};
  cache.set(file, exports);
  const source = ts.transpileModule(fs.readFileSync(file, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2021 }
  }).outputText;
  vm.runInNewContext(source, { exports, console, require(name) {
    if (name === '@ohos.net.http') return { default: { createHttp() { return {
      async request(url, options) {
        requests++;
        assert.equal(url, 'https://api.deepseek.com/v1/chat/completions');
        assert.equal(options.header.Authorization, 'Bearer test-only-key');
        return { responseCode: 200, result: JSON.stringify({ choices: [{ message: { content: 'test response' } }] }) };
      }, destroy() {}
    }; }, RequestMethod: { POST: 'POST' }, HttpDataType: { STRING: 'STRING' } } };
    if (name.endsWith('/PreferencesUtils')) return { PreferencesUtils: prefs, PrefKeys: {
      AI_PROVIDER: 'provider', AI_API_KEY: 'key', AI_BASE_URL: 'url', AI_ENABLED: 'enabled',
      AI_STYLE: 'style', AI_TIMEOUT: 'timeout', AI_ONLINE_CONSENT: 'consent'
    } };
    if (name.endsWith('/AppDistribution')) return { AppDistribution: { get onlineAiEnabled() { return online; } } };
    if (name.endsWith('/Logger')) return { Logger: { warn() {}, error() {} } };
    assert.ok(name.startsWith('.'), 'Unexpected import: ' + name);
    return load(path.resolve(path.dirname(file), name + '.ets'));
  } }, { filename: file });
  return exports;
}
(async () => {
  const { AiProvider } = load('model/ai/AiProvider.ets');
  const store = load('store/AiStore.ets').AiStore.getInstance();
  values.set('provider', AiProvider.DEEP_SEEK);
  values.set('key', 'test-only-key');
  values.set('enabled', true);
  await store.sendMessage('spy', 'test context');
  assert.equal(requests, 0, 'Old settings without consent must stay offline');
  values.set('consent', true);
  assert.equal(await store.sendMessage('spy', 'test context'), 'test response');
  assert.equal(requests, 1, 'Consented request should reach the adapter');
  values.set('consent', false);
  await store.sendMessage('spy', 'test context');
  assert.equal(requests, 1, 'Revoking consent must stop new requests');
  values.set('consent', true);
  for (const url of ['http://api.deepseek.com', 'https://unrelated.example']) {
    values.set('url', url);
    await store.sendMessage('spy', 'test context');
    assert.equal(requests, 1, 'Unapproved destinations must not receive a key');
  }
  values.delete('url');
  online = false;
  await store.sendMessage('spy', 'test context');
  assert.equal(requests, 1, 'Domestic channel must stay offline even with saved consent');
  console.log('PASS Harmony: legacy settings, consent, revocation, destination and domestic isolation');
})().catch(error => { console.error(error); process.exitCode = 1; });
