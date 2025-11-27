package org.walks.gamecopilot.ui.page.game


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.MorandiColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.data.entity.WordGroupManager
import org.walks.gamecopilot.getWordMapBySelectedGroups
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker
import org.walks.gamecopilot.ui.widget.FlipCard
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_asterisk
import yigamecopilotx.composeapp.generated.resources.icon_cardindex
import yigamecopilotx.composeapp.generated.resources.icon_cardindex_notitle
import yigamecopilotx.composeapp.generated.resources.icon_info
import yigamecopilotx.composeapp.generated.resources.icon_star


/**
 *  Created by Wing at 20:53 on 2025/3/25
 *  本地卧底游戏
 */

const val IDENTITY_DISMISS = 0
const val IDENTITY_SHOW = -1
const val IDENTITY_SHOW_ALL = -3

/**
 * 本地卧底游戏页面
 * 处理玩家人数选择、卧底数量配置及游戏数据显示等功能
 *
 * @param viewmodel MainViewmodel - 游戏主视图模型，用于处理业务逻辑和数据存储
 * @param onBack 返回回调函数
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalSpyGamePage(viewmodel: MainViewmodel, onBack: () -> Unit) {
    // 游戏状态控制：用于强制刷新游戏问候视图的key值
    var gameTimeState by remember { mutableIntStateOf(0) }

    // 从ViewModel获取当前游戏状态数据
    val gameEntity = viewmodel.gameEntity.collectAsState().value
    val currentGame = gameEntity.currentGame

    // 当前玩家总数，默认取最近一次记录或4人
    val playerNum = currentGame.totalPlayerNumber
    // 状态控制：控制人数选择器弹窗的显示状态
    var showNumberPicker by remember { mutableStateOf(false) }
    // 可选的游玩人数范围（4-12人）
    val numberList = (4..16).map { it.toString() }

    // 状态控制：词汇查看弹窗的显示状态
    var showWordsDialog by remember { mutableStateOf(false) }

    // 状态控制：词库选择区域的折叠状态
    var isWordLibraryExpanded by remember { mutableStateOf(true) }

    // 状态控制：公布所有身份
    var showAllIdentities by remember { mutableStateOf(false) }

    // 状态控制：所有玩家是否都已查看身份
    var allPlayersViewed by remember { mutableStateOf(false) }

    // 游戏开始后自动折叠词库区域
    LaunchedEffect(gameTimeState) {
        if (gameTimeState > 0) {
            isWordLibraryExpanded = false
        }
    }

    // 获取全局选中的词组
    val selectedWordGroups = gameEntity.globalSelectedWordGroups
    // 获取当前选中词组的所有词汇
    val currentWords = remember(selectedWordGroups) {
        getWordMapBySelectedGroups(selectedWordGroups)
    }


    LaunchedEffect(gameTimeState) {
        if (gameTimeState > 0) {
            PlatformHelper.getInstance().vibrateLongMethod()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp)) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🕵️ 本地卧底",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "找出隐藏的卧底，保护平民身份",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧按钮组
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 词汇选择按钮 - 只在词库区域隐藏时显示
                AnimatedVisibility(
                    visible = !isWordLibraryExpanded,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isWordLibraryExpanded = true }
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_info),
                            contentDescription = "词汇选择",
                            tint = Color.Unspecified, // 使用原色
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "词汇选择",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 折叠控制按钮 - 只在词库区域展开时显示
                AnimatedVisibility(
                    visible = isWordLibraryExpanded,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    IconButton(
                        onClick = { isWordLibraryExpanded = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isWordLibraryExpanded) 0f else 180f,
                            animationSpec = tween(durationMillis = 300),
                            label = "arrow_rotation"
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "折叠词库",
                            modifier = Modifier.rotate(rotationAngle),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 游戏配置区域 - 统一的卡片样式，包含词库选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 区域标题
                Text(
                    text = "游戏配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 词库选择区域 - 可折叠
                AnimatedVisibility(
                    visible = isWordLibraryExpanded,
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 }, // 从上方1/2位置滑入
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = androidx.compose.animation.core.EaseOutQuart
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = androidx.compose.animation.core.EaseOutQuart
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -it / 2 }, // 向上方1/2位置滑出
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = androidx.compose.animation.core.EaseInQuart
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = androidx.compose.animation.core.EaseInQuart
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "词库选择",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // 词汇查看按钮 - 使用原色
                            Icon(
                                painter = painterResource(Res.drawable.icon_info),
                                contentDescription = "查看词汇",
                                tint = Color.Unspecified, // 使用原色
                                modifier = Modifier.size(24.dp).clickable {
                                    showWordsDialog = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 词库选择内容
                        WordGroupSelectorContent(
                            selectedGroupIds = selectedWordGroups,
                            onGroupsChanged = { groupIds ->
                                viewmodel.handleGameIntent(GameIntent.RefreshWordGroups(groupIds))
                            }
                        )
                    }
                }

                // 分割线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 游戏设置和控制区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧：配置按钮列
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // 游戏状态控制：当前显示的卧底/空白卡选择器类型（0=隐藏，1=卧底，2=空白卡）
                        var showSpyNumberPicker by remember { mutableStateOf(0) }
                        // 最大卧底数计算（总人数的三分之一）
                        val maxSpyList = (1..playerNum / 3).map { "$it" }
                        // 从游戏状态获取当前配置值
                        val spyNumber = currentGame.spyNum
                        val blackNum = currentGame.blackNum

                        // 配置按钮列
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // 游玩人数按钮 - 小尺寸
                            SmallConfigButton(
                                title = "游玩人数",
                                value = playerNum.toString(),
                                onClick = { showNumberPicker = true },
                                color = MaterialTheme.colorScheme.primary
                            )

                            // 卧底人数按钮 - 小尺寸
                            SmallConfigButton(
                                title = "卧底人数",
                                value = spyNumber.toString(),
                                onClick = { showSpyNumberPicker = 1 },
                                color = MaterialTheme.colorScheme.error
                            )

                            // 空白卡数量按钮 - 小尺寸
                            SmallConfigButton(
                                title = "空白卡",
                                value = blackNum.toString(),
                                onClick = { showSpyNumberPicker = 2 },
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // 数值选择器组件（卧底/空白卡）
                        WeSingleColumnPicker(
                            visible = showSpyNumberPicker != 0,
                            title = if (showSpyNumberPicker == 1) "选择卧底人数" else "选择空白卡片数量",
                            range = if (showSpyNumberPicker == 1) maxSpyList else (0..currentGame.spyNum).map { "$it" },
                            onCancel = { showSpyNumberPicker = 0 },
                            onChange = {
                                gameTimeState++
                                when (showSpyNumberPicker) {
                                    1 -> {
                                        // 更新卧底数量时自动修正空白卡数值不超过新卧底数
                                        viewmodel.handleGameIntent(
                                            GameIntent.RefreshSpyNumber(
                                                spyNum = maxSpyList[it].toInt(),
                                                blackNum = if (blackNum <= spyNumber) blackNum else 0
                                            )
                                        )
                                    }

                                    2 -> {
                                        // 直接更新空白卡数量
                                        viewmodel.handleGameIntent(
                                            GameIntent.RefreshSpyNumber(
                                                spyNum = spyNumber,
                                                blackNum = it
                                            )
                                        )
                                    }
                                }
                            },
                            value = if (showSpyNumberPicker == 1) currentGame.spyNum else currentGame.blackNum
                        )
                    }

                    // 右侧：游戏控制区域
                    Column(
                        modifier = Modifier.weight(2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // 开始游戏按钮 - 大尺寸
                        GameControlButton(
                            text = if (gameTimeState == 0) "开始游戏" else "重新开始",
                            onClick = {
                                if (gameTimeState == 0) {
                                    gameTimeState++
                                }
                                viewmodel.handleGameIntent(GameIntent.StartGame)
                                if (gameTimeState > 0) {
                                    gameTimeState++
                                }
                                // 重新开始时重置身份公布状态
                                showAllIdentities = false
                            },
                            color = if (gameTimeState == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 游戏状态提示区域 - 统一的状态显示组件
                        AnimatedVisibility(
                            visible = gameTimeState > 0,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeOut(animationSpec = tween(durationMillis = 300))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .let { modifier ->
                                        if (allPlayersViewed && !showAllIdentities) {
                                            modifier.combinedClickable(
                                                onLongClick = {
                                                    PlatformHelper.getInstance().vibrateLongMethod()
                                                    showAllIdentities = true
                                                },
                                                onClick = {
                                                    PlatformHelper.getInstance().vibrateMethod()
                                                }
                                            )
                                                .background(
                                                    color = MaterialTheme.colorScheme.error.copy(
                                                        alpha = 0.1f
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.error.copy(
                                                        alpha = 0.3f
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        } else {
                                            modifier.background(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.3f
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // 根据状态显示不同的图标和文字
                                    if (allPlayersViewed && !showAllIdentities) {
                                        // 长按公布所有身份状态
                                        Icon(
                                            imageVector = Icons.Filled.Create,
                                            contentDescription = "公布所有身份",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "长按公布所有身份",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else if (showAllIdentities) {
                                        // 游戏结束状态
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.error,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "游戏已结束",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        // 游戏进行中状态
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "游戏进行中",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // 游戏未开始时的提示
                        AnimatedVisibility(
                            visible = gameTimeState == 0,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 300))
                        ) {
                            Text(
                                text = "配置完成，开始游戏",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }


                // 主人数选择器组件
                WeSingleColumnPicker(
                    visible = showNumberPicker,
                    title = "选择游玩人数",
                    range = numberList,
                    onCancel = { showNumberPicker = false },
                    onChange = {
                        gameTimeState++
                        viewmodel.handleGameIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt()))
                    },
                    value = numberList.indexOf(playerNum.toString())
                )
            }
        }


        // 游戏数据显示区 - 只有在游戏开始后才显示
        if (gameTimeState > 0) {
            Spacer(Modifier.height(16.dp))

            // 游戏结果显示组件，key控制强制刷新
            GameGreetingView(
                key = gameTimeState,
                gameState = currentGame,
                showAllIdentities = showAllIdentities,
                onShowAllIdentitiesChange = { showAllIdentities = it },
                onAllPlayersViewed = { allPlayersViewed = it }
            )
        }

    }

    // 词汇查看弹窗
    if (showWordsDialog) {
        Dialog(
            onDismissRequest = { showWordsDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 弹窗标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "当前可用词汇 (${currentWords.size}个)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { showWordsDialog = false }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "关闭"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 词汇列表 - 过滤掉当前游戏使用的词汇
                    val availableWords =
                        remember(currentWords, currentGame.gameWord, currentGame.spyWord) {
                            currentWords.entries.filterNot { (spyWord, normalWord) ->
                                // 过滤掉当前游戏使用的词汇对
                                (spyWord == currentGame.spyWord && normalWord == currentGame.gameWord) ||
                                        (spyWord == currentGame.gameWord && normalWord == currentGame.spyWord)
                            }
                        }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableWords) { (spyWord, normalWord) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = spyWord,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = normalWord,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 如果没有可用词汇，显示提示
                        if (availableWords.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "当前词库中暂无其他可用词汇",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * 词库选择内容组件（不包含外层容器和标题）
 * 用于整合到其他组件中
 */
