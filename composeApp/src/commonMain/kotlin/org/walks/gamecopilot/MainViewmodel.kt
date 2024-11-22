package org.walks.gamecopilot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.TimeEntity
import org.walks.gamecopilot.http.joinARoom
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

    private val wordList by lazy {
        addWordsToMap(wordMap)
        wordMap.values.toList()
        wordMap.keys.toList()
    }


    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.RefreshPlayerNumber -> {
                if(startedGameMode.value==0){
                    _roomEntityState.update {
                        it.copy(playerNum =roomEntityState.value.playerNum+1 )
                    }
                }else{
                    _roomEntityState.update {
                        it.copy(playerNum = intent.num)
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
                                gamePlayerNumber = roomEntityState.value.playerNum,
                                gameWord = wordList.random(),
                                spyNum = (1..roomEntityState.value.playerNum).random()
                            )
                        )
                        _gameEntity.value = _gameEntity.value.copy(gameMode = startedGameMode.value, timeEntityList = list)
                    }
                }
            }

            is GameIntent.CreateAGameRoom -> {
                viewModelScope.launch {
                    joinARoom(intent.roomName,intent.roomKey)
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
            }

            GameIntent.LeaveGameRoom -> {
                _roomEntityState.update {
                    it.copy(
                        roomNumber = "",
                        roomFinished = false,
                        playerNo = 0,
                        playerNum = 0,
                        startedGameMode = startedGameMode.value
                    )
                }
            }
        }
    }


    fun roomConfigure(){

    }

}