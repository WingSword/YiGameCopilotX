package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.distribution.AppDistribution
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.data.entity.OperationMode
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.theme.LocalAppDesign

private data class GameCardMeta(
    val mode: GameMode,
    val description: String,
    val players: String,
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
                players = "4-16 人",
                brush = Brush.linearGradient(
                    listOf(
                        GameMode.SPY_MAIN.gradientColors.start,
                        GameMode.SPY_MAIN.gradientColors.end
                    )
                )
            ),
            GameCardMeta(
                mode = GameMode.SPY_AWALONG,
                description = "阵营推理和任务博弈玩法，适合多人对抗。",
                players = "5-10 人",
                brush = Brush.linearGradient(
                    listOf(
                        GameMode.SPY_AWALONG.gradientColors.start,
                        GameMode.SPY_AWALONG.gradientColors.end
                    )
                )
            ),
            GameCardMeta(
                mode = GameMode.DRAW_GUESS,
                description = "轮流作画与猜词，适合快速组局。",
                players = "3-10 人",
                brush = Brush.linearGradient(
                    listOf(
                        GameMode.DRAW_GUESS.gradientColors.start,
                        GameMode.DRAW_GUESS.gradientColors.end
                    )
                )
            ),
            GameCardMeta(
                mode = GameMode.HUNT_TOWN,
                description = "单身份简化规则，同机需额外主持人。",
                players = "4-12 人",
                brush = Brush.linearGradient(
                    listOf(
                        GameMode.HUNT_TOWN.gradientColors.start,
                        GameMode.HUNT_TOWN.gradientColors.end
                    )
                )
            ),
            GameCardMeta(
                mode = GameMode.ONE_NIGHT_WEREWOLF,
                description = "一夜一白天，无需淘汰，快节奏身份推理。",
                players = "3-10 人",
                brush = Brush.linearGradient(
                    listOf(
                        GameMode.ONE_NIGHT_WEREWOLF.gradientColors.start,
                        GameMode.ONE_NIGHT_WEREWOLF.gradientColors.end
                    )
                )
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
            Text(
                text = "桌游助手",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (AppDistribution.roomsEnabled) Surface(
                onClick = { navi.navigate(NaviRoute.MULTIPLAYER.route) { launchSingleTop = true } },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.height(44.dp).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Cloud, contentDescription = null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("联机大厅", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        // Keep two columns on one row when dp sizes round up to fractional physical pixels.
        val cardWidth = (maxWidth - 11.dp * (columns - 1)) / columns
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = columns
        ) {
            cardList.forEach { card ->
                Box(Modifier.width(cardWidth)) {
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
                    onQuickEnter = { mode ->
                        PlatformHelper.getInstance().vibrateMethod()
                        navigateByMode(card.mode, mode, viewmodel, navi)
                    }
                )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun GameModeBadge(
    mode: GameMode,
    brush: Brush,
    overlayAlpha: Float,
    modifier: Modifier = Modifier
) {
    val ink = Color.White
    Box(
        modifier = modifier.background(
            brush,
            RoundedCornerShape(LocalAppDesign.current.cornerRadius.md)
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Color.Black.copy(alpha = overlayAlpha),
                    RoundedCornerShape(LocalAppDesign.current.cornerRadius.md)
                )
        )
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = w * 0.42f,
                center = Offset(w * 0.28f, h * 0.22f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(w * 0.06f, h * 0.08f),
                size = Size(w * 0.88f, h * 0.84f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
                style = Stroke(width = w * 0.04f)
            )
            when (mode) {
                GameMode.SPY_MAIN -> {
                    drawCircle(ink.copy(alpha = 0.96f), w * 0.16f, Offset(w * 0.34f, h * 0.4f))
                    drawCircle(ink.copy(alpha = 0.86f), w * 0.13f, Offset(w * 0.64f, h * 0.43f))
                    drawLine(
                        color = ink,
                        start = Offset(w * 0.25f, h * 0.7f),
                        end = Offset(w * 0.76f, h * 0.7f),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        Color.Black.copy(alpha = 0.24f),
                        w * 0.035f,
                        Offset(w * 0.29f, h * 0.39f)
                    )
                    drawCircle(
                        Color.Black.copy(alpha = 0.24f),
                        w * 0.03f,
                        Offset(w * 0.61f, h * 0.42f)
                    )
                }

                GameMode.SPY_AWALONG -> {
                    val crown = Path().apply {
                        moveTo(w * 0.18f, h * 0.64f)
                        lineTo(w * 0.26f, h * 0.32f)
                        lineTo(w * 0.45f, h * 0.54f)
                        lineTo(w * 0.58f, h * 0.25f)
                        lineTo(w * 0.76f, h * 0.54f)
                        lineTo(w * 0.86f, h * 0.34f)
                        lineTo(w * 0.82f, h * 0.64f)
                        close()
                    }
                    drawPath(crown, ink.copy(alpha = 0.96f))
                    drawRoundRect(
                        color = ink,
                        topLeft = Offset(w * 0.22f, h * 0.68f),
                        size = Size(w * 0.56f, h * 0.12f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                }

                GameMode.DRAW_GUESS -> {
                    drawLine(
                        ink,
                        Offset(w * 0.26f, h * 0.72f),
                        Offset(w * 0.72f, h * 0.26f),
                        w * 0.11f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        Color.Black.copy(alpha = 0.2f),
                        Offset(w * 0.64f, h * 0.2f),
                        Offset(w * 0.78f, h * 0.34f),
                        w * 0.12f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(ink.copy(alpha = 0.92f), w * 0.08f, Offset(w * 0.28f, h * 0.74f))
                    drawLine(
                        ink.copy(alpha = 0.82f),
                        Offset(w * 0.2f, h * 0.28f),
                        Offset(w * 0.42f, h * 0.22f),
                        w * 0.035f,
                        cap = StrokeCap.Round
                    )
                }

                GameMode.HUNT_TOWN -> {
                    val roof = Path().apply {
                        moveTo(w * 0.16f, h * 0.58f)
                        lineTo(w * 0.5f, h * 0.25f)
                        lineTo(w * 0.84f, h * 0.58f)
                        close()
                    }
                    drawPath(roof, ink.copy(alpha = 0.96f))
                    drawRoundRect(
                        color = ink.copy(alpha = 0.9f),
                        topLeft = Offset(w * 0.26f, h * 0.56f),
                        size = Size(w * 0.48f, h * 0.25f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    drawCircle(
                        Color.Black.copy(alpha = 0.22f),
                        w * 0.045f,
                        Offset(w * 0.5f, h * 0.67f)
                    )
                }

                GameMode.ONE_NIGHT_WEREWOLF -> {
                    drawCircle(ink.copy(alpha = 0.94f), w * 0.24f, Offset(w * 0.5f, h * 0.45f))
                    drawCircle(
                        Color.Black.copy(alpha = 0.28f),
                        w * 0.2f,
                        Offset(w * 0.6f, h * 0.35f)
                    )
                    drawLine(
                        ink,
                        Offset(w * 0.25f, h * 0.74f),
                        Offset(w * 0.75f, h * 0.74f),
                        w * 0.07f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(ink.copy(alpha = 0.78f), w * 0.035f, Offset(w * 0.78f, h * 0.22f))
                }
            }
        }
    }
}

private fun navigateByMode(
    gameMode: GameMode,
    operationMode: OperationMode,
    viewmodel: MainViewmodel,
    navi: NavHostController
) {
    viewmodel.handleGameIntent(GameIntent.SwitchGameMode(gameMode.ordinal))
    if (!AppDistribution.roomsEnabled && operationMode != OperationMode.LOCAL) return
    when (operationMode) {
        OperationMode.LOCAL -> when (gameMode) {
            GameMode.SPY_MAIN -> navi.navigate(NaviRoute.LOCAL_SPY.route)
            GameMode.SPY_AWALONG -> navi.navigate(NaviRoute.AWALONG.route)
            GameMode.DRAW_GUESS -> navi.navigate(NaviRoute.DRAW_GUESS.route)
            GameMode.HUNT_TOWN -> navi.navigate(NaviRoute.HUNT_TOWN.route)
            GameMode.ONE_NIGHT_WEREWOLF -> navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF.route)
        }

        OperationMode.LAN -> {
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

        OperationMode.ONLINE -> navi.navigate(NaviRoute.MULTIPLAYER.route)
    }
}

@Composable
private fun GameCard(
    meta: GameCardMeta,
    currentMode: OperationMode,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onModeClick: (OperationMode) -> Unit,
    onQuickEnter: (OperationMode) -> Unit
) {
    val supportedModes = when (meta.mode) {
        GameMode.SPY_MAIN, GameMode.ONE_NIGHT_WEREWOLF, GameMode.HUNT_TOWN, GameMode.SPY_AWALONG, GameMode.DRAW_GUESS -> OperationMode.entries.toList()
        else -> listOf(OperationMode.LOCAL, OperationMode.LAN)
    }.filter { it == OperationMode.LOCAL || AppDistribution.roomsEnabled }
        .filter { it != OperationMode.LAN || !org.walks.gamecopilot.getPlatform().name.startsWith("Web") }
    val effectiveMode = if (currentMode in supportedModes) currentMode else supportedModes.first()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.animateContentSize(tween(260))) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onExpandToggle).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GameModeBadge(meta.mode, meta.brush, 0f, Modifier.size(62.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(meta.mode.title, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(meta.description, style = MaterialTheme.typography.bodySmall,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(meta.players, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (isExpanded) "−" else "+", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isExpanded) {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (supportedModes.size > 1) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        supportedModes.forEach { mode ->
                            Surface(Modifier.weight(1f).clickable { onModeClick(mode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (effectiveMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                                Text(when(mode) { OperationMode.LOCAL -> if (meta.mode == GameMode.HUNT_TOWN) "同机主持" else "同机游玩"; OperationMode.LAN -> "局域网"; OperationMode.ONLINE -> "网络房间" },
                                    modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelLarge, maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Button(onClick = { onQuickEnter(effectiveMode) }, modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(16.dp)) { Text("开始游戏", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
