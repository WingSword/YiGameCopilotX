"""Check production answer-book transitions, especially one-tap repeat and interrupted playback."""
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CACHE = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle')) / 'caches/modules-2/files-2.1'
VERSION = re.search(r'kotlin = "([^"]+)"', (ROOT / 'gradle/libs.versions.toml').read_text()).group(1)


def jar(group, artifact, version=None):
    base = CACHE / group / artifact
    files = sorted((base / version if version else base).glob('**/*.jar'))
    if not files:
        raise SystemExit(f'Missing cached {artifact}; build Android first.')
    return str(files[-1])


java = Path(os.environ['JAVA_HOME']) / 'bin/java.exe' if os.environ.get('JAVA_HOME') else shutil.which('java')
stdlib = jar('org.jetbrains.kotlin', 'kotlin-stdlib', VERSION)
annotations = jar('org.jetbrains', 'annotations')
compiler = [jar('org.jetbrains.kotlin', 'kotlin-compiler-embeddable', VERSION), stdlib,
            jar('org.jetbrains.kotlin', 'kotlin-script-runtime'), jar('org.jetbrains.kotlin', 'kotlin-reflect'),
            jar('org.jetbrains.kotlinx', 'kotlinx-coroutines-core-jvm'), annotations]
source = ROOT / 'composeApp/src/commonMain/kotlin/org/walks/gamecopilot'
with tempfile.TemporaryDirectory(prefix='answer-book-') as directory:
    tmp = Path(directory)
    harness = tmp / 'AnswerBookCheck.kt'
    harness.write_text('''import org.walks.gamecopilot.data.*
import org.walks.gamecopilot.data.entity.*
fun main() {
    var checks = 0
    fun verify(condition: Boolean) { check(condition); checks++ }
    val first = AnswerBookEntry("first", AnswerCategory.POSITIVE)
    val second = AnswerBookEntry("second", AnswerCategory.NEGATIVE)
    val third = AnswerBookEntry("third", AnswerCategory.NEUTRAL)
    val initial = AnswerBookState(currentQuestion = "question")
    verify(initial.phase == AnswerBookPhase.CLOSED && !initial.isFlipping)
    var state = initial.beginFlip(first, 0)
    verify(state.currentAnswer == first && state.phase == AnswerBookPhase.OPENING && state.isFlipping)
    verify(state.beginFlip(second, 1) == state)
    verify(state.finishAnimation(AnswerBookPhase.CLOSING) == state)
    state = state.finishAnimation(AnswerBookPhase.OPENING)
    verify(state.phase == AnswerBookPhase.OPEN && !state.isFlipping)
    verify(state.currentQuestion == "question" && state.lastAnswerIndex == 0)
    verify(state.finishAnimation(AnswerBookPhase.OPENING) == state)
    state = state.beginFlip(second, 1)
    verify(state.phase == AnswerBookPhase.CLOSING && state.currentAnswer == first)
    verify(state.pendingAnswer == second && state.isFlipping)
    verify(state.beginFlip(third, 2) == state)
    verify(state.finishAnimation(AnswerBookPhase.OPENING) == state)
    // Returning after leaving during closing completes the existing request, without an extra tap.
    state = state.copy().finishAnimation(AnswerBookPhase.CLOSING)
    verify(state.phase == AnswerBookPhase.OPENING && state.currentAnswer == second)
    verify(state.pendingAnswer == null && state.isFlipping)
    verify(state.finishAnimation(AnswerBookPhase.CLOSING) == state)
    verify(state.beginFlip(third, 2) == state)
    // The same holds if the composition is interrupted while opening.
    state = state.copy().finishAnimation(AnswerBookPhase.OPENING)
    verify(state.phase == AnswerBookPhase.OPEN && !state.isFlipping)
    verify(state.currentAnswer == second && state.lastAnswerIndex == 1)
    state = state.beginFlip(third, 2).finishAnimation(AnswerBookPhase.CLOSING)
        .finishAnimation(AnswerBookPhase.OPENING)
    verify(state.currentAnswer == third && state.phase == AnswerBookPhase.OPEN)
    verify(state.currentQuestion == "question")
    var last = -1
    repeat(100) {
        val next = AnswerBookData.getRandomAnswerExcluding(last)
        val index = AnswerBookData.answers.indexOf(next)
        verify(index >= 0 && index != last)
        last = index
    }
    println("PASS answer-book: $checks checks (one-tap repeat, rapid taps, stale completion, interruption, non-repeating answers)")
}
''', encoding='utf-8')
    subprocess.run([str(java), '-cp', os.pathsep.join(compiler), 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
                    '-no-stdlib', '-no-reflect', '-classpath', os.pathsep.join([stdlib, annotations]),
                    '-d', str(tmp / 'classes'), str(source / 'data/AnswerBookData.kt'),
                    str(source / 'data/entity/AnswerBookState.kt'), str(harness)], check=True)
    subprocess.run([str(java), '-cp', os.pathsep.join([str(tmp / 'classes'), stdlib]), 'AnswerBookCheckKt'], check=True)
