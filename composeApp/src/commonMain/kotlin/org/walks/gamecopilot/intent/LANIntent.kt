package org.walks.gamecopilot.intent

import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANRoomInfo

sealed class LANIntent {
    data class SetPreferredGameType(val gameType: GameType) : LANIntent()
    data class StartDiscovery(val gameType: GameType = GameType.ALL) : LANIntent()
    data object StopDiscovery : LANIntent()
    data object ClearDiscoveredRooms : LANIntent()
    
    data class CreateRoom(
        val roomName: String,
        val hostName: String,
        val gameType: GameType,
        val maxPlayers: Int = 8,
        val password: String = ""
    ) : LANIntent()
    
    data class JoinRoom(
        val roomInfo: LANRoomInfo,
        val playerName: String,
        val password: String = ""
    ) : LANIntent()
    
    data object Disconnect : LANIntent()
    data object StartGame : LANIntent()
    data object EndGame : LANIntent()
    
    data class SyncGameState(val gameState: Any) : LANIntent()
    data class SendGameAction(val action: String, val data: Any? = null) : LANIntent()
    data class KickPlayer(val playerId: String, val reason: String = "") : LANIntent()
}
