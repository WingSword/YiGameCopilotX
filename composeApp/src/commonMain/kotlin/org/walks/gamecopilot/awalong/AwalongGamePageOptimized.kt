package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
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
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 优化后的阿瓦隆游戏主页面
 * 重构后的版本，结构更清晰，代码更易维护
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AwalongGamePageOptimized(viewmodel: MainViewmodel) {
    val gameConfig = viewmodel.awalongConfigState.value
    val gameState = viewmodel.awalongGameState.value
    var showRulesDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val nameChange = { newName: String, no: Int ->
        viewmodel.handleAwalongGameIntent(AwalongIntent.ChangeNickName(newName, no))
    }

    // 计算总页面数
    val totalPages = gameConfig.process.size + 1

    val pageState = rememberPagerState(initialPage = 0, pageCount = { totalPages })

    // 构建页面列表，传递 pageState 和 scope
    // 使用 remember 来避免不必要的重新构建，但监听游戏状态变化
    val pages = remember(gameState.playTime) {
        buildPages(viewmodel, gameConfig, nameChange, pageState, scope) { showRulesDialog = true }
    }

    // 计算当前可以访问的最大页面索引 - 实时计算
    val maxAccessiblePage = calculateMaxAccessiblePage(gameState)

    // 完全禁止用户滑动，只允许通过程序控制页面切换
    val userScrollEnabled = false

    // 实时监控页面状态，确保立即响应（去掉动画）
    LaunchedEffect(maxAccessiblePage, pageState.currentPage) {
        if (pageState.currentPage > maxAccessiblePage) {
            pageState.scrollToPage(maxAccessiblePage) // 使用scrollToPage而不是animateScrollToPage
        }
    }

    // 监听游戏状态变化，立即重新计算可访问页面
    LaunchedEffect(gameState.dayList) {
        val newMaxPage = calculateMaxAccessiblePage(gameState)
        if (pageState.currentPage > newMaxPage) {
            pageState.scrollToPage(newMaxPage) // 使用scrollToPage而不是animateScrollToPage
        }
    }

    Column {
        HorizontalPager(
            state = pageState,
            userScrollEnabled = userScrollEnabled, // 完全禁止用户滑动
            modifier = Modifier.weight(1f) // 让HorizontalPager占用剩余空间
        ) { page ->
            pages[page]()
        }

        // 统一的底部导航按钮
        PageNavigationButtons(
            currentPage = pageState.currentPage,
            maxAccessiblePage = maxAccessiblePage,
            totalPages = pages.size,
            scope = scope,
            pageState = pageState
        )

        // 规则弹窗
        if (showRulesDialog) {
            GameRulesDialog(
                onDismiss = { showRulesDialog = false },
                gameConfig = gameConfig
            )
        }
    }
}

/**
 * 构建所有页面
 */
@OptIn(ExperimentalFoundationApi::class)
private fun buildPages(
    viewmodel: MainViewmodel,
    gameConfig: AwalongConfig,
    nameChange: (String, Int) -> Unit,
    pageState: PagerState,
    scope: CoroutineScope,
    onShowRulesDialog: () -> Unit
): List<@Composable () -> Unit> {
    val pages = mutableListOf<@Composable () -> Unit>()

    // 第零日页面
    pages.add {
        // 使用remember来观察状态变化，确保重启时重新组合
        val currentGameState by viewmodel.awalongGameState.collectAsState()
        PageContent(
            title = "第零日",
            gameConfig = gameConfig,
            bgColor = MaterialTheme.colorScheme.background,
            currentPage = 1,
            totalPages = gameConfig.process.size + 1,
            showRulesDialog = onShowRulesDialog,
            onRestartGame = {
                viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
            }
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                // 第零日内容
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
        }
    }

    // 任务日页面
    gameConfig.process.forEachIndexed { index, taskNum ->
        pages.add {
            // 使用collectAsState来观察状态变化，确保重启时重新组合
            val currentGameState by viewmodel.awalongGameState.collectAsState()
            PageContent(
                title = "第${index + 1}日",
                gameConfig = gameConfig,
                bgColor = MaterialTheme.colorScheme.background,
                currentPage = index + 2,
                totalPages = gameConfig.process.size + 1,
                showRulesDialog = onShowRulesDialog,
                onRestartGame = {
                    viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
                }
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    PageDayTaskOptimized(
                        roleList = currentGameState.roleList,
                        nicknameList = currentGameState.nickNameList,
                        taskNum = gameConfig.process[index],
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
                                gamePhase = "TASK_RESULT" // 标记任务已完成
                            )
                            viewmodel.handleAwalongGameIntent(AwalongIntent.CheckTask(completedTask))
                        },
                        pageState = pageState,
                        scope = scope
                    )

                    // 优化的任务进度显示
                    TaskProgressBar(
                        currentDay = index,
                        dayList = currentGameState.dayList,
                        gameConfig = gameConfig
                    )
                }
            }
        }
    }

    return pages
}

