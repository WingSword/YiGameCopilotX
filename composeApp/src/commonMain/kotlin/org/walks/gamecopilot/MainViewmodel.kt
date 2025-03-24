package org.walks.gamecopilot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.RoomState
import org.walks.gamecopilot.data.entity.TimeEntity
import org.walks.gamecopilot.http.RoomModule
import org.walks.gamecopilot.http.client

import org.walks.gamecopilot.intent.GameIntent


class MainViewmodel : ViewModel() {

    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode

    private val _gameEntity = MutableStateFlow(GameEntity(0, mutableListOf()))
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    private val _roomEntityState = MutableStateFlow(RoomState())
    val roomEntityState: StateFlow<RoomState> = _roomEntityState


    var topTipState = mutableStateOf("")
        private set

    var userId=""

    private val wordList by lazy {
        addWordsToMap(wordMap)
        wordMap.values.toList()
        wordMap.keys.toList()
    }

    val roomModule by lazy {
        RoomModule(client)
    }


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.RefreshPlayerNumber -> {
                if (startedGameMode.value == 0) {
                    _roomEntityState.update {
                        it.copy(playerNum = roomEntityState.value.playerNum + 1)
                    }
                } else {
                    _gameEntity.update {
                        it.copy(
                            timeEntityList = mutableListOf(
                                TimeEntity(
                                    gamePlayerNumber = intent.num,
                                    gameWord = wordList.random(),
                                    spyNum = (1..intent.num).random()
                                )
                            )
                        )
                    }
                }
            }

            is GameIntent.RefreshSpyNumber -> {
                if (startedGameMode.value == 0) {

                } else {
                    val entity = TimeEntity(
                        gamePlayerNumber = intent.num,
                        gameWord = wordList.random(),
                        spyNum = (1..intent.num).random()
                    )
                    _gameEntity.update {
                        it.copy(
                            timeEntityList = mutableListOf(
                                TimeEntity(
                                    gamePlayerNumber = intent.num,
                                    gameWord = wordList.random(),
                                    spyNum = (1..intent.num).random()
                                )
                            )
                        )
                    }
                }
            }


            is GameIntent.SwitchGameMode -> {
                _startedGameMode.value = intent.mode
            }

            is GameIntent.StartGame -> {
                when (startedGameMode.value) {
                    1 -> {
                        val list = _gameEntity.value.timeEntityList
                        list.add(
                            TimeEntity(
                                gamePlayerNumber = gameEntity.value.timeEntityList.last().gamePlayerNumber,
                                gameWord = wordList.random(),
                                spyNum = (1..gameEntity.value.timeEntityList.last().gamePlayerNumber).random()
                            )
                        )
                        _gameEntity.update {
                            it.copy(
                                timeEntityList = list
                            )
                        }
                    }
                }
            }
            is GameIntent.RefreshRoomInfo -> {
                viewModelScope.launch {
                    val result = roomModule.getRoomInfo(roomEntityState.value.roomId,roomEntityState.value.roomKey)
                    if (result.isSuccess()) {
                        _roomEntityState.update {
                            roomEntityState.value.copy(

                            )
                        }

                    }
                }
            }

            is GameIntent.CreateAGameRoom -> {
                if (intent.roomKey.isBlank() || intent.roomId.isBlank()) {
                    topTipState.value = "房间名或密码不能为空"
                    return
                }
                viewModelScope.launch {
                    val result = roomModule.createRoom(intent.roomId, intent.roomKey)
                    if (result.isSuccess()) {
                        _roomEntityState.update {
                            it.copy(
                                roomId = intent.roomId,
                                roomFinished = true,
                                playerNo = 1,
                                playerNum = 1,

                                )
                        }
                    }
                }
            }

            is GameIntent.JoinToAGameRoom -> {

                viewModelScope.launch {
                    val result = roomModule.joinRoom(intent.roomId, intent.roomKey)
                    if (result.isSuccess()) {
                        _roomEntityState.update {
                            it.copy(
                                playerNo = 1,
                                playerNum = it.playerNum + 1,)
                        }
                    }
                }
            }

            GameIntent.LeaveGameRoom -> {
                _roomEntityState.update {
                    it.copy(
                        roomId = "",
                        roomFinished = false,
                        playerNo = 0,
                        playerNum = 0,
                        startedGameMode = startedGameMode.value
                    )
                }
            }
        }
    }


    fun roomConfigure() {

    }

}