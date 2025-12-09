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
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.sharp.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_CARD
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_COIN
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_DICE
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_WHEEL
import org.walks.gamecopilot.clickableWithoutRipple
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_card
import yigamecopilotx.composeapp.generated.resources.icon_coin
import yigamecopilotx.composeapp.generated.resources.icon_dice
import yigamecopilotx.composeapp.generated.resources.icon_wheel_svg


/**
 *  Created by Wing at 09:45 on 2025/4/27
 *
 */

enum class RandomCate(val key: String, val iconRes: DrawableResource?) {
    Empty("", null),
    Card(RANDOM_PAGE_CONFIG_CATE_CARD, Res.drawable.icon_card),
    Dice(RANDOM_PAGE_CONFIG_CATE_DICE, Res.drawable.icon_dice),
    Coin(RANDOM_PAGE_CONFIG_CATE_COIN, Res.drawable.icon_coin),
    Wheel(RANDOM_PAGE_CONFIG_CATE_WHEEL, Res.drawable.icon_wheel_svg);

    companion object {
        fun getCateByKey(key: String): RandomCate {
            return when (key) {
                RANDOM_PAGE_CONFIG_CATE_CARD -> Card
                RANDOM_PAGE_CONFIG_CATE_DICE -> Dice
                RANDOM_PAGE_CONFIG_CATE_COIN -> Coin
                RANDOM_PAGE_CONFIG_CATE_WHEEL -> Wheel
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
            else if (item.startsWith(RANDOM_PAGE_CONFIG_CATE_WHEEL))
                Wheel
            else
                Empty
        }
    }

}

@Composable
fun AddNewRandomCateActionBar(select: String, onClick: (RandomCate) -> Unit) {
    LazyRow(horizontalArrangement = spacedBy(4.dp)) {
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
                        .clickableWithoutRipple { onClick(cate) },
                    tint = if (isSelected) Color.Unspecified
                    else MaterialTheme.colorScheme.secondary.copy(0.5f)
                )
            }

        }
    }
}


@Composable
fun AddRandomDiceContent(
    diceListState: SnapshotStateList<RandomItem>
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = spacedBy(16.dp)
    ) {
        // 添加骰子配置按钮
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Sharp.Add,
                contentDescription = "新增骰子配置",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        diceListState.add(RandomItem(first = "1", second = "6"))
                    }
                    .border(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(0.6f),
                        width = 2.dp
                    )
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        // 骰子配置列表
        diceListState.forEachIndexed { index, diceState ->
            DiceItemInput(
                diceState = diceState,
                onDelete = {
                    diceListState.remove(diceState)
                }
            )
        }
    }
}

