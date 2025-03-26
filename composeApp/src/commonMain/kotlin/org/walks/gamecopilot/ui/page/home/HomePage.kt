package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
