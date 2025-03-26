package org.walks.gamecopilot.data.entity

import org.walks.gamecopilot.addWordsToMap
import org.walks.gamecopilot.wordMap

data class GameEntity(
    val gameMode: Int = 0,
    val timeEntityList: MutableList<TimeEntity> = mutableListOf()
) {

}

private val wordList by lazy {
    addWordsToMap(wordMap)
    wordMap.values.toList()
    wordMap.keys.toList()
}

data class TimeEntity(
    var gameWord: String = "",
    var spyNum: Int = 1,
    var totalPlayerNumber: Int = 1,
    var spyWord: String = "",
    var spies: List<Int> = listOf(),
    var blackNum: Int = 0
) {
    companion object {

    }

    // 方式一：基于洗牌法的安全实现（推荐）
    fun getUniqueRandomBatch() {
        if (totalPlayerNumber / 3 < spyNum) {
            spyNum = totalPlayerNumber / 3
        }
        require(totalPlayerNumber  >= 0) { "区间至少需要2个数字" }
        spies= (1..totalPlayerNumber).shuffled().take(spyNum)
    }

    fun optNewGameWord(list: List<String> = wordMap.keys.toList()) {
        this.gameWord = list.random()
    }
}

