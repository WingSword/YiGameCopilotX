package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.awalong.components.AwalongDayZeroPage
import org.walks.gamecopilot.awalong.components.GameRulesDialog
import org.walks.gamecopilot.awalong.components.PageDayTaskOptimized
import org.walks.gamecopilot.awalong.components.TaskProgressBar
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState
import org.walks.gamecopilot.ui.components.BackIcon
import org.walks.gamecopilot.ui.components.CommonTopBar
import org.walks.gamecopilot.ui.components.common.OfflinePassingGuideDialog
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_close
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 优化后的阿瓦隆游戏主页面
 * 重构后的版本，结构更清晰，代码更易维护
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AwalongGamePageOptimized(navi: NavController, viewmodel: MainViewmodel) {
    val gameConfig = viewmodel.awalongConfigState.value
    val customConfig = viewmodel.awalongCustomConfigState.value
    val gameState = viewmodel.awalongGameState.value
    var showRulesDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    val nameChange = { newName: String, no: Int ->
        viewmodel.handleAwalongGameIntent(AwalongIntent.ChangeNickName(newName, no))
    }

    // 检测是否使用自定义配置：如果游戏状态中的玩家数量与自定义配置匹配，则使用自定义配置
    val isUsingCustomConfig = gameState.roleList.size == customConfig.totalPlayers

    // 使用正确的配置流程
    val actualProcess = if (isUsingCustomConfig) {
        customConfig.process
    } else {
        gameConfig.process
    }

    // 计算总页面数
    val totalPages = actualProcess.size + 1

    val pageState = rememberPagerState(initialPage = 0, pageCount = { totalPages })

    // 游戏结束状态（需在 buildPages 之前声明，以便传入）
    var gameEndResult by remember { mutableStateOf<GameEndResult?>(null) }
    var showAssassinationDialog by remember { mutableStateOf(false) }
    var isGameLocked by remember { mutableStateOf(false) }

    // 构建页面列表，传递 pageState 和 scope
    // 使用 remember 来避免不必要的重新构建，但监听游戏状态变化
    val pages = remember(gameState.playTime, actualProcess, isGameLocked) {
        buildPages(
            viewmodel,
            gameConfig,
            actualProcess,
            nameChange,
            pageState,
            scope,
            isGameLocked
        ) { showRulesDialog = true }
    }

    // 计算当前可以访问的最大页面索引 - 实时计算
    val maxAccessiblePage = calculateMaxAccessiblePage(gameState, actualProcess)

    // 完全禁止用户滑动，只允许通过程序控制页面切换
    val userScrollEnabled = false

    // 实时监控页面状态，确保立即响应（去掉动画）
    LaunchedEffect(maxAccessiblePage, pageState.currentPage) {
        if (pageState.currentPage > maxAccessiblePage) {
            pageState.scrollToPage(maxAccessiblePage) // 使用scrollToPage而不是animateScrollToPage
        }
    }
    // 监听游戏状态变化，自动翻页和检测游戏结束
    LaunchedEffect(gameState.dayList, pageState.currentPage) {
        val newMaxPage = calculateMaxAccessiblePage(gameState, actualProcess)

        // 调试信息：打印当前页面和最大可访问页面
        println("页面状态：当前页=${pageState.currentPage}, 最大可访问页=$newMaxPage, 任务完成数=${gameState.dayList.count { it.gamePhase == "TASK_RESULT" }}")

        // 检查是否有新完成的任务需要自动翻页
        val completedTasks = gameState.dayList.count { it.gamePhase == "TASK_RESULT" }

        // 如果当前页面是任务页面且任务已完成，自动翻到下一页
        if (pageState.currentPage > 0 && pageState.currentPage < completedTasks && !isGameLocked) {
            val nextPage = pageState.currentPage + 1
            if (nextPage <= newMaxPage) {
                println("自动翻页：从${pageState.currentPage}页到${nextPage}页")
                pageState.scrollToPage(nextPage)
            }
        }

        // 如果当前页面超过最大可访问页面，调整到正确页面
        if (pageState.currentPage > newMaxPage) {
            pageState.scrollToPage(newMaxPage)
        }
    }


    // 检查游戏结束状态 - 监听所有相关状态变化
    LaunchedEffect(gameState.dayList, gameState.roleList, gameState.playTime) {
        val result = AwalongGameLogic.checkGameEnd(gameState)


        if (result != null && !isGameLocked) {
            // 如果是蓝方胜利且场上有刺客，显示刺杀弹窗
            if (result.winner == "蓝方" && gameState.roleList.contains(AwalongRole.CISHA) && result.reason.contains(
                    "进入刺杀阶段"
                )
            ) {
                if (!showAssassinationDialog && gameEndResult == null) {
                    println("触发刺杀弹窗")
                    showAssassinationDialog = true
                    isGameLocked = true
                }
            } else if (gameEndResult == null && !showAssassinationDialog) {
                // 其他情况直接显示游戏结果弹窗
                println("触发游戏结果弹窗：$result")
                gameEndResult = result
                isGameLocked = true

                // 游戏结束后自动返回到第0页
                scope.launch {
                    delay(2000) // 延迟2秒后返回，给用户足够时间查看结果
                    pageState.scrollToPage(0)
                    // 重置游戏结束状态，以便下次游戏
                    gameEndResult = null
                    isGameLocked = false
                }
            }
        }
    }

    // 监听页面退出事件，重置游戏状态
    LaunchedEffect(Unit) {
        // 当页面被销毁时，重置游戏状态
        // 使用DisposableEffect来监听页面生命周期
    }
    
    Column {
        CommonTopBar(
            title = when (pageState.currentPage) {
                0 -> "阿瓦隆 · 第零日"
                else -> "阿瓦隆 · 第${pageState.currentPage}日"
            },
            subtitle = "进度 ${pageState.currentPage + 1}/$totalPages",
            onBack = {
                viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
                navi.popBackStack()
            },
            customAction = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.icon_info),
                        contentDescription = "游戏规则",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showRulesDialog = true }
                    )
                    if (pageState.currentPage == 0) {
                        IconButton(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RectangleShape)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RectangleShape
                                ).rotate(rotation.value),
                            onClick = {
                                scope.launch {
                                    rotation.animateTo(
                                        targetValue = 360f,
                                        animationSpec = tween(
                                            durationMillis = 500,
                                            easing = LinearEasing
                                        )
                                    )
                                    rotation.snapTo(0f)
                                }
                                PlatformHelper.getInstance().vibrateLongMethod()
                                viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
                                scope.launch { pageState.scrollToPage(0) }
                                gameEndResult = null
                                showAssassinationDialog = false
                                isGameLocked = false
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                            )
                        ) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "重新开始",
                                tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.33f)
                            )
                        }
                    }
                }
            }
        )

        HorizontalPager(
            state = pageState,
            userScrollEnabled = userScrollEnabled && !isGameLocked, // 游戏锁定时禁止滑动
            modifier = Modifier.weight(1f)
        ) { page ->
            // 简化的页面内容，只包含核心内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                pages[page]()
            }
        }

        // 底部导航和进度条
        BottomNavigationWithProgress(
            currentPage = pageState.currentPage,
            maxAccessiblePage = if (isGameLocked) pageState.currentPage else maxAccessiblePage,
            totalPages = pages.size,
            scope = scope,
            pageState = pageState,
            gameState = gameState,
            gameConfig = gameConfig,
            actualProcess = actualProcess,
            isGameLocked = isGameLocked
        )

        // 规则弹窗
        if (showRulesDialog) {
            GameRulesDialog(
                onDismiss = { showRulesDialog = false },
                gameConfig = gameConfig
            )
        }

        OfflinePassingGuideDialog(
            show = showGuideDialog,
            gameTitle = "阿瓦隆",
            steps = listOf(
                "第零日阶段按顺序传递手机，每位玩家仅查看自己的身份与视野。",
                "看完身份后立即关闭卡片并交给下一位，避免身份泄露。",
                "随后进入任务流程：组队、投票、执行任务，直至触发胜负结算。"
            ),
            onDismiss = { showGuideDialog = false }
        )

        // 刺杀弹窗
        if (showAssassinationDialog) {
            AssassinationDialog(
                gameState = gameState,
                onAssassinationComplete = { success ->
                    showAssassinationDialog = false
                    // 保存刺杀结果到游戏状态
                    viewmodel.handleAwalongGameIntent(
                        AwalongIntent.UpdateAssassinationResult(
                            success
                        )
                    )
                    if (success) {
                        // 刺杀成功，红方胜利
                        gameEndResult = GameEndResult("红方", "刺客成功刺杀梅林")
                    } else {
                        // 刺杀失败，蓝方胜利
                        gameEndResult = GameEndResult("蓝方", "刺客未能刺杀梅林")
                    }
                }
            )
        }


        // 显示所有结果对话框
        if (gameEndResult != null) {
            AllResultsDialog(
                gameState = gameState,
                onDismiss = {
                    // 游戏结束后自动返回到第0页
                    scope.launch {
                        delay(1000)
                        pageState.scrollToPage(0)
                        gameEndResult = null
                        viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
                    }
                }
            )
        }

    }
}

