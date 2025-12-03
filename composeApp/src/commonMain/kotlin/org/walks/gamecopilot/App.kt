package org.walks.gamecopilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.intent.RandomPageIntent
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.navigation.NavigationHost
import org.walks.gamecopilot.theme.WeUITheme
import yigamecopilotx.composeapp.generated.resources.Icon_arrow_left
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_random_svg
import yigamecopilotx.composeapp.generated.resources.icon_setting_svg
import yigamecopilotx.composeapp.generated.resources.icon_sword_svg


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

/**
 * 悬浮导航栏组件
 * 悬浮的长条形状，包含导航项
 */
@Composable
fun BottomNavigationBar(navi: NavHostController, currentRoute: String) {
    // 获取主要导航路由（排除游戏页面）
    val mainRoutes = NaviRoute.entries.filter { it.type == 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 悬浮长条背景
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(30.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(30.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(30.dp),
                    clip = true
                )
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 显示所有导航项
            mainRoutes.forEachIndexed { index, route ->
                BottomNavItem(
                    route = route,
                    isSelected = currentRoute == route.route,
                    onClick = { navi.navigate(route.route) }
                )
            }
        }
    }
}

/**
 * 底部导航项组件
 */
@Composable
fun BottomNavItem(
    route: NaviRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            // 根据路由类型显示对应图标
            when (route) {
                NaviRoute.HOME -> {
                    Icon(
                        painter = painterResource(Res.drawable.icon_sword_svg),
                        contentDescription = "首页",
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected)
                            Color.Unspecified
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                NaviRoute.RANDOM -> {
                    Icon(
                        painter = painterResource(Res.drawable.icon_random_svg),
                        contentDescription = "随机工具",
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected)
                            Color.Unspecified
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                NaviRoute.SETTING -> {
                    Icon(
                        painter = painterResource(Res.drawable.icon_setting_svg),
                        contentDescription = "设置",
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected)
                            Color.Unspecified
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                else -> {
                    // 其他路由使用默认文字
                    Text(
                        text = route.label.take(2),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { mutableStateOf(SnackbarHostState()) }
    val navi = rememberNavController()
    var currentRoute by remember { mutableStateOf("") }

    var floatButtonShow by remember { mutableStateOf(true) }
    LaunchedEffect(navi) {
        navi.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route ?: ""

            currentRoute = entry.destination.route ?: ""
            // 复合条件判断（示例：当在RANDOM路由且mode=edit时显示）
            floatButtonShow = when (currentRoute) {
                NaviRoute.RANDOM.route -> true
                // 添加其他路由条件...
                else -> false
            }
        }
    }

    // 移除抽屉导航，改为悬浮导航栏
    Scaffold(
        topBar = {
            // 一级页面（首页、随机工具、设置）不显示TopBar，由页面自身决定
            // 二级页面（游戏页面）自行设置导航栏
            if (!isStartRoute(currentRoute) &&
                currentRoute != NaviRoute.HOME.route &&
                currentRoute != NaviRoute.RANDOM.route &&
                currentRoute != NaviRoute.SETTING.route &&
                currentRoute != NaviRoute.AWALONG.route
            ) {
                AppTopBar(navi, viewmodel)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackState.value)
        },
        floatingActionButton = {
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
        },
        bottomBar = {

        },

        ) { inp ->
        Box(
            modifier = Modifier
                .padding(inp)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 主要内容区域 - 为所有页面添加底部边距，避免内容被导航栏遮挡
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 0.dp), // 为所有页面预留足够底部空间
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NavigationHost(viewmodel, navi)
            }
            // 底部导航栏 - 显示在一级页面（首页、随机工具、设置）
            if (isStartRoute(currentRoute) ||
                currentRoute == NaviRoute.HOME.route ||
                currentRoute == NaviRoute.RANDOM.route ||
                currentRoute == NaviRoute.SETTING.route
            ) {
                BottomNavigationBar(navi, currentRoute)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navi: NavHostController, viewmodel: MainViewmodel) {
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    var roomTitle by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
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
                            text = viewmodel.roomEntityState.value.roomKey,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                            fontWeight = FontWeight.W900,
                            textAlign = TextAlign.End,
                            fontSize = 20.sp
                        )
                    }

                    Text(
                        text = viewmodel.roomEntityState.value.roomId,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W900
                    )
                }
            }

        },
        navigationIcon = {
            // 只在二级页面显示返回按钮
            if (!isStartRoute(current)) {
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
                            // 返回上一级页面
                            navi.popBackStack()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.Icon_arrow_left),
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
            if (!isStartRoute(current))
                if (current == NaviRoute.ROOM.route && !viewmodel.roomEntityState.value.isRoomOwner) {

                } else {
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
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        easing = LinearEasing
                                    )
                                )
                                rotation.snapTo(0f) // 重置角度准备下次旋转
                            }
                            if (current == NaviRoute.RANDOM.route) {
                                viewmodel.handleRandomPageIntent(RandomPageIntent.OnRefresh)
                                return@IconButton
                            }

                            viewmodel.handleRoomIntent(GameRoomIntent.StartGame)
                            viewmodel.handleAwalongGameIntent(AwalongIntent.RestartGame)
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
                }

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
