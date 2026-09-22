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
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.BAD_PERSON
import org.walks.gamecopilot.data.DrawGuessWordLibrary
import org.walks.gamecopilot.data.wordsEasy
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANPlayer
import org.walks.gamecopilot.lan.data.LANRoomState
import org.walks.gamecopilot.lan.lanRoomManager
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppScreen
import org.walks.gamecopilot.werewolf.data.WerewolfPresets
import kotlin.math.max
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANRoomLobbyPage(
    onLeaveRoom: () -> Unit,
    viewModel: MainViewmodel
) {
    val lanState by viewModel.lanState.collectAsState()
    val currentRoom by lanRoomManager.currentRoom.collectAsState()
    val players by lanRoomManager.players.collectAsState()
    val isHost by lanRoomManager.isHost.collectAsState()
    val connectionState by lanRoomManager.connectionState.collectAsState()
    
    var showKickDialog by remember { mutableStateOf<LANPlayer?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showEndGameDialog by remember { mutableStateOf(false) }
    
    val roomInfo = currentRoom?.roomInfo
    val currentPlayerId = lanRoomManager.currentPlayerId
    val currentPlayer = players.firstOrNull { it.id == currentPlayerId }
    val minimumPlayers = roomInfo?.gameType?.minimumPlayers ?: 2
    val canStart = players.size >= minimumPlayers &&
            players.all { it.isHost || it.isReady } &&
            connectionState.status == ConnectionStatus.CONNECTED
    val secretCard = remember(
        currentRoom?.gameState,
        roomInfo?.roomId,
        roomInfo?.gameType,
        players,
        currentPlayerId
    ) {
        currentRoom?.takeIf { it.gameStarted && it.gameState.isNotBlank() }?.let { room ->
            buildSessionCard(room, currentPlayerId)
        }
    }

    AppScreen(
        title = roomInfo?.roomName ?: "房间",
        subtitle = "房主: ${roomInfo?.hostName ?: "等待同步"} · ${roomInfo?.gameType?.displayName ?: "桌游"}",
        onBack = { showLeaveDialog = true }
    ) {
        RoomInfoCard(roomInfo)

        lanState.error?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        if (currentRoom?.gameStarted == true) {
            SessionCardPanel(
                currentPlayer = currentPlayer,
                secretCard = secretCard
            )
        } else {
            RoomPreparePanel(
                gameType = roomInfo?.gameType,
                playerCount = players.size,
                minimumPlayers = minimumPlayers
            )
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
                        canKick = isHost && !player.isHost && currentRoom?.gameStarted != true,
                        onKick = { showKickDialog = player }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isHost) {
            if (currentRoom?.gameStarted == true) {
                Button(
                    onClick = { showEndGameDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("结束本局")
                }
            } else {
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
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canStart
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始游戏")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("离开")
                }
                if (currentRoom?.gameStarted != true) {
                    Button(
                        onClick = { lanRoomManager.toggleReady() },
                        enabled = currentPlayer != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentPlayer?.isReady == true) "取消准备" else "准备")
                    }
                }
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

    if (showEndGameDialog) {
        AppDialog(
            title = "结束本局",
            subtitle = "所有玩家会返回准备状态，可继续在当前房间重新开局。",
            onDismiss = { showEndGameDialog = false },
            actions = {
                OutlinedButton(
                    onClick = { showEndGameDialog = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        viewModel.handleLANIntent(LANIntent.EndGame)
                        showEndGameDialog = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("结束本局")
                }
            }
        ) {
            Text(
                text = "本轮身份会清除，下一轮将重新随机分配。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class LANSecretCard(
    val label: String,
    val value: String,
    val group: String,
    val hint: String,
    val note: String
)

private data class HuntTownRoleCard(
    val roleName: String,
    val campName: String,
    val abilityHint: String
)

private fun buildSessionCard(room: LANRoomState, currentPlayerId: String?): LANSecretCard {
    val players = room.players.sortedWith(
        compareBy<LANPlayer> { it.playerIndex }.thenBy { it.id }
    )
    val localIndex = players.indexOfFirst { it.id == currentPlayerId }
    if (players.isEmpty() || localIndex < 0) {
        return LANSecretCard("我的身份", "等待同步", "", "请确认当前设备已加入玩家列表。", "")
    }

    return when (room.roomInfo.gameType) {
        GameType.LOCAL_SPY -> buildLocalSpyCard(room, players, localIndex)
        GameType.AWALONG -> buildAvalonCard(room, players, localIndex)
        GameType.DRAW_GUESS -> buildDrawGuessCard(room, players, localIndex)
        GameType.HUNT_TOWN -> buildHuntTownCard(room, players, localIndex)
        GameType.ONE_NIGHT_WEREWOLF -> buildWerewolfCard(room, players, localIndex)
        else -> LANSecretCard("当前玩法", "暂不支持", "", "请由房主结束本局并重新选择玩法。", "")
    }
}

private fun shuffledOrder(count: Int, seedText: String): List<Int> {
    val order = MutableList(count) { it }
    val random = Random(seedText.hashCode())
    for (index in order.lastIndex downTo 1) {
        val target = random.nextInt(index + 1)
        val value = order[index]
        order[index] = order[target]
        order[target] = value
    }
    return order
}

private fun buildLocalSpyCard(
    room: LANRoomState,
    players: List<LANPlayer>,
    localIndex: Int
): LANSecretCard {
    val pairs = wordsEasy.entries.toList()
    if (pairs.isEmpty()) {
        return LANSecretCard("你的身份", "题库为空", "", "请由房主结束本局。", "")
    }
    val wordRandom = Random(
        "${room.roomInfo.roomId}:${room.gameState}:spy:word".hashCode()
    )
    val pair = pairs[wordRandom.nextInt(pairs.size)]
    val order = shuffledOrder(
        players.size,
        "${room.roomInfo.roomId}:${room.gameState}:spy:roles"
    )
    val isSpy = order[localIndex] < if (players.size >= 8) 2 else 1
    return LANSecretCard(
        label = if (isSpy) "你的身份：卧底" else "你的身份：平民",
        value = if (isSpy) pair.value else pair.key,
        group = if (isSpy) "卧底阵营" else "平民阵营",
        hint = "依次描述这个词，但不要直接说出答案。",
        note = "身份和词语只在自己的设备上查看。"
    )
}

private fun buildAvalonCard(
    room: LANRoomState,
    players: List<LANPlayer>,
    localIndex: Int
): LANSecretCard {
    val badCount = when {
        players.size <= 6 -> 2
        players.size <= 9 -> 3
        else -> 4
    }
    val roles = mutableListOf(AwalongRole.MEILING, AwalongRole.PAIXIWEIWEIER)
    while (roles.size < players.size - badCount) roles += AwalongRole.ZHONGCHEN
    roles += AwalongRole.MOGANNA
    roles += AwalongRole.CISHA
    while (roles.size < players.size) roles += AwalongRole.ZHAOYA

    val order = shuffledOrder(
        players.size,
        "${room.roomInfo.roomId}:${room.gameState}:avalon"
    )
    val myRole = roles[order[localIndex]]
    val visible = buildList {
        players.forEachIndexed { index, player ->
            if (index == localIndex) return@forEachIndexed
            val role = roles[order[index]]
            val canSee = when {
                myRole == AwalongRole.MEILING -> role.roleType == BAD_PERSON
                myRole == AwalongRole.PAIXIWEIWEIER ->
                    role == AwalongRole.MEILING || role == AwalongRole.MOGANNA
                myRole.roleType == BAD_PERSON -> role.roleType == BAD_PERSON
                else -> false
            }
            if (canSee) add(player.name)
        }
    }
    val hint = when {
        visible.isEmpty() -> myRole.description.trim()
        myRole == AwalongRole.PAIXIWEIWEIER ->
            "${visible.joinToString("、")} 中，一位是梅林，一位是莫甘娜。"
        myRole == AwalongRole.MEILING -> "你看到的红方玩家：${visible.joinToString("、")}。"
        myRole.roleType == BAD_PERSON -> "你的红方队友：${visible.joinToString("、")}。"
        else -> myRole.description.trim()
    }
    return LANSecretCard(
        "你的身份",
        myRole.title,
        if (myRole.roleType == BAD_PERSON) "红方" else "蓝方",
        hint,
        "确认身份后收起页面，再开始讨论和任务投票。"
    )
}

private fun buildDrawGuessCard(
    room: LANRoomState,
    players: List<LANPlayer>,
    localIndex: Int
): LANSecretCard {
    val random = Random("${room.roomInfo.roomId}:${room.gameState}:draw".hashCode())
    val drawer = random.nextInt(players.size)
    val builtinWords = DrawGuessWordLibrary.categories.values.flatten().distinct()
    val word = builtinWords[random.nextInt(builtinWords.size)]
    return if (localIndex == drawer) {
        LANSecretCard(
            "本轮画手",
            word,
            "${localIndex + 1} 号 · ${players[localIndex].name}",
            "请根据题目作画，不要写文字或直接说出答案。",
            "请使用现场纸笔、白板或房主设备的画板作画；房间只负责私密发题。"
        )
    } else {
        LANSecretCard(
            "本轮猜词",
            "等待画手作画",
            "画手：${players[drawer].name}",
            "观察现场画面并直接说出你的猜测。",
            "题目只会显示在画手设备上，请不要偷看。"
        )
    }
}

private fun buildHuntTownCard(
    room: LANRoomState,
    players: List<LANPlayer>,
    localIndex: Int
): LANSecretCard {
    val witchCount = max(1, players.size / 4)
    val roles = buildList {
        repeat(witchCount) { add(HuntTownRoleCard("女巫", "邪恶阵营", "夜晚可指定一名目标。")) }
        add(HuntTownRoleCard("警长", "正义阵营", "凌晨阶段可守护一名玩家。"))
        repeat(max(0, players.size - witchCount - 1)) {
            add(HuntTownRoleCard("村民", "正义阵营", "白天讨论并投票放逐可疑目标。"))
        }
    }
    val order = shuffledOrder(
        roles.size,
        "${room.roomInfo.roomId}:${room.gameState}:hunt"
    )
    val role = roles[order[localIndex]]
    return LANSecretCard(
        "你的身份",
        role.roleName,
        role.campName,
        role.abilityHint,
        "身份只在自己的设备上查看。"
    )
}

private fun buildWerewolfCard(
    room: LANRoomState,
    players: List<LANPlayer>,
    localIndex: Int
): LANSecretCard {
    val roles = WerewolfPresets.getPresetForPlayerCount(players.size.coerceAtMost(10)).roles
    val order = shuffledOrder(
        roles.size,
        "${room.roomInfo.roomId}:${room.gameState}:werewolf"
    )
    val role = roles[order[localIndex]]
    return LANSecretCard(
        "你的初始身份",
        role.displayName,
        role.faction.displayName,
        role.description,
        "当前为身份同步模式；请按角色夜间顺序由房主口头主持。"
    )
}

@Composable
private fun RoomPreparePanel(
    gameType: GameType?,
    playerCount: Int,
    minimumPlayers: Int
) {
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
                text = "${gameType?.displayName ?: "桌游"} · 房间准备",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (playerCount < minimumPlayers) {
                    "还需 ${minimumPlayers - playerCount} 名玩家，至少 $minimumPlayers 人才能开始。"
                } else {
                    "人数已满足，请等待所有非房主玩家完成准备。"
                },
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "开始后每台设备只显示自己的身份或题目，并继续留在当前房间。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun SessionCardPanel(currentPlayer: LANPlayer?, secretCard: LANSecretCard?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = secretCard?.label ?: "我的身份",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = secretCard?.value ?: "等待同步",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (!secretCard?.group.isNullOrBlank()) {
                Text(
                    text = secretCard?.group.orEmpty(),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "玩家：${currentPlayer?.name ?: "未知"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (!secretCard?.hint.isNullOrBlank()) {
                Text(
                    text = secretCard?.hint.orEmpty(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (!secretCard?.note.isNullOrBlank()) {
                Text(
                    text = secretCard?.note.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
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
