package org.walks.gamecopilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.awalong.AwalongConfig
import org.walks.gamecopilot.awalong.AwalongCustomConfig
import org.walks.gamecopilot.awalong.AwalongGameLogic
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.DefaultCustomConfig
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState
import org.walks.gamecopilot.data.LANState
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.data.WheelItem
import org.walks.gamecopilot.data.WsRoomDataEntity
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.http.RoomModule
import org.walks.gamecopilot.http.roomModule
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.lan.data.LANGameState
import org.walks.gamecopilot.lan.lanRoomManager
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_SETTING_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_LABEL_NAME_KEY
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.utils.DateTimeUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 应用主视图模型
 * 
 * 职责：
 * - 管理应用全局状态
 * - 处理游戏模式切换
 * - 管理房间状态和WebSocket通信
 * - 处理随机工具相关逻辑
 * - 管理阿瓦隆游戏状态
 * - 管理局域网联机状态
 * 
 * 架构说明：
 * - 使用StateFlow管理可观察状态
 * - 使用SharedFlow处理一次性事件（如导航）
 * - 采用MVI架构模式处理用户意图
 */
class MainViewmodel : ViewModel() {

    /**
     * 当前选择的游戏模式索引
     * 0: 谁是卧底, 1: 阿瓦隆, 2: 你画我猜, 3: 随机工具, 4: 大富翁
     */
    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode
    private val _operationMode = MutableStateFlow<Int>(0)
    val operationMode: StateFlow<Int> = _operationMode

    /** 主题模式：SYSTEM / LIGHT / DARK */
    private val _themeMode = MutableStateFlow(org.walks.gamecopilot.theme.ThemeMode.SYSTEM)
    val themeMode: StateFlow<org.walks.gamecopilot.theme.ThemeMode> = _themeMode

    fun setThemeMode(mode: org.walks.gamecopilot.theme.ThemeMode) {
        _themeMode.value = mode
    }

    /**
     * 游戏实体数据
     * 包含游戏相关的通用数据
     */
    private val _gameEntity = MutableStateFlow(GameEntity())
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    /**
     * 房间实体状态
     * 包含房间ID、用户列表、游戏状态等信息
     */
    private val _roomEntityState = MutableStateFlow(WsRoomDataEntity())
    val roomEntityState: StateFlow<WsRoomDataEntity> = _roomEntityState

    /**
     * 导航事件流
     * 用于发送一次性导航指令
     */
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvents = _navigationEvents.asSharedFlow()

    /**
     * 顶部提示状态
     * 用于显示临时提示信息
     */
    private val _topTipState: MutableSharedFlow<String?> = MutableSharedFlow()
    var topTipState = _topTipState.asSharedFlow()

    /**
     * 当前随机工具内容状态
     * 包含当前选择的随机配置列表
     */
    private val _currentRandomContentState = MutableStateFlow(RandomListEntity())
    val currentRandomContentState: StateFlow<RandomListEntity> = _currentRandomContentState

    /**
     * 转盘选项状态
     * 用于转盘随机工具
     */
    private val _wheelItemsState = MutableStateFlow(getDefaultWheelItems())
    val wheelItemsState: StateFlow<List<WheelItem>> = _wheelItemsState

    /**
     * 随机配置标签列表
     * 保存所有已创建的随机配置名称
     */
    private val _randomLabelsState = MutableStateFlow(listOf<String>())
    val randomLabelsState: StateFlow<List<String>> = _randomLabelsState

    /**
     * 添加随机配置对话框状态
     */
    private val _addRandomConfigDialogState = MutableSharedFlow<Boolean>()
    val addRandomConfigDialogState = _addRandomConfigDialogState.asSharedFlow()

    /**
     * 阿瓦隆游戏配置状态
     * 当前选择的游戏配置（如5人局、7人局等）
     */
    private val _awalongConfigState = MutableStateFlow<AwalongConfig>(
        AwalongConfig.Standard_5
    )
    val awalongConfigState: StateFlow<AwalongConfig> = _awalongConfigState

    /**
     * 阿瓦隆自定义配置状态
     * 用于自定义角色配置
     */
    private val _awalongCustomConfigState = MutableStateFlow<AwalongCustomConfig>(
        DefaultCustomConfig
    )
    val awalongCustomConfigState: StateFlow<AwalongCustomConfig> = _awalongCustomConfigState

    /**
     * 阿瓦隆游戏状态
     * 包含游戏进行中的所有状态信息
     */
    private val _awalongGameState = MutableStateFlow<AwalongGameState>(AwalongGameState())
    val awalongGameState: StateFlow<AwalongGameState> = _awalongGameState

    /**
     * 局域网联机状态
     * 包含房间发现、连接、游戏同步等状态
     */
    private val _lanState = MutableStateFlow(LANState())
    val lanState: StateFlow<LANState> = _lanState

    private var userId = ""

