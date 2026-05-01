package org.walks.gamecopilot.data

import org.walks.gamecopilot.lan.data.LANConnectionState
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANPlayer
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.lan.data.LANRoomState

data class LANState(
    val preferredGameType: GameType = GameType.ALL,
    val isDiscovering: Boolean = false,
    val discoveredRooms: List<LANRoomInfo> = emptyList(),
    val currentRoom: LANRoomState? = null,
    val connectionState: LANConnectionState = LANConnectionState(
        org.walks.gamecopilot.lan.data.ConnectionStatus.DISCONNECTED
    ),
    val players: List<LANPlayer> = emptyList(),
    val isHost: Boolean = false,
    val error: String? = null
)
