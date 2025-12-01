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
import org.walks.gamecopilot.awalong.AwalongGameLogic
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.awalong.AwalongRole
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

            is AwalongIntent.ProphetCheck -> {
                // 预言者检查2名玩家阵营
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        prophetChecked = Pair(intent.player1Index, intent.player2Index)
                    )
                }
            }
            
            is AwalongIntent.LadyOfLakeCheck -> {
                // 湖中仙女检查玩家阵营
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        ladyOfLakeUsed = true,
                        ladyOfLakeChecked = intent.playerIndex
                    )
                }
            }
            
            is AwalongIntent.SirGalahadUseDoubleVote -> {
                // 圆桌骑士使用双倍投票权
                _awalongGameState.update {
                    it.copy(
                        playTime = Clock.System.now().toEpochMilliseconds(),
                        sirGalahadUsed = true
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
        }
    }


    @OptIn(ExperimentalTime::class)
    private fun resetAwalongGameState() {
        val config = awalongConfigState.value
        _awalongGameState.update {
            AwalongGameState(
                playTime = Clock.System.now().toEpochMilliseconds(),
                roleList = config.role.optimizedShuffle().toMutableList(),
                dayList = mutableListOf<AwalongGameDayEntity>().apply {
                    config.process.forEachIndexed { index, taskSize ->
                        // 根据任务大小判断是否需要2张失败卡（通常是较大的任务）
                        val requiresTwoFailures = taskSize >= 4 && config.playerNum >= 7
                        this.add(
                            AwalongGameDayEntity(
                                day = index,
                                captain = config.role.indices.random(),
                                requiresTwoFailures = requiresTwoFailures
                            )
                        )
                    }
                },
                nickNameList = (1..config.role.size).map { it.toString() }
                    .toMutableList(),
                // 初始化扩展包字段
                ladyOfLakeUsed = false,
                sirGalahadUsed = false,
                morguseUsed = false,
                prophetChecked = null,
                ladyOfLakeChecked = null,
                lancolotConverted = false,
                shapeshifterTarget = null
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