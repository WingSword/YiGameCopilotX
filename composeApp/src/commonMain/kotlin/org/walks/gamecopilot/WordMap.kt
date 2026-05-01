package org.walks.gamecopilot

import org.walks.gamecopilot.data.wordsEasy
import org.walks.gamecopilot.data.wordsHard
import org.walks.gamecopilot.data.wordsMiddle

// 默认词汇映射（包含所有词组）
val wordMap by lazy {
    mutableMapOf<String, String>().apply {
        putAll(wordsEasy)
        putAll(wordsMiddle)
        putAll(wordsHard)
        putAll(customSpyWordMaps.values.flatMap { it.entries }.associate { it.key to it.value })
    }
}

// 内置按词组分组的词汇映射
private val builtinWordMapByGroup = mapOf(
    "easy" to wordsEasy,
    "middle" to wordsMiddle,
    "hard" to wordsHard
)

// 自定义卧底词库存储（groupId -> 词对映射）
val customSpyWordMaps = mutableMapOf<String, Map<String, String>>()

// 自定义你画我猜词库存储（groupId -> 词汇列表）
val customDrawWordLists = mutableMapOf<String, List<String>>()

// 按词组分组的词汇映射（内置 + 自定义）
val wordMapByGroup: Map<String, Map<String, String>>
    get() = builtinWordMapByGroup + customSpyWordMaps

/**
 * 注册自定义卧底词组
 */
fun registerCustomSpyWords(groupId: String, words: Map<String, String>) {
    customSpyWordMaps[groupId] = words
}

/**
 * 移除自定义卧底词组
 */
fun unregisterCustomSpyWords(groupId: String) {
    customSpyWordMaps.remove(groupId)
}

/**
 * 注册自定义你画我猜词库
 */
fun registerCustomDrawWords(groupId: String, words: List<String>) {
    customDrawWordLists[groupId] = words
}

/**
 * 移除自定义你画我猜词库
 */
fun unregisterCustomDrawWords(groupId: String) {
    customDrawWordLists.remove(groupId)
}

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