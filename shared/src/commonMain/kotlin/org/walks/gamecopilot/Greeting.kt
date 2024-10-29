package org.walks.gamecopilot


import kotlin.random.Random

class Greeting {
    private val platform: Platform = getPlatform()
    private var playMode = "本地模式"
    val currentList = mutableListOf<String>()//卧底词汇列表

    private val gameList by lazy {
        mutableListOf<String>().apply {

        }
    }
    var badGuyNumber = 0
    var totalPlayer = 0
    var currentPlayerNumber = 0
    fun greet(): String {
        return playMode
    }

    fun optBadGuyNumber(): Int {
        badGuyNumber = Random.nextInt(1, totalPlayer)
        return badGuyNumber
    }

    fun optNewGameWord() {
        val randomInt = Random.nextInt(0, gameList.size)
        currentList.add(gameList[randomInt])
    }


}