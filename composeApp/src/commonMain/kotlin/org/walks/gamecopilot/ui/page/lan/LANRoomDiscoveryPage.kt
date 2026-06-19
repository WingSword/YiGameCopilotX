package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANRoomDiscoveryPage(
    onRoomSelected: (LANRoomInfo) -> Unit,
    onCreateRoom: () -> Unit,
    viewModel: MainViewmodel
) {
    val lanState by viewModel.lanState.collectAsState()
    val discoveredRooms = lanState.discoveredRooms
    val connectionState = lanState.connectionState

    var selectedGameType by remember(lanState.preferredGameType) {
        mutableStateOf(lanState.preferredGameType)
    }
    var showGameTypeFilter by remember { mutableStateOf(false) }
    
    val isDiscovering = connectionState.status == ConnectionStatus.DISCOVERING
    
    LaunchedEffect(Unit) {
        viewModel.handleLANIntent(LANIntent.StartDiscovery(selectedGameType))
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.handleLANIntent(LANIntent.StopDiscovery)
        }
    }

    AppScreen(
        title = "局域网房间",
        subtitle = "搜索同一 WiFi 下正在等待加入的桌游房间。",
        actions = {
            IconButton(onClick = { showGameTypeFilter = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "筛选")
            }

            IconButton(onClick = {
                if (isDiscovering) {
                    viewModel.handleLANIntent(LANIntent.StopDiscovery)
                } else {
                    viewModel.handleLANIntent(LANIntent.StartDiscovery(selectedGameType))
                }
            }) {
                Icon(
                    if (isDiscovering) Icons.Default.Stop else Icons.Default.Refresh,
                    contentDescription = if (isDiscovering) "停止" else "刷新"
                )
            }
        }
    ) {
        if (isDiscovering) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
        
        ConnectionStatusIndicator(connectionState.status)

        if (discoveredRooms.isEmpty() && !isDiscovering) {
            AppEmptyState(
                title = "未发现房间",
                description = "请确认设备在同一局域网内，且房主 App 保持在前台。",
                icon = Icons.Default.WifiOff,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(discoveredRooms, key = { it.roomId }) { room ->
                    RoomCard(
                        room = room,
                        onClick = { onRoomSelected(room) }
                    )
                }
            }
        }
        
        Button(
            onClick = onCreateRoom,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建房间")
        }
    }
    
    if (showGameTypeFilter) {
        GameTypeFilterDialog(
            currentType = selectedGameType,
            onDismiss = { showGameTypeFilter = false },
            onConfirm = { type ->
                selectedGameType = type
                showGameTypeFilter = false
                viewModel.handleLANIntent(LANIntent.SetPreferredGameType(type))
                viewModel.handleLANIntent(LANIntent.StopDiscovery)
                viewModel.handleLANIntent(LANIntent.StartDiscovery(type))
            }
        )
    }
}

@Composable
private fun RoomCard(
    room: LANRoomInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.roomName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (room.hasPassword) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "需要密码",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "房主: ${room.hostName}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GameTypeChip(room.gameType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${room.currentPlayers}/${room.maxPlayers}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "加入",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GameTypeChip(gameType: GameType) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = gameType.displayName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun ConnectionStatusIndicator(status: ConnectionStatus) {
    val (color, text) = when (status) {
        ConnectionStatus.DISCOVERING -> Color(0xFFFFA726) to "正在搜索房间..."
        ConnectionStatus.CONNECTING -> Color(0xFF42A5F5) to "正在连接..."
        ConnectionStatus.CONNECTED -> Color(0xFF66BB6A) to "已连接"
        ConnectionStatus.RECONNECTING -> Color(0xFFFFA726) to "正在重连..."
        ConnectionStatus.ERROR -> Color(0xFFEF5350) to "连接错误"
        ConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E) to "未连接"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GameTypeFilterDialog(
    currentType: GameType,
    onDismiss: () -> Unit,
    onConfirm: (GameType) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentType) }

    AppDialog(
        title = "筛选游戏类型",
        subtitle = "只显示你想加入的游戏房间。",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { onConfirm(selectedType) },
                modifier = Modifier.weight(1f)
            ) {
                Text("确定")
            }
        }
    ) {
            Column {
                GameType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.displayName)
                    }
                }
            }
    }
}
