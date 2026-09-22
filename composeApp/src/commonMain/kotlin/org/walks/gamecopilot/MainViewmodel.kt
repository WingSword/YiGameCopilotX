package org.walks.gamecopilot

import org.walks.gamecopilot.distribution.AppDistribution

import org.walks.gamecopilot.theme.RandomToolRules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import org.walks.gamecopilot.data.AnswerBookData
import org.walks.gamecopilot.data.LANState
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.data.WheelItem
import org.walks.gamecopilot.data.WsRoomDataEntity
import org.walks.gamecopilot.data.entity.AnswerBookState
import org.walks.gamecopilot.data.entity.AnswerBookPhase
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.http.RoomModule
import org.walks.gamecopilot.http.roomModule
import org.walks.gamecopilot.intent.AiIntent
import org.walks.gamecopilot.intent.AnswerBookIntent
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.lan.data.LANGameState
import org.walks.gamecopilot.lan.lanRoomManager
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.mmkv.MMKV_AI_API_KEY
import org.walks.gamecopilot.mmkv.MMKV_AI_BASE_URL
import org.walks.gamecopilot.mmkv.MMKV_AI_ENABLED_KEY
import org.walks.gamecopilot.mmkv.MMKV_AI_PROVIDER_KEY
import org.walks.gamecopilot.mmkv.MMKV_AI_STYLE_KEY
import org.walks.gamecopilot.mmkv.MMKV_AI_TIMEOUT_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_SETTING_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_DEFAULTS_INITIALIZED_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_LABEL_NAME_KEY
import org.walks.gamecopilot.mmkv.MMKV_THEME_MODE_KEY
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.service.ai.AiConfig
import org.walks.gamecopilot.service.ai.AiProvider
import org.walks.gamecopilot.service.ai.AiServiceFactory
import org.walks.gamecopilot.service.ai.AiStyle
import org.walks.gamecopilot.service.ai.prompts.GamePromptTemplates
import org.walks.gamecopilot.utils.DateTimeUtils
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class RandomToolResult(
    val value: String,
    val isPrimarySide: Boolean? = null
)

