package org.walks.gamecopilot.data.entity

import org.walks.gamecopilot.data.AnswerBookEntry

/**
 * 答案之书页面状态
 *
 * @param currentQuestion 当前输入的问题
 * @param currentAnswer 当前显示的答案
 * @param isFlipping 是否正在翻书动画中
 * @param lastAnswerIndex 上一次答案在列表中的索引（用于避免连续重复）
 * @param flipProgress 翻书动画进度（0.0 ~ 1.0）
 */
data class AnswerBookState(
    val currentQuestion: String = "",
    val currentAnswer: AnswerBookEntry? = null,
    val isFlipping: Boolean = false,
    val lastAnswerIndex: Int = -1,
    val flipProgress: Float = 0f
)