@Composable
fun WordGroupSelectorContent(
    selectedGroupIds: Set<String>,
    onGroupsChanged: (Set<String>) -> Unit
) {
    val allGroups = WordGroupManager.getAllGroups()

    // 词组选择区域
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allGroups.values.toList()) { group ->
            WordGroupItem(
                group = group,
                isSelected = selectedGroupIds.contains(group.id),
                onToggle = { groupId ->
                    val newSelection = if (selectedGroupIds.contains(groupId)) {
                        selectedGroupIds - groupId
                    } else {
                        selectedGroupIds + groupId
                    }
                    onGroupsChanged(newSelection)
                }
            )
        }
    }
}

/**
 * 词组项组件
 */
@Composable
private fun WordGroupItem(
    group: org.walks.gamecopilot.data.entity.WordGroup,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onToggle(group.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.displayName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        if (group.isBuiltIn) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "•",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}



/**
 * 游戏身份展示视图组件
 *
 * @param key 重组标识键，用于控制派生状态和记忆值的更新时机
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 * @param showAllIdentities 是否显示所有身份
 * @param onShowAllIdentitiesChange 显示所有身份状态变化回调
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGreetingView(
    key: Int,
    gameState: LocalSpyEntity,
    showAllIdentities: Boolean = false,
    onShowAllIdentitiesChange: (Boolean) -> Unit = {},
    onAllPlayersViewed: (Boolean) -> Unit = {}
) {
    // 当前选中玩家索引（1-based）
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    // 身份显示状态机（默认隐藏，但可以由外部控制）
    val identityDisPlayState = remember { mutableStateOf(IDENTITY_DISMISS) }

    // 监听外部showAllIdentities状态
    LaunchedEffect(showAllIdentities) {
        if (showAllIdentities) {
            identityDisPlayState.value = IDENTITY_SHOW_ALL
        }
    }
    //单个玩家身份展示状态
    val playerIdentityState = remember {
        mutableStateListOf<Int>()
    }

    // 派生游戏状态（根据key变化重置）
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    // 玩家查看次数记录列表
    val watchedTimeList = remember(key) {
        MutableList(17) { 0 }
    }

    /* 当key变化时重置游戏状态 */
    LaunchedEffect(key1 = realGameState.gameWord) {
        identityDisPlayState.value = IDENTITY_DISMISS
        watchedTimeList.map {
            watchedTimeList[it] = 0
        }
        playerIdentityState.clear()
        onAllPlayersViewed(false)
    }

    // 监听玩家查看状态，当所有玩家都查看过身份时通知外部
    LaunchedEffect(watchedTimeList) {
        val allViewed = (watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber
        onAllPlayersViewed(allViewed)
    }



    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        /* 玩家选择区域布局 */
        LocalPlayerSelectArea(
            playerNum = realGameState.totalPlayerNumber,
            getWatchedTime = { watchedTimeList[it] },
            gameState = realGameState,
            identityDisplayState = identityDisPlayState.value,
            playerIdentityState = playerIdentityState,
            onClick = { currentSelect ->
                currentSelectPlayer = currentSelect
                watchedTimeList[currentSelect] += 1
                identityDisPlayState.value = IDENTITY_SHOW
                PlatformHelper.getInstance().vibrateMethod()
            }
        ) { currentSelect ->
            playerIdentityState.add(currentSelect)
        }
    }

    // 身份卡片动画显示逻辑
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = identityDisPlayState.value == IDENTITY_SHOW,
        // 组合动画+物理效果
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
            initialOffsetY = { it }
        ) + fadeIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = slideOutVertically() + fadeOut()) {
        Dialog(
            onDismissRequest = { identityDisPlayState.value = IDENTITY_DISMISS },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // 关键属性
            )
        ) {
            LocalSpyIdentityCard(
                gameState = gameState,
                currentSelectPlayer = currentSelectPlayer,
            )
        }
    }
}