/**
 * 构建所有页面（简化版，只包含核心内容）
 */
@OptIn(ExperimentalFoundationApi::class)
private fun buildPages(
    viewmodel: MainViewmodel,
    gameConfig: AwalongConfig,
    actualProcess: List<Int>,
    nameChange: (String, Int) -> Unit,
    pageState: PagerState,
    scope: CoroutineScope,
    isGameLocked: Boolean,
    onShowRulesDialog: () -> Unit
): List<@Composable () -> Unit> {
    val pages = mutableListOf<@Composable () -> Unit>()

    // 第零日页面
    pages.add {
        val currentGameState by viewmodel.awalongGameState.collectAsState()
        AwalongDayZeroPage(
            roleList = currentGameState.roleList,
            nicknameList = currentGameState.nickNameList,
            playTime = currentGameState.playTime,
            onNameChange = nameChange,
            onRefreshRoles = {
                viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
            }
        )
    }

    // 任务日页面
    actualProcess.forEachIndexed { index, taskNum ->
        pages.add {
            val currentGameState by viewmodel.awalongGameState.collectAsState()
            PageDayTaskOptimized(
                roleList = currentGameState.roleList,
                nicknameList = currentGameState.nickNameList,
                taskNum = taskNum,
                dayEntity = currentGameState.dayList.getOrNull(index),
                taskIndex = index,
                gameConfig = gameConfig,
                gameState = currentGameState,
                viewmodel = viewmodel,
                onCheck = { map, result, cap ->
                    val completedTask = AwalongGameDayEntity(
                        day = index,
                        mainTask = map,
                        taskResult = result,
                        murderTask = -1,
                        captain = cap,
                        gamePhase = "TASK_RESULT"
                    )
                    viewmodel.handleAwalongGameIntent(AwalongIntent.CheckTask(completedTask))
                },
                pageState = pageState,
                scope = scope,
                totalTaskDays = actualProcess.size,
                isGameLocked = isGameLocked
            )
        }
    }

    return pages
}

