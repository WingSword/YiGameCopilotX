package org.walks.gamecopilot.ui.page.game.localspy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.data.entity.WordGroupManager
import org.walks.gamecopilot.getWordMapBySelectedGroups
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.page.game.localspy.components.GameConfigurationSection
import org.walks.gamecopilot.ui.page.game.localspy.components.GameControlSection
import org.walks.gamecopilot.ui.page.game.localspy.components.GameStatusDisplay
import org.walks.gamecopilot.ui.page.game.localspy.components.GameHeaderView
import org.walks.gamecopilot.ui.page.game.localspy.components.WordLibrarySection
import org.walks.gamecopilot.ui.page.game.localspy.components.WordsDialog
import org.walks.gamecopilot.ui.page.game.localspy.game.GameGreetingView

/**
 * 本地卧底游戏页面
 * 处理玩家人数选择、卧底数量配置及游戏数据显示等功能
 * 
 * 游戏状态说明：
 * - gameTimeState == 0: 游戏未开始
 * - gameTimeState > 0: 游戏进行中
 * - showAllIdentities: 游戏结束状态（长按公布身份后触发）
 * 
 * @param viewmodel MainViewmodel - 游戏主视图模型，用于处理业务逻辑和数据存储
 * @param onBack 返回回调函数
 */
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
    
    // 游戏开始时震动反馈
    LaunchedEffect(gameTimeState) {
        if (gameTimeState > 0) {
            PlatformHelper.getInstance().vibrateLongMethod()
        }
    }
    
    // 获取全局选中的词组
    val selectedWordGroups = gameEntity.globalSelectedWordGroups
    // 获取当前选中词组的所有词汇
    val currentWords = remember(selectedWordGroups) {
        getWordMapBySelectedGroups(selectedWordGroups)
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp)) {
        // 顶部导航栏
        GameHeaderView(
            onBack = onBack,
            isWordLibraryExpanded = isWordLibraryExpanded,
            onToggleWordLibrary = { isWordLibraryExpanded = it },
            onShowWordsDialog = { showWordsDialog = true }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 游戏配置区域
        GameConfigurationSection(
            gameTimeState = gameTimeState,
            currentGame = currentGame,
            playerNum = playerNum,
            showNumberPicker = showNumberPicker,
            numberList = numberList,
            selectedWordGroups = selectedWordGroups,
            currentWords = currentWords,
            isWordLibraryExpanded = isWordLibraryExpanded,
            showWordsDialog = showWordsDialog,
            showAllIdentities = showAllIdentities,
            allPlayersViewed = allPlayersViewed,
            onNumberPickerChange = { showNumberPicker = it },
            onWordsDialogChange = { showWordsDialog = it },
            onWordLibraryToggle = { isWordLibraryExpanded = it },
            onShowAllIdentities = { showAllIdentities = it },
            onAllPlayersViewed = { allPlayersViewed = it },
            onGameIntent = { viewmodel.handleGameIntent(it) },
            onGameTimeStateChange = { gameTimeState = it }
        )
        
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
    WordsDialog(
        show = showWordsDialog,
        onDismiss = { showWordsDialog = false },
        currentWords = currentWords,
        currentGame = currentGame
    )
}