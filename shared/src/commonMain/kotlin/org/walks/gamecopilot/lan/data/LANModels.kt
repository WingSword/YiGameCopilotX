package org.walks.gamecopilot.lan.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class LANRoomInfo(
    val roomId: String,
    val roomName: String,
    val hostName: String,
    val hostAddress: String,
    val port: Int,
    val gameType: GameType,
    val maxPlayers: Int,
    val currentPlayers: Int,
    val hasPassword: Boolean,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
enum class GameType(val displayName: String) {
    LOCAL_SPY("谁是卧底"),
    AWALONG("阿瓦隆"),
    HUNT_TOWN("猎巫镇"),
    DRAW_GUESS("你画我猜"),
    RANDOM_TOOLS("随机工具"),
    MONOPOLY("大富翁"),
    ONE_NIGHT_WEREWOLF("一夜终极狼人"),
    ALL("全部")
}

@Serializable
enum class LANMessageType(val code: String) {
    DISCOVERY_BROADCAST("DISCOVERY"),
    DISCOVERY_RESPONSE("DISCOVERY_RESP"),
    JOIN_ROOM("JOIN"),
    JOIN_RESPONSE("JOIN_RESP"),
    LEAVE_ROOM("LEAVE"),
    PLAYER_JOINED("PLAYER_JOINED"),
    PLAYER_LEFT("PLAYER_LEFT"),
    GAME_STATE_SYNC("GAME_STATE"),
    GAME_ACTION("GAME_ACTION"),
    CHAT_MESSAGE("CHAT"),
    ERROR("ERROR"),
    HEARTBEAT("HEARTBEAT"),
    START_GAME("START_GAME"),
    END_GAME("END_GAME"),
    ROOM_CLOSED("ROOM_CLOSED")
}

@Serializable
data class LANMessage(
    val type: LANMessageType,
    val roomId: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val payload: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class LANPlayer(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val playerIndex: Int = -1,
    val connectedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class LANRoomState(
    val roomInfo: LANRoomInfo,
    val players: List<LANPlayer> = emptyList(),
    val gameState: String = "",
    val gameStarted: Boolean = false,
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class LANConnectionState(
    val status: ConnectionStatus,
    val message: String = "",
    val retryCount: Int = 0
)

@Serializable
enum class ConnectionStatus {
    DISCONNECTED,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

@Serializable
data class LANGameState(
    val gameType: GameType,
    val rawData: String,
    val version: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class LANError(
    val code: Int,
    val message: String,
    val recoverable: Boolean = true
)

object LANErrorCodes {
    const val ROOM_NOT_FOUND = 1001
    const val ROOM_FULL = 1002
    const val INVALID_PASSWORD = 1003
    const val PLAYER_KICKED = 1004
    const val HOST_DISCONNECTED = 1005
    const val CONNECTION_TIMEOUT = 1006
    const val NETWORK_ERROR = 1007
    const val GAME_NOT_STARTED = 1008
    const val NOT_ROOM_OWNER = 1009
}

object LANConstants {
    const val HEARTBEAT_INTERVAL = 5000L
    const val DISCOVERY_BROADCAST_INTERVAL = 3000L
    const val DEFAULT_DISCOVERY_PORT = 37668
    const val CONNECTION_TIMEOUT = 15000L
}
