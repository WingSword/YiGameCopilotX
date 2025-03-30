package org.walks.gamecopilot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.walks.gamecopilot.data.UserInfoEntity
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.data.entity.RoomState
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.http.baseJsonConf
import org.walks.gamecopilot.http.roomModule
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.intent.GameRoomIntent


class MainViewmodel : ViewModel() {

    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode

    private val _gameEntity = MutableStateFlow(GameEntity(0, mutableListOf()))
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    private val _roomEntityState = MutableStateFlow(RoomState())
    val roomEntityState: StateFlow<RoomState> = _roomEntityState

    private val _navigationEvents: MutableStateFlow<NavigationEvent?> = MutableStateFlow(null)
    val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents

    private val _topTipState: MutableStateFlow<String?> = MutableStateFlow(null)
    var topTipState: StateFlow<String?> = _topTipState


    private var userId = ""


    fun handleRoomIntent(intent: GameRoomIntent) {
        when (intent) {
            is GameRoomIntent.RefreshRoomInfo -> {
                refreshRoomGameInfo()
            }

            is GameRoomIntent.CreateAGameRoom -> {
                viewModelScope.launch {
                    _navigationEvents.emit(
                        NavigationEvent.NavigateTo(
                            "room"
                        )
                    )
                }

                enterGameRoom(intent.roomId, intent.roomKey, true)
            }

            is GameRoomIntent.JoinToAGameRoom -> {
                enterGameRoom(intent.roomId, intent.roomKey, false)
            }

            GameRoomIntent.LeaveGameRoom -> {
                val id = roomEntityState.value.roomId
                val key = roomEntityState.value.roomKey
                viewModelScope.launch {
                    var result = roomModule.leaveRoom(
                        id,
                        key,
                        userId
                    )
                    if (result.isSuccess()) {
                        clearRoomState()
                    }
                }
            }

            GameRoomIntent.StartGame -> {
                roomStartGame()
            }

            GameRoomIntent.DeleteGameRoom -> {
                deleteRoom()
            }
        }
    }


    private fun roomStartGame() {
        viewModelScope.launch {
            val result = roomModule.startGame(
                roomEntityState.value.roomId,
                roomEntityState.value.roomKey,
                userId
            )
            if (result.isSuccess()) {
                refreshRoomGameInfo()
                return@launch
            }
            _topTipState.emit(
                result.msg ?: "游戏开始失败"
            )
             }
    }

    private fun refreshRoomGameInfo() {
        viewModelScope.launch {
            val result = roomModule.getRoomInfo(
                roomEntityState.value.roomId,
                roomEntityState.value.roomKey
            )
            if (result.isSuccess()) {
                val userList = baseJsonConf.decodeFromString<List<UserInfoEntity>>(
                    result.data?.users ?: ""
                )
                _roomEntityState.update { roomState ->
                    roomState.copy(
                        roomPlayerNum = userList.size,
                        users = result.data?.users ?: "",
                        memberList = userList,
                        playerNo = userList.indexOf(userList.find { it.userId == userId })
                    )
                }
            }
        }
    }

    private fun enterGameRoom(roomId: String, roomKey: String, asOwner: Boolean = false) {
        viewModelScope.launch {
            val result =
                if (asOwner) roomModule.createRoom(roomId, roomKey) else roomModule.joinRoom(
                    roomId,
                    roomKey
                )
            if (result.isSuccess()) {
                userId = result.data ?: ""
                _roomEntityState.update {
                    it.copy(
                        roomId = roomId,
                        roomFinished = true,
                        roomKey = roomKey,
                    )
                }
                refreshRoomGameInfo()
                return@launch
            }
            _topTipState.emit(result.msg ?: "加入房间失败")
        }
    }

    private fun deleteRoom() {
        viewModelScope.launch {
            val result = roomModule.deleteRoom(
                roomEntityState.value.roomId,
                roomEntityState.value.roomKey,
                userId
            )
            if (result.isSuccess()) {
                clearRoomState()
            }
        }
    }

    private fun clearRoomState() = _roomEntityState.update {
        it.copy(
            roomId = "",
            roomKey = "",
            roomFinished = false,
            playerNo = 0,
            roomPlayerNum = 0,
            startedGameMode = startedGameMode.value
        )
    }


    /**
     * 处理本地游戏相关意图的分发函数
     * @param intent 游戏操作意图对象，包含具体的游戏行为指令
     */
    fun handleLocalGameIntent(intent: GameIntent) {
        when (intent) {
            // region 刷新玩家数量处理
            is GameIntent.RefreshPlayerNumber -> {
                _gameEntity.update { entity ->
                    entity.copy(
                        timeEntityList = mutableListOf(
                            LocalSpyEntity(
                                totalPlayerNumber = intent.num
                            ).apply {
                                refreshGame()
                            }
                        )
                    )
                }
            }

            // region 刷新间谍数量处理
            is GameIntent.RefreshSpyNumber -> {
                val timeEntity = gameEntity.value.timeEntityList.lastOrNull() ?: return
                _gameEntity.update {
                    it.copy(
                        timeEntityList = mutableListOf(
                            timeEntity.copy(
                                spyNum = intent.spyNum,
                                blackNum = intent.blackNum
                            ).apply {
                                refreshGame()
                            }
                        )
                    )
                }
            }

            // region 游戏模式切换处理
            is GameIntent.SwitchGameMode -> {
                _startedGameMode.value = intent.mode
            }

            is GameIntent.StartGame -> {
                when (startedGameMode.value) {
                    1 -> {
                        restartLocalSpyGame()
                    }
                }
            }
        }
    }

    // 在 common 代码中调用
    fun vibrateLong() = PlatformHelper.getInstance().vibrateLongMethod()

    fun vibrite() {
        PlatformHelper.getInstance().vibrateMethod()
    }

    private fun restartLocalSpyGame() {
        val timeEntity = _gameEntity.value.timeEntityList.lastOrNull() ?: return
        _gameEntity.update { entity ->
            entity.copy(
                timeEntityList = entity.timeEntityList.also {
                    it.add(timeEntity.apply {
                        refreshGame()
                    })
                }
            )
        }
    }
}