package org.walks.gamecopilot.awalong

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.GoldanColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.components.PageDayTaskOptimized
import org.walks.gamecopilot.awalong.components.TaskPhase
import org.walks.gamecopilot.awalong.components.TaskProgressBar
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 *  Created by Wing at 17:39 on 2025/5/20
 *  阿瓦隆游戏界面 - 重构版本
 */

// 游戏规则弹窗组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRulesDialog(
    onDismiss: () -> Unit,
    gameConfig: AwalongConfig
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "阿瓦隆游戏规则",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 规则内容
                SelectionContainer {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            RuleSection(
                                title = "游戏目标",
                                icon = Icons.Default.Star,
                                content = """
                                    蓝方(好人)：完成3次任务成功且梅林未被刺杀
                                    红方(坏人)：完成2次任务失败或成功刺杀梅林
                                """.trimIndent()
                            )
                        }
                        
                        item {
                            RuleSection(
                                title = "角色详细说明",
                                icon = Icons.Default.Person,
                                content = """
                                    【好人阵营】
                                    • 梅林：可以看到除莫德雷德外的所有坏人身份，但不能暴露身份，否则会被刺客刺杀
                                    • 派西维尔：可以看到梅林和莫甘娜，但无法区分谁是真正的梅林
                                    • 忠臣：无特殊能力，需要通过逻辑推理找出坏人，保护梅林
                                    
                                    【坏人阵营】
                                    • 刺客：在好人完成3个任务后，可以刺杀梅林，如果刺杀成功则坏人获胜
                                    • 莫甘娜：伪装成梅林的样子，迷惑派西维尔
                                    • 莫德雷德：特殊身份，梅林无法看到其坏人身份
                                    • 奥伯伦：可以看到其他坏人，但其他坏人看不到奥伯伦，也不知道奥伯伦的存在
                                """.trimIndent()
                            )
                        }
                        
                        item {
                            RuleSection(
                                title = "详细游戏流程",
                                icon = Icons.Default.LocationOn,
                                content = """
                                    【第一阶段：组队】
                                    1. 每轮由一名玩家担任队长，按顺序轮换
                                    2. 队长根据当前任务人数要求选择执行任务的玩家
                                    3. 队长不能选择自己参与任务（除非特殊规则允许）
                                    
                                    【第二阶段：组队投票】
                                    1. 所有玩家对队长提出的队伍进行投票
                                    2. 超过半数同意则组队成功，进入任务执行阶段
                                    3. 组队失败则记录一次失败，轮换下一位队长
                                    4. 连续5次组队失败，坏人直接获胜
                                    
                                    【第三阶段：任务执行】
                                    1. 被选中的队伍成员秘密投票决定任务结果
                                    2. 好人必须投成功票，坏人可以选择投成功或失败票
                                    3. 只要有一张失败票，任务即失败（特殊任务除外）
                                    
                                    【第四阶段：刺杀阶段】
                                    1. 好人完成3个任务成功后，进入刺杀阶段
                                    2. 刺客需要指认谁是梅林
                                    3. 如果刺杀正确，坏人获胜；如果刺杀错误，好人获胜
                                """.trimIndent()
                            )
                        }
                        
                        item {
                            RuleSection(
                                title = "任务失败条件与特殊规则",
                                icon = Icons.Default.Lock,
                                content = """
                                    【任务失败条件】
                                    • 5-6人场：每轮任务只需要1张失败卡即任务失败
                                    • 7-9人场：第4轮任务需要2张失败卡才失败，其他轮次1张即可
                                    • 10人场：第4轮任务需要2张失败卡才失败，其他轮次1张即可
                                    
                                    【组队失败规则】
                                    • 同一轮中连续5次组队失败，坏人直接获胜
                                    • 每次组队失败后，队长按顺序轮换到下一位玩家
                                    
                                    【胜利条件】
                                    • 好人获胜：完成3次任务成功且梅林未被刺杀
                                    • 坏人获胜：完成3次任务失败或成功刺杀梅林或5次组队失败
                                    
                                    【特殊技能使用时机】
                                    • 梅林和派西维尔的技能在游戏开始时自动生效
                                    • 刺客的刺杀技能仅在好人完成3个任务后使用
                                """.trimIndent()
                            )
                        }
                        
                        item {
                            RuleSection(
                                title = "游戏策略与技巧",
                                icon = Icons.Default.Star,
                                content = """
                                    【好人策略】
                                    • 梅林：要巧妙地引导队伍，但不要暴露身份
                                    • 派西维尔：仔细观察梅林和莫甘娜的行为模式
                                    • 忠臣：通过投票模式和任务结果分析找出坏人
                                    
                                    【坏人策略】
                                    • 刺客：观察谁像梅林，为刺杀阶段做准备
                                    • 莫甘娜：模仿梅林的行为，迷惑派西维尔
                                    • 其他坏人：适度破坏任务，避免过早暴露
                                    
                                    【投票技巧】
                                    • 观察组队投票模式，找出可疑玩家
                                    • 注意任务执行结果与投票的关联性
                                    • 记住每个玩家的投票倾向
                                    
                                    【沟通技巧】
                                    • 合理表达怀疑，但不要过于激进
                                    • 注意保护关键角色（特别是梅林）
                                    • 通过逻辑推理建立可信度
                                """.trimIndent()
                            )
                        }
                        
                        item {
                            RuleSection(
                                title = "当前配置",
                                icon = Icons.Default.Star,
                                content = """
                                    玩家数量：${gameConfig.playerNum}人
                                    任务配置：${gameConfig.process.joinToString("-")}人
                                    角色分配：${gameConfig.role.joinToString(", ") { it.title }}
                                """.trimIndent()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "知道了",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 优化后的页面组件
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AwalongGamePageNew(viewmodel: MainViewmodel) {
    val gameConfig = viewmodel.awalongConfigState.value
    var showRulesDialog by remember { mutableStateOf(false) }

    val nameChange = { newName: String, no: Int ->
        viewmodel.handleAwalongGameIntent(AwalongIntent.ChangeNickName(newName, no))
    }
    
    val pages = mutableListOf<@Composable () -> Unit>(
        {
            PageContent(
                title = "第零日",
                gameConfig = gameConfig,
                bgColor = Color(0xff161823),
                currentPage = 1,
                totalPages = gameConfig.process.size + 1,
                showRulesDialog = { showRulesDialog = true }
            ) {
                PageDayZero(
                    viewmodel.awalongGameState.value.roleList,
                    viewmodel.awalongGameState.value.nickNameList,
                    nameChange
                )
            }
        },
    )
    
    gameConfig.process.forEachIndexed { index, taskNum ->
        pages.add({
            PageContent(
                title = "第${index + 1}日",
                gameConfig = gameConfig,
                bgColor = GoldanColorList[index % 5],
                currentPage = index + 2,
                totalPages = gameConfig.process.size + 1,
                showRulesDialog = { showRulesDialog = true }
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
        })
    }
    
    val pageState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    
    // 计算用户可以访问的最大页面索引
    // 用户可以访问所有已完成的页面 + 当前正在进行的页面
    val maxAccessiblePage = remember(viewmodel.awalongGameState.value.dayList) {
        // 找到第一个未完成任务的天数
        val firstIncompleteDay = viewmodel.awalongGameState.value.dayList.indexOfFirst { it.gamePhase != "TASK_RESULT" }
        if (firstIncompleteDay == -1) {
            // 所有任务都完成了，可以访问所有页面
            pages.size - 1
        } else {
            // 可以访问到当前未完成的任务（+1 因为包含第0页）
            firstIncompleteDay
        }
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

// 新的任务页面组件
@Composable
private fun PageDayTaskNew(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskNum: Int,
    dayEntity: AwalongGameDayEntity?,
    taskIndex: Int,
    gameConfig: AwalongConfig,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel,
    onCheck: (Map<Int, Int>, Int, Int) -> Unit
) {
    // 从 gameState 获取当前天的状态，确保状态持久化
    val currentDayState = gameState.dayList.getOrNull(taskIndex)
    
    // 使用 remember 来管理本地状态，但通过 ViewModel 同步
    var gamePhase by remember { 
        mutableStateOf(when (currentDayState?.gamePhase) {
            "TEAM_FORMATION" -> TaskPhase.TEAM_FORMATION
            "TASK_EXECUTION" -> TaskPhase.TASK_EXECUTION
            "TASK_RESULT" -> TaskPhase.TASK_RESULT
            else -> TaskPhase.TEAM_FORMATION
        })
    }
    
    // 监听 currentDayState 的变化，同步 gamePhase
    LaunchedEffect(currentDayState?.gamePhase) {
        currentDayState?.let { state ->
            gamePhase = when (state.gamePhase) {
                "TEAM_FORMATION" -> TaskPhase.TEAM_FORMATION
                "TASK_EXECUTION" -> TaskPhase.TASK_EXECUTION
                "TASK_RESULT" -> TaskPhase.TASK_RESULT
                else -> TaskPhase.TEAM_FORMATION
            }
        }
    }
    
    var result by remember { mutableStateOf(currentDayState?.taskResult ?: 0) }
    var taskPlayer by remember { mutableStateOf(currentDayState?.selectedTeam?.toMutableList() ?: mutableListOf<Int>()) }
    var teamVotes by remember { mutableStateOf(currentDayState?.teamVotes?.toMutableMap() ?: mutableMapOf<Int, Boolean>()) }
    var taskVotes by remember { mutableStateOf(currentDayState?.taskVotes?.toMutableMap() ?: mutableMapOf<Int, Boolean>()) }
    
    val currentCaptain = remember(taskIndex) {
        if (currentDayState?.currentCaptain != -1) {
            currentDayState?.currentCaptain ?: -1
        } else if (taskIndex == 0) {
            roleList.indices.random()
        } else {
            ((currentDayState?.captain ?: 0 - 1 + roleList.size) % roleList.size)
        }
    }

    val taskMap = remember {
        mutableMapOf<Int, Int>()
    }

    var showDialog by remember { mutableStateOf(-1) }
    
    // 保存状态的函数
    fun saveState() {
        val updatedDayEntity = AwalongGameDayEntity(
            day = taskIndex,
            mainTask = dayEntity?.mainTask ?: emptyMap(),
            taskResult = result,
            murderTask = dayEntity?.murderTask ?: -1,
            captain = dayEntity?.captain ?: -1,
            requiresTwoFailures = dayEntity?.requiresTwoFailures ?: false,
            morguseUsed = dayEntity?.morguseUsed ?: false,
            sirGalahadUsed = dayEntity?.sirGalahadUsed ?: false,
            plotCard = dayEntity?.plotCard,
            
            // 保存当前状态
            gamePhase = when (gamePhase) {
                TaskPhase.TEAM_FORMATION -> "TEAM_FORMATION"
                TaskPhase.TASK_EXECUTION -> "TASK_EXECUTION"
                TaskPhase.TASK_RESULT -> "TASK_RESULT"
            },
            teamVotes = teamVotes.toMap(),
            taskVotes = taskVotes.toMap(),
            selectedTeam = taskPlayer.toList(),
            currentCaptain = currentCaptain
        )
        
        // 通过 ViewModel 更新状态
        viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(updatedDayEntity))
    }
    
    // 监听状态变化并保存
    LaunchedEffect(gamePhase, taskPlayer, teamVotes, taskVotes, currentCaptain) {
        saveState()
    }
    
    Column {
        // 当前阶段指示器
        PhaseIndicator(currentPhase = gamePhase, taskResult = if (gamePhase == TaskPhase.TASK_RESULT) result else null)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (gamePhase) {
            TaskPhase.TEAM_FORMATION -> {
                TeamFormationPhase(
                    roleList = roleList,
                    nicknameList = nicknameList,
                    taskNum = taskNum,
                    taskPlayer = taskPlayer,
                    currentCaptain = currentCaptain,
                    onTeamComplete = { team ->
                        // 通过 ViewModel 更新状态
                        val updatedDayEntity = currentDayState?.copy(
                            selectedTeam = team,
                            gamePhase = "TASK_EXECUTION"
                        ) ?: AwalongGameDayEntity(
                            day = taskIndex,
                            mainTask = emptyMap(),
                            taskResult = 0,
                            murderTask = -1,
                            captain = currentCaptain,
                            selectedTeam = team,
                            gamePhase = "TASK_EXECUTION"
                        )
                        
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.UpdateDayState(updatedDayEntity)
                        )
                        
                        // 直接更新本地状态以确保UI立即响应
                        gamePhase = TaskPhase.TASK_EXECUTION
                    },
                    onTaskPlayerUpdate = { newTaskPlayer ->
                        taskPlayer = newTaskPlayer
                    }
                )
            }
            
            TaskPhase.TASK_EXECUTION -> {
                TaskExecutionPhase(
                    roleList = roleList,
                    nicknameList = nicknameList,
                    taskPlayer = taskPlayer,
                    taskVotes = taskVotes,
                    onExecutionComplete = { votes, success ->
                        taskVotes = votes.toMutableMap()
                        result = if (success) 1 else -1
                        gamePhase = TaskPhase.TASK_RESULT
                        
                        // 保存任务结果到dayEntity
                        val updatedDayEntity = currentDayState?.copy(
                            taskVotes = votes.toMap(),
                            taskResult = if (success) 1 else -1,
                            gamePhase = "TASK_RESULT"
                        ) ?: AwalongGameDayEntity(
                            day = taskIndex,
                            mainTask = taskPlayer.associateWith { if (success) 1 else -1 },
                            taskResult = if (success) 1 else -1,
                            murderTask = -1,
                            captain = currentCaptain,
                            taskVotes = votes.toMap(),
                            gamePhase = "TASK_RESULT"
                        )
                        
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.UpdateDayState(updatedDayEntity)
                        )
                        
                        // 保存任务结果
                        val finalTaskMap = mutableMapOf<Int, Int>()
                        taskPlayer.forEach { playerIndex ->
                            finalTaskMap[playerIndex] = if (success) 1 else -1
                        }
                        onCheck(finalTaskMap, result, currentCaptain)
                    },
                    onBackToTeamFormation = {
                        // 撤回到组队阶段
                        gamePhase = TaskPhase.TEAM_FORMATION
                        val updatedDayEntity = currentDayState?.copy(
                            gamePhase = "TEAM_FORMATION"
                        )
                        updatedDayEntity?.let {
                            viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(it))
                        }
                    }
                )
            }
            
            TaskPhase.TASK_RESULT -> {
                TaskResultPhase(
                    result = result,
                    gameState = gameState,
                    viewmodel = viewmodel,
                    taskIndex = taskIndex,
                    taskNum = taskNum,
                    onNextRound = {
                        // 进入下一轮或特殊技能阶段
                        // 这里可以添加湖中仙女等技能的触发逻辑
                    }
                )
            }
        }
    }
}

// 游戏阶段枚举
enum class TaskPhase {
    TEAM_FORMATION,    // 组队阶段
    TASK_EXECUTION,    // 任务执行阶段
    TASK_RESULT        // 任务结果阶段
}

@Composable
private fun PhaseIndicator(currentPhase: TaskPhase, taskResult: Int? = null) {
    val (containerColor, contentColor, phaseText) = when (currentPhase) {
        TaskPhase.TEAM_FORMATION -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            "队长组队"
        )
        TaskPhase.TASK_EXECUTION -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            "任务执行"
        )
        TaskPhase.TASK_RESULT -> {
            if (taskResult == 1) {
                Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primary,
                    "任务成功"
                )
            } else if (taskResult == -1) {
                Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.error,
                    "任务失败"
                )
            } else {
                Triple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    "任务结果"
                )
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前阶段：",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            
            Text(
                text = phaseText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun TeamFormationPhase(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskNum: Int,
    taskPlayer: MutableList<Int>,
    currentCaptain: Int,
    onTeamComplete: (List<Int>) -> Unit,
    onTaskPlayerUpdate: (MutableList<Int>) -> Unit
) {
    // 创建一个本地状态来触发重组
    var selectedTeam by remember { mutableStateOf(taskPlayer.toList()) }
    
    // 当外部 taskPlayer 变化时，同步到本地状态
    LaunchedEffect(taskPlayer) {
        selectedTeam = taskPlayer.toList()
    }
    Column {
        val captainName = if (currentCaptain >= 0 && currentCaptain < nicknameList.size) {
            nicknameList[currentCaptain]
        } else {
            "未确定"
        }
        
        Text(
            text = "队长（${captainName}）请选择 $taskNum 位玩家执行任务：",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(roleList.size) { playerIndex ->
                PlayerCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    isSelected = selectedTeam.contains(playerIndex),
                    isCaptain = playerIndex == currentCaptain,
                    onClick = {
                        val newTeam = if (selectedTeam.contains(playerIndex)) {
                            selectedTeam - playerIndex
                        } else if (selectedTeam.size < taskNum) {
                            selectedTeam + playerIndex
                        } else {
                            selectedTeam
                        }
                        selectedTeam = newTeam
                        
                        // 更新外部状态
                        onTaskPlayerUpdate(newTeam.toMutableList())
                    }
                )
            }
            
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (selectedTeam.size == taskNum) {
                            onTeamComplete(selectedTeam.toList())
                        }
                    },
                    enabled = selectedTeam.size == taskNum,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "确认组队 (${selectedTeam.size}/$taskNum)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamVotingPhase(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskPlayer: List<Int>,
    teamVotes: MutableMap<Int, Boolean>,
    onVotingComplete: (Map<Int, Boolean>, Boolean) -> Unit
) {
    var currentVoter by remember { mutableStateOf(0) }
    
    Column {
        Text(
            text = "组队投票阶段",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "任务队伍：${taskPlayer.joinToString(", ") { nicknameList[it] }}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (currentVoter < roleList.size) {
            VotingCard(
                voterName = nicknameList[currentVoter],
                voterRole = roleList[currentVoter],
                onVote = { approve ->
                    teamVotes[currentVoter] = approve
                    currentVoter++
                    
                    if (currentVoter >= roleList.size) {
                        val approvedVotes = teamVotes.values.count { it }
                        val totalVotes = teamVotes.size
                        val approved = approvedVotes > totalVotes / 2
                        onVotingComplete(teamVotes.toMap(), approved)
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskExecutionPhase(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskPlayer: List<Int>,
    taskVotes: MutableMap<Int, Boolean>,
    onExecutionComplete: (Map<Int, Boolean>, Boolean) -> Unit,
    onBackToTeamFormation: () -> Unit
) {
    val taskPlayerIndices = taskPlayer.filter { it < roleList.size }
    
    Column {
        Text(
            text = "任务执行阶段",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "请被选中的玩家点击自己的头像选择任务执行结果：",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Column {
            // 撤回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onBackToTeamFormation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("撤回组队")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
            items(taskPlayerIndices.size) { index ->
                val playerIndex = taskPlayerIndices[index]
                val hasVoted = taskVotes.containsKey(playerIndex)
                
                PlayerTaskCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    hasVoted = hasVoted,
                    voteResult = taskVotes[playerIndex],
                    onVote = { success ->
                        taskVotes[playerIndex] = success
                        
                        // 检查是否所有玩家都已投票
                        if (taskVotes.size == taskPlayerIndices.size) {
                            val successVotes = taskVotes.values.count { it }
                            val totalVotes = taskVotes.size
                            val allSuccess = successVotes == totalVotes
                            onExecutionComplete(taskVotes.toMap(), allSuccess)
                        }
                    }
                )
            }
            
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // 显示当前投票进度
                Text(
                    text = "投票进度：${taskVotes.size}/${taskPlayerIndices.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        }
    }
}

@Composable
private fun PlayerTaskCard(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    hasVoted: Boolean,
    voteResult: Boolean?,
    onVote: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasVoted -> if (voteResult == true) MaterialTheme.colorScheme.primaryContainer 
                          else MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasVoted) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (!hasVoted) showDialog = true }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 号码显示区域 - 与其他卡片保持一致的48dp大小，但按比例缩小适应aspectRatio
            Box(
                modifier = Modifier
                    .size(36.dp) // 略小于48dp以适应正方形布局
                    .background(
                        color = if (hasVoted) {
                            if (voteResult == true) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        } else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${playerIndex + 1}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nickname,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            if (hasVoted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (voteResult == true) "成功" else "失败",
                    fontSize = 9.sp,
                    color = if (voteResult == true) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击投票",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // 投票对话框
    if (showDialog) {
        TaskVoteDialog(
            playerName = nickname,
            playerRole = role,
            onDismiss = { showDialog = false },
            onVote = { success ->
                onVote(success)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TaskVoteDialog(
    playerName: String,
    playerRole: AwalongRole,
    onDismiss: () -> Unit,
    onVote: (Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$playerName，请选择任务执行结果：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onVote(false) },
                        enabled = playerRole.roleType == BAD_PERSON,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("任务失败")
                    }
                    
                    Button(
                        onClick = { onVote(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("任务成功")
                    }
                }
                
                if (playerRole.roleType == GOOD_PERSON) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "提示：作为好人阵营，你必须选择任务成功",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskResultPhase(
    result: Int,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel,
    taskIndex: Int,
    taskNum: Int,
    onNextRound: () -> Unit
) {
    var showAllResults by remember { mutableStateOf(false) }
    
    // 判断游戏是否结束
    val isGameComplete = remember(gameState.dayList) {
        val successCount = gameState.dayList.count { it.taskResult == 1 }
        val failureCount = gameState.dayList.count { it.taskResult == -1 }
        val totalRounds = gameState.dayList.size
        
        // 好人完成3次任务成功，或坏人完成2次任务失败，或已完成所有轮次
        successCount >= 3 || failureCount >= 2 || (totalRounds >= 5 && (successCount >= 3 || failureCount >= 2))
    }
    
    // 获取所有锁定的玩家
    val lockedPlayers = remember(taskIndex) {
        gameState.dayList.take(taskIndex + 1).flatMap { it.lockedPlayers }.toSet()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 操作按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("下一轮")
            }
            
            // 只有在游戏结束时才显示查看结果按钮
            if (isGameComplete) {
                Button(
                    onClick = { showAllResults = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看结果")
                }
            }
        }
        
        // 角色选择区域（显示锁定状态）
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(gameState.roleList.size) { playerIndex ->
                val isLocked = lockedPlayers.contains(playerIndex)
                PlayerCard(
                    playerIndex = playerIndex,
                    nickname = gameState.nickNameList[playerIndex],
                    role = gameState.roleList[playerIndex],
                    isSelected = false, // 任务完成后不显示选中状态
                    isCaptain = false,
                    isLocked = isLocked, // 添加锁定状态
                    onClick = { /* 锁定的玩家不可点击 */ }
                )
            }
        }
        
        // 显示所有结果对话框
        if (showAllResults) {
            AllResultsDialog(
                gameState = gameState,
                onDismiss = { showAllResults = false }
            )
        }
    }
}

@Composable
private fun TaskProgressBar(
    currentDay: Int,
    dayList: List<AwalongGameDayEntity>,
    gameConfig: AwalongConfig
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "任务进度：",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 进度条背景
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dayList.forEachIndexed { index, day ->
                    val taskNum = if (index < gameConfig.process.size) gameConfig.process[index] else 0
                    val isCompleted = day.taskResult != 0
                    val isSuccess = day.taskResult == 1
                    val isCurrent = index == currentDay
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    isCompleted && isSuccess -> MaterialTheme.colorScheme.primary
                                    isCompleted && !isSuccess -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Sharp.Check else Icons.Sharp.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp))

                            }
                            
                            Text(
                                text = "${taskNum}人",
                                color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    isSelected: Boolean,
    isCaptain: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    Card(
        onClick =  {
            if (!isLocked) onClick.invoke()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCaptain -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 号码显示区域 - 与RoleItem保持一致的48dp大小
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = when {
                                isLocked -> MaterialTheme.colorScheme.surfaceVariant
                                isCaptain -> MaterialTheme.colorScheme.secondary 
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${playerIndex + 1}号",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isLocked -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onPrimary
                        },
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nickname,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    if (isCaptain) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "队长",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (isLocked) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "已锁定",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "已锁定",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Sharp.CheckCircle,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun VotingCard(
    voterName: String,
    voterRole: AwalongRole,
    onVote: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$voterName 请投票",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "是否同意这个队伍配置？",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onVote(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("反对")
                }
                
                Button(
                    onClick = { onVote(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("同意")
                }
            }
        }
    }
}

@Composable
private fun TaskVotingCard(
    executorName: String,
    executorRole: AwalongRole,
    onVote: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$executorName 请执行任务",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "选择任务结果（此选择只有你自己能看到）：",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onVote(false) },
                    enabled = executorRole.roleType == BAD_PERSON,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("任务失败")
                }
                
                Button(
                    onClick = { onVote(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("任务成功")
                }
            }
            
            if (executorRole.roleType == GOOD_PERSON) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "作为好人，你只能选择任务成功",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}