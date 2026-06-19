package org.walks.gamecopilot.data.entity

import kotlinx.serialization.Serializable
import org.walks.gamecopilot.currentTimeMillis

@Serializable
data class MonopolyPlayer(
    val id: String,
    val name: String,
    val balance: Long = 1500000,
    val properties: List<Int> = emptyList(),
    val isInJail: Boolean = false,
    val jailTurns: Int = 0,
    val position: Int = 0,
    val isBankrupt: Boolean = false
)

@Serializable
data class MonopolyGameState(
    val players: List<MonopolyPlayer> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val bankBalance: Long = Long.MAX_VALUE,
    val houses: Map<Int, Int> = emptyMap(),
    val hotels: Map<Int, Boolean> = emptyMap(),
    val gameStarted: Boolean = false,
    val roundNumber: Int = 1
)

@Serializable
data class MonopolyTransaction(
    val fromPlayerId: String?,
    val toPlayerId: String?,
    val amount: Long,
    val description: String,
    val timestamp: Long = currentTimeMillis()
)

@Serializable
data class MonopolySyncData(
    val type: MonopolySyncType,
    val transaction: MonopolyTransaction? = null,
    val playerUpdate: MonopolyPlayer? = null,
    val stateUpdate: MonopolyGameState? = null
)

@Serializable
enum class MonopolySyncType {
    TRANSACTION,
    PLAYER_UPDATE,
    FULL_STATE_SYNC,
    START_GAME,
    END_GAME,
    NEXT_TURN
}
