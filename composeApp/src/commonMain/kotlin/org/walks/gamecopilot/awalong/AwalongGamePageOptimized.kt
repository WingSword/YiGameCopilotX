package org.walks.gamecopilot.awalong

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.GoldanColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
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
    var showRulesDialog by remember { mutableStateOf(false) }

    val nameChange = { newName: String, no: Int ->
        viewmodel.handleAwalongGameIntent(AwalongIntent.ChangeNickName(newName, no))
    }
    
    // 构建页面列表
    val pages = buildPages(viewmodel, gameConfig, nameChange) { showRulesDialog = true }
    
    val pageState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    
    // 计算当前可以访问的最大页面索引
    val maxAccessiblePage = remember(viewmodel.awalongGameState.value.dayList) {
        calculateMaxAccessiblePage(viewmodel.awalongGameState.value)
    }

    Column {
        HorizontalPager(
            state = pageState,
            userScrollEnabled = true // 允许用户自由切换页面
        ) { page ->
            pages[page]()
        }
        
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
private fun buildPages(
    viewmodel: MainViewmodel,
    gameConfig: AwalongConfig,
    nameChange: (String, Int) -> Unit,
    onShowRulesDialog: () -> Unit
): List<@Composable () -> Unit> {
    val pages = mutableListOf<@Composable () -> Unit>()
    
    // 第零日页面
    pages.add {
        PageContent(
            title = "第零日",
            gameConfig = gameConfig,
            bgColor = Color(0xff161823),
            currentPage = 1,
            totalPages = gameConfig.process.size + 1,
            showRulesDialog = onShowRulesDialog
        ) {
            PageDayZero(
                viewmodel.awalongGameState.value.roleList,
                viewmodel.awalongGameState.value.nickNameList,
                nameChange
            )
        }
    }
    
    // 任务日页面
    gameConfig.process.forEachIndexed { index, taskNum ->
        pages.add {
            PageContent(
                title = "第${index + 1}日",
                gameConfig = gameConfig,
                bgColor = GoldanColorList[index % 5],
                currentPage = index + 2,
                totalPages = gameConfig.process.size + 1,
                showRulesDialog = onShowRulesDialog
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    PageDayTaskOptimized(
                        roleList = viewmodel.awalongGameState.value.roleList,
                        nicknameList = viewmodel.awalongGameState.value.nickNameList,
                        taskNum = gameConfig.process[index],
                        dayEntity = viewmodel.awalongGameState.value.dayList.getOrNull(index),
                        taskIndex = index,
                        gameConfig = gameConfig,
                        gameState = viewmodel.awalongGameState.value,
                        viewmodel = viewmodel,
                        onCheck = { map, result, cap ->
                            viewmodel.handleAwalongGameIntent(
                                AwalongIntent.CheckTask(
                                    AwalongGameDayEntity(
                                        day = index,
                                        mainTask = map,
                                        taskResult = result,
                                        murderTask = -1,
                                        captain = cap
                                    )
                                )
                            )
                        }
                    )

                    // 优化的任务进度显示
                    TaskProgressBar(
                        currentDay = index,
                        dayList = viewmodel.awalongGameState.value.dayList,
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
 */
private fun calculateMaxAccessiblePage(gameState: AwalongGameState): Int {
    // 找到第一个未完成任务的天数
    val firstIncompleteDay = gameState.dayList.indexOfFirst { it.gamePhase != "TASK_RESULT" }
    return if (firstIncompleteDay == -1) {
        // 所有任务都完成了，可以访问所有页面
        gameState.dayList.size
    } else {
        // 可以访问到当前未完成的任务页面（+1 因为包含第0页）
        firstIncompleteDay + 1
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
            onShowRules = showRulesDialog
        )
        
        // 主内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
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
    onShowRules: () -> Unit
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
                IconButton(
                    onClick = onShowRules,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.icon_info),
                        contentDescription = "游戏规则",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}