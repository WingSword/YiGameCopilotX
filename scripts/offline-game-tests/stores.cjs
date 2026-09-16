const fs=require('node:fs'), path=require('node:path'), vm=require('node:vm'), assert=require('node:assert/strict');
const ts=require('C:/Program Files/Huawei/DevEco Studio/sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript');
const root=path.resolve(__dirname,'../../../YiGameCopilotX-Harmony/entry/src/main/ets');
const cache=new Map();
const prefs={get:async(k,v)=>v,getObject:async(k,v)=>v,put:async()=>{},putObject:async()=>{}};
function load(file) {
  file=path.resolve(root,file); if(cache.has(file))return cache.get(file);
  const m={exports:{}}; cache.set(file,m.exports);
  const context={exports:m.exports,Observed:v=>v,Map,Set,Date,Math,console,require(p) {
    if(p.endsWith('/Logger'))return {Logger:{info(){},error(){},debug(){}}};
    if(p.endsWith('/PlatformHelper'))return {PlatformHelper:{getInstance:()=>({vibrateMethod(){},vibrateLongMethod(){},startPersistentAlert(){},stopPersistentAlert(){}})}};
    if(p.endsWith('/PreferencesUtils'))return {PreferencesUtils:prefs,PrefKeys:{}};
    assert.ok(p.startsWith('.'),'Unexpected platform import '+p); return load(path.resolve(path.dirname(file),p+'.ets'));
  }};
  vm.runInNewContext(ts.transpileModule(fs.readFileSync(file,'utf8'),{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2021,experimentalDecorators:true}}).outputText,context,{filename:file});
  return context.exports;
}
const W=load('store/WerewolfStore.ets').WerewolfStore;
const R=load('model/werewolf/WerewolfRole.ets').WerewolfRole;
const RI=load('model/werewolf/WerewolfRole.ets').WerewolfRoleInfo;
const M=load('model/werewolf/WerewolfGameState.ets');
const P=load('model/werewolf/WerewolfPreset.ets').WerewolfPresets;
let rounds=0;
for(let n=3;n<=10;n++)for(let repeat=0;repeat<80;repeat++) {
  const w=new W(); w.setPlayerCount(n); w.initializeGame();
  let g=w.gameState;
  assert.equal(g.players.length,n);assert.equal(g.centerCards.length,3);
  const order=g.nightActionOrder.map(i=>RI.getNightOrder(g.players[i].initialRole)).filter(v=>v>0);
  assert.equal(JSON.stringify(order),JSON.stringify([...order].sort((a,b)=>a-b)));
  for(let i=0;i<n;i++){w.advanceDealCard();assert.equal(g.dealCardRevealed,true);w.advanceDealCard();}
  assert.equal(g.phase,M.WerewolfGamePhase.NIGHT_START);
  for(let step=0;step<g.nightActionOrder.length;step++) {
    g.currentNightStep=step;const p=g.players[g.nightActionOrder[step]],others=g.players.filter(x=>x.id!==p.id);
    switch(p.initialRole) {
      case R.SEER:w.executeSeerViewCenter(0,1);break;
      case R.ROBBER:w.executeRobberSwap(p.id,others[0].id);break;
      case R.TROUBLEMAKER:w.executeTroublemakerSwap(others[0].id,others[1].id);break;
      case R.DRUNK:w.executeDrunkSwap(p.id,0);break;
      case R.INSOMNIAC:assert.ok(w.getInsomniacResult(p.id));break;
    }
  }
  w.finalizeNightActions();w.startVoting();
  for(let i=0;i<n;i++){w.setVote(i,(i+1)%n);w.advanceVoter();}
  assert.ok(g.players.every(p=>p.isAlive));w.finishGame();assert.equal(g.phase,M.WerewolfGamePhase.GAME_OVER);rounds++;
}
const spyClass=load('model/entity/LocalSpyEntity.ets').LocalSpyEntity;
const copied=new W();const Player=M.WerewolfPlayer;
copied.gameState.players=[R.DOPPELGANGER,R.ROBBER,R.INSOMNIAC,R.WEREWOLF].map((r,i)=>new Player(i,r,r,'P'+i));
copied.gameState.playerCount=4;copied.gameState.nightActionOrder=[0,3,1,2];
copied.executeDoppelgangerAction(0,1);copied.scheduleDoppelgangerFollowUp();assert.equal(copied.gameState.doppelgangerFollowUpStep,1);
copied.gameState.currentNightStep=1;copied.executeRobberSwap(0,2);copied.completeDoppelgangerFollowUp();copied.gameState.currentNightStep=3;
assert.ok(copied.executeRobberSwap(1,2).includes('化身幽灵'));copied.finalizeNightActions();
assert.equal(JSON.stringify(copied.gameState.players.map(p=>p.currentRole)),JSON.stringify([R.INSOMNIAC,R.ROBBER,R.ROBBER,R.WEREWOLF]));
for(let n=4;n<=16;n++)for(let count=1;count<=Math.floor(n/3);count++)for(let blanks=0;blanks<=count;blanks++)for(let r=0;r<20;r++) {
  const s=new spyClass();s.totalPlayerNumber=n;s.spyNum=count;s.blackNum=blanks;s.setWords('咖啡','奶茶');s.refreshGame();
  assert.equal(new Set(s.spies).size,count);assert.equal(Array.from({length:n},(_,i)=>s.optIdentity(i+1)).filter(x=>x==='[空白]').length,blanks);
}
const H=load('store/HuntTownStore.ets').HuntTownStore;
const HP=load('model/hunttown/HuntPhase.ets').HuntPhase;
const HR=load('model/hunttown/HuntRole.ets').HuntRole;
for(let n=4;n<=12;n++) {
  const h=new H();h.setPlayerCount(n);h.setWitchCount(1);h.startGame();
  assert.equal(h.phase,HP.DEAL_CARDS);
  for(let i=0;i<n;i++){assert.equal(h.dealIndex,i);h.advanceDeal();assert.equal(h.dealRevealed,true);h.advanceDeal();}
  assert.equal(h.phase,HP.NIGHT_CLOSE_EYES);
  h.advancePhase();h.selectMurderTarget(0);h.advancePhase();h.advancePhase();h.selectProtectTarget(0);h.advancePhase();
  assert.equal(h.lastNightDeath,null);h.advancePhase();assert.equal(h.phase,HP.DAY_DISCUSS);
  const witch=h.players.find(p=>p.role===HR.WITCH);h.revealPlayer(witch.id);h.revealPlayer((witch.id+1)%n);
  assert.equal(h.players.filter(p=>!p.alive).length,1);h.advancePhase();assert.equal(h.phase,HP.GAME_END);
  h.advancePhase();h.startGame();assert.equal(h.dealIndex,0);assert.equal(h.dealRevealed,false);
  const sheriff=h.players.find(p=>p.role===HR.SHERIFF);sheriff.alive=false;
  h.selectProtectTarget(0);assert.equal(h.selectedProtectTarget,null,'dead sheriff cannot protect');
  h.players.filter(p=>p.role===HR.WITCH).forEach(p=>{p.alive=false;});
  h.phase=HP.DAY_RESULT;h.advancePhase();assert.equal(h.phase,HP.GAME_END,'night elimination cannot continue into another exile');
}
const D=load('store/DrawGuessStore.ets').DrawGuessStore;
const draw=new D();assert.ok(draw.pickRandomWord());
draw.addPath('#000000',8,false);draw.addPointToCurrentPath({x:2,y:3});draw.undo();assert.equal(draw.paths.length,0);
draw.redo();assert.equal(draw.paths.length,1);draw.undo();draw.addPath('#FFFFFF',16,true);assert.equal(draw.redoPaths.length,0);
draw.clear();assert.equal(draw.paths.length,0);assert.ok(draw.pickRandomWordFromAll());
const A=load('store/AwalongStore.ets').AwalongStore;
const a=new A();a.initGame();assert.equal(a.gameState.roleList.length,5);
assert.equal(a.gameState.nickNameList.length,5);for(let i=0;i<5;i++)assert.ok(a.getVisibleRoles(i) instanceof Map);
a.selectTeam([0,1]);a.voteTeam(new Map([[0,true],[1,true],[2,true],[3,false],[4,false]]));a.executeTask(new Map([[0,true],[1,true]]));assert.equal(a.getCurrentDay().taskResult,1);
const process=load('model/awalong/AwalongCustomConfig.ets').calculateProcess;
for(let n=5;n<=10;n++)assert.equal(process(n).length,5);
const logic=load('store/AwalongStore.ets').AwalongGameLogic;
const Day=load('model/awalong/AwalongGameState.ets').AwalongGameDayEntity;
a.gameState.dayList=[1,-1,1,-1].map((r,i)=>{const d=new Day(i+1);d.taskResult=r;return d;});
assert.equal(a.checkGameEnd(),null);const fifth=new Day(5);fifth.taskResult=1;a.gameState.dayList.push(fifth);
assert.equal(a.checkGameEnd().winner,'蓝方');assert.equal(logic.checkAssassinationSuccess(a.gameState.roleList.indexOf(load('model/awalong/AwalongRole.ets').AwalongRole.MEILING),a.gameState),true);
a.resetGame();a.initGame();assert.equal(a.gameState.dayList.filter(d=>d.taskResult!==0).length,0);
console.log(`PASS Harmony offline: ${rounds} complete one-night rounds, all spy/blank counts, 9 full hunt flows and restarts, Avalon deal/team/vote/task, drawing/undo/redo/word changes`);
const LE=load('model/entity/MonopolyEntity.ets'), LR=load('model/entity/MonopolyRules.ets').MonopolyRules;
let ledgerCases=0;
for(const preset of ['electronic','property','score','chips'])for(let n=2;n<=20;n++) {
  const original=new LE.MonopolyGameState(Array.from({length:n},(_,i)=>new LE.MonopolyPlayer('p'+(i+1),'玩家'+(i+1),1000,[],false,0,false,i)),[],preset,1000);
  const entries=original.players.map(p=>new LE.MonopolyTransaction(null,p.id,10,'全员发放',123,null,p.name,'batch'));
  const posted=LR.apply(original,entries); assert.ok(posted.players.every(p=>p.balance===1010)); assert.equal(posted.transactions.length,n);
  assert.equal(LR.undo(posted).toJson(),original.toJson()); assert.equal(posted.preset,preset); assert.equal(posted.initialBalance,1000);
  const pay=new LE.MonopolyTransaction('p1','p2',500,'转账');
  const transferred=LR.apply(posted,[pay]); assert.equal(transferred.players.reduce((s,p)=>s+p.balance,0),posted.players.reduce((s,p)=>s+p.balance,0));
  assert.equal(LR.undo(transferred).toJson(),posted.toJson());
  for(const invalid of [new LE.MonopolyTransaction('p1','missing',1,''),new LE.MonopolyTransaction('p1','p2',0,''),new LE.MonopolyTransaction('p1','p1',1,'')])assert.throws(()=>LR.apply(original,[invalid]));
  const overdraw=()=>LR.apply(original,[new LE.MonopolyTransaction('p1','p2',1001,'')]); if(preset==='chips')assert.throws(overdraw);else assert.equal(overdraw().players[0].balance,-1);
  const bad=entries.map((e,i)=>i===n-1?new LE.MonopolyTransaction(null,e.toPlayerId,LR.limit,''):e);assert.throws(()=>LR.apply(original,bad));
  assert.ok(original.players.every(p=>p.balance===1000));
  assert.equal(LE.MonopolyGameState.fromJson(posted.toJson()).toJson(),posted.toJson(),'scene/group survives persisted round trip');
  ledgerCases++;
}
const groups=Array.from({length:10},(_,b)=>Array.from({length:17},(_,i)=>new LE.MonopolyTransaction(null,'p'+i,1,'batch',123,null,null,''+b))).flat();
assert.equal(LR.retainGroups(groups).length,85,'never retain a partial old batch');
const legacy=LE.MonopolyGameState.fromJson('{"players":[{"id":"a","name":"A","balance":1000}],"transactions":[{"fromPlayerId":null,"toPlayerId":"a","amount":5,"description":"old"}]}');
assert.equal(LR.undo(legacy).players[0].balance,995);assert.equal(legacy.preset,'electronic');
const exportState=new LE.MonopolyGameState([new LE.MonopolyPlayer('a','=SUM(1)',0)]);
assert.ok(LR.csv(exportState).includes('"\'=SUM(1)"'),'CSV formula-like nickname is escaped');
console.log(`PASS Harmony ledger: ${ledgerCases} scene/player-count cases, atomic batch/undo, history retention, legacy persistence and CSV escaping`);
