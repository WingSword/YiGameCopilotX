"""Execute production Kotlin and ArkTS rules against identical samples and expected boundary cases.

Uses the project's cached Kotlin compiler plus DevEco's TypeScript transpiler.
Override GRADLE_USER_HOME, JAVA_HOME, DEVECO_HOME or ARKTS_TYPESCRIPT when needed.
"""
import itertools
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
HARMONY = Path(os.environ.get('HARMONY_PROJECT', ROOT.parent / 'YiGameCopilotX-Harmony'))
DEVECO = Path(os.environ.get('DEVECO_HOME', 'C:/Program Files/Huawei/DevEco Studio'))
CACHE = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle')) / 'caches/modules-2/files-2.1'
VERSION = re.search(r'kotlin = "([^"]+)"', (ROOT / 'gradle/libs.versions.toml').read_text()).group(1)

def jar(group, artifact, version=None):
    base = CACHE / group / artifact
    files = sorted((base / version if version else base).glob('**/*.jar'))
    if not files:
        raise SystemExit(f'Missing cached {artifact}; run the Android build first.')
    return str(files[-1])

def run(args, **kwargs):
    result = subprocess.run([str(a) for a in args], text=True, encoding='utf-8', capture_output=True, **kwargs)
    if result.returncode:
        raise SystemExit(result.stdout + result.stderr)
    return result.stdout

spec = json.loads((ROOT / 'cross-platform/random-tools.json').read_text(encoding='utf-8'))
cases = spec['cases']
inputs = [(c['first'], c['second'], c['sample']) for c in cases]
for first, second in [('1','6'), ('6','1'), ('3','3'), ('-5','500'), ('','oops'), ('100','1')]:
    inputs.extend((first, second, n / 1000) for n in range(1000))
inputs += [('1','6', x) for x in [-1, 1, 1.01]]
wire = ''.join(f'{a}\t{b}\t{u}\n' for a,b,u in inputs)
java = Path(os.environ['JAVA_HOME']) / 'bin/java.exe' if os.environ.get('JAVA_HOME') else shutil.which('java')
node = shutil.which('node') or DEVECO / 'tools/node/node.exe'
stdlib = jar('org.jetbrains.kotlin', 'kotlin-stdlib', VERSION)
annotations = jar('org.jetbrains', 'annotations')
compiler = [jar('org.jetbrains.kotlin','kotlin-compiler-embeddable',VERSION), stdlib,
            jar('org.jetbrains.kotlin','kotlin-script-runtime'), jar('org.jetbrains.kotlin','kotlin-reflect'),
            jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm'), annotations]
with tempfile.TemporaryDirectory(prefix='random-parity-') as directory:
    tmp = Path(directory)
    harness = tmp / 'Parity.kt'
    harness.write_text('''import org.walks.gamecopilot.theme.RandomToolRules as R
fun main() {
    generateSequence(::readlnOrNull).forEach { line ->
        val p = line.split('\\t')
        val sample = p[2].toDouble()
        try { println("${R.dice(p[0], p[1], sample)}|${R.heads(sample)}") }
        catch (e: IllegalArgumentException) { println("ERR") }
    }
}
''', encoding='utf-8')
    rules = ROOT / 'composeApp/src/commonMain/kotlin/org/walks/gamecopilot/theme/RandomToolRules.kt'
    run([java, '-cp', os.pathsep.join(compiler), 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
         '-no-stdlib', '-no-reflect', '-classpath', os.pathsep.join([stdlib, annotations]),
         '-d', tmp / 'classes', rules, harness])
    kotlin = run([java, '-cp', os.pathsep.join([str(tmp / 'classes'), stdlib]), 'ParityKt'], input=wire).splitlines()
    harness_js = tmp / 'parity.cjs'
    harness_js.write_text('''const fs = require('fs'), vm = require('vm');
const ts = require(process.argv[2]);
const js = ts.transpileModule(fs.readFileSync(process.argv[3], 'utf8'), {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2021 }
}).outputText;
const sandbox = { exports: {} }; vm.runInNewContext(js, sandbox);
const R = sandbox.exports.RandomToolRules;
for (const line of fs.readFileSync(0,'utf8').trimEnd().split('\\n')) {
  const [a,b,u] = line.split('\\t');
  try { console.log(`${R.dice(a,b,Number(u))}|${R.heads(Number(u))}`); }
  catch { console.log('ERR'); }
}
''', encoding='utf-8')
    transpiler = os.environ.get('ARKTS_TYPESCRIPT', str(DEVECO / 'sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript'))
    arkts = run([node, harness_js, transpiler, HARMONY / 'entry/src/main/ets/theme/RandomToolRules.ets'], input=wire).splitlines()
    assert len(kotlin) == len(inputs), (len(kotlin), len(inputs))
    assert kotlin == arkts, 'Kotlin / ArkTS behavior diverged'
    for result, case in zip(kotlin, cases):
        assert result == f'{case["dice"]}|{str(case["heads"]).lower()}', (case, result)
    assert kotlin[-3:] == ['ERR'] * 3, 'Invalid entropy must be rejected'
    # An exhaustive deterministic unit interval must reach every die face equally.
    distribution = [int(v.split('|')[0]) for v in kotlin[len(cases):len(cases)+1000]]
    assert set(distribution) == set(range(1,7))
    assert max(distribution.count(v) for v in range(1,7)) - min(distribution.count(v) for v in range(1,7)) <= 1
print(f'PASS: {len(inputs)} matching Kotlin / ArkTS executions, expected boundaries, invalid samples and uniform face coverage')
