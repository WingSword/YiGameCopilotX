package org.walks.gamecopilot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.theme.LocalThemeMode
import org.walks.gamecopilot.theme.WeUITheme
import yigamecopilotx.composeapp.generated.resources.Icon_arrow_left
import yigamecopilotx.composeapp.generated.resources.Res


@Composable
@Preview
fun App() {
    val viewModel = remember { MainViewmodel() }

    val themeMode by viewModel.themeMode.collectAsState()

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        WeUITheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppView(viewModel)
            }
        }
    }
}

/**
 * 悬浮导航栏组件
 * 悬浮的长条形状，包含导航项
 */
private data class HomeBottomNavItem(
    val id: String,
    val route: String?,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomNavigationBar(navi: NavHostController, currentRoute: String) {
    val design = LocalAppDesign.current
    val navItems = listOf(
        HomeBottomNavItem(
            id = "home",
            route = NaviRoute.HOME.route,
            label = "首页",
            icon = Icons.Outlined.Home
        ),
        HomeBottomNavItem(
            id = "bag",
            route = NaviRoute.RANDOM.route,
            label = "工具",
            icon = Icons.Outlined.Handyman
        ),
        HomeBottomNavItem(
            id = "multiplayer",
            route = NaviRoute.MULTIPLAYER.route,
            label = "联机",
            icon = Icons.Outlined.Groups
        ),
        HomeBottomNavItem(
            id = "profile",
            route = NaviRoute.SETTING.route,
            label = "我的",
            icon = Icons.Outlined.PersonOutline
        )
    )
    val routeSelectedId = when {
        isStartRoute(currentRoute) || currentRoute == NaviRoute.HOME.route -> "home"
        currentRoute == NaviRoute.RANDOM.route -> "bag"
        currentRoute == NaviRoute.MULTIPLAYER.route -> "multiplayer"
        currentRoute == NaviRoute.STATS.route -> "profile"
        currentRoute == NaviRoute.SETTING.route -> "profile"
        else -> null
    }
    val selectedItemId = routeSelectedId ?: "home"

    BoxWithConstraints(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.width(minOf(maxWidth * 0.92f, 440.dp)).height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    BottomNavItem(
                        modifier = Modifier.weight(1f),
                        item = item,
                        isSelected = selectedItemId == item.id,
                        onClick = {
                            item.route?.let { route ->
                                if (route != currentRoute) {
                                    navi.navigate(route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(NaviRoute.HOME.route) {
                                            saveState = true
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 底部导航项组件
 */
@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    item: HomeBottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.height(46.dp).clip(RoundedCornerShape(23.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f) else Color.Transparent)
            .clickable(onClick = onClick).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        AnimatedVisibility(isSelected) {
            Row {
                Spacer(Modifier.width(5.dp))
                Text(item.label, color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}



@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { SnackbarHostState() }
    val navi = rememberNavController()
    var currentRoute by remember { mutableStateOf("") }
    LaunchedEffect(navi) {
        navi.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route ?: ""
        }
    }

    // 移除抽屉导航，改为悬浮导航栏
    Scaffold(
        topBar = {
            if (shouldShowAppTopBar(currentRoute)) {
                AppTopBar(navi, viewmodel)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackState)
        },
        ) { inp ->
        val showBottomNavigation = shouldShowBottomNavigation(currentRoute)
        Box(
            modifier = Modifier
                .padding(inp)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 主要内容区域 - 为所有页面添加底部边距，避免内容被导航栏遮挡
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                NavigationHost(viewmodel, navi)
            }
            // 底部导航栏 - 显示在一级页面（首页、随机工具、设置）
            if (showBottomNavigation) {
                BottomNavigationBar(navi, currentRoute)
            }
        }


    }

    LaunchedEffect(key1 = Unit) {
        viewmodel.topTipState.collect {
            if (it != null) {
                snackState.showSnackbar(it)
            }
        }
    }

}

private fun isStartRoute(route: String?): Boolean {
    return route == "start" || route == null
}

private fun shouldShowBottomNavigation(route: String?): Boolean {
    return isStartRoute(route) ||
            route == NaviRoute.HOME.route ||
            route == NaviRoute.RANDOM.route ||
            route == NaviRoute.MULTIPLAYER.route ||
            route == NaviRoute.SETTING.route
}

private fun shouldShowAppTopBar(route: String?): Boolean {
    return route == NaviRoute.LAN_DISCOVERY.route ||
            route == NaviRoute.LAN_CREATE_ROOM.route ||
            route == NaviRoute.LAN_LOBBY.route
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navi: NavHostController, viewmodel: MainViewmodel) {
    val design = LocalAppDesign.current
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    var current by remember { mutableStateOf("") }
    LaunchedEffect(navi) {
        navi.currentBackStackEntryFlow.collectLatest {
            current = navi.currentDestination?.route ?: ""
        }

    }
    CenterAlignedTopAppBar(
        title = {
            if (current == "room") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                        viewmodel.handleRoomIntent(GameRoomIntent.StartGame)
                    }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${viewmodel.roomEntityState.value.roomId} · ${viewmodel.roomEntityState.value.roomKey}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        },
        navigationIcon = {
            // 只在二级页面显示返回按钮
            if (!isStartRoute(current)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(design.cornerRadius.md))
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(design.cornerRadius.md)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(design.cornerRadius.md)
                        )
                        .clickable {
                        if (navi.previousBackStackEntry?.destination?.route == "start") {
                            try {
                                navi.popBackStack()
                            } catch (e: Exception) {
                                GameLogger.warn("返回上一页失败: ${e.message}")
                            }
                            if (current == "start") {
                                viewmodel.handleRoomIntent(GameRoomIntent.LeaveGameRoom)
                            }
                        } else {
                            // 返回上一级页面
                            navi.popBackStack()
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.Icon_arrow_left),
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
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
                            .clip(RoundedCornerShape(10.dp))
                            .rotate(rotation.value),
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        content = {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "刷新房间人数",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

        },
        modifier = Modifier
            .padding(top = 10.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}
