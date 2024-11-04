package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.button.CircleButton
import org.walks.gamecopilot.ui.input.HalfRadioTextField

@Composable
fun RoomEntranceCard(viewmodel: MainViewmodel) {
    var roomName by remember { mutableStateOf("") }
    var roomKey by remember { mutableStateOf("") }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding().scrollable(
            state = rememberScrollState(),
            orientation = Orientation.Vertical
        )
    ) {
        Text(
            "创建/加入房间",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Card(
            modifier = Modifier.fillMaxWidth()
                .shadow(10.dp, shape = RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HalfRadioTextField(
                    value = roomName,
                    label = "房间名称",
                    onValueChange = { roomName = it })
                HalfRadioTextField(
                    value = roomKey,
                    label = "房间密钥",
                    onValueChange = { roomKey = it })

                Row(
                    modifier = Modifier.height(68.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    CircleButton("创建房间 ", onClick = {
                        viewmodel.handleIntent(
                            GameIntent.CreateAGameRoom(
                                roomName,
                                roomKey,
                            )
                        )
                    })
                    Spacer(Modifier.width(16.dp))
                    CircleButton("加入房间 ", backColor = MaterialTheme.colorScheme.secondaryContainer, onClick = {
                        GameIntent.JoinToAGameRoom(roomName, roomKey)
                    })
                }

            }
        }
    }
}