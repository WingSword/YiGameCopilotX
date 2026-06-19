package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.getPlatform
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun MultiplayerPage(viewmodel: MainViewmodel, navi: NavHostController) {
    val design = LocalAppDesign.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = design.spacing.xl, vertical = design.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
    ) {
        Text(
            text = "联机大厅",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MultiplayerTab(
                text = "局域网",
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            MultiplayerTab(
                text = "网络房间",
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedTab == 0) {
            LanPanel(navi)
        } else {
            OnlineRoomPanel(viewmodel)
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun MultiplayerTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LanPanel(navi: NavHostController) {
    val platformName = remember { getPlatform().name }
    val platformHint = if (platformName.startsWith("Android")) {
        "当前 Android 端支持创建主机房间、搜索并加入局域网房间。"
    } else {
        "当前平台的局域网主机能力仍在适配中，建议优先使用 Android 设备作为房主。"
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MultiplayerActionCard(
            icon = Icons.Rounded.WifiTethering,
            title = "局域网联机",
            description = "房主手机会作为主机，同一 WiFi 下的玩家可搜索并加入。",
            primaryText = "搜索房间",
            secondaryText = "创建房间",
            onPrimaryClick = { navi.navigate(NaviRoute.LAN_DISCOVERY.route) },
            onSecondaryClick = { navi.navigate(NaviRoute.LAN_CREATE_ROOM.route) }
        )

        Text(
            text = "适合面对面聚会、宿舍、同一路由器网络。创建房间后保持房主 App 在前台，其他设备在“搜索房间”中加入。$platformHint",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnlineRoomPanel(viewmodel: MainViewmodel) {
    var roomId by remember { mutableStateOf("") }
    var roomKey by remember { mutableStateOf("") }
    val canSubmit = roomId.isNotBlank() && roomKey.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MultiplayerActionCard(
            icon = Icons.Rounded.Cloud,
            title = "网络房间联机",
            description = "通过在线房间服务器组局，适合不在同一局域网的玩家。",
            primaryText = null,
            secondaryText = null,
            onPrimaryClick = null,
            onSecondaryClick = null
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it.trim() },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("房间号") },
                    leadingIcon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
                )
                OutlinedTextField(
                    value = roomKey,
                    onValueChange = { roomKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("房间密钥") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewmodel.handleRoomIntent(
                                GameRoomIntent.CreateAGameRoom(
                                    roomId,
                                    roomKey
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("创建")
                    }
                    OutlinedButton(
                        onClick = {
                            viewmodel.handleRoomIntent(
                                GameRoomIntent.JoinToAGameRoom(
                                    roomId,
                                    roomKey
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("加入")
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiplayerActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    primaryText: String?,
    secondaryText: String?,
    onPrimaryClick: (() -> Unit)?,
    onSecondaryClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (primaryText != null && onPrimaryClick != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(primaryText)
                    }
                    if (secondaryText != null && onSecondaryClick != null) {
                        OutlinedButton(
                            onClick = onSecondaryClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(secondaryText)
                        }
                    }
                }
            }
        }
    }
}
