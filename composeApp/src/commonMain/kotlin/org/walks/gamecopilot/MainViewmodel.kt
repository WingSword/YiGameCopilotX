package org.walks.gamecopilot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.RoomState
import org.walks.gamecopilot.data.entity.TimeEntity
import org.walks.gamecopilot.intent.GameIntent


class MainViewmodel : ViewModel() {
    private val _playerNumber = MutableStateFlow<Int>(0)
    val playerNumber: StateFlow<Int> = _playerNumber

    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode

    private val _gameEntity = MutableStateFlow(GameEntity(0, mutableListOf()))
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    private val _roomEntityState = MutableStateFlow(RoomState())
    val roomEntityState: StateFlow<RoomState> = _roomEntityState

    private val _roomFinished=MutableStateFlow(false)
    val roomFinished: StateFlow<Boolean> = _roomFinished

    var topTipState = mutableStateOf("")
        private set

    private val wordList by lazy {
        addWordsToMap(wordMap)
        wordMap.values.toList()
        wordMap.keys.toList()
    }


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.updatePlayerNum -> {
                _playerNumber.value = intent.num
            }


            is GameIntent.updateGameMode -> {
                _startedGameMode.value = intent.mode

            }

            is GameIntent.startGame -> {
                when (intent.mode) {
                    1 -> {
                        val list = _gameEntity.value.timeEntityList
                        list.add(
                            TimeEntity(
                                gamePlayerNumber = playerNumber.value,
                                gameWord = wordList.random(),
                                spyNum = (1..playerNumber.value).random()
                            )
                        )
                        _gameEntity.value =
                            _gameEntity.value.copy(gameMode = intent.mode, timeEntityList = list)
                    }
                }
            }

            is GameIntent.CreateAGameRoom -> {
                if (intent.roomKey.isBlank() || intent.roomName.isBlank()) {
                    topTipState.value = "房间名或密码不能为空"
                    return
                }

                _roomEntityState.update {
                    it.copy(
                        roomNumber = intent.roomKey,
                        roomFinished = true,
                        playerNo = 1,
                        playerNum = 1,
                        startedGameMode = startedGameMode.value
                    )
                }
                _roomFinished.update {
                    true
                }
            }

            is GameIntent.JoinToAGameRoom -> {
                if (intent.roomKey.isBlank() || intent.roomName.isBlank()) {
                    topTipState.value = "房间名或密码不能为空"
                    return
                }

                _roomEntityState.update {
                    it.copy(
                        roomNumber = intent.roomKey,
                        roomFinished = true,
                        playerNo = 1,
                        playerNum = 1,
                        startedGameMode = startedGameMode.value
                    )
                }
                _roomFinished.update {
                    true
                }
            }
        }
    }


}