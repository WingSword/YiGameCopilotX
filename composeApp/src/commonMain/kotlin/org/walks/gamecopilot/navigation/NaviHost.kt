package org.walks.gamecopilot.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongGamePageOptimized
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.ui.page.game.localspy.LocalSpyGamePage
import org.walks.gamecopilot.ui.page.home.HomePage
import org.walks.gamecopilot.ui.page.other.ErrorPage
import org.walks.gamecopilot.ui.page.random.RandomPage
import org.walks.gamecopilot.ui.page.room.RoomPage
import org.walks.gamecopilot.ui.page.setting.SettingPage

/**
 *  Created by Wing at 10:35 on 2025/4/25
 *
 */


@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    val navEntries = remember { NaviRoute.entries }

    NavHost(navi, startDestination = NaviRoute.HOME.route) {

        navEntries.forEach { naviEntry ->
            composable(naviEntry.route) {
                when (naviEntry) {
                    NaviRoute.HOME -> HomePage(viewmodel, navi)
                    NaviRoute.LOCAL_SPY -> LocalSpyGamePage(viewmodel) { navi.popBackStack() }
                    NaviRoute.ROOM -> RoomPage(viewmodel)
                    NaviRoute.RANDOM -> RandomPage(viewmodel)
                    NaviRoute.AWALONG -> AwalongGamePageOptimized(viewmodel)
                    NaviRoute.SETTING -> SettingPage(viewmodel)
                    // 显式列出所有路由，移除else分支
                    // 当新增路由时编译器会提示需要补充分支
                    else -> ErrorPage()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewmodel.navigationEvents.collect { event ->

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