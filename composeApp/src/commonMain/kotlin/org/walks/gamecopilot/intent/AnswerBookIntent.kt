package org.walks.gamecopilot.intent

import org.walks.gamecopilot.data.entity.AnswerBookPhase

/**
 * 答案之书意图
 */
sealed class AnswerBookIntent {
    /** 翻开答案之书 */
    data object FlipBook : AnswerBookIntent()

    /** 由页面帧动画完成后推进，离开页面时不会继续播放不可见的动画。 */
    data class AnimationFinished(val phase: AnswerBookPhase) : AnswerBookIntent()

    /** 更新问题输入 */
    data class UpdateQuestion(val question: String) : AnswerBookIntent()
}
