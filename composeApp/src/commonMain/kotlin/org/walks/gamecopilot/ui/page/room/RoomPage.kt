package org.walks.gamecopilot.ui.page.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.MemberEntry


@Composable
fun RoomPage(viewmodel: MainViewmodel) {
    var animVisible by remember { mutableStateOf(false) }
    val roomState=viewmodel.roomEntityState.collectAsState()
    val memberList = mutableStateListOf<MemberEntry>()
    for (memberNo in 1.. roomState.value.playerNum) {
        memberList.add(MemberEntry(memberNo,memberNo==roomState.value.playerNo))
    }

    Column {
        FlopArea(viewmodel,roomState)
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            visible = animVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 500)
            )
        ) {
            MemberList(memberList)
        }
    }
    // 可以根据路由导航的时机来触发visible为true
    LaunchedEffect(Unit) {
        animVisible = true
    }
}

