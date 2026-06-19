package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_CARD
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_COIN
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_DICE
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_FINGER
import org.walks.gamecopilot.RANDOM_PAGE_CONFIG_CATE_WHEEL
import org.walks.gamecopilot.RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME
import org.walks.gamecopilot.data.RandomItem
import org.walks.gamecopilot.data.RandomListEntity
import org.walks.gamecopilot.data.WheelItem
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.animation.DiceAnimation
import org.walks.gamecopilot.ui.animation.RollCoinAnimation
import org.walks.gamecopilot.ui.components.AppDialog
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_edit
import kotlin.math.ceil
import kotlin.random.Random

private const val SHUFFLE_CYCLE_DURATION = 800
private const val MAX_SHUFFLE_CYCLES = 5
private val CARD_OFFSET = 64.dp
private const val MAX_ROTATION = 30f
private const val SCALE_FACTOR = 1.2f
private const val SHUFFLE_DURATION = 1200
private val CONTRACT_OFFSET = 96.dp
private const val MIN_SCALE = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomPage(viewmodel: MainViewmodel) {
    val design = LocalAppDesign.current
    var addRandomDialogShow by remember { mutableStateOf(false) }
    var editRandomDialogShow by remember { mutableStateOf(false) }
    var editConfigName by remember { mutableStateOf("") }
    var isShuffling by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    viewmodel.handleRandomPageIntent(RandomPageIntent.OnChangeNewRandomLabel)
    val rawLabels = viewmodel.randomLabelsState.value.asReversed()
    val randomLabelsList = buildList {
        if (rawLabels.contains(RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME)) {
            add(RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME)
        }
        addAll(rawLabels.filter { it != RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME })
    }
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource()
            ) {
                isEditMode = false
            }
    ) {
        val currentType = RandomCate.getCateByItem(currentSelectLabel)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = design.spacing.lg)
        ) {
            RandomConfigList(
                randomLabelsList = randomLabelsList,
                currentSelectLabel = currentSelectLabel,
                isEditMode = isEditMode,
                onItemClick = { i ->
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
                onDelete = { i ->
                    if (isEditMode) {
                        viewmodel.handleRandomPageIntent(
                            RandomPageIntent.DeleteRandomConfig(i)
                        )
                    }
                    isEditMode = false
                },
                onLongClick = {
                    isEditMode = !isEditMode
                },
                onEdit = { configName ->
                    editConfigName = configName
                    editRandomDialogShow = true
                    isEditing = true
                    isEditMode = false
                },
                isSystemDefault = { label ->
                    label.startsWith(RANDOM_PAGE_CONFIG_CATE_FINGER) ||
                            label.startsWith(RANDOM_PAGE_CONFIG_CATE_ANSWER_BOOK)
                }
            )
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = design.cornerRadius.xxxl,
                topEnd = design.cornerRadius.xxxl
            ),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = design.elevation.lg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = design.spacing.lg,
                        start = design.spacing.lg,
                        end = design.spacing.lg,
                        bottom = design.spacing.xxxl
                    )
            ) {
                if (currentType == RandomCate.Finger) {
                    FingerSpinnerComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(design.spacing.md)
                    )
                } else if (currentType == RandomCate.AnswerBook) {
                    AnswerBookPage(
                        viewmodel = viewmodel,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                } else if (currentType == RandomCate.Wheel) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(design.spacing.lg),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val wheelItems = itemList.mapIndexed { index, randomItem ->
                            val weight = try {
                                randomItem.second.toFloatOrNull() ?: 1.0f
                            } catch (e: Exception) {
                                1.0f
                            }

                            WheelItem(
                                id = randomItem.id.toString(),
                                text = randomItem.first,
                                color = WheelItem.DEFAULT_COLORS.getOrElse(index) { Color.Gray },
                                weight = weight
                            )
                        }

                        WheelRandomComponent(
                            items = wheelItems,
                            onItemsChange = { newItems ->
                                viewmodel.updateWheelItems(newItems)
                            },
                            onTriggerRandom = {
                                viewmodel.handleRandomPageIntent(RandomPageIntent.TriggerRandom)
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(design.spacing.md)
                        ) {
                            itemList.forEach { randomItem ->
                                AnimatedShuffleContent(
                                    card = randomItem,
                                    isShuffling = isShuffling,
                                    index = itemList.indexOf(randomItem),
                                    total = itemList.size,
                                    contentCate = currentType,
                                    randomState = randomContentDisplayState,
                                    onTriggerRandom = {
                                        viewmodel.handleRandomPageIntent(RandomPageIntent.TriggerRandom)
                                    },
                                    viewmodel = viewmodel
                                )
                            }
                        }
                    }
                }
            }
        }

        if (currentType == RandomCate.Card) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.lg)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (randomContentDisplayState.intValue == 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(design.cornerRadius.button),
                    onClick = {
                        randomContentDisplayState.intValue =
                            if (randomContentDisplayState.intValue == 0) 1 else 0
                    }
                ) {
                    Text(
                        "一键翻转",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = design.spacing.lg)
                    )
                }
            }
        }

        AddNewRandomDialog(
            addRandomDialogShow,
            onDismiss = { addRandomDialogShow = false },
            onSave = {
                addRandomDialogShow = false
                viewmodel.handleRandomPageIntent(RandomPageIntent.OnAddNewRandom(it))
                viewmodel.handleRandomPageIntent(RandomPageIntent.OnChangeNewRandomLabel)
            })

        EditRandomDialog(
            editRandomDialogShow,
            onDismiss = {
                editRandomDialogShow = false
                isEditing = false
            },
            onSave = {
                editRandomDialogShow = false
                isEditing = false
                viewmodel.handleRandomPageIntent(RandomPageIntent.OnEditRandomConfig(it))
                viewmodel.handleRandomPageIntent(RandomPageIntent.OnChangeNewRandomLabel)
            },
            configName = editConfigName,
            onShow = { isEditing = true }
        )
    }
}

