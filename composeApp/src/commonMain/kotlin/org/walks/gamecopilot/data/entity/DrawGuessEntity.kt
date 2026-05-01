package org.walks.gamecopilot.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class DrawGuessEntity(
    val currentWord: String = "",
    val currentDrawerIndex: Int = 0,
    val roundNumber: Int = 1,
    val maxRounds: Int = 3,
    val timeLeft: Int = 60,
    val gameState: DrawGuessGameState = DrawGuessGameState.WAITING,
    val scores: Map<String, Int> = emptyMap(),
    val playerList: List<String> = emptyList(),
    val currentPathData: String = "",
    val guessedPlayers: List<String> = emptyList()
)

@Serializable
enum class DrawGuessGameState {
    WAITING,
    DRAWING,
    GUESSING,
    ROUND_END,
    GAME_END
}

@Serializable
data class DrawPathPoint(
    val x: Float,
    val y: Float
)

@Serializable
data class DrawPathData(
    val points: List<DrawPathPoint>,
    val color: Long,
    val strokeWidth: Float,
    val isEraser: Boolean
)

@Serializable
data class DrawGuessSyncData(
    val type: DrawGuessSyncType,
    val drawerId: String? = null,
    val pathData: DrawPathData? = null,
    val guessText: String? = null,
    val guesserId: String? = null,
    val guesserName: String? = null
)

@Serializable
enum class DrawGuessSyncType {
    PATH_START,
    PATH_ADD_POINT,
    PATH_END,
    CLEAR_CANVAS,
    GUESS,
    CORRECT_GUESS,
    NEXT_ROUND,
    START_GAME,
    END_GAME,
    SYNC_FULL_STATE
}
