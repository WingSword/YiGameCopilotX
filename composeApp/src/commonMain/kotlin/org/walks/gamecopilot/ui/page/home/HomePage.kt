package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.data.entity.OperationMode
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.navigation.NaviRoute
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_captain
import yigamecopilotx.composeapp.generated.resources.icon_edit
import yigamecopilotx.composeapp.generated.resources.icon_moon
import yigamecopilotx.composeapp.generated.resources.icon_spy_more

private data class GameCardMeta(
    val mode: GameMode,
    val description: String,
    val players: String,
    val icon: DrawableResource,
    val brush: Brush
)

@Composable
fun HomePage(viewmodel: MainViewmodel, navi: NavHostController) {
    val selectedGameIndex = viewmodel.startedGameMode.collectAsState().value
    val selectedOperationMode = viewmodel.operationMode.collectAsState().value
    val operationMode =
        OperationMode.entries.getOrElse(selectedOperationMode) { OperationMode.LOCAL }
    val selectedGameMode = GameMode.entries.getOrElse(selectedGameIndex) { GameMode.SPY_MAIN }
    var expandedGameMode by remember(selectedGameMode) { mutableStateOf<GameMode?>(selectedGameMode) }
    val cardList = remember {
        listOf(
            GameCardMeta(
                mode = GameMode.SPY_MAIN,
                description = "主持人发牌后轮流发言与投票，找出卧底。",
                players = "4-12 人",
                icon = Res.drawable.icon_spy_more,
                brush = Brush.linearGradient(listOf(Color(0xFFF3D66E), Color(0xFFE3AF19)))
            ),
            GameCardMeta(
                mode = GameMode.SPY_AWALONG,
                description = "阵营推理和任务博弈玩法，适合多人对抗。",
                players = "5-10 人",
                icon = Res.drawable.icon_captain,
                brush = Brush.linearGradient(listOf(Color(0xFFF4D57A), Color(0xFFD89D0F)))
            ),
            GameCardMeta(
                mode = GameMode.DRAW_GUESS,
                description = "轮流作画与猜词，轻松快速开局。",
                players = "3-10 人",
                icon = Res.drawable.icon_edit,
                brush = Brush.linearGradient(listOf(Color(0xFFF7DF98), Color(0xFFE1AB1A)))
            ),
            GameCardMeta(
                mode = GameMode.HUNT_TOWN,
                description = "多身份对抗玩法，白天讨论夜晚行动。",
                players = "4-12 人",
                icon = Res.drawable.icon_moon,
                brush = Brush.linearGradient(listOf(Color(0xFFECCE77), Color(0xFFC08B09)))
            ),
            GameCardMeta(
                mode = GameMode.ONE_NIGHT_WEREWOLF,
                description = "一夜一白天，无需淘汰，快节奏身份推理。",
                players = "3-10 人",
                icon = Res.drawable.icon_moon,
                brush = Brush.linearGradient(listOf(Color(0xFFD64545), Color(0xFF8B1A1A)))
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(40.dp))
            Text(
                text = "游戏大厅",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "菜单",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = "全部游戏",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cardList.forEach { card ->
                GameCard(
                    meta = card,
                    currentMode = operationMode,
                    isExpanded = expandedGameMode == card.mode,
                    onExpandToggle = {
                        expandedGameMode = if (expandedGameMode == card.mode) null else card.mode
                    },
                    onModeClick = { mode ->
                        viewmodel.handleGameIntent(GameIntent.SwitchOperationMode(mode.ordinal))
                    },
                    onQuickEnter = {
                        PlatformHelper.getInstance().vibrateMethod()
                        navigateByMode(card.mode, operationMode, viewmodel, navi)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

private fun navigateByMode(
    gameMode: GameMode,
    operationMode: OperationMode,
    viewmodel: MainViewmodel,
    navi: NavHostController
) {
    viewmodel.handleGameIntent(GameIntent.SwitchGameMode(gameMode.ordinal))
    if (operationMode == OperationMode.LOCAL) {
        when (gameMode) {
            GameMode.SPY_MAIN -> navi.navigate(NaviRoute.LOCAL_SPY.route)
            GameMode.SPY_AWALONG -> navi.navigate(NaviRoute.AWALONG.route)
            GameMode.DRAW_GUESS -> navi.navigate(NaviRoute.DRAW_GUESS.route)
            GameMode.HUNT_TOWN -> navi.navigate(NaviRoute.HUNT_TOWN.route)
            GameMode.ONE_NIGHT_WEREWOLF -> navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF.route)
        }
    } else {
        val mappedType = when (gameMode) {
            GameMode.SPY_MAIN -> GameType.LOCAL_SPY
            GameMode.SPY_AWALONG -> GameType.AWALONG
            GameMode.DRAW_GUESS -> GameType.DRAW_GUESS
            GameMode.HUNT_TOWN -> GameType.HUNT_TOWN
            GameMode.ONE_NIGHT_WEREWOLF -> GameType.ONE_NIGHT_WEREWOLF
        }
        viewmodel.handleLANIntent(LANIntent.SetPreferredGameType(mappedType))
        navi.navigate(NaviRoute.LAN_DISCOVERY.route)
    }
}

@Composable
private fun GameCard(
    meta: GameCardMeta,
    currentMode: OperationMode,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onModeClick: (OperationMode) -> Unit,
    onQuickEnter: () -> Unit
) {
    val supportedModes = when (meta.mode) {
        GameMode.HUNT_TOWN -> listOf(OperationMode.LAN, OperationMode.ONLINE)
        else -> OperationMode.entries.toList()
    }
    val modeTitle = { mode: OperationMode ->
        when (mode) {
            OperationMode.LOCAL -> "单手机游玩"
            OperationMode.LAN -> "同网络游玩"
            OperationMode.ONLINE -> "跨网络游玩"
        }
    }
    val modeDescription = { mode: OperationMode ->
        when (mode) {
            OperationMode.LOCAL -> "同设备本地游戏，适合朋友面对面快速开局。"
            OperationMode.LAN -> "同一局域网联机，适合宿舍或聚会多人同步。"
            OperationMode.ONLINE -> "跨网络远程联机，适合异地好友随时组局。"
        }
    }
    val cardColor =
        if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val iconMask = if (isExpanded) 0.22f else 0.08f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Text(
                        text = meta.mode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = if (isExpanded) "已展开 · 点击收起" else "折叠状态 · 点击展开",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(meta.brush),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = iconMask))
                    )
                    Image(
                        painter = painterResource(meta.icon),
                        contentDescription = meta.mode.title,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = meta.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = "适合人数：${meta.players}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        supportedModes.forEach { mode ->
                            val selected = currentMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clickable { onModeClick(mode) },
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RectangleShape,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = modeTitle(mode),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = modeDescription(currentMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        modifier = Modifier
                            .widthIn(min = 96.dp)
                            .height(38.dp)
                            .clickable(onClick = onQuickEnter),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RectangleShape,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "进入游戏",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isExpanded) 1f else 0.35f)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
