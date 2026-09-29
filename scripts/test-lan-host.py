"""Exercise the production Android Ktor host lifecycle on the JVM using cached dependencies.

Only the expect/actual factory declarations and Android logger are adapted.
Run after an Android build populates the Gradle cache; no network service is used.
"""
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CACHE = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle')) / 'caches/modules-2/files-2.1'
VERSION = re.search(r'kotlin = "([^"]+)"', (ROOT / 'gradle/libs.versions.toml').read_text()).group(1)
JAVA = Path(os.environ.get('JAVA_HOME', 'C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot')) / 'bin/java.exe'


def jar(group, name, version=None):
    base = CACHE / group / name
    paths = sorted(p for p in (base / version if version else base).glob('**/*.jar')
                   if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
    if not paths:
        raise FileNotFoundError(f'Build Android first to populate {base}')
    return str(paths[-1])


stdlib = jar('org.jetbrains.kotlin', 'kotlin-stdlib', VERSION)
annotations = jar('org.jetbrains', 'annotations')
coroutines = jar('org.jetbrains.kotlinx', 'kotlinx-coroutines-core-jvm', '1.10.2')
compiler = [jar('org.jetbrains.kotlin', 'kotlin-compiler-embeddable', VERSION), stdlib, annotations,
            jar('org.jetbrains.kotlin', 'kotlin-script-runtime'), jar('org.jetbrains.kotlin', 'kotlin-reflect'), coroutines]
runtime = [stdlib, annotations, coroutines, jar('org.slf4j', 'slf4j-api'), jar('org.jetbrains.kotlin', 'kotlin-reflect')]
runtime += [jar('org.jetbrains.kotlinx', name) for name in ['kotlinx-serialization-core-jvm',
            'kotlinx-serialization-json-jvm', 'kotlinx-io-core-jvm', 'kotlinx-io-bytestring-jvm']]
runtime += [jar('io.ktor', name, '3.3.3') for name in ['ktor-server-core-jvm', 'ktor-server-netty-jvm',
            'ktor-server-websockets-jvm', 'ktor-http-jvm', 'ktor-http-cio-jvm', 'ktor-utils-jvm', 'ktor-io-jvm',
            'ktor-events-jvm', 'ktor-websockets-jvm', 'ktor-websocket-serialization-jvm', 'ktor-network-jvm',
            'ktor-serialization-jvm']]
runtime += [jar('io.netty', name, '4.2.7.Final') for name in ['netty-common', 'netty-buffer', 'netty-transport',
            'netty-resolver', 'netty-codec-base', 'netty-codec-http', 'netty-codec-http2', 'netty-handler',
            'netty-transport-native-unix-common', 'netty-transport-classes-kqueue', 'netty-transport-classes-epoll',
            'netty-codec-compression']]
with tempfile.TemporaryDirectory(prefix='lan-host-tests-') as directory:
    tmp = Path(directory)
    base = ROOT / 'shared/src'
    interface = tmp / 'LANHostServer.kt'
    interface.write_text((base / 'commonMain/kotlin/org/walks/gamecopilot/lan/server/LANHostServer.kt')
                         .read_text(encoding='utf-8').replace('expect fun createLANHostServer(): LANHostServer', ''), encoding='utf-8')
    host = tmp / 'AndroidLANHostServer.kt'
    host.write_text((base / 'androidMain/kotlin/org/walks/gamecopilot/lan/server/AndroidLANHostServer.kt')
                   .read_text(encoding='utf-8').replace('actual fun createLANHostServer()', 'fun createLANHostServer()'), encoding='utf-8')
    sources = [interface, host, base / 'commonMain/kotlin/org/walks/gamecopilot/lan/data/LANModels.kt',
               ROOT / 'scripts/lan-host-tests/GameLogger.kt', ROOT / 'scripts/lan-host-tests/HostLifecycleChecks.kt']
    subprocess.run([str(JAVA), '-cp', os.pathsep.join(compiler), 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
                    '-no-stdlib', '-no-reflect', '-jvm-target', '17',
                    '-Xplugin=' + jar('org.jetbrains.kotlin', 'kotlin-serialization-compiler-plugin-embeddable', VERSION),
                    '-classpath', os.pathsep.join(runtime), '-d', str(tmp / 'classes')] + list(map(str, sources)), check=True)
    subprocess.run([str(JAVA), '-XX:ActiveProcessorCount=2', '-cp', os.pathsep.join([str(tmp / 'classes')] + runtime),
                    'HostLifecycleChecksKt'], check=True, timeout=60)
