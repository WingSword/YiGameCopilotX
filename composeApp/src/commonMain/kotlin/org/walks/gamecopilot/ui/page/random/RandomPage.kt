package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.MorandiBlue
import com.yi.yigamecopilot.android.theme.MorandiGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_COIN
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_DICE
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.ui.animation.DiceAnimation
import org.walks.gamecopilot.ui.animation.RollCoinAnimation
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_card
import kotlin.math.ceil
import kotlin.random.Random

/**
 *  Created by Wing at 09:47 on 2025/4/25
 *  随机组件页
 */
// 洗牌动画配置
private const val SHUFFLE_CYCLE_DURATION = 800  // 单次循环时长
private const val MAX_SHUFFLE_CYCLES = 5       // 循环次数
private val CARD_OFFSET = 64.dp               // 最大位移量
private const val MAX_ROTATION = 30f          // 最大旋转角度
private const val SCALE_FACTOR = 1.2f         // 缩放比例

// 新增动画配置参数
private const val SHUFFLE_DURATION = 1200
private val CONTRACT_OFFSET = 96.dp
private const val MIN_SCALE = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomPage(viewmodel: MainViewmodel) {
    var addRandomDialogShow by remember { mutableStateOf(false) }
    var isShuffling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    viewmodel.handleRandomPageIntent(RandomPageIntent.OnChangeNewRandomLabel)
    // 修改为 mutableStateList 类型
    var randomLabelsList = viewmodel.randomLabelsState.value.asReversed()
    var itemList = viewmodel.currentRandomContentState.collectAsState().value.list
    var currentSelectLabel = viewmodel.currentRandomContentState.collectAsState().value.name

    var randomContentDisplayState = remember {
        mutableIntStateOf(0)
    }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(viewmodel.addRandomConfigDialogState) {
        viewmodel.addRandomConfigDialogState.collectLatest {
            addRandomDialogShow = it
        }
    }

    LaunchedEffect(viewmodel.currentRandomContentState.collectAsState().value.refreshTime) {
        PlatformHelper.getInstance().vibrateMethod()
        isShuffling = !isShuffling
        scope.launch {
            delay(500L)
            isShuffling = !isShuffling
        }

    }

    Column(
        modifier = Modifier.fillMaxSize().clickable { isEditMode = false }
    ) {
        val currentType = RandomCate.getCateByItem(currentSelectLabel)

        // 顶部操作栏 - 改为圆形布局
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(onLongClick = { isEditMode = true }, onClick = {})
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 一键翻转按钮（仅当选择卡片类型时显示）
                if (currentType == RandomCate.Card) {
                    AnimatedVisibility(currentType == RandomCate.Card) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (randomContentDisplayState.intValue == 0) MorandiBlue else MorandiGreen,
                            ),
                            onClick = {
                                randomContentDisplayState.intValue =
                                    if (randomContentDisplayState.intValue == 0) 1 else 0
                            }
                        ) {
                            Text("一键翻转")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 配置列表 - 圆形布局
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = randomLabelsList,
                        key = { it.hashCode() }
                    ) { i ->
                        val currentSelectLabelType = RandomCate.getCateByItem(i)
                        RandomConfigCircleItem(
                            isSelected = currentSelectLabel == i,
                            isEdit = isEditMode,
                            label = i.replaceFirst(currentSelectLabelType.key, ""),
                            iconRes = currentSelectLabelType.iconRes ?: Res.drawable.icon_card,
                            onClick = {
                                isEditMode = false
                                if (currentSelectLabel == i) {
                                    currentSelectLabel = ""
                                    viewmodel.handleRandomPageIntent(
                                        RandomPageIntent.OnCancelLabel("")
                                    )
                                } else {
                                    currentSelectLabel = i
                                    viewmodel.handleRandomPageIntent(
                                        RandomPageIntent.OnSelectLabel(i)
                                    )
                                }
                            },
                            onDelete = {
                                viewmodel.handleRandomPageIntent(
                                    RandomPageIntent.DeleteRandomConfig(i)
                                )
                                isEditMode = false
                            }
                        )
                    }
                }
            }
        }

        // 内容区域
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f)
        ) {

            items(itemList) { randomItem ->
                AnimatedShuffleContent(
                    card = randomItem,
                    isShuffling = isShuffling,
                    index = itemList.indexOf(randomItem),
                    total = itemList.size,
                    contentCate = currentType,
                    randomState = randomContentDisplayState
                )
            }
        }
    }
    AddNewRandomDialog(addRandomDialogShow, onDismiss = { addRandomDialogShow = false }, onSave = {
        addRandomDialogShow = false
        viewmodel.handleRandomPageIntent(RandomPageIntent.OnAddNewRandom(it))
        viewmodel.handleRandomPageIntent(RandomPageIntent.OnChangeNewRandomLabel)
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedShuffleDice(
    start: Int,
    end: Int,
    roll: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DiceAnimation(range = start..end, isRollingDice = roll)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$start-$end",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                shape = RoundedCornerShape(8.dp)
            ).padding(horizontal = 24.dp, vertical = 4.dp).combinedClickable(
                onLongClickLabel = "长按删除",
                onLongClick = {
                    PlatformHelper.getInstance().vibrateMethod()

                },
                onClick = {
                    PlatformHelper.getInstance().vibrateMethod()
                }
            )
        )

    }

}

