package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANPlayer
import org.walks.gamecopilot.lan.lanRoomManager
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppScreen
import kotlin.math.max
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANRoomLobbyPage(
    onStartGame: () -> Unit,
    onLeaveRoom: () -> Unit,
    viewModel: MainViewmodel
) {
    val currentRoom by lanRoomManager.currentRoom.collectAsState()
    val players by lanRoomManager.players.collectAsState()
    val isHost by lanRoomManager.isHost.collectAsState()
    val connectionState by lanRoomManager.connectionState.collectAsState()
    
    var showKickDialog by remember { mutableStateOf<LANPlayer?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    
    val roomInfo = currentRoom?.roomInfo
    val isHuntTown = roomInfo?.gameType == GameType.HUNT_TOWN
    val currentPlayerId = lanRoomManager.currentPlayerId
    val roleMapping = remember(players, roomInfo?.roomId, isHuntTown) {
        val roomId = roomInfo?.roomId
        if (isHuntTown && !roomId.isNullOrBlank()) {
            buildHuntTownRoleMap(players, roomId)
        } else {
            emptyMap()
        }
    }
    val currentPlayerRole = currentPlayerId?.let(roleMapping::get)

    AppScreen(
        title = roomInfo?.roomName ?: "房间",
        subtitle = "房主: ${roomInfo?.hostName ?: "等待同步"} · ${roomInfo?.gameType?.displayName ?: "桌游"}",
        actions = {
            IconButton(onClick = { showLeaveDialog = true }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "离开房间")
            }
        }
    ) {
        RoomInfoCard(roomInfo)
        
        if (isHuntTown) {
            if (currentRoom?.gameStarted == true) {
                HuntTownInGamePanel(
                    currentPlayer = players.firstOrNull { it.id == currentPlayerId },
                    currentPlayerRole = currentPlayerRole,
                    players = players,
                    currentPlayerId = currentPlayerId
                )
            } else {
                HuntTownPreparePanel(players = players)
            }
        }

        Text(
            text = "玩家列表 (${players.size}/${roomInfo?.maxPlayers ?: 8})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (players.isEmpty()) {
            AppEmptyState(
                title = "等待玩家加入",
                description = "保持房主 App 在前台，其他玩家可从局域网房间列表加入。",
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerCard(
                        player = player,
                        isHost = isHost,
                        canKick = isHost && !player.isHost,
                        onKick = { showKickDialog = player }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isHost) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("关闭房间")
                }
                
                Button(
                    onClick = {
                        viewModel.handleLANIntent(LANIntent.StartGame)
                        if (!isHuntTown) onStartGame()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = players.size >= 2 && connectionState.status == ConnectionStatus.CONNECTED
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始游戏")
                }
            }
        } else {
            Button(
                onClick = { showLeaveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("离开房间")
            }
        }
    }
    
    showKickDialog?.let { player ->
        AppDialog(
            title = "移出玩家",
            subtitle = "确定要将 ${player.name} 移出房间吗？",
            onDismiss = { showKickDialog = null },
            actions = {
                OutlinedButton(
                    onClick = { showKickDialog = null },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        viewModel.handleLANIntent(LANIntent.KickPlayer(player.id))
                        showKickDialog = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("移出")
                }
            }
        ) {
            Text(
                text = "该玩家会被断开连接，需要重新加入才能回到房间。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    if (showLeaveDialog) {
        AppDialog(
            title = if (isHost) "关闭房间" else "离开房间",
            subtitle = if (isHost) "所有玩家都将被断开连接。" else "你将退出当前局域网房间。",
            onDismiss = { showLeaveDialog = false },
            actions = {
                OutlinedButton(
                    onClick = { showLeaveDialog = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        viewModel.handleLANIntent(LANIntent.Disconnect)
                        showLeaveDialog = false
                        onLeaveRoom()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isHost) "关闭" else "离开")
                }
            }
        ) {
            Text(
                text = if (isHost) "建议在确认所有玩家已完成当前流程后再关闭房间。" else "离开后如需继续游戏，请从房间列表重新加入。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class HuntTownRoleCard(
    val roleName: String,
    val campName: String,
    val abilityHint: String
)

private fun buildHuntTownRoleMap(
    players: List<LANPlayer>,
    roomId: String
): Map<String, HuntTownRoleCard> {
    if (players.isEmpty()) return emptyMap()
    val sortedPlayers = players.sortedBy { it.id }
    val witchCount = max(1, sortedPlayers.size / 4)
    val villagerCount = max(0, sortedPlayers.size - witchCount - 1)
    val rolePool = buildList {
        repeat(witchCount) {
            add(
                HuntTownRoleCard(
                    roleName = "女巫",
                    campName = "邪恶阵营",
                    abilityHint = "夜晚可指定一名目标。"
                )
            )
        }
        add(
            HuntTownRoleCard(
                roleName = "警长",
                campName = "正义阵营",
                abilityHint = "凌晨阶段可守护一名玩家。"
            )
        )
        repeat(villagerCount) {
            add(
                HuntTownRoleCard(
                    roleName = "村民",
                    campName = "正义阵营",
                    abilityHint = "白天讨论并投票放逐可疑目标。"
                )
            )
        }
    }.toMutableList()

    val shuffled = rolePool.shuffled(Random(roomId.hashCode().toLong()))
    return sortedPlayers.mapIndexed { index, player ->
        player.id to shuffled[index]
    }.toMap()
}

@Composable
private fun HuntTownPreparePanel(players: List<LANPlayer>) {
    val playerCount = players.size
    val recommendedWitchCount = max(1, playerCount / 4)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "猎巫镇 · 房间准备",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "建议配置：$playerCount 人局，女巫 $recommendedWitchCount 名，警长 1 名，其余为村民。",
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "房主点击“开始游戏”后，将在当前房间显示每位玩家的身份视图。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun HuntTownInGamePanel(
    currentPlayer: LANPlayer?,
    currentPlayerRole: HuntTownRoleCard?,
    players: List<LANPlayer>,
    currentPlayerId: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "我的身份卡",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "玩家：${currentPlayer?.name ?: "未知"}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "身份：${currentPlayerRole?.roleName ?: "等待分配"}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "阵营：${currentPlayerRole?.campName ?: "未确定"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "提示：${currentPlayerRole?.abilityHint ?: "等待房主开始游戏"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "其他玩家列表",
                    fontWeight = FontWeight.Bold
                )
                players.filter { it.id != currentPlayerId }.forEach { player ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${player.playerIndex + 1}号 · ${player.name}")
                        Text(
                            text = "身份隐藏",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomInfoCard(roomInfo: org.walks.gamecopilot.lan.data.LANRoomInfo?) {
    if (roomInfo == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "房间ID",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = roomInfo.roomId,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "游戏类型",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = roomInfo.gameType.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (roomInfo.hasPassword) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "有密码",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "私密",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(
    player: LANPlayer,
    isHost: Boolean,
    canKick: Boolean,
    onKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (player.isHost) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name.firstOrNull()?.toString() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (player.isHost) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (player.isHost) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "房主",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = "玩家 ${player.playerIndex + 1}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.isReady) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "已准备",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (canKick) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onKick) {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription = "移出",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