/**
 * 计算当前可以访问的最大页面索引
 * 页面0：第零日，页面1：第1日（对应任务索引0），页面2：第2日（对应任务索引1）...
 */
private fun calculateMaxAccessiblePage(gameState: AwalongGameState, actualProcess: List<Int>): Int {
    // 第0页（第零日）总是可以访问下一页（第1页）
    var maxAccessiblePage = 1 // 第0页总是可以到第1页

    // 如果没有任何任务日数据，第0页可以到第1页
    if (gameState.dayList.isEmpty()) {
        return maxAccessiblePage
    }

    // 检查每个任务日是否完成
    for (taskIndex in gameState.dayList.indices) {
        // 确保任务索引不超过实际流程的范围
        if (taskIndex >= actualProcess.size) {
            break
        }
        
        val dayEntity = gameState.dayList[taskIndex]

        if (dayEntity.gamePhase == "TASK_RESULT") {
            // 任务完成，可以访问下一个任务页面（任务索引+2，因为页面索引=任务索引+1）
            maxAccessiblePage = taskIndex + 2
        } else {
            // 遇到未完成的任务，只能访问当前任务页面
            // 第一个未完成的任务页面也是可以访问的（任务索引+1）
            maxAccessiblePage = taskIndex + 1
            break // 遇到未完成的任务就停止检查
        }
    }

    // 确保最大可访问页面不超过总页面数（基于实际流程配置）
    val totalPages = actualProcess.size + 1 // 第零日 + 任务日数量
    return maxAccessiblePage.coerceAtMost(totalPages - 1)
}

