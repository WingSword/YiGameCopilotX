package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameIntent

@Composable
fun StartPage(viewmodel: MainViewmodel) {

    val gameModeList = listOf("谁是卧底", "谁是卧底（本地）", "谁是卧底3")
    val gameMode = viewmodel.startedGameMode.collectAsState()
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        ModeSelectList(gameModeList, gameMode.value) { position ->
            viewmodel.handleLocalGameIntent(GameIntent.SwitchGameMode(position))
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
