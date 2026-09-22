package org.walks.gamecopilot.ui.page.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.ui.page.drawguess.DrawBoardPage

/** Keep the public game route while entering the board without another navigation step. */
@Composable
fun DrawGuessEntrance(viewmodel: MainViewmodel, navController: NavHostController) {
    DrawBoardPage(viewmodel) { navController.popBackStack() }
}
