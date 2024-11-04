package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.GreetingView
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.button.CommonButton
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

@Composable
fun StartPage(viewmodel: MainViewmodel) {
    var showNumberPicker by remember { mutableStateOf(false) }
    val numberList = listOf("4", "5", "6", "7", "8", "9", "10")
    val gameModeList= listOf("谁是卧底", "谁是卧底（本地）", "谁是卧底3")
    val gameMode = viewmodel.startedGameMode.collectAsState()
    val playerNum = viewmodel.playerNumber.collectAsState()
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        ModeSelectList(gameModeList, gameMode.value) {position->
            viewmodel.handleIntent(GameIntent.updateGameMode(position))
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(gameMode.value == 0) {
            RoomEntranceCard(viewmodel)
        }

        AnimatedVisibility(gameMode.value == 1) {
            CommonButton("选择游玩人数 " + numberList[playerNum.value], onClick = {
                showNumberPicker = true
            })
        }

        AnimatedVisibility(gameMode.value == 1) {
            val gameStateList = viewmodel.gameEntity.collectAsState().value.timeEntityList
            if (gameStateList.isNotEmpty()) {
                GreetingView(
                    gameStateList.last().spyNum,
                    gameStateList.last().gameWord
                )
            }
        }
    }


    WeSingleColumnPicker(
        visible = showNumberPicker,
        title = "选择游玩人数",
        range = numberList,
        onCancel = { showNumberPicker = false },
        onChange = { viewmodel.handleIntent(GameIntent.updatePlayerNum(it)) },
        value = playerNum.value
    )
}