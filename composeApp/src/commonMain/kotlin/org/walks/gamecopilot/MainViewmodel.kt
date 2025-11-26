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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.awalong.AwalongConfig
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.data.WsRoomDataEntity
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.http.RoomModule
import org.walks.gamecopilot.http.roomModule
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_SETTING_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_LABEL_NAME_KEY
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.ui.page.random.optimizedShuffle
import org.walks.gamecopilot.utils.DateTimeUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class MainViewmodel : ViewModel() {

    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode

    private val _gameEntity = MutableStateFlow(GameEntity())
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    private val _roomEntityState = MutableStateFlow(WsRoomDataEntity())
    val roomEntityState: StateFlow<WsRoomDataEntity> = _roomEntityState

    // 修改事件流类型
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvents = _navigationEvents.asSharedFlow()

    private val _topTipState: MutableSharedFlow<String?> = MutableSharedFlow()
    var topTipState = _topTipState.asSharedFlow()

    private val _currentRandomContentState = MutableStateFlow(RandomListEntity())
    val currentRandomContentState: StateFlow<RandomListEntity> = _currentRandomContentState
    private val _randomLabelsState = MutableStateFlow(listOf<String>())
    val randomLabelsState: StateFlow<List<String>> = _randomLabelsState

    private val _addRandomConfigDialogState = MutableSharedFlow<Boolean>()
    val addRandomConfigDialogState = _addRandomConfigDialogState.asSharedFlow()

    private val _awalongConfigState = MutableStateFlow<AwalongConfig>(
        AwalongConfig.Standard_5
    )
    val awalongConfigState: StateFlow<AwalongConfig> = _awalongConfigState

    private val _awalongGameState = MutableStateFlow<AwalongGameState>(AwalongGameState())
    val awalongGameState: StateFlow<AwalongGameState> = _awalongGameState

    private var userId = ""

    init {
        // 监听连接状态
        roomModule.connectionState
            .onEach { state ->
                when (state) {
                    RoomModule.ConnectionState.CONNECTED -> GameLogger.debug("已连接")
                    RoomModule.ConnectionState.DISCONNECTED -> GameLogger.debug("已断开")
                    RoomModule.ConnectionState.CONNECTING -> GameLogger.debug("连接中")
                }
            }
            .launchIn(viewModelScope)

        // 监听消息流
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

    fun handleRoomIntent(intent: GameRoomIntent) {
        when (intent) {
            is GameRoomIntent.RefreshRoomInfo -> {
                //
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
                try {
                    // 序列化卡片列表
                    val jsonCards = Json.encodeToString(intent.randomListEntity)
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

            RandomPageIntent.OnAddNewRandomDialogSave -> TODO()
        }
    }

    fun handleGameIntent(intent: GameIntent) {
        when (intent) {
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
        }
    }
    
    private fun startNewLocalSpyGame() {
        _gameEntity.update { current ->
            // 保存当前游戏到历史记录
            if (current.currentGame != null) {
                saveCurrentGameToHistory(current.currentGame, current.gameCount)
            }
            
            // 创建新游戏
            val newGame = LocalSpyEntity(
                totalPlayerNumber = current.currentGame?.totalPlayerNumber ?: 4,
                spyNum = current.currentGame?.spyNum ?: 1,
                blackNum = current.currentGame?.blackNum ?: 0
            )
            
            // 刷新游戏词汇
            newGame.refreshGame(current.globalSelectedWordGroups)
            
            current.copy(
                currentGame = newGame,
                gameCount = current.gameCount + 1
            )
        }
    }
    
    private fun saveCurrentGameToHistory(game: LocalSpyEntity, currentCount: Int) {
        try {
            val historyKey = "local_spy_game_history_${currentCount}"
            val gameJson = Json.encodeToString(game)
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
                        it.copy(
                            playTime = Clock.System.now().toEpochMilliseconds(),
                            dayList = it.dayList.apply {
                                if (this.find { it.day == intent.task.day } == null) {
                                    add(intent.task)
                                } else {
                                    set(
                                        this.indexOfFirst { it.day == intent.task.day },
                                        intent.task
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    private fun resetAwalongGameState() {
        viewModelScope.launch {
            _awalongGameState.emit(
                AwalongGameState(
                    playTime = awalongGameState.value.playTime+1,
                    roleList = awalongConfigState.value.role.optimizedShuffle().toMutableList(),
                    dayList = mutableListOf<AwalongGameDayEntity>().apply {
                        awalongConfigState.value.process.forEachIndexed { index, i ->
                            this.add(
                                AwalongGameDayEntity(
                                    day = index,
                                    captain = awalongConfigState.value.role.indices.random(),
                                )
                            )
                        }
                    }
                ,
                    nickNameList = (1..awalongConfigState.value.role.size).map { it.toString() }
                        .toMutableList()
                )
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


}