/**
 * 底部导航和进度条组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomNavigationWithProgress(
    currentPage: Int,
    maxAccessiblePage: Int,
    totalPages: Int,
    scope: CoroutineScope,
    pageState: PagerState,
    gameState: AwalongGameState,
    gameConfig: AwalongConfig,
    actualProcess: List<Int>,
    isGameLocked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 任务进度条（返回按钮已移至当前阶段栏）
            if (currentPage > 0) {
                val currentDay = currentPage - 1
                if (currentDay < gameState.dayList.size) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        TaskProgressBar(
                            currentDay = currentDay,
                            dayList = gameState.dayList,
                            gameConfig = gameConfig, // 使用正确的配置
                            actualProcess = actualProcess
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // 前进按钮（只在第零日显示）
            val isNextEnabled = currentPage == 0 || currentPage < maxAccessiblePage
            AnimatedVisibility(isNextEnabled && currentPage == 0 && !isGameLocked) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                        .background(MaterialTheme.colorScheme.surface, RectangleShape)
                        .clickable {
                            scope.launch {
                                pageState.scrollToPage(currentPage + 1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BackIcon(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(180f)
                    )
                }
            }
        }

        // 游戏锁定提示
        if (isGameLocked) {
            Text(
                text = "游戏已结束，无法进行导航操作",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


/**
 * 刺杀弹窗组件
 */
@Composable
private fun AssassinationDialog(
    gameState: AwalongGameState,
    onAssassinationComplete: (Boolean) -> Unit
) {
    var selectedTarget by remember { mutableStateOf<Int?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onAssassinationComplete(false) },
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("刺客刺杀阶段") },
        text = {
            Column {
                Text("蓝方已完成3个任务，刺客请选择刺杀目标。")
                Text("刺杀梅林则红方胜利，否则蓝方胜利。", fontSize = 14.sp)

                Spacer(modifier = Modifier.padding(8.dp))

                Text("请选择刺杀目标：", fontWeight = FontWeight.Bold)

                // 显示所有玩家供选择
                gameState.nickNameList.forEachIndexed { index, name ->
                    val isSelected = selectedTarget == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedTarget = index }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                                .padding(2.dp)
                        ) {
                            if (isSelected) {
                                Text("✓", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${index + 1}号玩家：$name")
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    selectedTarget?.let { target ->
                        val success = AwalongGameLogic.checkAssassinationSuccess(target, gameState)
                        onAssassinationComplete(success)
                    }
                },
                enabled = selectedTarget != null
            ) {
                Text("确认刺杀")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = { onAssassinationComplete(false) }
            ) {
                Text("取消")
            }
        }
    )
}

/**
 * 顶部导航栏组件
 */
@Composable
private fun TopNavigationBar(
    title: String,
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    onShowRules: () -> Unit,
    onRestartGame: (() -> Unit)? = null
) {
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier
                    .clip(RectangleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RectangleShape
                    ),
                onClick = {
                    onClose()
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_close),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp)
                )
            }
            // 页面标题和进度
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
//                Text(
//                    text = "进度 $currentPage / $totalPages",
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }

            // 操作按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.icon_info),
                    contentDescription = "游戏规则",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onShowRules()
                        }
                )

                // 添加重新开始按钮
                onRestartGame?.let {
                    AnimatedVisibility(currentPage == 1) {
                        IconButton(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clip(RectangleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RectangleShape
                                ).rotate(rotation.value),
                            onClick = {
                                scope.launch {
                                    rotation.animateTo(
                                        targetValue = 360f,
                                        animationSpec = tween(
                                            durationMillis = 500,
                                            easing = LinearEasing
                                        )
                                    )
                                    rotation.snapTo(0f) // 重置角度准备下次旋转
                                }
                                PlatformHelper.getInstance().vibrateLongMethod()
                                it.invoke()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                            content = {
                                Icon(
                                    modifier = Modifier.fillMaxSize(),
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "刷新房间人数",
                                    tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.33f)
                                )
                            }
                        )
                    }

                }
            }
        }
    }
}