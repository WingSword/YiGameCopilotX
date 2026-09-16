"""Compile the production Kotlin client and run the same HTTP contract suite against both clients.

Requires the existing Gradle cache, JDK and DevEco SDK; installs no dependencies.
Harmony uses its production Store with only OS HTTP/preferences/UUID adapters replaced.
"""
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
HARMONY = Path(os.environ.get('HARMONY_PROJECT', ROOT.parent / 'YiGameCopilotX-Harmony'))
DEVECO = Path(os.environ.get('DEVECO_HOME', 'C:/Program Files/Huawei/DevEco Studio'))
CACHE = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle')) / 'caches/modules-2/files-2.1'
VERSION = re.search(r'kotlin = "([^"]+)"', (ROOT / 'gradle/libs.versions.toml').read_text()).group(1)
JAVA = Path(os.environ.get('JAVA_HOME', 'C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot')) / 'bin/java.exe'


def jar(group, name, version=None):
    base = CACHE / group / name
    paths = sorted((base / version if version else base).glob('**/*.jar'))
    if not paths:
        raise FileNotFoundError(f'Build Android first to populate {base}')
    return str(paths[-1])


stdlib = jar('org.jetbrains.kotlin', 'kotlin-stdlib', VERSION)
annotations = jar('org.jetbrains', 'annotations')
coroutines = jar('org.jetbrains.kotlinx', 'kotlinx-coroutines-core-jvm')
compiler = [jar('org.jetbrains.kotlin', 'kotlin-compiler-embeddable', VERSION), stdlib, annotations,
            jar('org.jetbrains.kotlin', 'kotlin-script-runtime'), jar('org.jetbrains.kotlin', 'kotlin-reflect'), coroutines]
runtime = [stdlib, annotations, coroutines, jar('org.slf4j', 'slf4j-api'),
           jar('com.squareup.okhttp3', 'okhttp'), jar('com.squareup.okio', 'okio-jvm')]
runtime += [jar('org.jetbrains.kotlinx', n) for n in ['kotlinx-serialization-core-jvm',
            'kotlinx-serialization-json-jvm', 'kotlinx-io-core-jvm', 'kotlinx-io-bytestring-jvm']]
runtime += [jar('io.ktor', n, '3.3.3') for n in ['ktor-client-core-jvm', 'ktor-http-jvm', 'ktor-utils-jvm',
            'ktor-io-jvm', 'ktor-events-jvm', 'ktor-websockets-jvm', 'ktor-websocket-serialization-jvm', 'ktor-sse-jvm']]
runtime += [jar('io.ktor', 'ktor-client-okhttp-jvm', '3.1.1')]
with tempfile.TemporaryDirectory(prefix='cloud-client-tests-') as directory:
    tmp = Path(directory)
    subprocess.run([str(JAVA), '-cp', os.pathsep.join(compiler), 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
                    '-no-stdlib', '-no-reflect', '-jvm-target', '17',
                    '-Xplugin=' + jar('org.jetbrains.kotlin', 'kotlin-serialization-compiler-plugin-embeddable', VERSION),
                    '-classpath', os.pathsep.join(runtime), '-d', str(tmp / 'classes'),
                    str(ROOT / 'composeApp/src/commonMain/kotlin/org/walks/gamecopilot/online/CloudRoomClient.kt'),
                    str(ROOT / 'composeApp/src/commonMain/kotlin/org/walks/gamecopilot/online/CloudInvitations.kt'),
                    str(ROOT / 'shared/src/commonMain/kotlin/org/walks/gamecopilot/online/CloudRoomModels.kt'),
                    str(ROOT / 'shared/src/commonMain/kotlin/org/walks/gamecopilot/online/CloudLedgerModels.kt'),
                    str(ROOT / 'shared/src/commonMain/kotlin/org/walks/gamecopilot/online/CloudViewPolicy.kt'),
                    str(ROOT / 'scripts/cloud-room-tests/InvitationChecks.kt'),
                    str(ROOT / 'scripts/cloud-room-tests/Settings.kt'),
                    str(ROOT / 'scripts/cloud-room-tests/ClientBridge.kt')], check=True)
    subprocess.run([str(JAVA), '-cp', os.pathsep.join([str(tmp / 'classes')] + runtime), 'InvitationChecksKt'],
                   env=dict(os.environ, CLOUD_TEST_STORE=str(tmp / 'invitation.properties')), check=True)
    subprocess.run([str(DEVECO / 'tools/node/node.exe'), str(ROOT / 'scripts/cloud-room-tests/contract.cjs'),
                    str(JAVA), os.pathsep.join([str(tmp / 'classes')] + runtime), str(tmp), str(HARMONY),
                    str(DEVECO / 'sdk/default/openharmony/ets/build-tools/ets-loader/node_modules/typescript')], check=True)
