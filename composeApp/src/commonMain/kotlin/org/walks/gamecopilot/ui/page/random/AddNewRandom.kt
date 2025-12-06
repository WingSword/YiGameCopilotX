package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.Build
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_CARD
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_COIN
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_DICE
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_card
import yigamecopilotx.composeapp.generated.resources.icon_coin
import yigamecopilotx.composeapp.generated.resources.icon_dice


/**
 *  Created by Wing at 09:45 on 2025/4/27
 *
 */

enum class RandomCate(val key: String, val iconRes: DrawableResource?) {
    Empty("", null),
    Card(RANDOM_PAGE_CONFIG_CATE_CARD, Res.drawable.icon_card),
    Dice(RANDOM_PAGE_CONFIG_CATE_DICE, Res.drawable.icon_dice),
    Coin(RANDOM_PAGE_CONFIG_CATE_COIN, Res.drawable.icon_coin);

    companion object {
        fun getCateByKey(key: String): RandomCate {
            return when (key) {
                RANDOM_PAGE_CONFIG_CATE_CARD -> Card
                RANDOM_PAGE_CONFIG_CATE_DICE -> Dice
                RANDOM_PAGE_CONFIG_CATE_COIN -> Coin
                else -> Empty
            }
        }

        fun getCateByItem(item: String): RandomCate {
            return if (item.startsWith(RANDOM_PAGE_CONFIG_CATE_CARD))
                Card
            else if (item.startsWith(RANDOM_PAGE_CONFIG_CATE_DICE))
                Dice
            else if (item.startsWith(RANDOM_PAGE_CONFIG_CATE_COIN))
                Coin
            else
                Empty
        }
    }

}

@Composable
fun AddNewRandomCateActionBar(select: String, onClick: (RandomCate) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(RandomCate.entries) { cate ->
            val isSelected = select == cate.key
            // 边框宽度动画
            val animatedBorderWidth by animateDpAsState(
                targetValue = if (isSelected) 2.dp else 0.dp,
                animationSpec = tween(durationMillis = 300)
            )
            // 边框颜色动画
            val animatedBorderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary.copy(0.5f),
                animationSpec = tween(durationMillis = 300)
            )

            val animatedOffset by animateDpAsState(
                targetValue = if (isSelected) 2.dp else 0.dp
            )

            // 若要实现脉冲效果，可以添加无限动画
            val infiniteTransition = rememberInfiniteTransition()
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )

            cate.iconRes?.let {
                Icon(
                    painter = painterResource(cate.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp)
                        .offset(y = animatedOffset)
                        .border(
                            width = animatedBorderWidth,
                            color = if (select == cate.key) animatedBorderColor.copy(alpha = pulseAlpha) else MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clickable { onClick(cate) },
                    tint = if (isSelected) Color.Unspecified
                    else MaterialTheme.colorScheme.secondary.copy(0.5f)
                )
            }

        }
    }
}


