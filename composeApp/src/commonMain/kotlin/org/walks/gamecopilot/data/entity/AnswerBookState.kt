package org.walks.gamecopilot.data.entity

import org.walks.gamecopilot.data.AnswerBookEntry

enum class AnswerBookPhase { CLOSED, CLOSING, OPENING, OPEN }

/** 旧答案在合上封面前保留；只有合页结束后才替换为下一条答案。 */
data class AnswerBookState(
    val currentQuestion: String = "",
    val currentAnswer: AnswerBookEntry? = null,
    val pendingAnswer: AnswerBookEntry? = null,
    val phase: AnswerBookPhase = AnswerBookPhase.CLOSED,
    val lastAnswerIndex: Int = -1,
) {
    val isFlipping: Boolean
        get() = phase == AnswerBookPhase.CLOSING || phase == AnswerBookPhase.OPENING

    fun beginFlip(answer: AnswerBookEntry, answerIndex: Int): AnswerBookState {
        if (isFlipping) return this
        return if (currentAnswer == null) {
            copy(currentAnswer = answer, phase = AnswerBookPhase.OPENING, lastAnswerIndex = answerIndex)
        } else {
            copy(pendingAnswer = answer, phase = AnswerBookPhase.CLOSING, lastAnswerIndex = answerIndex)
        }
    }

    fun finishAnimation(completedPhase: AnswerBookPhase): AnswerBookState {
        if (phase != completedPhase) return this
        return when (phase) {
            AnswerBookPhase.CLOSING -> copy(
                currentAnswer = pendingAnswer,
                pendingAnswer = null,
                phase = AnswerBookPhase.OPENING
            )
            AnswerBookPhase.OPENING -> copy(phase = AnswerBookPhase.OPEN)
            else -> this
        }
    }
}
