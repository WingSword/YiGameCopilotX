package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.ui.input.HalfRadioTextField

@Composable
fun RoomEntranceCard(viewmodel: MainViewmodel, navi: NavHostController? = null) {
    var roomId by remember { mutableStateOf("") }
    var roomKey by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding().scrollable(
            state = rememberScrollState(),
            orientation = Orientation.Vertical
        )
    ) {
        // 本地游戏入口 - 长条长方形带圆角按钮
        Card(
            modifier = Modifier.fillMaxWidth()
                .height(80.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    navi?.navigate("localSpy")
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "本地卧底游戏",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "同屏传机查看身份并进行发言投票",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    "开始游戏",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 房间输入框区域
        Card(
            modifier = Modifier.fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "房间信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                
                HalfRadioTextField(
                    value = roomId,
                    label = "房间名称",
                    onValueChange = { roomId = it })
                HalfRadioTextField(
                    value = roomKey,
                    label = "房间密钥",
                    onValueChange = { roomKey = it })
            }
        }

        // 创建房间按钮 - 长条长方形带圆角按钮
        Card(
            modifier = Modifier.fillMaxWidth()
                .height(80.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (roomId.isBlank() || roomKey.isBlank()) {
                        scope.launch {
                            snackState.showSnackbar("请输入房间名称和密钥")
                        }
                    } else {
                        viewmodel.handleRoomIntent(
                            GameRoomIntent.CreateAGameRoom(
                                roomId,
                                roomKey,
                            )
                        )
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "创建房间",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "创建新的在线游戏房间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    "创建",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 加入房间按钮 - 长条长方形带圆角按钮
        Card(
            modifier = Modifier.fillMaxWidth()
                .height(80.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (roomId.isBlank() || roomKey.isBlank()) {
                        scope.launch {
                            snackState.showSnackbar("请输入房间名称和密钥")
                        }
                    } else {
                        viewmodel.handleRoomIntent(GameRoomIntent.JoinToAGameRoom(roomId, roomKey))
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "加入房间",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "加入已有的在线游戏房间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    "加入",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 添加 SnackbarHost 以显示 Snackbar
        SnackbarHost(hostState = snackState)
    }
}