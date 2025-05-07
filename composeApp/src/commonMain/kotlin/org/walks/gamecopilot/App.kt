package org.walks.gamecopilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.navigation.NavigationHost
import org.walks.gamecopilot.theme.WeUITheme
import yigamecopilotx.composeapp.generated.resources.Icon_arrow_left
import yigamecopilotx.composeapp.generated.resources.Res


@Composable
@Preview
fun App() {
    val viewModelFactory = viewModelFactory { initializer { MainViewmodel() } }
    val extras: CreationExtras = MutableCreationExtras()
    val viewModel = viewModelFactory.create(MainViewmodel::class, extras)

    WeUITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppView(viewModel)
        }
    }
}


@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { mutableStateOf(SnackbarHostState()) }
    val navi = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf("") }

    var floatButtonShow by remember { mutableStateOf(true) }
    LaunchedEffect(navi) {
        navi.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route ?: ""

            currentRoute = entry.destination.route ?: ""
            // 获取页面参数（示例参数名为"mode"）
            //val pageMode = entry.arguments?.getString("mode")

            // 复合条件判断（示例：当在RANDOM路由且mode=edit时显示）
            floatButtonShow = when (currentRoute) {
                NaviRoute.RANDOM.route -> true//pageMode == "edit"
                // 添加其他路由条件...
                else -> false
            }
        }
    }


    // 修改 AppView 中的 ModalNavigationDrawer 部分
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true, // 允许手势操作
        drawerContent = {
            JellyDrawerContent(
                drawerState = drawerState,
                onClose = { scope.launch { drawerState.close() } },
                modifier = Modifier.wrapContentSize()
                    .background(MaterialTheme.colorScheme.background),
                navi = navi
            )
        },
        scrimColor = Color.Transparent // 去掉默认遮罩
    ) {
        // 添加内容区域偏移动画
        val contentOffset by animateDpAsState(
            targetValue = if (drawerState.isOpen) 150.dp else 0.dp,
            animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = contentOffset)
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(navi, viewmodel, drawerState)
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackState.value)
                }, floatingActionButton = {
                    var isOpen by remember { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (isOpen) -45f else 0f,
                        animationSpec = tween(durationMillis = 300)
                    )
                    if (floatButtonShow) {
                        // 菜单项垂直排列
                        LazyColumn(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp),

                            modifier = Modifier.padding(bottom = 72.dp) // 给菜单按钮留出空间
                        ) {
                            item {
                                AnimatedVisibility(isOpen) {
                                    Button(
                                        onClick = {
                                            viewmodel.handleRandomPageIntent(RandomPageIntent.OnAddNewRandomDialogShow)
                                            isOpen = false
                                        },
                                        modifier = Modifier

                                    ) {
                                        Text("新增配置")
                                    }
                                }
                            }
                            // 菜单项1
                            item {
                                AnimatedVisibility(isOpen) {
                                    Button(
                                        onClick = { /* 其他操作 */ },
                                        modifier = Modifier
                                    ) {
                                        Text("临时添加")
                                    }
                                }
                            }
                            item {
                                // 主按钮
                                FloatingActionButton(
                                    onClick = { isOpen = !isOpen },
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .wrapContentSize()
                                        .rotate(rotation)
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    Icon(Icons.Sharp.Add, "展开菜单")
                                }
                            }
                        }
                    }
                }

            ) { inp ->
                Column(
                    modifier = Modifier
                        .padding(inp)
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NavigationHost(viewmodel, navi)
                }
            }
        }
    }


    LaunchedEffect(key1 = Unit) {
        viewmodel.topTipState.collect {
            if (it != null) {
                snackState.value.showSnackbar(it)
            }
        }
    }

}

private fun isStartRoute(route: String?): Boolean {
    return route == "start" || route == null
}

@Composable
fun JellyDrawerContent(
    drawerState: DrawerState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    navi: NavHostController
) {
    val list = NaviRoute.entries.filter {
        it.type == 0
    }
    val offsetX by animateDpAsState(
        targetValue = if (drawerState.isOpen) 0.dp else (-200).dp,
        animationSpec = spring(
            dampingRatio = 0.6f,  // 增加阻尼比增强果冻感
            stiffness = 400f
        )
    )

    Surface(
        modifier = modifier
            .padding(vertical = 24.dp)
            .width(150.dp).fillMaxHeight()
            .offset(x = offsetX),
//            .border(
//                4.dp,
//                MaterialTheme.colorScheme.primaryContainer,
//                RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
//            ),
        shadowElevation = 24.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentHeight()
        ) {
            items(list.size) { index ->
                val isSelected = navi.currentDestination?.route == list[index].route
                val item = list[index]
                var isPressed by remember { mutableStateOf(false) }
                val scale = animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                )
                TextButton(
                    onClick = {
                        navi.navigate(item.route)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(
                            width = 4.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                }
                            )
                        }
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                ) {
                    Text(
                        text = item.label,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navi: NavHostController, viewmodel: MainViewmodel, drawerState: DrawerState) {
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    var roomTitle by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var playerNum by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        navi.currentBackStackEntryFlow.collectLatest {
            current = navi.currentDestination?.route ?: ""
            roomTitle = viewmodel.roomEntityState.value.roomId
        }

    }
    CenterAlignedTopAppBar(
        title = {
            if (current == "room") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = CircleShape
                    ).clip(shape = CircleShape).height(40.dp).clickable {
                        viewmodel.handleRoomIntent(GameRoomIntent.StartGame)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(end = 4.dp).fillMaxHeight()
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Play now",
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                            fontWeight = FontWeight.W900,
                            textAlign = TextAlign.End,
                            fontSize = 20.sp
                        )
                    }

                    Text(
                        text = if (current == "room") roomTitle else "卧底游戏",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W900
                    )
                }
            }

        },
        navigationIcon = {
            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                onClick = {
                    if (navi.previousBackStackEntry?.destination?.route == "start") {
                        try {
                            navi.popBackStack()
                        } catch (e: Exception) {
                            // 处理 popBackStack 异常，例如记录日志或提示用户
                            println("Error popping back stack: ${e.message}")
                        }
                        if (current == "start") {
                            viewmodel.handleRoomIntent(GameRoomIntent.LeaveGameRoom)
                        }
                    } else {
                        scope.launch {  // 新增抽屉开关逻辑
                            if (drawerState.isClosed) drawerState.open()
                            else drawerState.close()
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (isStartRoute(current)) -180f else 0f,
                    animationSpec = tween(durationMillis = 300)
                )

                Icon(
                    painter = painterResource(Res.drawable.Icon_arrow_left),
                    contentDescription = "back button",
                    modifier = Modifier.rotate(rotation).size(24.dp),

                    )
            }
        },
        actions = {
            if (!isStartRoute(current))
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        ).rotate(rotation.value),
                    onClick = {

                        scope.launch {
                            rotation.animateTo(
                                targetValue = 360f,
                                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                            )
                            rotation.snapTo(0f) // 重置角度准备下次旋转
                        }
                        if (current == NaviRoute.RANDOM.route) {
                            viewmodel.handleRandomPageIntent(RandomPageIntent.OnRefresh)
                            return@IconButton
                        }
                        viewmodel.handleRoomIntent(GameRoomIntent.RefreshRoomInfo)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    content = {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新房间人数",
                            tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.33f)
                        )
                    }
                )
        },
        modifier = Modifier
            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}