// ========== 独立组件函数 ==========

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

@Composable
fun FlippableCard(
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit,
    front: @Composable () -> Unit,
    cardColor: Color = Color.White,
    cornerRadius: Dp = 0.dp,
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
            .shadow(
                4.dp,
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(
                cardColor,
                shape = RoundedCornerShape(cornerRadius)
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
fun CardContent(
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
fun AnimatedShuffleContent(
    card: RandomItem,
    isShuffling: Boolean,
    index: Int,
    total: Int,
    contentCate: RandomCate = RandomCate.Card,
    randomState: MutableIntState,
    onTriggerRandom: () -> Unit = {},
    viewmodel: MainViewmodel
) {
    val density = LocalDensity.current.density

    // 生成随机动画参数（每个卡片不同）
    val (randomRotate, _) = remember(index) {
        Pair(
            (-60..60).random().toFloat(), // 扩大旋转角度范围
            1 // 扩大缩放范围f
        )
    }

    // 只有卡牌类型才应用洗牌动画
    val shouldShuffle = isShuffling && contentCate == RandomCate.Card

    // 水平位移动画（向中心聚拢）
    val offsetX by animateFloatAsState(
        targetValue = if (shouldShuffle) {
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
        targetValue = if (shouldShuffle) {
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
        targetValue = if (shouldShuffle) randomRotate else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shuffleRotation"
    )

    // 3D旋转
    val rotateX by animateFloatAsState(
        targetValue = if (shouldShuffle) 25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "3dRotation"
    )

    Box(
        modifier = Modifier
            .width(
                when (contentCate) {
                    RandomCate.Card -> 138.dp
                    else -> 116.dp
                }
            )
            .padding(6.dp)
            .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                // 点击触发随机事件
                onTriggerRandom()
            }
            .graphicsLayer {
                cameraDistance = 12f * density
                translationX = offsetX
                translationY = offsetY
                rotationZ = rotation
                rotationX = rotateX
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
    ) {
        when (contentCate.key) {
            RANDOM_PAGE_CONFIG_CATE_DICE -> {
                // 为骰子类型提供默认数值，避免字符串转换错误
                val start = card.first.toIntOrNull() ?: 1
                val end = card.second.toIntOrNull() ?: 6
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AnimatedShuffleDice(start, end, isShuffling)
                }
            }

            RANDOM_PAGE_CONFIG_CATE_COIN -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RollCoinAnimation(
                        onFlipComplete = { _ ->
                            // 处理硬币翻转结果
                            PlatformHelper.getInstance().vibrateMethod()
                        },
                        isRolling = isShuffling,
                        frontText = card.first,
                        backText = card.second,
                        modifier = Modifier
                            .width(108.dp)
                            .aspectRatio(1f),
                    )
                }
            }

            // 转盘类型现在单独显示，不再显示在网格中

            else -> {
                FlippableCard(
                    modifier = Modifier
                        .width(138.dp)
                        .aspectRatio(0.72f),
                    cardColor = MaterialTheme.colorScheme.primaryContainer,
                    back = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                "" + card.id + "",
                                color = MaterialTheme.colorScheme.primary.copy(0.28f),
                                fontWeight = FontWeight.W900,
                                fontSize = 50.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                card.first,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.fillMaxHeight(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    front = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                card.second,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    flippedState = randomState
                )
            }
        }
    }
}

// 配置列表组件 - 横向滚动布局
@Composable
fun RandomConfigList(
    randomLabelsList: List<String>,
    currentSelectLabel: String,
    isEditMode: Boolean,
    onItemClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onLongClick: (String) -> Unit,
    onEdit: (String) -> Unit = {},
    isSystemDefault: (String) -> Boolean = { false }
) {
    // 过滤掉空的配置项（没有类别的配置）
    val filteredLabels = randomLabelsList.filter { item ->
        val cate = RandomCate.getCateByItem(item)
        cate != RandomCate.Empty
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filteredLabels) { item ->
            val currentSelectLabelType = RandomCate.getCateByItem(item)
            val isProtected = isSystemDefault(item)
            // 确保有有效的图标资源
            val iconRes = currentSelectLabelType.iconRes ?: return@items

            RandomConfigCircleItem(
                isSelected = currentSelectLabel == item,
                isEdit = isEditMode,
                label = item.replaceFirst(currentSelectLabelType.key, ""),
                iconRes = iconRes,
                onClick = { onItemClick(item) },
                onDelete = {
                    if (!isProtected) {
                        onDelete(item)
                    }
                },
                onLongClick = { onLongClick(item) },
                onEdit = {
                    if (!isProtected) {
                        onEdit(item)
                    }
                },
                showEditActions = !isProtected
            )
        }
    }
}

// 圆形配置项组件
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomConfigCircleItem(
    isSelected: Boolean,
    isEdit: Boolean,
    label: String,
    iconRes: DrawableResource,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit = {},
    showEditActions: Boolean = true
) {
    val design = LocalAppDesign.current
    Column(
        modifier = Modifier
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(design.cornerRadius.lg))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(design.cornerRadius.lg)
                    )
                    .combinedClickable(
                        onLongClick = {
                            // 长按切换编辑模式
                            onLongClick()
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                // 主图标
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = if (isSelected)
                        Color.Unspecified
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }

            // 删除按钮（编辑模式下显示在右上角）
            if (isEdit && showEditActions) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = (-3).dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDelete() }
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .padding(2.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                // 编辑按钮（编辑模式下显示在左上角）
                Icon(
                    painter = painterResource(Res.drawable.icon_edit),
                    contentDescription = "编辑",
                    modifier = Modifier
                        .size(33.dp)
                        .align(Alignment.Center)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onEdit() }
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                            CircleShape
                        )
                        .padding(4.dp),
                    tint = Color.Unspecified
                )
            }
        }

        // 标签文字
        Text(
            text = label.take(4), // 限制显示长度
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 11.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1
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

/**
 * 文本骰子动画组件 - 显示用户输入的文本而不是数字
 */
@Composable
fun TextDiceAnimation(
    frontText: String,
    backText: String,
    isRolling: Boolean = false,
    modifier: Modifier = Modifier
) {
    val rotationY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var showFront by remember { mutableStateOf(true) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scope.launch {
                // 骰子翻转动画
                rotationY.animateTo(
                    targetValue = (720..1080).random().toFloat(),
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )

                // 计算最终角度：确保回正到0度或180度
                val currentAngle = rotationY.value % 360f
                val targetAngle = if (currentAngle < 180f) {
                    if (currentAngle < 90f) 0f else 180f
                } else {
                    if (currentAngle < 270f) 180f else 0f
                }

                // 缓慢停止到最终位置
                rotationY.animateTo(
                    targetValue = targetAngle,
                    animationSpec = tween(200)
                )

                // 根据最终角度确定显示哪一面
                showFront = targetAngle < 90f || targetAngle > 270f
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationY = rotationY.value
                transformOrigin = TransformOrigin.Center
                cameraDistance = 8f * density
            }
    ) {
        // 正面文本
        TextDiceFace(
            text = frontText,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 当旋转超过90度时隐藏正面
                    alpha = if (rotationY.value <= 90f || rotationY.value >= 270f) 1f else 0f
                }
        )

        // 背面文本
        TextDiceFace(
            text = backText,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 当旋转在90-270度之间时显示背面
                    alpha = if (rotationY.value > 90f && rotationY.value < 270f) 1f else 0f
                    // 背面文字需要反向旋转180度以保持正向显示

                }
        )
    }
}

