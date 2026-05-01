package org.walks.gamecopilot.ui.page.lan

import androidx.compose.runtime.*
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.lan.lanRoomManager

@Composable
fun LANPageRouter(
    onNavigateBack: () -> Unit,
    onNavigateToGame: (String) -> Unit
) {
    var currentPage by remember { mutableStateOf("entrance") }
    var selectedRoom by remember { mutableStateOf<LANRoomInfo?>(null) }
    
    when (currentPage) {
        "entrance" -> {
            LANEntrancePage(
                onNavigateToDiscovery = { currentPage = "discovery" },
                onNavigateToCreateRoom = { currentPage = "create" }
            )
        }
        "discovery" -> {
            LANRoomDiscoveryPage(
                onRoomSelected = { room ->
                    selectedRoom = room
                    currentPage = "join"
                },
                onCreateRoom = { currentPage = "create" }
            )
        }
        "create" -> {
            LANCreateRoomPage(
                onRoomCreated = { currentPage = "lobby" },
                onCancel = { currentPage = "entrance" }
            )
        }
        "join" -> {
            selectedRoom?.let { room ->
                JoinRoomDialog(
                    roomInfo = room,
                    onDismiss = { 
                        selectedRoom = null
                        currentPage = "discovery" 
                    },
                    onJoinSuccess = { currentPage = "lobby" }
                )
            }
        }
        "lobby" -> {
            LANRoomLobbyPage(
                onStartGame = { 
                    val gameType = lanRoomManager.currentRoomInfo?.gameType
                    when (gameType) {
                        org.walks.gamecopilot.lan.data.GameType.LOCAL_SPY -> {
                            onNavigateToGame("localSpy")
                        }
                        org.walks.gamecopilot.lan.data.GameType.AWALONG -> {
                            onNavigateToGame("awalong")
                        }
                        org.walks.gamecopilot.lan.data.GameType.DRAW_GUESS -> {
                            onNavigateToGame("drawGuess")
                        }
                        org.walks.gamecopilot.lan.data.GameType.HUNT_TOWN -> {
                            // 猎巫镇在房间内完成准备与身份卡预览，不跳转到外部页面
                        }
                        else -> {
                            onNavigateToGame("home")
                        }
                    }
                },
                onLeaveRoom = {
                    currentPage = "entrance"
                }
            )
        }
    }
}
