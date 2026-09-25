"""Compile the production AI providers and exercise privacy gates without contacting any API."""
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
            'ktor-io-jvm', 'ktor-events-jvm', 'ktor-websockets-jvm', 'ktor-websocket-serialization-jvm', 'ktor-sse-jvm',
            'ktor-client-content-negotiation-jvm', 'ktor-serialization-kotlinx-json-jvm',
            'ktor-serialization-jvm', 'ktor-serialization-kotlinx-jvm']]
runtime += [jar('io.ktor', 'ktor-client-okhttp-jvm', '3.1.1')]
base = ROOT / 'composeApp/src/commonMain/kotlin/org/walks/gamecopilot'
sources = [base / 'distribution/AppDistribution.kt', ROOT / 'scripts/cloud-room-tests/Distribution.kt',
           ROOT / 'scripts/ai-privacy-tests/PrivacyChecks.kt']
sources += [base / 'service/ai' / name for name in ['AiConfig.kt', 'AiProvider.kt', 'AiService.kt',
           'AiServiceFactory.kt', 'DeepSeekProvider.kt', 'FallbackAiService.kt']]
with tempfile.TemporaryDirectory(prefix='ai-privacy-tests-') as directory:
    classes = Path(directory) / 'classes'
    subprocess.run([str(JAVA), '-cp', os.pathsep.join(compiler), 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
                    '-no-stdlib', '-no-reflect', '-jvm-target', '17',
                    '-Xplugin=' + jar('org.jetbrains.kotlin', 'kotlin-serialization-compiler-plugin-embeddable', VERSION),
                    '-classpath', os.pathsep.join(runtime), '-d', str(classes)] + list(map(str, sources)), check=True)
    for channel in ('domestic', 'googlePlay', 'direct', 'fdroid'):
        subprocess.run([str(JAVA), '-cp', os.pathsep.join([str(classes)] + runtime), 'PrivacyChecksKt'],
                       env=dict(os.environ, YIGAME_TEST_CHANNEL=channel), check=True)
