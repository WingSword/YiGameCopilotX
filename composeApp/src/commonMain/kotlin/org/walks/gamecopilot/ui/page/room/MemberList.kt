package org.walks.gamecopilot.ui.page.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.yi.yigamecopilot.android.theme.MorandiGreen
import com.yi.yigamecopilot.android.theme.MorandiRed
import com.yi.yigamecopilot.android.theme.MorandiYellow
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.UserInfoEntity


@Composable
fun MemberList(itemList: List<UserInfoEntity>) {
    val scrollState = rememberLazyGridState()
    var showIdentity by remember { mutableStateOf(false) }
    var showAll by remember { mutableStateOf(false) }
    var outList = remember {
        mutableSetOf<String>()
    }
    LaunchedEffect(itemList) {
        PlatformHelper.getInstance().vibrateLongMethod()
        showAll = false
        outList = mutableSetOf()
        showIdentity = false
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
            .padding(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = scrollState
    ) {
        item(span = { GridItemSpan(3) }) {
            Row {
                TextButton(onClick = {
                    showIdentity = !showIdentity
                }) {
                    Text(
                        text = if (showIdentity) "取消" else "检视结果",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ).padding(8.dp)
                    )
                }
                AnimatedVisibility(showIdentity) {
                    TextButton(onClick = {
                        showAll = true
                    }) {
                        Text(
                            text = "查看所有玩家身份",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W900,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ).padding(8.dp)
                        )
                    }
                }
            }


        }
        items(itemList.size) {
            Column {
                Box() {
                    RoomMemberListItem(itemList[it])
                    if (showIdentity && !outList.contains(itemList[it].index)) {
                        TextButton(
                            onClick = {
                                outList.add(itemList[it].index ?: "")
                                showIdentity = false
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "投票",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W900,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(outList.contains(itemList[it].index)) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxSize()) {
                            Text(
                                text = "${itemList[it].role}已出局",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W900,
                                color = if (itemList[it].role == "SPY") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                if (showAll) {
                    Box() {
                        Text(
                            text = "${itemList[it].assignedWord}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W900,
                            color = if (itemList[it].role == "SPY") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun RoomMemberListItem(item: UserInfoEntity) {
    var memberStanding by remember { mutableStateOf(0) }


    Column(
        modifier = Modifier.size(88.dp)
            .background(
                color = if (item.isMine) MorandiBlue else roomMemberListItemStandingColor[memberStanding],
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !item.isMine) {
                memberStanding = (memberStanding + 1) % roomMemberListItemStanding.size
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.index.toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.W900,
            color = (if (item.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary).copy(
                alpha = 0.45f
            ),
            textAlign = TextAlign.Right
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "[" + if (item.isMine) "自己]" else roomMemberListItemStanding[memberStanding] + "]",
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