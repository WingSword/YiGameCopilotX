package org.walks.gamecopilot.data.entity

import kotlinx.serialization.Serializable
import org.walks.gamecopilot.getWordMapBySelectedGroups

data class GameEntity(
    val gameMode: Int = 0,
    val currentGame: LocalSpyEntity = LocalSpyEntity(),
    val gameCount: Int = 0, // 游戏局数
    val globalSelectedWordGroups: Set<String> = WordGroupManager.getDefaultSelectedGroups() // 全局词库选择
)

@Serializable
data class LocalSpyEntity(
    var gameWord: String = "",
    var spyNum: Int = 1,
    var totalPlayerNumber: Int = 4,
    var spyWord: String = "",
    var spies: List<Int> = listOf(),
    var blackNum: Int = 0,
    var nicknames: List<String> = listOf() // 玩家昵称列表
) {
    fun refreshGame(selectedWordGroups: Set<String> = WordGroupManager.getDefaultSelectedGroups()){
        getUniqueRandomBatch()
        optNewGameWord(selectedWordGroups)
    }

    // 基于洗牌法的安全实现
    private fun getUniqueRandomBatch() {
        totalPlayerNumber = totalPlayerNumber.coerceIn(4, 16)
        spyNum = spyNum.coerceIn(1, totalPlayerNumber / 3)
        blackNum = blackNum.coerceIn(0, spyNum)
        spies = (1..totalPlayerNumber).shuffled().take(spyNum)
    }

    private fun optNewGameWord(selectedWordGroups: Set<String>) {
        val selectedWordMap = getWordMapBySelectedGroups(selectedWordGroups)
        if (selectedWordMap.isEmpty()) {
            // 如果没有选中任何词组，不设置词汇
            gameWord = ""
            spyWord = ""
        } else {
            optNewGameWordWithMap(selectedWordMap)
        }
    }
    
    private fun optNewGameWordWithMap(wordMap: MutableMap<String, String>) {
        val n = (0..1).random()
        val randomIndex = (0..<wordMap.size).random()
        if (n == 0) {
            gameWord = wordMap.getValue(wordMap.keys.elementAt(randomIndex))
            spyWord = wordMap.keys.elementAt(randomIndex)
        } else {
            spyWord = wordMap.getValue(wordMap.keys.elementAt(randomIndex))
            gameWord = wordMap.keys.elementAt(randomIndex)
        }
    }

    fun optIdentity(currentSelectPlayer: Int): String {
        if (!spies.contains(currentSelectPlayer)) return gameWord
        if (currentSelectPlayer in spies.take(blackNum.coerceAtLeast(0))) return "[空白]"
        return spyWord
    }

    fun isSpy(num:Int): Boolean{
        return spies.contains(num)
    }
}

