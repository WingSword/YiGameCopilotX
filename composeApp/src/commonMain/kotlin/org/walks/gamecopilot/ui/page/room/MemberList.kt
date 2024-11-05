package org.walks.gamecopilot.ui.page.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.data.entity.MemberEntry
import org.walks.gamecopilot.ui.button.CircleButton


@Composable
fun MemberList(itemList: List<MemberEntry>) {

    val minWidth=(100+itemList.size*44).dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(start = 24.dp).fillMaxWidth().heightIn(min = minWidth)
            .background(Color.White, shape = RoundedCornerShape(32, 0, 0, 0))
            .padding(top = 56.dp, bottom = 10.dp),
        reverseLayout = true,
        horizontalArrangement = Arrangement.spacedBy(16.dp),

    ) {
        items(itemList.size) {
            RoomMemberListItem(itemList[it], it % 2 == 0)
        }
    }

}

@Composable
fun RoomMemberListItem(item: MemberEntry, isLeft: Boolean) {
    var memberStanding by remember { mutableStateOf(0) }
    val bubbleShape =
        if (isLeft) RoundedCornerShape(0, 32, 32, 0)
        else RoundedCornerShape(32, 0, 0, 32)
    Row(
        modifier = Modifier.padding(bottom = 16.dp).height(64.dp).width(128.dp)
            .background(
                color = Color(0xFFF6B550), shape = bubbleShape
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