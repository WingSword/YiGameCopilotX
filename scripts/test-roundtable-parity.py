"""Run both production layout implementations and check geometry, paging and color stability."""
import itertools, json, os, re, subprocess, tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HARMONY = Path(os.environ.get('HARMONY_PROJECT', ROOT.parent/'YiGameCopilotX-Harmony'))
DEVECO = Path('C:/Program Files/Huawei/DevEco Studio')
CACHE = Path(os.environ.get('GRADLE_USER_HOME', Path.home()/'.gradle'))/'caches/modules-2/files-2.1'
VERSION = re.search(r'kotlin = "([^"]+)"', (ROOT/'gradle/libs.versions.toml').read_text()).group(1)
def jar(group, name, version=None):
    base=CACHE/group/name
    return str(sorted((base/version if version else base).glob('**/*.jar'))[-1])
def run(args, **kwargs):
    p=subprocess.run([str(a) for a in args],capture_output=True,text=True,encoding='utf-8',**kwargs)
    if p.returncode:raise RuntimeError(p.stdout+p.stderr)
    return p.stdout

inputs=list(itertools.product([280,360,520,599,600,720,840,1040,1440], [188,211,240,280,300,340,400,520,700], range(21), range(6)))
wire=''.join('\t'.join(map(str,c))+'\n' for c in inputs)
java=Path(os.environ.get('JAVA_HOME','C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot'))/'bin/java.exe'
node=DEVECO/'tools/node/node.exe'
ts=DEVECO/'sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript'
stdlib=jar('org.jetbrains.kotlin','kotlin-stdlib',VERSION)
ann=jar('org.jetbrains','annotations')
compiler=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable',VERSION),stdlib,
    jar('org.jetbrains.kotlin','kotlin-script-runtime'),jar('org.jetbrains.kotlin','kotlin-reflect'),
    jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm'),ann]
with tempfile.TemporaryDirectory(prefix='roundtable-parity-') as d:
    tmp=Path(d)
    (tmp/'Table.kt').write_text('''import org.walks.gamecopilot.ui.page.monopoly.RoundTableLayout as R
fun main() {
    val colors = R.assignColors(listOf(2, 2, -1, 0, -1))
    check(colors == listOf(2, 1, 3, 0, 4))
    check(R.assignColors(colors) == colors)
    check(R.assignColors(List(20) { -1 }).distinct().size == 20)
    check(R.assignColors(listOf(2, 0, 4)) == listOf(2, 0, 4))
    generateSequence(::readlnOrNull).forEach { line ->
        val v = line.split('\\t').map { it.toDouble() }
        val p = R.plan(v[0],v[1],v[2].toInt(),v[3].toInt())
        println("${p.capacity}|${p.page}|${p.pages}|" + p.seats.joinToString(";") { "${it.playerIndex},${it.x},${it.y},${it.width},${it.height}" })
    }
}
''',encoding='utf-8')
    run([java,'-cp',os.pathsep.join(compiler),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect',
        '-classpath',os.pathsep.join([stdlib,ann]),'-d',tmp/'classes',
        ROOT/'composeApp/src/commonMain/kotlin/org/walks/gamecopilot/ui/page/monopoly/RoundTableLayout.kt',tmp/'Table.kt'])
    kotlin=run([java,'-cp',os.pathsep.join([str(tmp/'classes'),stdlib]),'TableKt'],input=wire).splitlines()
    (tmp/'test.cjs').write_text('''const fs=require('fs'),vm=require('vm'),assert=require('assert'),ts=require(process.argv[2]);
function load(p) {const s={exports:{}};vm.runInNewContext(ts.transpileModule(fs.readFileSync(p,'utf8'),{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2021}}).outputText,s);return s.exports;}
const R=load(process.argv[3]).RoundTableLayout;
assert.equal(JSON.stringify(R.assignColors([2,2,-1,0,-1])), '[2,1,3,0,4]');
assert.equal(JSON.stringify(R.assignColors([2,1,3,0,4])), '[2,1,3,0,4]');
assert.equal(new Set(R.assignColors(Array(20).fill(-1))).size,20);
assert.equal(JSON.stringify(R.assignColors([2,0,4])), '[2,0,4]');
const M=load(process.argv[4]);
const old=M.MonopolyGameState.fromJson(' {"players":[{"id":"one","name":"One","balance":123456}],"transactions":[]}');
assert.equal(old.players[0].balance,123456);assert.equal(old.players[0].colorIndex,-1);
old.players[0].colorIndex=7;
const restored=M.MonopolyGameState.fromJson(old.toJson());
assert.equal(restored.players[0].colorIndex,7);assert.equal(restored.players[0].balance,123456);
for(const line of fs.readFileSync(0,'utf8').trim().split('\\n')) {
 const p=R.plan(...line.split('\\t').map(Number));
 console.log(`${p.capacity}|${p.page}|${p.pages}|`+p.seats.map(s=>`${s.playerIndex},${s.x},${s.y},${s.width},${s.height}`).join(';'));
}
''',encoding='utf-8')
    arkts=run([node,tmp/'test.cjs',ts,HARMONY/'entry/src/main/ets/pages/monopoly/RoundTableLayout.ets',
        HARMONY/'entry/src/main/ets/model/entity/MonopolyEntity.ets'],input=wire).splitlines()
    assert len(kotlin)==len(arkts)==len(inputs)
    covered={}
    def overlap(a,b):return a[0] < b[2]-1e-6 and a[2] > b[0]+1e-6 and a[1] < b[3]-1e-6 and a[3] > b[1]+1e-6
    for case,left,right in zip(inputs,kotlin,arkts):
        a=left.split('|');b=right.split('|');assert a[:3]==b[:3],(case,a,b)
        sa=[[float(v) for v in t.split(',')] for t in a[3].split(';') if t]
        sb=[[float(v) for v in t.split(',')] for t in b[3].split(';') if t]
        assert len(sa)==len(sb)
        for x,y in zip(sa,sb):assert all(abs(i-j)<1e-5 for i,j in zip(x,y)),(case,x,y)
        w,h,n,_=case;h=max(188 if w>=600 else 280,h);rects=[];center=((w-220)/2,(h-84)/2,(w+220)/2,(h+84)/2)
        for index,x,y,cw,ch in sa:
            assert 0<=index<n and 0<=x and 0<=y and x+cw<=w+1e-6 and y+ch<=h+1e-6,(case,sa)
            rect=(x,y,x+cw,y+ch)
            assert not overlap(rect,center),('center blocked',case,rect)
            assert not any(overlap(rect,r) for r in rects),('seats overlap',case,rect)
            rects.append(rect)
            covered.setdefault((w,h,n),set()).add(int(index))
    for (w,h,n),seen in covered.items():assert seen==set(range(n)),('missing players',w,h,n,seen)
print(f'PASS: {len(inputs)} matching Kotlin/ArkTS layouts; no overlap, center access, full paging; unique/stable colors and legacy/new Harmony serialization.')
