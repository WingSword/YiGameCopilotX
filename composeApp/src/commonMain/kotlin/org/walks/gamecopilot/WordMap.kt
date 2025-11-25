package org.walks.gamecopilot

import org.walks.gamecopilot.data.wordsEasy
import org.walks.gamecopilot.data.wordsMiddle
import org.walks.gamecopilot.data.wordsHard

// 默认词汇映射（包含所有词组）
val wordMap by lazy {
    mutableMapOf<String, String>().apply {
        putAll(wordsEasy)
        putAll(wordsMiddle)
        putAll(wordsHard)
    }
}

// 按词组分组的词汇映射
val wordMapByGroup = mapOf(
    "easy" to wordsEasy,
    "middle" to wordsMiddle,
    "hard" to wordsHard
)

/**
 * 根据选中的词组ID获取词汇映射
 */
fun getWordMapBySelectedGroups(selectedGroupIds: Set<String>): MutableMap<String, String> {
    return mutableMapOf<String, String>().apply {
        selectedGroupIds.forEach { groupId ->
            putAll(wordMapByGroup[groupId] ?: emptyMap())
        }
    }
}

/**
 * 获取指定词组的词汇映射
 */
fun getWordMapByGroup(groupId: String): Map<String, String> {
    return wordMapByGroup[groupId] ?: emptyMap()
}