// Execute production ArkTS reset handlers and statistics persistence against isolated fixtures.
// UI rendering and device preferences are covered separately by native QA.
const fs = require('fs'), path = require('path'), vm = require('vm'), assert = require('assert/strict');
const root = path.resolve(__dirname, '../../YiGameCopilotX-Harmony/entry/src/main/ets');
const ts = require(process.env.ARKTS_TYPESCRIPT || 'C:/Program Files/Huawei/DevEco Studio/sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript');
const data = new Map(), cache = new Map();
const PreferencesUtils = {get:async (k,d)=>data.has(k)?data.get(k):d, put:async (k,v)=>data.set(k,v)};
function evaluate(source, filename, imports = {}) {
  const exports = {};
  const context = {exports, Observed:v=>v, ...imports, require:name=> {
    if(name.endsWith('/PreferencesUtils')) return {PreferencesUtils, PrefKeys:{GAME_STATS_RECORDS:'game_stats_records'}};
    if(name.endsWith('/Logger')) return {Logger:{error:()=>{},info:()=>{}}};
    return load(path.resolve(path.dirname(filename), name+'.ets'));
  }};
  vm.runInNewContext(ts.transpileModule(source, {compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2021,experimentalDecorators:true}}).outputText, context, {filename});
  return exports;
}
function load(filename) {
  if(!cache.has(filename)) cache.set(filename, evaluate(fs.readFileSync(filename,'utf8'), filename));
  return cache.get(filename);
}
function method(source, name) {
  const begin=source.indexOf('  private '+name+'('); assert(begin>=0, 'Production handler missing');
  const brace=source.indexOf('{',begin); let depth=1,end=brace+1;
  while(depth>0 && end<source.length) {if(source[end]==='{')depth++;if(source[end]==='}')depth--;end++;}
  assert.equal(depth,0); return source.slice(begin,end).replace('private ','');
}
(async()=>{
  const entity=load(path.join(root,'model/entity/MonopolyEntity.ets'));
  const filename=path.join(root,'pages/monopoly/MonopolyMoneyPage.ets');
  const source=fs.readFileSync(filename,'utf8');
  const wrapper=`export class Fixture {
    players=[]; transactions=[]; allowed=true; dialog=null; saves=0;
    canEdit(){return this.allowed;} getUIContext(){return {showAlertDialog:(d)=>this.dialog=d};}
    syncState(p,t){this.players=p;this.transactions=t;this.saves++;}
    ${method(source,'confirmReset')}
  }`;
  const {Fixture}=evaluate(wrapper,filename,{MonopolyPlayer:entity.MonopolyPlayer});
  const fixture=new Fixture();
  fixture.players=[new entity.MonopolyPlayer('a','甲',-50000,[1],true,2,true),new entity.MonopolyPlayer('b','乙',3000000)];
  fixture.players[0].colorIndex=3;fixture.players[1].colorIndex=5;
  fixture.transactions=[new entity.MonopolyTransaction('a','b',50000,'测试')];
  const original=JSON.stringify([fixture.players,fixture.transactions]);
  fixture.allowed=false;fixture.confirmReset();assert.equal(fixture.dialog,null,'Guests must not open destructive action');
  fixture.allowed=true;fixture.confirmReset();fixture.dialog.primaryButton.action();
  assert.equal(JSON.stringify([fixture.players,fixture.transactions]),original,'Cancel must preserve all data');
  fixture.confirmReset();fixture.allowed=false;fixture.dialog.secondaryButton.action();assert.equal(fixture.saves,0,'Authority must be checked again at confirmation');
  fixture.allowed=true;fixture.confirmReset();fixture.dialog.secondaryButton.action();
  assert.equal(JSON.stringify(fixture.players.map(p=>[p.id,p.name,p.balance])),JSON.stringify([['a','甲',1500000],['b','乙',1500000]]));
  assert.equal(fixture.transactions.length,0);assert(fixture.players.every(p=>!p.isBankrupt && !p.isInJail));
  const restored=entity.MonopolyGameState.fromJson(new entity.MonopolyGameState(fixture.players,fixture.transactions).toJson());
  assert.equal(restored.players[0].balance,1500000);assert.equal(restored.transactions.length,0);
  assert.equal(restored.players[0].colorIndex,3);assert.equal(restored.players[1].colorIndex,5);
  const manager=load(path.join(root,'data/GameStatsManager.ets')).GameStatsManager.getInstance();
  await manager.recordGameStart(0,4);await manager.completeLatestGame(0,'平民');await manager.recordGameStart(1,5);
  assert.equal(manager.totalGames(),2);assert.equal(manager.totalPlayerParticipations(),9);
  const store=load(path.join(root,'store/StatsStore.ets')).StatsStore.getInstance();
  await store.clearAllStats();assert.equal(store.totalGames,0);assert.equal(store.totalPlayers,0);assert.equal(store.allRecords.length,0);
  assert.equal(data.get('game_stats_records'),'[]','Reset must persist an empty history');
  cache.clear(); const reloaded=load(path.join(root,'data/GameStatsManager.ets')).GameStatsManager.getInstance();
  await reloaded.loadFromStorage();assert.equal(reloaded.totalGames(),0,'Restart must not resurrect reset records');
  console.log('PASS: production Harmony ledger reset, cancellation, host authority, player preservation, serialization; statistics reset and reload');
})().catch(e=>{console.error(e);process.exitCode=1;});
