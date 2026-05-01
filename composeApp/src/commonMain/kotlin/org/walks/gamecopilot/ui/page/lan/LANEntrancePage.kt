package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.lan.data.GameType

data class LANFeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gameType: GameType
)

@Composable
fun LANEntrancePage(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToCreateRoom: () -> Unit
) {
    var showCreateRoom by remember { mutableStateOf(false) }
    var selectedGameType by remember { mutableStateOf<GameType?>(null) }
    
    val features = listOf(
        LANFeatureItem(
            title = "谁是卧底",
            description = "多人词汇推理游戏",
            icon = Icons.Default.PersonOff,
            gameType = GameType.LOCAL_SPY
        ),
        LANFeatureItem(
            title = "阿瓦隆",
            description = "阵营对抗推理游戏",
            icon = Icons.Default.Shield,
            gameType = GameType.AWALONG
        ),
        LANFeatureItem(
            title = "你画我猜",
            description = "绘画猜词游戏",
            icon = Icons.Default.Brush,
            gameType = GameType.DRAW_GUESS
        ),
        LANFeatureItem(
            title = "猎巫镇",
            description = "房间准备 + 开局身份卡预览",
            icon = Icons.Default.Nightlight,
            gameType = GameType.HUNT_TOWN
        ),
        LANFeatureItem(
            title = "随机工具",
            description = "骰子、转盘等",
            icon = Icons.Default.Casino,
            gameType = GameType.RANDOM_TOOLS
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "局域网联机",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "与同一局域网内的朋友一起游戏",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToDiscovery,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜索房间")
            }
            
            OutlinedButton(
                onClick = onNavigateToCreateRoom,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建房间")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "支持的游戏",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        features.forEach { feature ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                feature.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = feature.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = feature.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "确保所有设备连接到同一个 WiFi 网络",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
