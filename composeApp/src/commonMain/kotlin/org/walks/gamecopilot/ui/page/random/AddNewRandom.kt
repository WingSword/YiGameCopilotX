package org.walks.gamecopilot.ui.page.random

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.data.RandomCardItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_NAME_SETTING_KEY
import org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_SETTING_KEY
import org.walks.gamecopilot.ui.input.HalfRadioTextField
import org.walks.gamecopilot.ui.popup.WePopup

/**
 *  Created by Wing at 09:45 on 2025/4/27
 *
 */


@Composable
fun AddNewRandomDialog(
    isShow: Boolean,
    editRandomState: RandomListEntity? = null,
    onDismiss: () -> Unit,
    onSave: (RandomListEntity) -> Unit
) {
    var pageState by remember {
        mutableStateOf(0)
    }
    val cardListState = remember {
        mutableStateListOf(RandomCardItem())
    }
    var cardTitle by remember {
        mutableStateOf("")
    }

    WePopup(
        visible = isShow,
        onClose = {
            onDismiss()
        }
    ) {

        Column(
            modifier = Modifier.heightIn(max = 600.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 4.dp, end = 4.dp, top = 12.dp)
        ) {
            when (pageState) {
                0 -> {
                    AddRandomListContent(cardListState)
                }

                1 -> {
                    HalfRadioTextField(
                        value = cardTitle,
                        onValueChange = {
                            cardTitle = it
                        },
                        label = "请输入配置名称"
                    )
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = if (pageState == 1) "上一步" else "",
                    modifier = Modifier.weight(1f).clickable {
                        if (pageState == 0) {
                            return@clickable
                        }
                        pageState = 0
                    }.clip(CircleShape)
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    textAlign = TextAlign.Center,
                    text = if (pageState == 0) "下一步" else "保存",
                    modifier = Modifier.weight(1f).clickable {
                        if (pageState == 0) {
                            pageState = 1
                            return@clickable
                        }
                        onSave(RandomListEntity(cardListState, cardTitle))
                    }.background(
                        shape = CircleShape,
                        color = if (pageState == 0) Color.Transparent else MaterialTheme.colorScheme.primary
                    )
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddRandomListContent(cardListState: SnapshotStateList<RandomCardItem>) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stickyHeader {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "新增一项",
                    modifier = Modifier.weight(1f).clickable {
                        cardListState.add(RandomCardItem())
                    }.clip(CircleShape)
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    textAlign = TextAlign.Center,
                    text = "删除一项",
                    modifier = Modifier.weight(1f).clickable {
                        cardListState.remove(cardListState.find { it.back.isEmpty() && it.front.isEmpty() })
                    }.clip(CircleShape)
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        items(cardListState) { cardState ->
            ItemInput(cardState)
        }

    }
}

fun saveSetting() {

}

@Composable
fun ItemInput(cardState: RandomCardItem) {
    var cardTextBack by remember {
        mutableStateOf(cardState.back)
    }
    var cardTextFront by remember {
        mutableStateOf(cardState.front)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            onValueChange = {
                cardState.back = it
                cardTextBack = it
            },
            label = {
                Text(text = "反面（第一面）", fontSize = 12.sp)
            },
            value = cardTextBack,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp),
            shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(
                    0.55f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.secondary.copy(
                    0.55f
                ),
                focusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.secondary,
            )
        )
        Spacer(Modifier.width(4.dp))
        OutlinedTextField(
            onValueChange = {
                cardState.front = it
                cardTextFront = it
            },
            label = {
                Text(text = "正面（第二面）", fontSize = 12.sp)
            },
            value = cardTextFront,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp),
            shape = RoundedCornerShape(8.dp, 0.dp, 8.dp, 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                    0.55f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(
                    0.55f
                ),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ),
        )

    }
}


