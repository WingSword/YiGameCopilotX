package org.walks.gamecopilot.intent

/**
 * 答案之书意图
 */
sealed class AnswerBookIntent {
    /** 翻开答案之书 */
    data object FlipBook : AnswerBookIntent()

    /** 重置，再翻一次 */
    data object ResetFlip : AnswerBookIntent()

    /** 更新问题输入 */
    data class UpdateQuestion(val question: String) : AnswerBookIntent()
}