/**
 * 本地播放器选择区域组件，用于展示可交互的玩家选择网格
 *
 * @param playerNum 总玩家数量，默认4个玩家
 * @param getWatchedTime 获取指定玩家观看时长的回调函数（参数：玩家序号，返回：观看时间单位数）
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 * @param identityDisplayState 身份显示状态
 * @param playerIdentityState 单个玩家身份展示状态列表
 * @param onClick 玩家头像点击事件回调（参数：被点击的玩家序号）
 * @param onLongClick 玩家头像长按事件回调（参数：被长按的玩家序号）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlayerSelectArea(
    playerNum: Int = 4,
    getWatchedTime: (Int) -> Int,
    gameState: LocalSpyEntity,
    identityDisplayState: Int,
    playerIdentityState: List<Int>,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit
) {


    // 创建4列的垂直网格布局
    LazyVerticalGrid(
        GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(playerNum) { pos ->
            val currentPlayer = pos + 1
            val watchedTime = getWatchedTime(currentPlayer)



            Box(modifier = Modifier.padding(top = 8.dp)) {
                // 玩家头像容器，包含点击交互和状态显示
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .combinedClickable(
                            onLongClick = { onLongClick(currentPlayer) },
                            onClick = { onClick(currentPlayer) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        painter = painterResource(
                            if (watchedTime < 1) Res.drawable.icon_cardindex_notitle
                            else Res.drawable.icon_cardindex
                        ),
                        modifier = Modifier.rotate(180f),
                        contentDescription = "Card index icon",
                        alpha = if (watchedTime >= 1) 0.6f else 1f
                    )

                    // 身份显示区域 - 在卡片内部显示身份信息
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 根据不同状态显示身份信息
                        when {
                            identityDisplayState == IDENTITY_SHOW_ALL -> {
                                Text(
                                    text = gameState.optIdentity(currentPlayer),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (gameState.isSpy(currentPlayer))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            playerIdentityState.contains(currentPlayer) -> {
                                Text(
                                    text = if (gameState.isSpy(currentPlayer)) "卧底" else "平民",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (gameState.isSpy(currentPlayer))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            watchedTime > 1 -> {
                                Text(
                                    text = "查看${watchedTime}次",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                Text(
                                    text = "",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 玩家序号文字显示，根据观看状态改变颜色
                    Text(
                        text = currentPlayer.toString(),
                        color = MaterialTheme.colorScheme.secondary.copy(if (watchedTime >= 1) 0.5f else 1f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 20.dp, top = 5.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


/**
 * 身份卡片组件，用于显示玩家身份信息及操作提示
 *
 * @param gameState 当前游戏状态实体，包含玩家身份数据及操作逻辑
 * @param currentSelectPlayer 当前选择的玩家编号（默认1号玩家）
 */
