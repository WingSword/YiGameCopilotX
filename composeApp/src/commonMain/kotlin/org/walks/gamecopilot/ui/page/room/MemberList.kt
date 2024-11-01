package org.walks.gamecopilot.ui.page.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.data.entity.MemberEntry
import org.walks.gamecopilot.ui.button.CircleButton


@Composable
fun MemberList(itemList: List<MemberEntry>) {

    LazyColumn(
        modifier = Modifier.padding(start = 24.dp).fillMaxWidth().heightIn(min = 200.dp)
            .background(Color.White, shape = RoundedCornerShape(32, 0, 0, 0))
            .padding(top = 32.dp, bottom = 10.dp),
        reverseLayout = true
    ) {
        items(itemList.size) {
            RoomMemberListItem(itemList[it])
        }
    }

}

@Composable
fun RoomMemberListItem(item: MemberEntry) {
    var memberStanding by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier.height(64.dp).width(128.dp)
            .background(
                color = Color(0xFFF6B550), shape = RoundedCornerShape(0, 32, 32, 0)
            ).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleButton("${item.playerNo}号", canClickable = !item.isMine) {
            memberStanding = (memberStanding + 1) % roomMemberListItemStanding.size
        }
        Text(
            if (item.isMine) "自己" else roomMemberListItemStanding[memberStanding],
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

val roomMemberListItemStanding: List<String> = listOf("身份存疑", "卧底", "好人")