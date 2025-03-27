package org.walks.gamecopilot.data.entity

import org.walks.gamecopilot.wordMap

data class GameEntity(
    val gameMode: Int = 0,
    val timeEntityList: MutableList<LocalSpyEntity> = mutableListOf()
) {

}


data class LocalSpyEntity(
    var gameWord: String = "",
    var spyNum: Int = 1,
    var totalPlayerNumber: Int = 1,
    var spyWord: String = "",
    var spies: List<Int> = listOf(),
    var blackNum: Int = 0
) {
    fun refreshGame(){
        getUniqueRandomBatch()
        optNewGameWord()
    }

    // 基于洗牌法的安全实现
    private fun getUniqueRandomBatch() {
        if (totalPlayerNumber / 3 < spyNum) {
            spyNum = totalPlayerNumber / 3
        }
        require(totalPlayerNumber >= 0) { "区间至少需要2个数字" }
        spies = (1..totalPlayerNumber).shuffled().take(spyNum)
    }

    private fun optNewGameWord() {
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
        for (i in 0..<blackNum) {
            if (spies[i] == currentSelectPlayer) {
                return "[空白]"
            }
        }
        return spyWord
    }

    fun isSpy(num:Int): Boolean{
        return spies.contains(num)
    }
}

