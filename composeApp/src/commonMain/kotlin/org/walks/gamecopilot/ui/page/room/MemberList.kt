package org.walks.gamecopilot.ui.page.room

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.MorandiBlue
import com.yi.yigamecopilot.android.theme.MorandiColorList
import com.yi.yigamecopilot.android.theme.MorandiGreen
import com.yi.yigamecopilot.android.theme.MorandiRed
import com.yi.yigamecopilot.android.theme.MorandiYellow
import org.walks.gamecopilot.data.entity.MemberEntry
import org.walks.gamecopilot.ui.button.CircleButton


@Composable
fun MemberList(itemList: List<MemberEntry>) {
    val scrollState = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
             .padding(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = scrollState
    ) {
        items(itemList.size) {
            RoomMemberListItem(itemList[it])
        }
    }
}

@Composable
fun RoomMemberListItem(item: MemberEntry) {
    var memberStanding by remember { mutableStateOf(0) }
    Column (
        modifier = Modifier.size(88.dp)
            .background(
                color =if(item.isMine) MorandiBlue else roomMemberListItemStandingColor[memberStanding], shape = RoundedCornerShape(12.dp)
            )
            .clickable (enabled = !item.isMine ){
                memberStanding = (memberStanding + 1) % roomMemberListItemStanding.size
            }
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.playerNo.toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.W900,
            color =(if(item.isMine)MaterialTheme.colorScheme.primary else  MaterialTheme.colorScheme.onPrimary).copy(alpha = 0.45f),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "["+if (item.isMine) "自己]" else roomMemberListItemStanding[memberStanding]+"]",
            textAlign = TextAlign.Center,
            color = if (item.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraLight,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
    }
}

val roomMemberListItemStandingColor: List<Color> = listOf(MorandiYellow, MorandiRed, MorandiGreen)
val roomMemberListItemStanding: List<String> = listOf("身份存疑", "卧底", "好人")