@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    currentSelectPlayer: Int = 1,
) {
    val flipState = remember { mutableStateOf(false) }
    // 主容器：包含卡片布局和交互效果
    FlipCard(
        modifier = Modifier.height(200.dp).width(140.dp).clip(RoundedCornerShape(12.dp))
            .clickable { flipState.value = !flipState.value },
        backContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {

                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(Res.drawable.icon_asterisk),
                        alpha = 0.33f,
                        modifier = Modifier.size(100.dp),
                        contentDescription = ""
                    )
                    Text(
                        text = currentSelectPlayer.toString(),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (gameState.isSpy(currentSelectPlayer)) "" else "",
                    textAlign = TextAlign.Center,
                    color = if (gameState.isSpy(currentSelectPlayer)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraLight,
                    modifier = Modifier.fillMaxWidth()
                )
                // 操作提示文本：根据显示状态切换提示语
                Text(
                    "点击卡片查看身份词",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        frontContent = {
            Box(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface
                ).border(
                    BorderStroke(
                        width = 4.dp,
                        color = MorandiColorList[(0..7).random()] // 随机生成边框颜色
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painterResource(Res.drawable.icon_star),
                        alpha = 0.33f,
                        modifier = Modifier.size(120.dp).rotate(-30f),
                        contentDescription = ""
                    )

                    Text(
                        text = currentSelectPlayer.toString(),
                        modifier = Modifier.rotate(-30f),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        gameState.optIdentity(currentSelectPlayer),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.weight(1f))

                }
            }

        },
        isFlipped = !flipState.value,
        onFlipComplete = {

        }
    )
}


/**
 * 小尺寸配置按钮组件
 * 用于垂直排列的配置选项
 */
@Composable
fun SmallConfigButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }

            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 游戏控制按钮组件
 * 大尺寸的主要操作按钮
 */
@Composable
fun GameControlButton(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}