    init {
        initLANObservers()
        initDefaultRandomConfigs()
        
        roomModule.connectionState
            .onEach { state ->
                when (state) {
                    RoomModule.ConnectionState.CONNECTED -> GameLogger.debug("已连接")
                    RoomModule.ConnectionState.DISCONNECTED -> GameLogger.debug("已断开")
                    RoomModule.ConnectionState.CONNECTING -> GameLogger.debug("连接中")
                }
            }
            .launchIn(viewModelScope)

        roomModule.messages
            .onEach { rawMessage ->
                try {
                    when (val message = Json.decodeFromString<WsRoomDataEntity>(rawMessage)) {
                        is WsRoomDataEntity -> handleWsData(message)
                        else -> GameLogger.debug("未知消息类型")
                    }
                } catch (e: Exception) {
                    GameLogger.error("消息解析失败", e)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 处理房间相关意图
     * @param intent 房间意图（创建、加入、离开、开始游戏等）
     */
    fun handleRoomIntent(intent: GameRoomIntent) {
        when (intent) {
            is GameRoomIntent.RefreshRoomInfo -> {
            }

            is GameRoomIntent.CreateAGameRoom -> {
                enterGameRoom(intent.roomId, intent.roomKey, true)
            }

            is GameRoomIntent.JoinToAGameRoom -> {
                enterGameRoom(intent.roomId, intent.roomKey, false)
            }

            GameRoomIntent.LeaveGameRoom -> {
                viewModelScope.launch {
                    roomModule.leaveRoom(
                        roomEntityState.value.roomId,
                        roomEntityState.value.roomKey
                    )
                }
            }

            GameRoomIntent.StartGame -> {
                viewModelScope.launch {
                    roomModule.startGame(
                        roomEntityState.value.roomId,
                        roomEntityState.value.roomKey
                    )
                }
            }

            GameRoomIntent.DeleteGameRoom -> {
                viewModelScope.launch {
                    roomModule.deleteRoom(
                        roomEntityState.value.roomId,
                        roomEntityState.value.roomKey
                    )
                }
            }
        }
    }

    /**
     * 处理随机工具页面意图
     * @param intent 随机工具意图（刷新、添加、删除配置等）
     */
    @OptIn(ExperimentalTime::class)
    fun handleRandomPageIntent(intent: RandomPageIntent) {
        when (intent) {
            is RandomPageIntent.OnRefresh -> {
                val shuffledCards = with(currentRandomContentState.value) {
                    this.list.map { it.second }.optimizedShuffle()
                        .zip(this.list.map { it.first }.optimizedShuffle()) { front, back ->
                            RandomItem(second = front, first = back)
                        }
                }

                viewModelScope.launch {
                    _currentRandomContentState.emit(
                        currentRandomContentState.value.copy(
                            list = shuffledCards,
                            refreshTime = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                }

            }

            is RandomPageIntent.OnAddNewRandom -> {
                if (intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER)) {
                    return
                }
                try {
                    // 序列化卡片列表
                    val jsonCards =
                        Json.encodeToString(RandomListEntity.serializer(), intent.randomListEntity)
                    // 保存到 MMKV
                    MMKVUtils.apply {
                        put(MMKV_RANDOM_CARDS_SETTING_KEY + intent.randomListEntity.name, jsonCards)
                        putSet(
                            MMKV_RANDOM_LABEL_NAME_KEY,
                            getSet(MMKV_RANDOM_LABEL_NAME_KEY)
                                ?.plus(intent.randomListEntity.name)
                                ?: setOf(intent.randomListEntity.name)
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 添加错误处理

                }
            }

            is RandomPageIntent.OnEditRandomConfig -> {
                if (intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER)) {
                    return
                }
                try {
                    // 序列化卡片列表
                    val jsonCards =
                        Json.encodeToString(RandomListEntity.serializer(), intent.randomListEntity)
                    // 保存到 MMKV（使用相同的key，会覆盖原有配置）
                    MMKVUtils.apply {
                        put(MMKV_RANDOM_CARDS_SETTING_KEY + intent.randomListEntity.name, jsonCards)

                        // 更新名称列表（确保配置名称存在）
                        val currentNameSet = getSet(MMKV_RANDOM_LABEL_NAME_KEY) ?: setOf()
                        if (!currentNameSet.contains(intent.randomListEntity.name)) {
                            putSet(
                                MMKV_RANDOM_LABEL_NAME_KEY,
                                currentNameSet.plus(intent.randomListEntity.name)
                            )
                        }
                    }

                    // 如果编辑的是当前选中的配置，更新当前显示的内容
                    val currentConfigName = _currentRandomContentState.value.name
                    if (currentConfigName == intent.randomListEntity.name) {
                        _currentRandomContentState.value = intent.randomListEntity
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 添加错误处理
                }
            }

            is RandomPageIntent.OnChangeNewRandomLabel -> {
                viewModelScope.launch {
                    _randomLabelsState.emit(
                        MMKVUtils.getSet(MMKV_RANDOM_LABEL_NAME_KEY)?.toList() ?: emptyList()
                    )
                }
            }

            is RandomPageIntent.OnSelectLabel -> {
                randomLabelChange(intent.label)
            }

            is RandomPageIntent.DeleteRandomConfig -> {
                if (intent.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER)) {
                    return
                }
                randomLabelChange("")
                val list=randomLabelsState.value.minus(intent.name)
                _randomLabelsState.update {
                    mutableListOf()
                }
                _randomLabelsState.update {
                   list
                }

                try {
                    // 保存到 MMKV
                    MMKVUtils.apply {
                        remove(MMKV_RANDOM_CARDS_SETTING_KEY + intent.name)
                        putSet(
                            MMKV_RANDOM_LABEL_NAME_KEY,
                            getSet(MMKV_RANDOM_LABEL_NAME_KEY)
                                ?.minus(intent.name)?:setOf()

                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 添加错误处理

                }
            }
            RandomPageIntent.OnAddNewRandomDialogShow -> {
                viewModelScope.launch {
                    _addRandomConfigDialogState.emit(true)
                }

            }

            is RandomPageIntent.OnCancelLabel -> {
                randomLabelChange("")
            }

            RandomPageIntent.TriggerRandom -> {
                // 触发随机事件，增加refreshTime来触发洗牌动画
                _currentRandomContentState.update { current ->
                    current.copy(refreshTime = Clock.System.now().toEpochMilliseconds())
                }
            }

            RandomPageIntent.OnAddNewRandomDialogSave -> {
                // 保存当前配置（空实现保留，实际保存由 OnAddNewRandom 处理）
            }
        }
    }

    fun handleGameIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.SwitchOperationMode -> {
                _operationMode.value = intent.mode
            }
            is GameIntent.SwitchGameMode -> {
                _startedGameMode.value = intent.mode
            }
            
            is GameIntent.RefreshPlayerNumber -> {
                _gameEntity.update { current ->
                    current.copy(
                        currentGame = current.currentGame?.copy(
                            totalPlayerNumber = intent.num
                        ) ?: LocalSpyEntity(totalPlayerNumber = intent.num)
                    )
                }
            }
            
            is GameIntent.RefreshSpyNumber -> {
                _gameEntity.update { current ->
                    current.copy(
                        currentGame = current.currentGame?.copy(
                            spyNum = intent.spyNum,
                            blackNum = intent.blackNum
                        ) ?: LocalSpyEntity(spyNum = intent.spyNum, blackNum = intent.blackNum)
                    )
                }
            }
            
            is GameIntent.RefreshWordGroups -> {
                _gameEntity.update { current ->
                    current.copy(
                        globalSelectedWordGroups = intent.selectedGroups
                    )
                }
            }
            
            GameIntent.StartGame -> {
                startNewLocalSpyGame()
            }

            GameIntent.RefreshIdentities -> {
                refreshCurrentGameIdentities()
            }

            is GameIntent.UpdateNickname -> {
                updatePlayerNickname(intent.playerIndex, intent.newNickname)
            }
        }
    }
    
    private fun startNewLocalSpyGame() {
        _gameEntity.update { current ->
            // 保存当前游戏到历史记录
            if (current.currentGame != null) {
                saveCurrentGameToHistory(current.currentGame, current.gameCount)
            }

            // 创建新游戏，保留可能的昵称设置
            val previousNicknames = current.currentGame?.nicknames ?: emptyList()
            val playerNumber = current.currentGame?.totalPlayerNumber ?: 4
            
            val newGame = LocalSpyEntity(
                totalPlayerNumber = playerNumber,
                spyNum = current.currentGame?.spyNum ?: 1,
                blackNum = current.currentGame?.blackNum ?: 0,
                nicknames = if (previousNicknames.size == playerNumber) previousNicknames
                else List(playerNumber) { (it + 1).toString() } // 初始化默认昵称
            )
            
            // 刷新游戏词汇
            newGame.refreshGame(current.globalSelectedWordGroups)
            
            current.copy(
                currentGame = newGame,
                gameCount = current.gameCount + 1
            )
        }
    }

    private fun refreshCurrentGameIdentities() {
        _gameEntity.update { current ->
            current.currentGame?.let { game ->
                // 创建新的游戏实例，确保重新分配身份，但保留昵称
                val refreshedGame = LocalSpyEntity(
                    totalPlayerNumber = game.totalPlayerNumber,
                    spyNum = game.spyNum,
                    blackNum = game.blackNum,
                    nicknames = game.nicknames // 保留现有昵称
                ).apply {
                    refreshGame(current.globalSelectedWordGroups)
                }

                GameLogger.debug("重新分配身份完成，旧的卧底索引: ${game.spies}, 新的卧底索引: ${refreshedGame.spies}")
                GameLogger.debug("词汇也重新分配了，旧词汇: ${game.gameWord}/${game.spyWord}, 新词汇: ${refreshedGame.gameWord}/${refreshedGame.spyWord}")
                current.copy(
                    currentGame = refreshedGame
                )
            } ?: current
        }
    }

    private fun updatePlayerNickname(playerIndex: Int, newNickname: String) {
        _gameEntity.update { current ->
            current.currentGame?.let { game ->
                // 确保昵称列表长度足够
                val updatedNicknames = if (game.nicknames.size >= game.totalPlayerNumber) {
                    game.nicknames.toMutableList().apply {
                        set(playerIndex, newNickname.ifEmpty { (playerIndex + 1).toString() })
                    }
                } else {
                    // 如果昵称列表长度不够，创建新的列表
                    List(game.totalPlayerNumber) { index ->
                        if (index == playerIndex) {
                            newNickname.ifEmpty { (playerIndex + 1).toString() }
                        } else if (index < game.nicknames.size) {
                            game.nicknames[index]
                        } else {
                            (index + 1).toString()
                        }
                    }
                }

                GameLogger.debug("更新昵称: 玩家${playerIndex + 1} -> $newNickname")
                current.copy(
                    currentGame = game.copy(nicknames = updatedNicknames)
                )
            } ?: current
        }
    }
    
    private fun saveCurrentGameToHistory(game: LocalSpyEntity, currentCount: Int) {
        try {
            val historyKey = "local_spy_game_history_${currentCount}"
            val gameJson = Json.encodeToString(LocalSpyEntity.serializer(), game)
            MMKVUtils.put(historyKey, gameJson)
        } catch (e: Exception) {
            GameLogger.error("保存游戏历史失败", e)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun handleAwalongGameIntent(intent: AwalongIntent) {
        when (intent) {
            is AwalongIntent.StartGame -> {
                _awalongConfigState.update {
                    intent.gameConfig
                }
                resetAwalongGameState()

            }

            is AwalongIntent.StartCustomGame -> {
                _awalongCustomConfigState.update {
                    intent.customConfig
                }
                resetAwalongGameStateWithCustomConfig()
            }

            AwalongIntent.RestartGame -> {
                resetAwalongGameState()
            }

            is AwalongIntent.ChangeNickName -> {
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        nickNameList = it.nickNameList.toMutableList().apply {
                            this[intent.sn] = intent.nickName
                        }
                    )
                }
            }

            is AwalongIntent.CheckTask -> {
                viewModelScope.launch {
                    _awalongGameState.update {
                        val newDayList = it.dayList.toMutableList().apply {
                            if (find { dayEntity -> dayEntity.day == intent.task.day } == null) {
                                add(intent.task)
                            } else {
                                set(
                                    indexOfFirst { dayEntity -> dayEntity.day == intent.task.day },
                                    intent.task
                                )
                            }
                        }
                        val completedCount =
                            newDayList.count { dayEntity -> dayEntity.taskResult != 0 }
                        val firstCaptain = newDayList.getOrNull(0)?.captain ?: -1
                        val assignLady =
                            it.useLadyOfLake && completedCount >= 2 && it.ladyOfLakeHolder == null && firstCaptain >= 0 && it.roleList.isNotEmpty()
                        val initialHolder =
                            if (assignLady) (firstCaptain + 1) % it.roleList.size else null
                        it.copy(
                            playTime = Clock.System.now().toEpochMilliseconds(),
                            dayList = newDayList,
                            ladyOfLakeHolder = if (assignLady) initialHolder else it.ladyOfLakeHolder,
                            ladyOfLakeHoldersHistory = if (assignLady) setOf(initialHolder!!) else it.ladyOfLakeHoldersHistory
                        )
                    }
                }
            }

            is AwalongIntent.LadyOfLakeCheck -> {
                // 湖中仙女头衔：使用后传给被查验的玩家
                _awalongGameState.update {
                    val holder = it.ladyOfLakeHolder ?: return@update it
                    val newHolder = intent.playerIndex
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        ladyOfLakeHolder = newHolder,
                        ladyOfLakeHoldersHistory = it.ladyOfLakeHoldersHistory + newHolder,
                        ladyOfLakeUsedForTaskIndex = intent.taskIndex,
                        ladyOfLakeChecked = newHolder
                    )
                }
            }

            is AwalongIntent.MorguseConvertSuccessToFailure -> {
                // 莫高斯将成功卡转为失败卡
                _awalongGameState.update {
                    val updatedDayList = it.dayList.toMutableList()
                    val targetDay = updatedDayList.getOrNull(intent.taskIndex)
                    if (targetDay != null) {
                        updatedDayList[intent.taskIndex] = targetDay.copy(
                            morguseUsed = true
                        )
                    }
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        dayList = updatedDayList,
                        morguseUsed = true
                    )
                }
            }
            
            is AwalongIntent.ShapeshifterCopy -> {
                // 变形者复制角色
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        shapeshifterTarget = intent.targetRole
                    )
                }
            }
            
            is AwalongIntent.LancelotConvert -> {
                // 兰斯洛特转换阵营
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        lancolotConverted = true
                    )
                }
            }
            
            is AwalongIntent.DrawPlotCard -> {
                // 抽取情节卡（预留接口）
                viewModelScope.launch {
                    _topTipState.emit("抽取了情节卡")
                }
            }
            
            is AwalongIntent.SelectCaptain -> {
                // 选择队长
                _awalongGameState.update {
                    val updatedDayList = it.dayList.toMutableList()
                    if (updatedDayList.isNotEmpty()) {
                        updatedDayList[0] = updatedDayList[0].copy(captain = intent.captainIndex)
                    }
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        dayList = updatedDayList
                    )
                }
            }
            
