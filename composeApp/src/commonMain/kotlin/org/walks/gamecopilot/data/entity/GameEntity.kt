package org.walks.gamecopilot.data.entity

import kotlin.random.Random

data class GameEntity(
    val gameMode: Int = 0,
    val timeEntityList: MutableList<TimeEntity> = mutableListOf()
) {

}

data class TimeEntity(
    var gameWord: String = "",
    var spyNum: Int = 1,
    val gamePlayerNumber: Int = 0,
) {


    fun optBadGuyNumber(): Int {
        this.spyNum = Random.nextInt(1, gamePlayerNumber)
        return this.spyNum
    }

    fun optNewGameWord(list: List<String>) {
        this.gameWord = list.random()
    }
}

