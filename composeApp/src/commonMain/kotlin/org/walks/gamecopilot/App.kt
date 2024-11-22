package org.walks.gamecopilot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.data.entity.NaviRoute
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.theme.WeUITheme

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { mutableStateOf(SnackbarHostState()) }
    val playerNum = viewmodel.roomEntityState.collectAsState().value.playerNum
    val roomTitle = viewmodel.roomEntityState.collectAsState().value.roomNumber
    val navi = rememberNavController()
    navi.addOnDestinationChangedListener { _, destination, _ ->
        val route = destination.route
        // 根据route进行相关操作，如记录日志或更新UI
        when (route) {

        }
    }
    Scaffold(
        topBar = {
            AppTopBar(navi)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navi: NavHostController) {
    var topBarTitle by remember { mutableStateOf("") }
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = topBarTitle,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        navigationIcon = {
            AppTopBarNavigationIcon(navi)
        },
        modifier = Modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}

@Composable
fun AppTopBarNavigationIcon(navi: NavHostController){
    IconButton(
        modifier = Modifier
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ),
        onClick = {
            if (navi.currentBackStackEntry?.destination?.route == "start") {


            } else {
                navi.popBackStack()
            }

        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        Icon(
            imageVector = if (navi.currentBackStackEntry?.destination?.route == "start") Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "back button"
        )
    }
}


@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    NavHost(navi, startDestination = NaviRoute.NaviStart.name) {
        composable(NaviRoute.NaviStart.name,) {

        }
        composable(NaviRoute.NaviGuide.name) {

        }
    }
}

