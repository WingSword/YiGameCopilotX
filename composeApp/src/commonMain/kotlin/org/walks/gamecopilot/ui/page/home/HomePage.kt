package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.GreetingView
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.button.CommonButton
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

@Composable
fun StartPage(viewmodel: MainViewmodel) {

    val gameModeList = listOf("谁是卧底", "谁是卧底（本地）", "谁是卧底3")
    val gameMode = viewmodel.startedGameMode.collectAsState()
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        ModeSelectList(gameModeList, gameMode.value) { position ->
            viewmodel.handleIntent(GameIntent.SwitchGameMode(position))
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(gameMode.value == 0) {
            RoomEntranceCard(viewmodel)
        }

        AnimatedVisibility(gameMode.value == 1) {
            LocalSpyGame(viewmodel)
        }

        AnimatedVisibility(gameMode.value == 1) {

        }
    }


}

@Composable
fun LocalSpyGame(viewmodel: MainViewmodel) {

    var showNumberPicker by remember { mutableStateOf(false) }
    val numberList = listOf("4", "5", "6", "7", "8", "9", "10")
    val gameStateList = viewmodel.gameEntity.collectAsState().value.timeEntityList
    val playerNum = gameStateList.lastOrNull()?.gamePlayerNumber ?: 4
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            CommonButton("选择游玩人数 $playerNum", onClick = {
                showNumberPicker = true
            })

            if (playerNum>4) {
                var showSpyNumberPicker by remember { mutableStateOf(false) }
                val list={1..playerNum/2-1}
                Spacer(Modifier.width(8.dp))
                CommonButton("选择卧底人数", onClick = {
                    showSpyNumberPicker=true
                })

                WeSingleColumnPicker(
                    visible = showSpyNumberPicker,
                    title = "选择卧底人数",
                    range = list as List<String>,
                    onCancel = { showSpyNumberPicker = false },
                    onChange = { viewmodel.handleIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt())) },
                    value = numberList.indexOf(playerNum.toString())
                )
            }
        }


        if (gameStateList.isNotEmpty()) {
            GreetingView(
                gameStateList.last()
            )
        }
    }


    WeSingleColumnPicker(
        visible = showNumberPicker,
        title = "选择游玩人数",
        range = numberList,
        onCancel = { showNumberPicker = false },
        onChange = { viewmodel.handleIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt())) },
        value = numberList.indexOf(playerNum.toString())
    )
}