// 创建扩展函数
fun <T> List<T>.shuffledWithAnimation(): List<T> {
    return this
        .map { it to Random.nextFloat() }
        .sortedBy { it.second }
        .map { it.first }
}

// 使用 Fisher-Yates 洗牌算法
fun <T> List<T>.optimizedShuffle(): List<T> {
    val list = this.toMutableList()
    for (i in list.size - 1 downTo 1) {
        val j = Random.nextInt(i + 1)
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
    }
    return list
}

@Composable
fun FlippableCard(
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit,
    front: @Composable () -> Unit,
    cardColor: Color = Color.White,
    cornerRadius: Dp = 8.dp,
    animationDuration: Int = 500,
    flippedState: MutableIntState = mutableIntStateOf(0)
) {
    var flipped by remember { mutableStateOf(true) }
    LaunchedEffect(flippedState.value) {
        flipped = flippedState.value != 0
    }

    val rotation = animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(animationDuration), label = "cardRotation",

        )

    val cameraDistance = 12f * LocalDensity.current.density

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = rotation.value  // 改回X轴旋转
                this.cameraDistance = cameraDistance
            }
            .fillMaxWidth(0.33f)
            .aspectRatio(1.25f)
            .shadow(
                4.dp,
                shape = RoundedCornerShape(cornerRadius, cornerRadius, 0.dp, cornerRadius)
            )
            .background(
                cardColor,
                shape = RoundedCornerShape(cornerRadius, cornerRadius, 0.dp, cornerRadius)
            )
            .clickable { flipped = !flipped }
    ) {
        // 正面内容（前90度可见）
        if (rotation.value <= 90f) {
            CardContent(
                rotation = rotation.value,
                isFront = true,
                content = back
            )
        }
        // 背面内容（后90度可见）
        else {
            CardContent(
                rotation = rotation.value,
                isFront = false,
                content = front
            )
        }
    }
}

@Composable
private fun CardContent(
    rotation: Float,
    isFront: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // 关键修正：保持内容正立的补偿逻辑
                rotationX = if (isFront) 0f else -(180f)
                // 优化透明度过渡
                alpha = when {
                    isFront && rotation < 90f -> 1f - (rotation / 90f)
                    !isFront && rotation > 90f -> (rotation - 90f) / 90f
                    else -> 0f
                }

            }
            .padding(10.dp)
    ) {
        content()
    }
}

