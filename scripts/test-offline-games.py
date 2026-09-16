"""Run production Kotlin rules and ArkTS stores without platform UI dependencies."""
import os,re,subprocess,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
CACHE=Path.home()/'.gradle/caches/modules-2/files-2.1'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe')
VERSION=re.search(r'kotlin = "([^"]+)"',(ROOT/'gradle/libs.versions.toml').read_text()).group(1)
def jar(g,n,v=None): return str(sorted((CACHE/g/n/v if v else CACHE/g/n).glob('**/*.jar'))[-1])
stdlib=jar('org.jetbrains.kotlin','kotlin-stdlib',VERSION)
annotations=jar('org.jetbrains','annotations')
compiler=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable',VERSION),stdlib,annotations,jar('org.jetbrains.kotlin','kotlin-script-runtime'),jar('org.jetbrains.kotlin','kotlin-reflect'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm')]
runtime=[stdlib,annotations,jar('org.jetbrains.kotlinx','kotlinx-serialization-core-jvm')]
with tempfile.TemporaryDirectory(prefix='offline-game-rules-') as d:
    tmp=Path(d)
    # Word content is an adapter; production identity allocation and lookup are compiled unchanged.
    (tmp/'Words.kt').write_text('package org.walks.gamecopilot\nfun currentTimeMillis() = System.currentTimeMillis()\nfun getWordMapBySelectedGroups(groups: Set<String>) = mutableMapOf("咖啡" to "奶茶")',encoding='utf-8')
    (tmp/'Groups.kt').write_text('package org.walks.gamecopilot.data.entity\nobject WordGroupManager { fun getDefaultSelectedGroups() = setOf("test") }',encoding='utf-8')
    base=ROOT/'composeApp/src/commonMain/kotlin/org/walks/gamecopilot'
    shared=ROOT/'shared/src/commonMain/kotlin/org/walks/gamecopilot'
    sources=[shared/'werewolf/WerewolfGameLogic.kt',shared/'werewolf/data/WerewolfModels.kt',base/'data/entity/GameEntity.kt',ROOT/'scripts/offline-game-tests/Rules.kt',tmp/'Words.kt',tmp/'Groups.kt']
    sources += [base/'awalong'/name for name in ['AwalongConfig.kt','AwalongCustomConfig.kt','AwalongGameLogic.kt','data/AwalongGameState.kt']]
    sources += [base/'data/entity/MonopolyEntity.kt', base/'data/entity/MonopolyLedgerRules.kt', ROOT/'scripts/offline-game-tests/LedgerRules.kt']
    subprocess.run([str(JAVA),'-cp',os.pathsep.join(compiler),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-classpath',os.pathsep.join(runtime),'-d',str(tmp/'classes')]+list(map(str,sources)),check=True)
    subprocess.run([str(JAVA),'-cp',os.pathsep.join([str(tmp/'classes')]+runtime),'RulesKt'],check=True)
subprocess.run([r'C:\Program Files\Huawei\DevEco Studio\tools\node\node.exe',str(ROOT/'scripts/offline-game-tests/stores.cjs')],check=True)