            is AwalongIntent.FormTeam -> {
                // 组队
                _awalongGameState.update {
                    val updatedDayList = it.dayList.toMutableList()
                    if (intent.taskIndex < updatedDayList.size) {
                        val currentDay = updatedDayList[intent.taskIndex]
                        updatedDayList[intent.taskIndex] = currentDay.copy(
                            mainTask = intent.teamMembers.associateWith { 1 }
                        )
                    }
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        dayList = updatedDayList
                    )
                }
            }
            
            is AwalongIntent.VoteTeam -> {
                // 投票（简化处理）
                viewModelScope.launch {
                    _topTipState.emit("投票${if (intent.vote) "通过" else "拒绝"}")
                }
            }
            
            is AwalongIntent.ExecuteTask -> {
                // 执行任务
                _awalongGameState.update {
                    val updatedDayList = it.dayList.toMutableList()
                    if (intent.taskIndex < updatedDayList.size) {
                        val currentDay = updatedDayList[intent.taskIndex]
                        updatedDayList[intent.taskIndex] = currentDay.copy(
                            taskResult = if (intent.success) 1 else -1
                        )
                    }
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        dayList = updatedDayList
                    )
                }
            }
            
            is AwalongIntent.UpdateDayState -> {
                // 更新天的状态
                _awalongGameState.update {
                    val updatedDayList = it.dayList.toMutableList()
                    val existingIndex = updatedDayList.indexOfFirst { it.day == intent.dayState.day }
                    
                    if (existingIndex >= 0) {
                        updatedDayList[existingIndex] = intent.dayState
                    } else {
                        updatedDayList.add(intent.dayState)
                        updatedDayList.sortBy { it.day }
                    }
                    
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        dayList = updatedDayList
                    )
                }
            }
            
            is AwalongIntent.UpdateCurrentPage -> {
                // 更新当前页面索引
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        currentPage = intent.pageIndex
                    )
                }
            }
            
            is AwalongIntent.Assassinate -> {
                // 刺杀
                val targetRole = _awalongGameState.value.roleList.getOrNull(intent.targetIndex)
                val isMerlinKilled = targetRole == AwalongRole.MEILING
                
                viewModelScope.launch {
                    _topTipState.emit(
                        if (isMerlinKilled) "刺杀成功！红方获胜！" else "刺杀失败！蓝方获胜！"
                    )
                }
            }
            


            is AwalongIntent.CheckGameEnd -> {
                // 检查游戏结束
                val result = AwalongGameLogic.checkGameEnd(_awalongGameState.value)
                result?.let {
                    viewModelScope.launch {
                        _topTipState.emit("${it.winner}获胜：${it.reason}")
                    }
                }
            }

            is AwalongIntent.UpdateAssassinationResult -> {
                // 更新刺客刺杀结果
                _awalongGameState.update { currentState ->
                    currentState.copy(assassinationResult = intent.success)
                }
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    private fun resetAwalongGameState() {
        val currentState = _awalongGameState.value
        val customConfig = awalongCustomConfigState.value
        val standardConfig = awalongConfigState.value

        // 检查是否使用自定义配置
        val isUsingCustomConfig = currentState.roleList.size == customConfig.totalPlayers

        if (isUsingCustomConfig) {
            // 使用自定义配置重置
            val roleList = customConfig.generateRoleList()
            _awalongGameState.update {
                AwalongGameState(
                    playTime = Clock.System.now().toEpochMilliseconds(),
                    roleList = roleList.optimizedShuffle().toMutableList(),
                    dayList = mutableListOf<AwalongGameDayEntity>().apply {
                        customConfig.process.forEachIndexed { index, taskSize ->
                            // 根据阿瓦隆规则判断是否需要2张失败卡
                            val requiresTwoFailures = AwalongGameLogic.requiresTwoFailures(
                                index,
                                customConfig.totalPlayers
                            )
                            this.add(
                                AwalongGameDayEntity(
                                    day = index,
                                    captain = roleList.indices.random(),
                                    requiresTwoFailures = requiresTwoFailures
                                )
                            )
                        }
                    },
                    // 保留当前的昵称列表，而不是重置为默认值
                    nickNameList = currentState.nickNameList,
                    useLadyOfLake = customConfig.useLadyOfLake,
                    ladyOfLakeHolder = null,
                    ladyOfLakeHoldersHistory = emptySet(),
                    ladyOfLakeUsedForTaskIndex = null,
                    ladyOfLakeChecked = null,
                    morguseUsed = false,
                    lancolotConverted = false,
                    shapeshifterTarget = null,
                    assassinationResult = null
                )
            }
        } else {
            _awalongGameState.update {
                AwalongGameState(
                    playTime = Clock.System.now().toEpochMilliseconds(),
                    roleList = standardConfig.role.optimizedShuffle().toMutableList(),
                    dayList = mutableListOf<AwalongGameDayEntity>().apply {
                        standardConfig.process.forEachIndexed { index, taskSize ->
                            val requiresTwoFailures = AwalongGameLogic.requiresTwoFailures(
                                index,
                                standardConfig.playerNum
                            )
                            this.add(
                                AwalongGameDayEntity(
                                    day = index,
                                    captain = standardConfig.role.indices.random(),
                                    requiresTwoFailures = requiresTwoFailures
                                )
                            )
                        }
                    },
                    nickNameList = currentState.nickNameList,
                    useLadyOfLake = false,
                    ladyOfLakeHolder = null,
                    ladyOfLakeHoldersHistory = emptySet(),
                    ladyOfLakeUsedForTaskIndex = null,
                    ladyOfLakeChecked = null,
                    morguseUsed = false,
                    lancolotConverted = false,
                    shapeshifterTarget = null,
                    assassinationResult = null
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun resetAwalongGameStateWithCustomConfig() {
        val customConfig = awalongCustomConfigState.value
        val roleList = customConfig.generateRoleList()
        _awalongGameState.update {
            AwalongGameState(
                playTime = Clock.System.now().toEpochMilliseconds(),
                roleList = roleList.optimizedShuffle().toMutableList(),
                dayList = mutableListOf<AwalongGameDayEntity>().apply {
                    customConfig.process.forEachIndexed { index, taskSize ->
                        // 根据阿瓦隆规则判断是否需要2张失败卡
                        val requiresTwoFailures =
                            AwalongGameLogic.requiresTwoFailures(index, customConfig.totalPlayers)
                        this.add(
                            AwalongGameDayEntity(
                                day = index,
                                captain = roleList.indices.random(),
                                requiresTwoFailures = requiresTwoFailures
                            )
                        )
                    }
                },
                nickNameList = (1..customConfig.totalPlayers).map { it.toString() }
                    .toMutableList(),
                useLadyOfLake = customConfig.useLadyOfLake,
                ladyOfLakeHolder = null,
                ladyOfLakeHoldersHistory = emptySet(),
                ladyOfLakeUsedForTaskIndex = null,
                ladyOfLakeChecked = null,
                morguseUsed = false,
                lancolotConverted = false,
                shapeshifterTarget = null,
                assassinationResult = null
            )
        }
    }

    private fun randomLabelChange(selectedLabel: String) {
        if (selectedLabel.isEmpty()) {
            viewModelScope.launch {
                _currentRandomContentState.emit(RandomListEntity())
            }
            return
        }
        try {
            val jsonCard = Json.decodeFromString<RandomListEntity>(
                MMKVUtils.getString(
                    MMKV_RANDOM_CARDS_SETTING_KEY + selectedLabel,
                    ""
                )
            )
            viewModelScope.launch {
                _currentRandomContentState.emit(jsonCard)
            }
        } catch (e: Exception) {
            // 如果发生异常，则使用默认值
        }
    }

    private suspend fun emitNavigationEvent(event: NavigationEvent) {
        _navigationEvents.emit(event)
    }


    // 连接管理
    fun connectToServer() {
        viewModelScope.launch {
            try {
                roomModule.connect()
            } catch (e: Exception) {
            }
        }
    }

    private fun enterGameRoom(roomId: String, roomKey: String, asOwner: Boolean = false) {
        viewModelScope.launch {
            emitNavigationEvent(NavigationEvent.NavigateTo("room"))
        }
        _roomEntityState.update {
            it.copy(
                roomId = roomId,
                roomKey = roomKey,
                isRoomOwner = asOwner
            )
        }
        viewModelScope.launch {
            // 串行执行连接和创建房间
            val createResult = withContext(Dispatchers.Default) {
                // 先连接
                val connected = roomModule.connect()
                if (!connected) return@withContext null

                if (asOwner) {
                    roomModule.createRoom(roomId, roomKey)
                } else {
                    roomModule.joinRoom(roomId, roomKey)
                }
            }

            // 处理创建结果
            createResult?.let {
                GameLogger.debug("房间创建成功: $roomId")
            } ?: run {
                GameLogger.error("房间创建失败")
                clearRoomState()
                emitNavigationEvent(NavigationEvent.NavigateTo(route = NaviRoute.HOME.route))
            }
        }
    }

    // 处理业务消息
    private fun handleWsData(data: WsRoomDataEntity) {
        GameLogger.debug(data.toString())
        _roomEntityState.update {
            data.copy(
                updateTime = DateTimeUtils.getTimeNow(),
                roomFinished = 1,
                startedGameMode = 1,
                isRoomOwner = it.isRoomOwner,
                roomKey = it.roomKey
            )
        }
    }


    private fun clearRoomState() = _roomEntityState.update {
        it.copy(
            roomId = "",
            roomKey = "",
            roomFinished = 0,
            index = "",
            usersNumber = 0,
            startedGameMode = startedGameMode.value
        )
    }




    // 在 common 代码中调用
    fun vibrateLong() = PlatformHelper.getInstance().vibrateLongMethod()

    fun vibrite() {
        PlatformHelper.getInstance().vibrateMethod()
    }

    /**
     * 初始化预置随机配置
     */
    private fun initDefaultRandomConfigs() {
        viewModelScope.launch {
            try {
                // 获取当前已保存的配置列表
                val existingLabels =
                    MMKVUtils.getSet(MMKV_RANDOM_LABEL_NAME_KEY)?.toMutableSet() ?: mutableSetOf()

                // 预置配置名称
                val fingerConfigName = RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME
                val diceConfigName = RANDOM_PAGE_CONFIG_CATE_DICE + "六面骰子"
                val coinConfigName = RANDOM_PAGE_CONFIG_CATE_COIN + "硬币"
                val wheelConfigName = RANDOM_PAGE_CONFIG_CATE_WHEEL + "今天吃啥"

                if (!existingLabels.contains(fingerConfigName)) {
                    val fingerConfig = RandomListEntity(
                        name = fingerConfigName,
                        list = emptyList()
                    )
                    val fingerJson =
                        Json.encodeToString(RandomListEntity.serializer(), fingerConfig)
                    MMKVUtils.put(MMKV_RANDOM_CARDS_SETTING_KEY + fingerConfig.name, fingerJson)
                    existingLabels.add(fingerConfig.name)
                }

                // 检查并添加预置六面骰子
                if (!existingLabels.contains(diceConfigName)) {
                    val diceConfig = RandomListEntity(
                        name = diceConfigName,
                        list = listOf(
                            RandomItem(first = "1", second = "6")
                        )
                    )

                    // 保存骰子配置数据
                    val diceJson = Json.encodeToString(RandomListEntity.serializer(), diceConfig)
                    MMKVUtils.put(MMKV_RANDOM_CARDS_SETTING_KEY + diceConfig.name, diceJson)
                    existingLabels.add(diceConfig.name)
                }

                // 检查并添加预置硬币
                if (!existingLabels.contains(coinConfigName)) {
                    val coinConfig = RandomListEntity(
                        name = coinConfigName,
                        list = listOf(
                            RandomItem(first = "正面", second = "反面")
                        )
                    )

                    // 保存硬币配置数据
                    val coinJson = Json.encodeToString(RandomListEntity.serializer(), coinConfig)
                    MMKVUtils.put(MMKV_RANDOM_CARDS_SETTING_KEY + coinConfig.name, coinJson)
                    existingLabels.add(coinConfig.name)
                }

                // 检查并添加"今天吃啥"转盘预设
                if (!existingLabels.contains(wheelConfigName)) {
                    val wheelConfig = RandomListEntity(
                        name = wheelConfigName,
                        list = listOf(
                            RandomItem(first = "火锅", second = "25"),
                            RandomItem(first = "烧烤", second = "20"),
                            RandomItem(first = "日料", second = "15"),
                            RandomItem(first = "中餐", second = "15"),
                            RandomItem(first = "西餐", second = "10"),
                            RandomItem(first = "快餐", second = "10"),
                            RandomItem(first = "外卖", second = "5")
                        )
                    )

                    // 保存转盘配置数据
                    val wheelJson = Json.encodeToString(RandomListEntity.serializer(), wheelConfig)
                    MMKVUtils.put(MMKV_RANDOM_CARDS_SETTING_KEY + wheelConfig.name, wheelJson)
                    existingLabels.add(wheelConfig.name)
                }

                // 保存更新后的配置列表
                MMKVUtils.putSet(MMKV_RANDOM_LABEL_NAME_KEY, existingLabels)

                // 更新状态
                _randomLabelsState.value = existingLabels.toList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新转盘选项
     */
    fun updateWheelItems(items: List<WheelItem>) {
        _wheelItemsState.value = items

        // 获取当前选中的转盘配置名称
        val currentConfig = _currentRandomContentState.value.name
        if (currentConfig.isNotEmpty()) {
            // 将 WheelItem 列表转换为 RandomItem 列表并保存
            val randomItems = items.mapIndexed { index, wheelItem ->
                RandomItem(
                    id = index,
                    first = wheelItem.text,
                    second = wheelItem.weight.toString()
                )
            }

            // 保存到持久化存储
            val configEntity = RandomListEntity(
                name = currentConfig,
                list = randomItems
            )

            try {
                val json = Json.encodeToString(RandomListEntity.serializer(), configEntity)
                MMKVUtils.put(MMKV_RANDOM_CARDS_SETTING_KEY + currentConfig, json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initLANObservers() {
        lanRoomManager.discoveredRooms
            .onEach { rooms ->
                _lanState.update { it.copy(discoveredRooms = rooms) }
            }
            .launchIn(viewModelScope)

        lanRoomManager.currentRoom
            .onEach { room ->
                _lanState.update { it.copy(currentRoom = room) }
            }
            .launchIn(viewModelScope)

        lanRoomManager.connectionState
            .onEach { state ->
                _lanState.update { it.copy(connectionState = state) }
            }
            .launchIn(viewModelScope)

        lanRoomManager.players
            .onEach { players ->
                _lanState.update { it.copy(players = players) }
            }
            .launchIn(viewModelScope)

        lanRoomManager.isHost
            .onEach { isHost ->
                _lanState.update { it.copy(isHost = isHost) }
            }
            .launchIn(viewModelScope)

        lanRoomManager.errors
            .onEach { error ->
                _lanState.update { it.copy(error = error.message) }
                _topTipState.emit("错误: ${error.message}")
            }
            .launchIn(viewModelScope)

        lanRoomManager.gameStateUpdates
            .onEach { gameState ->
                handleLANGameStateUpdate(gameState)
            }
            .launchIn(viewModelScope)
    }

    fun handleLANIntent(intent: LANIntent) {
        when (intent) {
            is LANIntent.SetPreferredGameType -> {
                _lanState.update { it.copy(preferredGameType = intent.gameType) }
            }

            is LANIntent.StartDiscovery -> {
                lanRoomManager.startDiscovery(intent.gameType)
                _lanState.update { it.copy(isDiscovering = true) }
            }

            is LANIntent.StopDiscovery -> {
                lanRoomManager.stopDiscovery()
                _lanState.update { it.copy(isDiscovering = false) }
            }

            is LANIntent.ClearDiscoveredRooms -> {
                lanRoomManager.clearDiscoveredRooms()
            }

            is LANIntent.CreateRoom -> {
                lanRoomManager.createRoom(
                    roomName = intent.roomName,
                    hostName = intent.hostName,
                    gameType = intent.gameType,
                    maxPlayers = intent.maxPlayers,
                    password = intent.password
                )
            }

            is LANIntent.JoinRoom -> {
                lanRoomManager.joinRoom(
                    roomInfo = intent.roomInfo,
                    playerName = intent.playerName,
                    password = intent.password
                )
            }

            is LANIntent.Disconnect -> {
                lanRoomManager.disconnect()
            }

            is LANIntent.StartGame -> {
                lanRoomManager.startGame()
            }

            is LANIntent.EndGame -> {
                lanRoomManager.endGame()
            }

            is LANIntent.SyncGameState -> {
                lanRoomManager.syncGameState(intent.gameState)
            }

            is LANIntent.SendGameAction -> {
                lanRoomManager.sendGameAction(intent.action, intent.data)
            }

            is LANIntent.KickPlayer -> {
                lanRoomManager.kickPlayer(intent.playerId, intent.reason)
            }
        }
    }

    private fun handleLANGameStateUpdate(gameState: LANGameState) {
        viewModelScope.launch {
            try {
                when (gameState.gameType) {
                    org.walks.gamecopilot.lan.data.GameType.LOCAL_SPY -> {
                        val localSpyEntity =
                            Json.decodeFromString<LocalSpyEntity>(gameState.rawData)
                        _gameEntity.update { it.copy(currentGame = localSpyEntity) }
                    }

                    org.walks.gamecopilot.lan.data.GameType.AWALONG -> {
                        val awalongState =
                            Json.decodeFromString<AwalongGameState>(gameState.rawData)
                        _awalongGameState.value = awalongState
                    }

                    org.walks.gamecopilot.lan.data.GameType.DRAW_GUESS -> {
                        GameLogger.debug("收到你画我猜游戏状态更新")
                    }

                    org.walks.gamecopilot.lan.data.GameType.HUNT_TOWN -> {
                        GameLogger.debug("收到猎巫镇房间状态更新")
                    }

                    org.walks.gamecopilot.lan.data.GameType.RANDOM_TOOLS -> {
                        GameLogger.debug("收到随机工具状态更新")
                    }

                    org.walks.gamecopilot.lan.data.GameType.MONOPOLY -> {
                        GameLogger.debug("收到大富翁状态更新")
                    }

                    org.walks.gamecopilot.lan.data.GameType.ONE_NIGHT_WEREWOLF -> {
                        GameLogger.debug("收到一夜终极狼人状态更新")
                    }

                    else -> {
                        GameLogger.debug("收到未知类型游戏状态: ${gameState.gameType}")
                    }
                }
            } catch (e: Exception) {
                GameLogger.error("解析游戏状态失败", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        lanRoomManager.dispose()
    }
}

/**
 * 获取默认的转盘选项
 */
private fun getDefaultWheelItems(): List<WheelItem> {
    return listOf(
        WheelItem("1", "选项1", WheelItem.DEFAULT_COLORS[0]),
        WheelItem("2", "选项2", WheelItem.DEFAULT_COLORS[1]),
        WheelItem("3", "选项3", WheelItem.DEFAULT_COLORS[2]),
        WheelItem("4", "选项4", WheelItem.DEFAULT_COLORS[3]),
        WheelItem("5", "选项5", WheelItem.DEFAULT_COLORS[4]),
        WheelItem("6", "选项6", WheelItem.DEFAULT_COLORS[5])
    )
}