data class RandomToolOutcome(
    val configName: String = "",
    val rollId: Long = 0L,
    val results: List<RandomToolResult> = emptyList()
)

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
    private val _themeMode = MutableStateFlow(
        org.walks.gamecopilot.theme.ThemeMode.values().firstOrNull {
            it.name == MMKVUtils.getString(MMKV_THEME_MODE_KEY, "")
        } ?: org.walks.gamecopilot.theme.ThemeMode.SYSTEM
    )
    val themeMode: StateFlow<org.walks.gamecopilot.theme.ThemeMode> = _themeMode

    fun setThemeMode(mode: org.walks.gamecopilot.theme.ThemeMode) {
        _themeMode.value = mode
        MMKVUtils.put(MMKV_THEME_MODE_KEY, mode.name)
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
    private var randomToolsOpened = false
    fun openRandomTools() {
        if (!randomToolsOpened) {
            randomToolsOpened = true
            randomLabelChange(RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK + "答案之书")
        }
    }

    private val _currentRandomContentState = MutableStateFlow(RandomListEntity())
    val currentRandomContentState: StateFlow<RandomListEntity> = _currentRandomContentState

    /** 骰子/硬币的本次真实结果；动画只负责呈现该状态。 */
    private val _randomToolOutcomeState = MutableStateFlow(RandomToolOutcome())
    val randomToolOutcomeState: StateFlow<RandomToolOutcome> = _randomToolOutcomeState

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

    /**
     * 答案之书状态
     * 包含当前问题、答案、翻书动画进度等
     */
    private val _answerBookState = MutableStateFlow(AnswerBookState())
    val answerBookState: StateFlow<AnswerBookState> = _answerBookState

    /**
     * 一夜狼人入口页选定的开局配置
     * 游戏页直接消费该配置，避免进入游戏后重复出现配置界面。
     */
    private val _oneNightWerewolfPlayerCount = MutableStateFlow(5)
    val oneNightWerewolfPlayerCount: StateFlow<Int> = _oneNightWerewolfPlayerCount
    private val _oneNightWerewolfNicknames = MutableStateFlow(List(5) { "玩家${it + 1}" })
    val oneNightWerewolfNicknames: StateFlow<List<String>> = _oneNightWerewolfNicknames

    /**
     * AI 助手配置状态
     * 包含 AI 提供商、API Key、风格等配置
     */
    private val _aiConfig = MutableStateFlow(AiConfig())
    val aiConfig: StateFlow<AiConfig> = _aiConfig

    /**
     * AI 消息内容状态
     * 当前 AI 回复的文本内容
     */
    private val _aiMessage = MutableStateFlow("")
    val aiMessage: StateFlow<String> = _aiMessage

    /**
     * AI 加载中状态
     * 标识当前是否正在等待 AI 回复
     */
    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi

    private var userId = ""

    fun prepareOneNightWerewolfGame(playerCount: Int, nicknames: List<String>) {
        val safeCount = playerCount.coerceIn(3, 10)
        _oneNightWerewolfPlayerCount.value = safeCount
        _oneNightWerewolfNicknames.value = List(safeCount) { index ->
            nicknames.getOrNull(index)?.takeIf { it.isNotBlank() } ?: "玩家${index + 1}"
        }
    }

    init {
        initLANObservers()
        initDefaultRandomConfigs()
        loadAiConfig()
        
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
                    val message = Json.decodeFromString<WsRoomDataEntity>(rawMessage)
                    handleWsData(message)
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
        if (!AppDistribution.roomsEnabled) return
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
                val current = _currentRandomContentState.value
                val refreshedItems = if (
                    current.name.startsWith(RANDOM_PAGE_CONFIG_CATE_CARD)
                ) {
                    current.list.optimizedShuffle()
                } else {
                    current.list
                }
                publishRandomOutcome(
                    current.copy(
                        list = refreshedItems,
                        refreshTime = current.refreshTime + 1L
                    )
                )
            }

            is RandomPageIntent.OnAddNewRandom -> {
                if (intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER) ||
                    intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK)
                ) {
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
                    GameLogger.error("保存随机配置失败: ${intent.randomListEntity.name}", e)
                }
            }

            is RandomPageIntent.OnEditRandomConfig -> {
                if (intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER) ||
                    intent.randomListEntity.name.startsWith(RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK)
                ) {
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
                        _randomToolOutcomeState.value = RandomToolOutcome(
                            configName = intent.randomListEntity.name
                        )
                    }
                } catch (e: Exception) {
                    GameLogger.error("编辑随机配置失败: ${intent.randomListEntity.name}", e)
                }
            }

            is RandomPageIntent.OnChangeNewRandomLabel -> {
                viewModelScope.launch {
                    _randomLabelsState.emit(
                        sortRandomLabels(
                            MMKVUtils.getSet(MMKV_RANDOM_LABEL_NAME_KEY) ?: emptySet()
                        )
                    )
                }
            }

            is RandomPageIntent.OnSelectLabel -> {
                randomLabelChange(intent.label)
            }

            is RandomPageIntent.DeleteRandomConfig -> {
                if (intent.name.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER) ||
                    intent.name.startsWith(RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK)
                ) {
                    return
                }
                randomLabelChange("")
                val list = randomLabelsState.value.minus(intent.name)
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
                                ?.minus(intent.name) ?: setOf()

                        )
                    }
                } catch (e: Exception) {
                    GameLogger.error("删除随机配置失败: ${intent.name}", e)
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
                val current = _currentRandomContentState.value
                publishRandomOutcome(
                    current.copy(refreshTime = current.refreshTime + 1L)
                )
            }

            RandomPageIntent.OnAddNewRandomDialogSave -> {
                // 保存当前配置（空实现保留，实际保存由 OnAddNewRandom 处理）
            }
        }
    }

    fun handleGameIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.SwitchOperationMode -> {
                _operationMode.value = if (AppDistribution.roomsEnabled) intent.mode else 0
            }
            is GameIntent.SwitchGameMode -> {
                _startedGameMode.value = intent.mode
            }
            
            is GameIntent.RefreshPlayerNumber -> {
                _gameEntity.update { current ->
                    current.copy(
                        currentGame = current.currentGame.copy(
                            totalPlayerNumber = intent.num
                        )
                    )
                }
            }
            
            is GameIntent.RefreshSpyNumber -> {
                _gameEntity.update { current ->
                    current.copy(
                        currentGame = current.currentGame.copy(
                            spyNum = intent.spyNum,
                            blackNum = intent.blackNum
                        )
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
            saveCurrentGameToHistory(current.currentGame, current.gameCount)

            // 创建新游戏，保留可能的昵称设置
            val previousGame = current.currentGame
            val previousNicknames = previousGame.nicknames
            val playerNumber = previousGame.totalPlayerNumber
            
            val newGame = LocalSpyEntity(
                totalPlayerNumber = playerNumber,
                spyNum = previousGame.spyNum,
                blackNum = previousGame.blackNum,
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
            val game = current.currentGame
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
        }
    }

    private fun updatePlayerNickname(playerIndex: Int, newNickname: String) {
        _gameEntity.update { current ->
            val game = current.currentGame
            if (playerIndex !in 0 until game.totalPlayerNumber) {
                return@update current
            }

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

    private fun publishRandomOutcome(content: RandomListEntity) {
        val results = when {
            content.name.startsWith(RANDOM_PAGE_CONFIG_CATE_DICE) -> {
                content.list.map { item ->
                    RandomToolResult(value = RandomToolRules.dice(item.first, item.second, Random.nextDouble()).toString())
                }
            }

            content.name.startsWith(RANDOM_PAGE_CONFIG_CATE_COIN) -> {
                content.list.map { item ->
                    val isPrimarySide = RandomToolRules.heads(Random.nextDouble())
                    RandomToolResult(
                        value = if (isPrimarySide) item.first else item.second,
                        isPrimarySide = isPrimarySide
                    )
                }
            }

            else -> emptyList()
        }

        _currentRandomContentState.value = content
        _randomToolOutcomeState.value = RandomToolOutcome(
            configName = content.name,
            rollId = content.refreshTime,
            results = results
        )
    }

    private fun randomLabelChange(selectedLabel: String) {
        _randomToolOutcomeState.value = RandomToolOutcome(configName = selectedLabel)
        if (selectedLabel.isEmpty()) {
            _currentRandomContentState.value = RandomListEntity()
            return
        }
        try {
            val jsonCard = Json.decodeFromString<RandomListEntity>(
                MMKVUtils.getString(
                    MMKV_RANDOM_CARDS_SETTING_KEY + selectedLabel,
                    ""
                )
            )
            _currentRandomContentState.value = jsonCard
        } catch (e: Exception) {
            // 清理损坏或已经丢失的配置，避免它在工具栏中反复出现。
            val labels = (MMKVUtils.getSet(MMKV_RANDOM_LABEL_NAME_KEY) ?: emptySet())
                .minus(selectedLabel)
            MMKVUtils.remove(MMKV_RANDOM_CARDS_SETTING_KEY + selectedLabel)
            MMKVUtils.putSet(MMKV_RANDOM_LABEL_NAME_KEY, labels)
            _randomLabelsState.value = sortRandomLabels(labels)
            _currentRandomContentState.value = RandomListEntity()
            GameLogger.error("加载随机配置失败，已移除: $selectedLabel", e)
        }
    }

    private suspend fun emitNavigationEvent(event: NavigationEvent) {
        _navigationEvents.emit(event)
    }


    // 连接管理
    fun connectToServer() {
        if (!AppDistribution.roomsEnabled) return
        viewModelScope.launch {
            try {
                roomModule.connect()
            } catch (e: Exception) {
            }
        }
    }

    private fun enterGameRoom(roomId: String, roomKey: String, asOwner: Boolean = false) {
        viewModelScope.launch {
            // 串行执行连接和创建房间
            val roomReady = withContext(Dispatchers.Default) {
                // 先连接
                val connected = roomModule.connect()
                if (!connected) return@withContext false

                if (asOwner) {
                    roomModule.createRoom(roomId, roomKey)
                } else {
                    roomModule.joinRoom(roomId, roomKey)
                }
            }

            // 处理创建结果
            if (roomReady) {
                _roomEntityState.update {
                    it.copy(
                        roomId = roomId,
                        roomKey = roomKey,
                        isRoomOwner = asOwner
                    )
                }
                GameLogger.debug("房间创建成功: $roomId")
                emitNavigationEvent(NavigationEvent.NavigateTo(NaviRoute.ROOM.route))
            } else {
                GameLogger.error("房间创建失败")
                clearRoomState()
                _topTipState.emit("网络房间连接失败，请检查网络或稍后重试")
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
                val storedLabels = MMKVUtils.getSet(MMKV_RANDOM_LABEL_NAME_KEY) ?: emptySet()
                // 老版本没有初始化标记；只要已有配置就视为初始化过，从而尊重用户
                // 主动删除骰子、硬币或转盘的选择。
                val hasInitialized = MMKVUtils.getBoolean(
                    MMKV_RANDOM_DEFAULTS_INITIALIZED_KEY,
                    storedLabels.isNotEmpty()
                )
                val existingLabels = sanitizeStoredRandomConfigs(storedLabels)

                // 预置配置名称
                val fingerConfigName = RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME
                val diceConfigName = RANDOM_PAGE_CONFIG_CATE_DICE + "六面骰子"
                val coinConfigName = RANDOM_PAGE_CONFIG_CATE_COIN + "硬币"
                val wheelConfigName = RANDOM_PAGE_CONFIG_CATE_WHEEL + "今天吃啥"
                val answerBookConfigName = RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK + "答案之书"

                ensureRandomConfig(
                    labels = existingLabels,
                    config = RandomListEntity(name = fingerConfigName, list = emptyList())
                )

                // 骰子、硬币和转盘仅在首次启动时创建；升级后用户删除的工具不复活。
                if (!hasInitialized) {
                    ensureRandomConfig(
                        labels = existingLabels,
                        config = RandomListEntity(
                        name = diceConfigName,
                        list = listOf(RandomItem(first = "1", second = "6"))
                    )
                    )
                    ensureRandomConfig(
                        labels = existingLabels,
                        config = RandomListEntity(
                            name = coinConfigName,
                            list = listOf(RandomItem(first = "正面", second = "反面"))
                        )
                    )
                    ensureRandomConfig(
                        labels = existingLabels,
                        config = RandomListEntity(
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
                    )
                }

                ensureRandomConfig(
                    labels = existingLabels,
                    config = RandomListEntity(
                        name = answerBookConfigName,
                        list = emptyList() // 答案之书数据由 AnswerBookData 对象提供
                    )
                )

                migrateLegacyDefaultRandomConfigs(
                    diceConfigName = diceConfigName,
                    coinConfigName = coinConfigName
                )

                // 保存更新后的配置列表
                MMKVUtils.putSet(MMKV_RANDOM_LABEL_NAME_KEY, existingLabels)
                MMKVUtils.put(MMKV_RANDOM_DEFAULTS_INITIALIZED_KEY, true)

                // 更新状态
                _randomLabelsState.value = sortRandomLabels(existingLabels)
            } catch (e: Exception) {
                GameLogger.error("初始化默认随机配置失败", e)
            }
        }
    }

    private fun ensureRandomConfig(
        labels: MutableSet<String>,
        config: RandomListEntity
    ) {
        if (labels.add(config.name)) {
            MMKVUtils.put(
                MMKV_RANDOM_CARDS_SETTING_KEY + config.name,
                Json.encodeToString(RandomListEntity.serializer(), config)
            )
        }
    }

    /**
     * 清理跨端导入、旧版本或异常退出留下的配置。损坏项会被移除；可修复项会
     * 限制名称、数量、文本和数值范围后重新保存。
     */
    private fun sanitizeStoredRandomConfigs(labels: Set<String>): MutableSet<String> {
        val sanitizedLabels = linkedSetOf<String>()
        labels.forEach { storedLabel ->
            val storageKey = MMKV_RANDOM_CARDS_SETTING_KEY + storedLabel
            val storedJson = MMKVUtils.getString(storageKey, "")
            val storedConfig = try {
                Json.decodeFromString<RandomListEntity>(storedJson)
            } catch (e: Exception) {
                MMKVUtils.remove(storageKey)
                GameLogger.error("随机配置已损坏并清理: $storedLabel", e)
                return@forEach
            }

            val sanitized = sanitizeRandomConfig(storedLabel, storedConfig)
            if (sanitized == null || !sanitizedLabels.add(sanitized.name)) {
                if (storedLabel != sanitized?.name) MMKVUtils.remove(storageKey)
                return@forEach
            }

            if (storedLabel != sanitized.name) MMKVUtils.remove(storageKey)
            MMKVUtils.put(
                MMKV_RANDOM_CARDS_SETTING_KEY + sanitized.name,
                Json.encodeToString(RandomListEntity.serializer(), sanitized)
            )
        }
        return sanitizedLabels
    }

    private fun sanitizeRandomConfig(
        storedLabel: String,
        config: RandomListEntity
    ): RandomListEntity? {
        val prefixes = listOf(
            RANDOM_PAGE_CONFIG_CATE_DICE,
            RANDOM_PAGE_CONFIG_CATE_CARD,
            RANDOM_PAGE_CONFIG_CATE_COIN,
            RANDOM_PAGE_CONFIG_CATE_WHEEL,
            RANDOM_PAGE_CONFIG_CATE_FINGER,
            RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK
        )
        val candidateName = config.name.takeIf { name ->
            prefixes.any { name.startsWith(it) }
        } ?: storedLabel
        val prefix = prefixes.firstOrNull { candidateName.startsWith(it) } ?: return null
        val displayName = candidateName.removePrefix(prefix).trim().take(20)
        if (displayName.isBlank()) return null

        fun clean(value: String, fallback: String, maxLength: Int): String =
            value.trim().takeIf { it.isNotEmpty() }?.take(maxLength) ?: fallback

        val source = config.list.take(if (prefix == RANDOM_PAGE_CONFIG_CATE_WHEEL) 20 else 30)
        val items = when (prefix) {
            RANDOM_PAGE_CONFIG_CATE_DICE -> source.mapIndexed { index, item ->
                var first = item.first.toIntOrDefault(1).coerceIn(1, 100)
                var second = item.second.toIntOrDefault(6).coerceIn(1, 100)
                if (first > second) {
                    val originalFirst = first
                    first = second
                    second = originalFirst
                }
                RandomItem(index, second.toString(), first.toString(), prefix)
            }.ifEmpty { listOf(RandomItem(0, "6", "1", prefix)) }

            RANDOM_PAGE_CONFIG_CATE_WHEEL -> source.mapIndexed { index, item ->
                RandomItem(
                    id = index,
                    first = clean(item.first, "选项${index + 1}", 24),
                    second = item.second.toIntOrDefault(1).coerceIn(1, 100).toString(),
                    cate = prefix
                )
            }.toMutableList().apply {
                while (size < 2) {
                    val index = size
                    add(RandomItem(index, "50", "选项${index + 1}", prefix))
                }
            }

            RANDOM_PAGE_CONFIG_CATE_COIN,
            RANDOM_PAGE_CONFIG_CATE_CARD -> source.mapIndexed { index, item ->
                val firstFallback = if (prefix == RANDOM_PAGE_CONFIG_CATE_COIN) {
                    "正面"
                } else {
                    "牌背 ${index + 1}"
                }
                val secondFallback = if (prefix == RANDOM_PAGE_CONFIG_CATE_COIN) {
                    "反面"
                } else {
                    "牌面 ${index + 1}"
                }
                RandomItem(
                    id = index,
                    first = clean(item.first, firstFallback, 40),
                    second = clean(item.second, secondFallback, 40),
                    cate = prefix
                )
            }.ifEmpty {
                if (prefix == RANDOM_PAGE_CONFIG_CATE_COIN) {
                    listOf(RandomItem(0, "反面", "正面", prefix))
                } else {
                    listOf(RandomItem(0, "牌面 1", "牌背 1", prefix))
                }
            }

            else -> emptyList()
        }
        return RandomListEntity(
            list = items,
            name = prefix + displayName,
            refreshTime = config.refreshTime.coerceAtLeast(0L)
        )
    }

    private fun sortRandomLabels(labels: Collection<String>): List<String> {
        fun rank(name: String): Int = when (name) {
            RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK + "答案之书" -> 0
            RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME -> 1
            RANDOM_PAGE_CONFIG_CATE_WHEEL + "今天吃啥" -> 2
            RANDOM_PAGE_CONFIG_CATE_COIN + "硬币" -> 3
            RANDOM_PAGE_CONFIG_CATE_DICE + "六面骰子" -> 4
            else -> 100
        }
        // sortedBy is stable: system tools get a fixed rank while custom tools keep the
        // user's existing insertion order instead of being renamed/reordered alphabetically.
        return labels.sortedBy { rank(it) }
    }

    /**
     * 兼容旧包或跨端导入可能留下的旧默认结构：6 个固定骰子
     * （1..1 到 6..6）或 2 个正反面相同的固定硬币。仅当默认名称和结构
     * 同时精确匹配时才迁移，不改动普通自定义配置。
     */
    private fun migrateLegacyDefaultRandomConfigs(
        diceConfigName: String,
        coinConfigName: String
    ) {
        migrateLegacyDefaultRandomConfig(
            configName = diceConfigName,
            replacementItems = listOf(RandomItem(first = "1", second = "6"))
        ) { items ->
            items.size == 6 &&
                    items.all { it.first == it.second } &&
                    items.map { it.first.toIntOrDefault() }.sorted() == (1..6).toList()
        }

        migrateLegacyDefaultRandomConfig(
            configName = coinConfigName,
            replacementItems = listOf(RandomItem(first = "正面", second = "反面"))
        ) { items ->
            items.size == 2 && items.all { it.first == it.second }
        }
    }

    private fun migrateLegacyDefaultRandomConfig(
        configName: String,
        replacementItems: List<RandomItem>,
        isLegacy: (List<RandomItem>) -> Boolean
    ) {
        val storageKey = MMKV_RANDOM_CARDS_SETTING_KEY + configName
        val storedJson = MMKVUtils.getString(storageKey, "")
        if (storedJson.isBlank()) return

        try {
            val storedConfig = Json.decodeFromString<RandomListEntity>(storedJson)
            if (!isLegacy(storedConfig.list)) return

            val migratedConfig = storedConfig.copy(
                list = replacementItems,
                refreshTime = 0L
            )
            MMKVUtils.put(
                storageKey,
                Json.encodeToString(RandomListEntity.serializer(), migratedConfig)
            )
            GameLogger.debug("已迁移旧版随机配置: $configName")
        } catch (e: Exception) {
            GameLogger.error("迁移旧版随机配置失败: $configName", e)
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
                GameLogger.error("保存转盘配置失败: $currentConfig", e)
            }
        }
    }

    private fun initLANObservers() {
        if (!AppDistribution.roomsEnabled) return
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

    /**
     * 处理答案之书意图
     * @param intent 答案之书意图（翻书、动画完成、更新问题）
     */
    fun handleAnswerBookIntent(intent: AnswerBookIntent) {
        when (intent) {
            is AnswerBookIntent.FlipBook -> {
                val currentState = _answerBookState.value
                if (currentState.isFlipping) return

                val answer = AnswerBookData.getRandomAnswerExcluding(currentState.lastAnswerIndex)
                _answerBookState.update {
                    it.beginFlip(answer, AnswerBookData.answers.indexOf(answer))
                }
                PlatformHelper.getInstance().vibrateMethod()
            }

            is AnswerBookIntent.AnimationFinished -> {
                val completedOpening = _answerBookState.value.phase == intent.phase &&
                    intent.phase == AnswerBookPhase.OPENING
                _answerBookState.update {
                    it.finishAnimation(intent.phase)
                }
                if (completedOpening) PlatformHelper.getInstance().vibrateLongMethod()
            }

            is AnswerBookIntent.UpdateQuestion -> {
                _answerBookState.update {
                    it.copy(currentQuestion = intent.question)
                }
            }
        }
    }

    /**
     * 处理 AI 助手相关意图
     * @param intent AI 意图（发送消息、更新配置等）
     */
    fun handleAiIntent(intent: AiIntent) {
        when (intent) {
            is AiIntent.SendMessage -> {
                sendAiMessage(intent.gameType, intent.context)
            }

            is AiIntent.UpdateConfig -> {
                _aiConfig.value = intent.config
                saveAiConfig(intent.config)
            }

            is AiIntent.ToggleAi -> {
                _aiConfig.update { it.copy(isEnabled = intent.enabled) }
                saveAiConfig(_aiConfig.value)
            }

            is AiIntent.UpdateApiKey -> {
                _aiConfig.update { it.copy(apiKey = intent.key) }
                saveAiConfig(_aiConfig.value)
            }

            is AiIntent.UpdateProvider -> {
                _aiConfig.update {
                    it.copy(
                        provider = intent.provider,
                        baseUrl = intent.provider.defaultBaseUrl
                    )
                }
                saveAiConfig(_aiConfig.value)
            }

            is AiIntent.UpdateStyle -> {
                _aiConfig.update { it.copy(aiStyle = intent.style) }
                saveAiConfig(_aiConfig.value)
            }

            is AiIntent.ClearMessage -> {
                _aiMessage.value = ""
            }
        }
    }

    /**
     * 发送消息给 AI 并更新回复
     * @param gameType 游戏类型标识
     * @param context 用户输入的上下文内容
     */
    private fun sendAiMessage(gameType: String, context: String) {
        viewModelScope.launch {
            _isLoadingAi.value = true
            try {
                val config = _aiConfig.value
                val systemPrompt = GamePromptTemplates.getPromptForGame(gameType, config.aiStyle)
                val service = AiServiceFactory.create(config)

                val response = service.chat(
                    org.walks.gamecopilot.service.ai.AiRequest(
                        prompt = context,
                        systemPrompt = systemPrompt
                    )
                )

                if (response.isSuccess) {
                    _aiMessage.value = response.content
                } else {
                    // AI 调用失败，自动降级为本地预设
                    GameLogger.warn("AI 请求失败: ${response.errorMessage}，降级为本地预设")
                    val fallbackService = org.walks.gamecopilot.service.ai.FallbackAiService()
                    val fallbackResponse = fallbackService.chat(
                        org.walks.gamecopilot.service.ai.AiRequest(
                            prompt = context,
                            systemPrompt = systemPrompt
                        )
                    )
                    _aiMessage.value = if (fallbackResponse.isSuccess) {
                        fallbackResponse.content
                    } else {
                        "AI 助手暂时不可用，请稍后再试~"
                    }
                }
            } catch (e: Exception) {
                GameLogger.error("AI 消息处理异常", e)
                _aiMessage.value = "AI 助手遇到了一些问题，请稍后再试~"
            } finally {
                _isLoadingAi.value = false
            }
        }
    }

    /**
     * 从持久化存储加载 AI 配置
     */
    private fun loadAiConfig() {
        try {
            val providerName = MMKVUtils.getString(MMKV_AI_PROVIDER_KEY, AiProvider.FALLBACK.name)
            val provider = try {
                AiProvider.valueOf(providerName)
            } catch (_: Exception) {
                AiProvider.FALLBACK
            }
            val apiKey = MMKVUtils.getString(MMKV_AI_API_KEY, "")
            val baseUrl = MMKVUtils.getString(MMKV_AI_BASE_URL, AiProvider.DEEP_SEEK.defaultBaseUrl)
            val isEnabled = MMKVUtils.getBoolean(MMKV_AI_ENABLED_KEY, false)
            val styleName = MMKVUtils.getString(MMKV_AI_STYLE_KEY, AiStyle.HUMOROUS.name)
            val aiStyle = try {
                AiStyle.valueOf(styleName)
            } catch (_: Exception) {
                AiStyle.HUMOROUS
            }
            val timeoutMs = MMKVUtils.getLong(MMKV_AI_TIMEOUT_KEY, 10000L)

            _aiConfig.value = AiConfig(
                provider = provider,
                apiKey = apiKey,
                baseUrl = baseUrl,
                isEnabled = isEnabled,
                aiStyle = aiStyle,
                timeoutMs = timeoutMs
            )
        } catch (e: Exception) {
            GameLogger.error("加载 AI 配置失败", e)
        }
    }

    /**
     * 保存 AI 配置到持久化存储
     * @param config AI 配置
     */
    private fun saveAiConfig(config: AiConfig) {
        try {
            MMKVUtils.apply {
                put(MMKV_AI_PROVIDER_KEY, config.provider.name)
                put(MMKV_AI_API_KEY, config.apiKey)
                put(MMKV_AI_BASE_URL, config.baseUrl)
                put(MMKV_AI_ENABLED_KEY, config.isEnabled)
                put(MMKV_AI_STYLE_KEY, config.aiStyle.name)
                put(MMKV_AI_TIMEOUT_KEY, config.timeoutMs)
            }
        } catch (e: Exception) {
            GameLogger.error("保存 AI 配置失败", e)
        }
    }

    fun handleLANIntent(intent: LANIntent) {
        if (!AppDistribution.roomsEnabled) return
        when (intent) {
            is LANIntent.SetPreferredGameType -> {
                _lanState.update { it.copy(preferredGameType = intent.gameType) }
            }

            is LANIntent.StartDiscovery -> {
                lanRoomManager.startDiscovery(intent.gameType)
                _lanState.update { it.copy(isDiscovering = true, error = null) }
            }

            is LANIntent.StopDiscovery -> {
                lanRoomManager.stopDiscovery()
                _lanState.update { it.copy(isDiscovering = false) }
            }

            is LANIntent.ClearDiscoveredRooms -> {
                lanRoomManager.clearDiscoveredRooms()
            }

            is LANIntent.CreateRoom -> {
                _lanState.update { it.copy(error = null) }
                lanRoomManager.createRoom(
                    roomName = intent.roomName,
                    hostName = intent.hostName,
                    gameType = intent.gameType,
                    maxPlayers = intent.maxPlayers,
                    password = intent.password
                )
            }

            is LANIntent.JoinRoom -> {
                _lanState.update { it.copy(error = null) }
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
        if (AppDistribution.roomsEnabled) lanRoomManager.dispose()
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
