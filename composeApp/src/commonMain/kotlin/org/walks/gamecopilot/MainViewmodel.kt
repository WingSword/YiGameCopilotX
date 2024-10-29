package org.walks.gamecopilot

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.walks.gamecopilot.data.entity.GameEntity
import org.walks.gamecopilot.data.entity.TimeEntity
import org.walks.gamecopilot.intent.GameIntent


class MainViewmodel : ViewModel() {
    private val _playerNumber = MutableStateFlow<Int>(0)
    val playerNumber: StateFlow<Int> = _playerNumber

    private val _startedGameMode = MutableStateFlow<Int>(0)
    val startedGameMode: StateFlow<Int> = _startedGameMode

    private val _gameEntity = MutableStateFlow(GameEntity(0, mutableListOf()))
    val gameEntity: StateFlow<GameEntity> = _gameEntity

    var topTipState= mutableStateOf("")
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
                if(intent.roomKey.isNullOrBlank()||intent.roomName.isNullOrBlank()){
                    topTipState.value = "房间名或密码不能为空"
                }
            }
            is GameIntent.JoinToAGameRoom -> {

            }
        }
    }


}