@Composable
fun AddRandomDiceContent() {
    LazyColumn {
        item {
            Row(
                modifier = Modifier.height(44.dp).fillParentMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "选择最小面 ",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "选择最大面 ",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Sharp.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddRandomListContent(
    cardListState: SnapshotStateList<RandomItem>,
    randomCate: String = RANDOM_PAGE_CONFIG_CATE_CARD
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stickyHeader {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Sharp.Add,
                    contentDescription = "新增一项",
                    modifier = Modifier.fillMaxWidth(0.25f).clickable {
                        cardListState.add(RandomItem())
                    }.border(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(0.6f),
                        width = 2.dp
                    )
                        .padding(vertical = 4.dp, horizontal = 24.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )

            }
        }
        items(cardListState) { cardState ->
            ItemInput(cardState, randomCate, {
                cardListState.remove(cardState)
            })
        }

    }
}

fun saveSetting() {

}

@Composable
fun ItemInput(
    cardState: RandomItem,
    randomCate: String = RANDOM_PAGE_CONFIG_CATE_CARD,
    onDelete: () -> Unit
) {
    var textBack by remember {
        mutableStateOf(cardState.first)
    }
    var textFront by remember {
        mutableStateOf(cardState.second)
    }
    val firstDesc = when (randomCate) {
        RANDOM_PAGE_CONFIG_CATE_CARD -> "背面"
        RANDOM_PAGE_CONFIG_CATE_DICE -> "起始数字"
        else -> "第一面"
    }
    val secondDesc = when (randomCate) {
        RANDOM_PAGE_CONFIG_CATE_CARD -> "正面"
        RANDOM_PAGE_CONFIG_CATE_DICE -> "结束数字"
        else -> "第二面"
    }
    var showPicker by remember {
        mutableStateOf(0)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            onValueChange = {
                cardState.first = it
                textBack = it
            },

            label = {
                Text(text = firstDesc, fontSize = 12.sp)
            },
            value = textBack,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp).clickable {
                if (randomCate == RANDOM_PAGE_CONFIG_CATE_DICE) {
                    showPicker = 1
                }
            },
            supportingText = {
                if (randomCate == RANDOM_PAGE_CONFIG_CATE_DICE) {
                    Text(
                        text = "请输入数字",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(0.55f)
                    )
                }
            },

            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
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
                cardState.second = it
                textFront = it
            },
            label = {
                Text(text = secondDesc, fontSize = 12.sp)
            },
            supportingText = {
                if (randomCate == RANDOM_PAGE_CONFIG_CATE_DICE) {
                    Text(
                        text = "请输入数字",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(0.55f)
                    )
                }
            },

            value = textFront,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp).clickable {
                if (randomCate == RANDOM_PAGE_CONFIG_CATE_DICE) {
                    showPicker = 2
                }
            },
            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
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

        Icon(
            imageVector = Icons.Sharp.Clear,
            contentDescription = "删除",
            modifier = Modifier.padding(start = 8.dp).clickable {
                cardState.first = ""
                cardState.second = ""
                onDelete()
            },
            tint = MaterialTheme.colorScheme.error
        )
        val list = listOf("0", "1", "2", "3", "4", "5", "6")

        WeSingleColumnPicker(
            visible = showPicker != 0,
            title = "选择${if (showPicker == 1) "起始" else "结束"}数字",
            onCancel = {
                showPicker = 0
            },
            range = list,
            onChange = {
                if (showPicker == 2) {
                    if (list.get(it).toInt() > ((textBack.toIntOrNull()) ?: 0)) {
                        textFront = list.get(it)
                    }
                } else if (showPicker == 1) {
                    textBack = list.get(it)
                }
            },
            value = if (showPicker == 1) 1 else 6
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewRandomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (RandomListEntity) -> Unit
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                var randomName by remember { mutableStateOf("") }
                var selectedCate by remember { mutableStateOf(RANDOM_PAGE_CONFIG_CATE_CARD) }
                val cardListState = remember { SnapshotStateList<RandomItem>() }

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    // 顶部标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "添加新随机配置",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 配置名称输入
                    OutlinedTextField(
                        value = randomName,
                        onValueChange = { randomName = it },
                        label = { Text("配置名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 类型选择栏
                    Text(
                        text = "选择类型",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AddNewRandomCateActionBar(
                        select = selectedCate,
                        onClick = { cate ->
                            selectedCate = cate.key
                            cardListState.clear()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 内容编辑区域
                    when (selectedCate) {
                        RANDOM_PAGE_CONFIG_CATE_DICE -> {
                            AddRandomDiceContent()
                        }

                        else -> {
                            AddRandomListContent(
                                cardListState = cardListState,
                                randomCate = selectedCate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 底部操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                if (randomName.isNotBlank()) {
                                    onSave(
                                        RandomListEntity(
                                            name = selectedCate + randomName,
                                            list = cardListState.toList()
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = randomName.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}