@Composable
fun DiceItemInput(
    diceState: RandomItem,
    onDelete: () -> Unit
) {
    var minValue by remember { mutableStateOf(diceState.first) }
    var maxValue by remember { mutableStateOf(diceState.second) }
    var showMinPicker by remember { mutableStateOf(false) }
    var showMaxPicker by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 最小面输入
        OutlinedTextField(
            onValueChange = {
                diceState.first = it
                minValue = it
            },
            label = { Text(text = "最小面", fontSize = 12.sp) },
            value = minValue,
            modifier = Modifier
                .weight(1f)
                .clickable { showMinPicker = true },
            supportingText = {
                Text(
                    text = "点击选择数字",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(0.55f)
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.55f),
                unfocusedTextColor = MaterialTheme.colorScheme.secondary.copy(0.55f),
                focusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.secondary,
            )
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "到",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.width(8.dp))

        // 最大面输入
        OutlinedTextField(
            onValueChange = {
                diceState.second = it
                maxValue = it
            },
            label = { Text(text = "最大面", fontSize = 12.sp) },
            value = maxValue,
            modifier = Modifier
                .weight(1f)
                .clickable { showMaxPicker = true },
            supportingText = {
                Text(
                    text = "点击选择数字",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(0.55f)
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.55f),
                unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(0.55f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.primary,
            )
        )

        Spacer(Modifier.width(8.dp))

        // 删除按钮
        Icon(
            imageVector = Icons.Sharp.Clear,
            contentDescription = "删除",
            modifier = Modifier
                .size(24.dp)
                .clickable { onDelete() },
            tint = MaterialTheme.colorScheme.error
        )
    }

    // 数字选择器
    val numberList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    WeSingleColumnPicker(
        visible = showMinPicker,
        title = "选择最小面",
        onCancel = { showMinPicker = false },
        range = numberList,
        onChange = { selectedIndex ->
            val selectedValue = numberList[selectedIndex]
            diceState.first = selectedValue
            minValue = selectedValue
            showMinPicker = false
        },
        value = numberList.indexOf(minValue).coerceAtLeast(0)
    )

    WeSingleColumnPicker(
        visible = showMaxPicker,
        title = "选择最大面",
        onCancel = { showMaxPicker = false },
        range = numberList,
        onChange = { selectedIndex ->
            val selectedValue = numberList[selectedIndex]
            diceState.second = selectedValue
            maxValue = selectedValue
            showMaxPicker = false
        },
        value = numberList.indexOf(maxValue).coerceAtLeast(0)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddRandomListContent(
    cardListState: SnapshotStateList<RandomItem>,
    randomCate: String = RANDOM_PAGE_CONFIG_CATE_CARD
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = spacedBy(4.dp),
    ) {
        // 添加按钮
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Sharp.Add,
                contentDescription = "新增一项",
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .clickable {
                        cardListState.add(RandomItem())
                    }
                    .border(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(0.6f),
                        width = 2.dp
                    )
                    .padding(vertical = 4.dp, horizontal = 24.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        // 列表项
        cardListState.forEach { cardState ->
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
                    .heightIn(max = 600.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                var randomName by remember { mutableStateOf("") }
                var selectedCate by remember { mutableStateOf(RANDOM_PAGE_CONFIG_CATE_CARD) }
                val cardListState = remember { SnapshotStateList<RandomItem>() }
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
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
                            AddRandomDiceContent(diceListState = cardListState)
                        }
                        RANDOM_PAGE_CONFIG_CATE_WHEEL -> {
                            AddRandomWheelContent(wheelListState = cardListState)
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

/**
 * 转盘配置内容组件
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRandomWheelContent(
    wheelListState: SnapshotStateList<RandomItem>
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = spacedBy(16.dp)
    ) {
        // 添加转盘选项按钮
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Sharp.Add,
                contentDescription = "新增转盘选项",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (wheelListState.size < 20) {
                            wheelListState.add(
                                RandomItem(
                                    first = "选项${wheelListState.size + 1}",
                                    second = ""
                                )
                            )
                        }
                    }
                    .border(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(0.6f),
                        width = 2.dp
                    )
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        // 显示当前选项数量
        Text(
            text = "当前选项: ${wheelListState.size}/20",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp
        )

        // 转盘选项列表 - 使用FlowRow实现两列布局
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(8.dp),
            verticalArrangement = spacedBy(8.dp)
        ) {
            wheelListState.forEachIndexed { index, wheelState ->
                Box(modifier = Modifier.weight(0.5f)) {
                    WheelItemInput(
                        wheelState = wheelState,
                        index = index,
                        onDelete = {
                            wheelListState.remove(wheelState)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 转盘选项输入组件
 */
@Composable
fun WheelItemInput(
    wheelState: RandomItem,
    index: Int,
    onDelete: () -> Unit
) {
    var optionText by remember { mutableStateOf(wheelState.first) }
    var descriptionText by remember { mutableStateOf(wheelState.second) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // 选项编号
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "选项 ${index + 1}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // 删除按钮
            Icon(
                imageVector = Icons.Sharp.Clear,
                contentDescription = "删除",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDelete() },
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 选项名称输入
        OutlinedTextField(
            onValueChange = {
                wheelState.first = it
                optionText = it
            },
            label = { Text(text = "选项名称", fontSize = 12.sp) },
            value = optionText,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.55f),
                unfocusedTextColor = MaterialTheme.colorScheme.secondary.copy(0.55f),
                focusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.secondary,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 描述部分（可折叠）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                isDescriptionExpanded = !isDescriptionExpanded
            }
        ) {
            Text(
                text = "描述",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            // 展开/折叠图标
            Icon(
                imageVector = if (isDescriptionExpanded) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (isDescriptionExpanded) "折叠描述" else "展开描述",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        // 展开的描述输入框
        if (isDescriptionExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                onValueChange = {
                    wheelState.second = it
                    descriptionText = it
                },
                value = descriptionText,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.55f),
                    unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(0.55f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.88f),
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}
