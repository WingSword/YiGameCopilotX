package org.walks.gamecopilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.theme.WeUITheme
import org.walks.gamecopilot.ui.page.home.HomePage
import org.walks.gamecopilot.ui.page.room.RoomPage


@Composable
@Preview
fun App() {
    val viewModelFactory = viewModelFactory { initializer { MainViewmodel() } }
    val extras: CreationExtras = MutableCreationExtras()
    val viewModel = viewModelFactory.create(MainViewmodel::class, extras)

    WeUITheme {
        Surface(
            modifier = Modifier.fillMaxSize().background(Color.White),
            color = Color.White
        ) {
            AppView(viewModel)
        }
    }
}


@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { mutableStateOf(SnackbarHostState()) }
    val navi = rememberNavController()

    Scaffold(
        topBar = {
            AppTopBar(navi, viewmodel, viewmodel.roomEntityState.value.roomId)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackState.value)
        },
    ) { inp ->
        Column(
            modifier = Modifier
                .padding(inp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NavigationHost(viewmodel, navi)
        }
    }
    LaunchedEffect(Unit) {
        viewmodel.topTipState.collect { tip ->
            tip?.let { snackState.value.showSnackbar(it) }
        }
    }
}

// 提取公共逻辑到辅助函数
private fun isStartRoute(navi: NavHostController): Boolean {
    return navi.currentDestination?.route == "start" || navi.currentDestination?.route == null
}

private fun isStartRoute(route: String?): Boolean {
    return route == "start" || route == null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navi: NavHostController, viewmodel: MainViewmodel, roomTitle: String) {
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    var current by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        navi.currentBackStackEntryFlow.collectLatest {
            current = navi.currentDestination?.route ?: ""
        }
    }
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (current == "room") roomTitle else "卧底游戏",
                    color = MaterialTheme.colorScheme.onPrimary
                )
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
                    if (!isStartRoute(current)) {
                        try {
                            navi.popBackStack()
                        } catch (e: Exception) {
                            // 处理 popBackStack 异常，例如记录日志或提示用户
                            println("Error popping back stack: ${e.message}")
                        }
                        if (current == "start") {
                            viewmodel.handleRoomIntent(GameRoomIntent.LeaveGameRoom)
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            ) {
                if (!isStartRoute(current)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back button"
                    )
                }
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
                        viewmodel.handleRoomIntent(GameRoomIntent.RefreshRoomInfo)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    content = {
                        Text(viewmodel.roomEntityState.value.roomPlayerNum.toString())
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
            .padding(24.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    NavHost(navi, startDestination = "start") {
        composable("start") {
            HomePage(viewmodel)
        }
        composable("room") {
            RoomPage(viewmodel)
        }
    }
    LaunchedEffect(Unit) {
        viewmodel.navigationEvents.collect { event ->
            event?.let {
                when (event) {
                    is NavigationEvent.NavigateTo -> {
                        navi.navigate(event.route) {
                            event.popUpToRoute?.let { route ->
                                popUpTo(route) { inclusive = event.inclusive }
                            }
                        }
                    }

                    NavigationEvent.PopBackStack -> navi.popBackStack()
                    is NavigationEvent.PopUpTo -> navi.popBackStack(event.route, event.inclusive)
                }
            }
        }
    }

}


@Preview
@Composable
fun DefaultPreview() {
    WeUITheme {
        //GreetingView(1, "",4)
    }
}
