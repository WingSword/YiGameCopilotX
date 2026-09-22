package org.walks.gamecopilot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.navigation.NavigationHost
import org.walks.gamecopilot.theme.LocalThemeMode
import org.walks.gamecopilot.theme.WeUITheme


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
            id = "profile",
            route = NaviRoute.SETTING.route,
            label = "我的",
            icon = Icons.Outlined.PersonOutline
        )
    )
    val routeSelectedId = when {
        isStartRoute(currentRoute) || currentRoute == NaviRoute.HOME.route -> "home"
        currentRoute == NaviRoute.RANDOM.route -> "bag"
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

    // 二级页面自行提供返回栏；只有三个一级页面显示悬浮导航。
    Scaffold(
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
            NavigationHost(viewmodel, navi)
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
            route == NaviRoute.SETTING.route
}