/**
 * 计算当前可以访问的最大页面索引
 * 页面0：第零日，页面1：第1日（对应任务索引0），页面2：第2日（对应任务索引1）...
 */
private fun calculateMaxAccessiblePage(gameState: AwalongGameState): Int {
    // 第0页（第零日）总是可以访问下一页（第1页）
    var maxAccessiblePage = 1 // 第0页总是可以到第1页

    // 如果没有任何任务日数据，第0页可以到第1页
    if (gameState.dayList.isEmpty()) {
        return maxAccessiblePage
    }

    // 检查每个任务日是否完成
    for (taskIndex in gameState.dayList.indices) {
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

    // 确保最大可访问页面不超过总页面数
    val totalPages = gameState.dayList.size + 1 // 第零日 + 任务日数量
    return maxAccessiblePage.coerceAtMost(totalPages - 1)
}

/**
 * 页面导航按钮组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PageNavigationButtons(
    currentPage: Int,
    maxAccessiblePage: Int,
    totalPages: Int,
    scope: CoroutineScope,
    pageState: PagerState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedVisibility(currentPage > 0) {
            Button(
                onClick = {
                    if (currentPage > 0) {
                        scope.launch {
                            pageState.scrollToPage(currentPage - 1)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red // 红色确保可见
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )

            }
        }
        Spacer(modifier = Modifier.weight(1f))
        val isNextEnabled = currentPage == 0 || currentPage < maxAccessiblePage

        AnimatedVisibility(isNextEnabled && currentPage == 0) {
            Button(
                onClick = {
                    scope.launch {
                        pageState.scrollToPage(currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue // 强制蓝色确保可见
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.rotate(180f)
                )
            }
        }
    }
}


/**
 * 页面内容容器组件
 */
@Composable
private fun PageContent(
    title: String,
    gameConfig: AwalongConfig,
    bgColor: Color,
    currentPage: Int,
    totalPages: Int,
    showRulesDialog: () -> Unit,
    onRestartGame: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        TopNavigationBar(
            title = title,
            currentPage = currentPage,
            totalPages = totalPages,
            onShowRules = showRulesDialog,
            onRestartGame = onRestartGame
        )

        // 主内容区域 - 使用weight让内容区域自适应
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(bgColor)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * 顶部导航栏组件
 */
@Composable
private fun TopNavigationBar(
    title: String,
    currentPage: Int,
    totalPages: Int,
    onShowRules: () -> Unit,
    onRestartGame: (() -> Unit)? = null
) {
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
            // 页面标题和进度
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "进度 $currentPage / $totalPages",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 操作按钮
            Row {
                // 添加重新开始按钮
                onRestartGame?.let {
                    AnimatedVisibility(currentPage == 1) {
                        TextButton(onClick = {
                            PlatformHelper.getInstance().vibrateLongMethod()
                            it.invoke()
                        }, colors = ButtonDefaults.textButtonColors()) {
                            Text(
                                text = "重新开始",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                    }

                }

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
            }
        }
    }
}