@Composable
fun AssistChipRandom(icon: ImageVector, onclick: () -> Unit, text: String) {
    AssistChip(
        onClick = {
            onclick()
        },
        label = { Text(text, color = MaterialTheme.colorScheme.tertiary) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = "Localized description",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomModFilterChip(
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isEdit: Boolean = false,
    isSelected: Boolean = false,
    onclick: (Boolean) -> Unit,
    onDelete: () -> Unit
) {

    FilterChip(
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceDim
            },
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = true,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent
        ),
        modifier = Modifier.height(44.dp),
        shape = CircleShape,
        onClick = {
            onclick(false)
        },
        label = {
            Text(label)
        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            {
                leadingIcon?.invoke()
            }
        },
        trailingIcon = {
            if (isEdit) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Localized description",
                    Modifier.size(InputChipDefaults.AvatarSize).clickable {
                        onDelete()
                    }
                )
            }
        },
    )
}


@Composable
private fun AnimatedShuffleContent(
    card: RandomItem,
    isShuffling: Boolean,
    index: Int,
    total: Int,
    contentCate: RandomCate = RandomCate.Card,
    randomState: MutableIntState
) {
    val density = LocalDensity.current.density

    // 生成随机动画参数（每个卡片不同）
    val (randomRotate, _) = remember(index) {
        Pair(
            (-60..60).random().toFloat(), // 扩大旋转角度范围
            1 // 扩大缩放范围f
        )
    }

    // 水平位移动画（向中心聚拢）
    val offsetX by animateFloatAsState(
        targetValue = if (isShuffling) {
            // 根据网格位置计算偏移量
            val column = index % 3
            when (column) {
                2 -> -100f * density // 左侧卡片右移
                0 -> 100f * density  // 右侧卡片左移
                else -> 0f           // 中间卡片不动
            }
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "centerOffset"
    )

    // 垂直位移动画（向中间聚拢）
    val offsetY by animateFloatAsState(
        targetValue = if (isShuffling) {
            // 计算实际行数（每行3个）
            val row = index / 3
            // 计算总行数（精确算法）
            val totalRows = ceil(total.toDouble() / 3).toInt()
            // 计算精确中间点
            val middlePoint = (totalRows - 1) / 2f

            // 计算偏移量（正数向上，负数向下）
            (middlePoint - row) * 40f * density // 注意符号方向调整
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "verticalCenterOffset"
    )


    // 旋转动画
    val rotation by animateFloatAsState(
        targetValue = if (isShuffling) randomRotate else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shuffleRotation"
    )

    // 3D旋转
    val rotateX by animateFloatAsState(
        targetValue = if (isShuffling) 25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "3dRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .graphicsLayer {
                cameraDistance = 12f * density
                translationX = offsetX
                translationY = offsetY
                rotationZ = rotation
                rotationX = rotateX
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
            }
    ) {
        when (contentCate.key) {
            RANDOM_PAGE_CONFIG_CATE_DICE -> {
                // 为骰子类型提供默认数值，避免字符串转换错误
                val start = card.first.toIntOrNull() ?: 1
                val end = card.second.toIntOrNull() ?: 6
                AnimatedShuffleDice(start, end, isShuffling)
            }

            RANDOM_PAGE_CONFIG_CATE_COIN -> {
                RollCoinAnimation(
                    onFlipComplete = { result ->
                        // 处理硬币翻转结果
                        PlatformHelper.getInstance().vibrateMethod()
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),

                )
            }

            else -> {

                FlippableCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),
                    back = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                "" + card.id + "",
                                color = MaterialTheme.colorScheme.primary.copy(0.3f),
                                fontWeight = FontWeight.W900,
                                fontSize = 50.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                card.first,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.9F),
                                modifier = Modifier.fillMaxHeight()
                            )
                        }


                    },
                    front = { Text(card.second, color = MorandiBlue) },
                    flippedState = randomState
                )
            }
        }
    }
}

// 圆形配置项组件
@Composable
fun RandomConfigCircleItem(
    isSelected: Boolean,
    isEdit: Boolean,
    label: String,
    iconRes: DrawableResource,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = label.take(4), // 限制显示长度
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            if (isEdit) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() },
                    tint = Color.Red
                )
            }
        }
    }
}