/**
 * 文本骰子面组件
 */
@Composable
fun TextDiceFace(
    text: String,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(design.cornerRadius.lg)
            )
            .border(
                2.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(design.cornerRadius.lg)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * 编辑配置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRandomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (RandomListEntity) -> Unit,
    configName: String,
    onShow: () -> Unit = {}
) {
    val design = LocalAppDesign.current
    if (show && configName.isNotEmpty()) {
        // 通知父组件进入编辑状态
        LaunchedEffect(show) {
            if (show) {
                onShow()
            }
        }
                var randomName by remember { mutableStateOf("") }
                var selectedCate by remember { mutableStateOf(RANDOM_PAGE_CONFIG_CATE_CARD) }
                val cardListState = remember { SnapshotStateList<RandomItem>() }
                var isEditing by remember { mutableStateOf(true) }
                val scrollState = rememberScrollState()

                // 从配置名称中提取类型和名称
                LaunchedEffect(configName) {
                    // 判断配置类型
                    when {
                        configName.startsWith(RANDOM_PAGE_CONFIG_CATE_CARD) -> {
                            selectedCate = RANDOM_PAGE_CONFIG_CATE_CARD
                            randomName = configName.replaceFirst(RANDOM_PAGE_CONFIG_CATE_CARD, "")
                        }

                        configName.startsWith(RANDOM_PAGE_CONFIG_CATE_DICE) -> {
                            selectedCate = RANDOM_PAGE_CONFIG_CATE_DICE
                            randomName = configName.replaceFirst(RANDOM_PAGE_CONFIG_CATE_DICE, "")
                        }

                        configName.startsWith(RANDOM_PAGE_CONFIG_CATE_COIN) -> {
                            selectedCate = RANDOM_PAGE_CONFIG_CATE_COIN
                            randomName = configName.replaceFirst(RANDOM_PAGE_CONFIG_CATE_COIN, "")
                        }

                        configName.startsWith(RANDOM_PAGE_CONFIG_CATE_WHEEL) -> {
                            selectedCate = RANDOM_PAGE_CONFIG_CATE_WHEEL
                            randomName = configName.replaceFirst(RANDOM_PAGE_CONFIG_CATE_WHEEL, "")
                        }
                    }

                    // 加载配置数据
                    try {
                        val jsonCards = org.walks.gamecopilot.mmkv.MMKVUtils.getString(
                            org.walks.gamecopilot.mmkv.MMKV_RANDOM_CARDS_SETTING_KEY + configName,
                            ""
                        )
                        if (jsonCards.isNotEmpty()) {
                            val config = Json.decodeFromString<RandomListEntity>(jsonCards)
                            cardListState.clear()
                            cardListState.addAll(config.list)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

        AppDialog(
            title = "编辑随机配置",
            subtitle = "调整配置名称、类型和随机内容。",
            onDismiss = onDismiss,
            actions = {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (randomName.isNotBlank()) {
                            // 对于转盘类型，保存前先校正权重
                            if (selectedCate == RANDOM_PAGE_CONFIG_CATE_WHEEL) {
                                validateAndCorrectWheelWeights(cardListState)
                            }
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
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 配置名称输入
                    OutlinedTextField(
                        value = randomName,
                        onValueChange = { randomName = it },
                        label = { Text("配置名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(design.cornerRadius.input)
                    )

                    // 类型选择栏
                    Text(
                        text = "选择类型",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    AddNewRandomCateActionBar(
                        select = selectedCate,
                        isEditing = isEditing,
                        onClick = { cate ->
                            selectedCate = cate.key
                            // 注意：编辑时不应该清空列表，而是保留原有数据
                        }
                    )

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
                }
        }
    }
}



