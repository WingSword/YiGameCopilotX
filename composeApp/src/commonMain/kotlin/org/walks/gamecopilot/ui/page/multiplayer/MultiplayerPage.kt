package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Button
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
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppSegmentedControl

@Composable
fun MultiplayerPage(viewmodel: MainViewmodel, navi: NavHostController) {
    val design = LocalAppDesign.current
    val isWeb = remember { getPlatform().name.startsWith("Web") }
    var selectedTab by remember { mutableIntStateOf(if (isWeb || viewmodel.operationMode.value == 2) 1 else 0) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
    Column(
        modifier = Modifier.widthIn(max = 680.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = design.spacing.xl, vertical = design.spacing.lg).padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
    ) {
        Text(
            text = "联机大厅",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Text(if(isWeb) "输入房间号和密钥，即可与手机端一起游玩。请在同一浏览器中返回房间。" else "同一 WiFi 可使用局域网，也可通过网络房间一起游玩。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if(!isWeb) AppSegmentedControl(
            options = listOf("局域网", "网络房间"),
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it }
        )

        if (selectedTab == 0) {
            LanPanel(navi)
        } else {
            CloudRoomEntry(initialGameType = when (viewmodel.startedGameMode.value) {
                org.walks.gamecopilot.data.entity.GameMode.ONE_NIGHT_WEREWOLF.ordinal -> "werewolf"
                org.walks.gamecopilot.data.entity.GameMode.HUNT_TOWN.ordinal -> "hunt"
                org.walks.gamecopilot.data.entity.GameMode.SPY_AWALONG.ordinal -> "avalon"
                org.walks.gamecopilot.data.entity.GameMode.DRAW_GUESS.ordinal -> "drawing"
                else -> "spy"
            }) { navi.navigate(NaviRoute.ROOM.route) }
        }

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
private fun MultiplayerActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    primaryText: String?,
    secondaryText: String?,
    onPrimaryClick: (() -> Unit)?,
    onSecondaryClick: (() -> Unit)?
) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.small,
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
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
