package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.lan.lanRoomManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANRoomDiscoveryPage(
    onRoomSelected: (LANRoomInfo) -> Unit,
    onCreateRoom: () -> Unit,
    viewModel: MainViewmodel = viewModel()
) {
    val lanState by viewModel.lanState.collectAsState()
    val discoveredRooms by lanRoomManager.discoveredRooms.collectAsState()
    val connectionState by lanRoomManager.connectionState.collectAsState()

    var selectedGameType by remember(lanState.preferredGameType) {
        mutableStateOf(lanState.preferredGameType)
    }
    var showGameTypeFilter by remember { mutableStateOf(false) }
    
    val isDiscovering = connectionState.status == ConnectionStatus.DISCOVERING
    
    LaunchedEffect(Unit) {
        viewModel.handleLANIntent(LANIntent.StartDiscovery(selectedGameType))
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "局域网房间",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        }
        
        if (isDiscovering) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
        
        ConnectionStatusIndicator(connectionState.status)
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        if (discoveredRooms.isEmpty() && !isDiscovering) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未发现房间",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "请确保设备在同一局域网内",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选游戏类型") },
        text = {
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
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedType) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
