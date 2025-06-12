package org.walks.gamecopilot.ui.page.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.KeepScreenOn
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.MemberEntry
import org.walks.gamecopilot.intent.GameRoomIntent


@Composable
fun PrepairPage(playerNum: Int, isOwner: Boolean, startGame: () -> Unit) {
    var tips by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = tips.isNotEmpty()) {
            Text(
                tips,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.W500
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "房间已加入：$playerNum 人 ${if (isOwner && playerNum >= 2) "\n点击开始游戏" else if (isOwner) "" else "\n请等待房主开始游戏"}",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.W500,
            modifier = Modifier.border(
                2.dp,
                MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ).padding(12.dp).clickable(enabled = playerNum>=2){
                startGame()
            },
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun RoomPage(viewmodel: MainViewmodel) {
    KeepScreenOn()
    var animVisible by remember { mutableStateOf(false) }
    val roomState = viewmodel.roomEntityState.collectAsState()
    val memberList = viewmodel.roomEntityState.collectAsState().value.users

    memberList?.find { it.index==roomState.value.index }?.isMine=true

    if (roomState.value.role.isNullOrBlank() || roomState.value.role == "NOROLE") {
        PrepairPage(memberList?.size?:0, roomState.value.isRoomOwner, startGame = {
            viewmodel.handleRoomIntent(GameRoomIntent.StartGame)
        })
    } else {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Text(roomState.value.index.toString(), fontWeight = FontWeight.W900, fontSize = 199.sp, modifier = Modifier.fillMaxSize(), textAlign = TextAlign.End)
            AnimatedVisibility(
                visible = animVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                MemberList(memberList?:listOf())
            }
            FlopArea(roomState.value.assignedWord ?: "",roomState.value.updateTime)

        }
        // 可以根据路由导航的时机来触发visible为true
        LaunchedEffect(Unit) {
            animVisible = true
        }
    }

}

