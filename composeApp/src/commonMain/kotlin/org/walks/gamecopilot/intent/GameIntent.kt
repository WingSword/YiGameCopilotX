package org.walks.gamecopilot.intent

sealed class GameIntent {
    data class updatePlayerNum(val num: Int): GameIntent()
    data class updateGameMode(val mode: Int): GameIntent()
    data class startGame(val mode: Int): GameIntent()
    data class CreateAGameRoom(val roomName:String,val roomKey: String): GameIntent()
    data class JoinToAGameRoom(val roomName:String,val roomKey: String): GameIntent()
}