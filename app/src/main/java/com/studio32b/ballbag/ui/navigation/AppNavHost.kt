package com.studio32b.ballbag.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studio32b.ballbag.ui.arsenal.MyBallBagScreen

sealed class Screen(val route: String) {
    object MyBallBag : Screen("my_ball_bag")
    object BowlingBallSearch : Screen("bowling_ball_search")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null,
    onExitApp: (() -> Unit)? = null
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    var myBallBagFadeInShown by remember { mutableStateOf(false) }
    val screenName = when {
        currentRoute == Screen.MyBallBag.route -> "My Ball Bag"
        currentRoute == Screen.BowlingBallSearch.route -> "Ball Search"
        else -> ""
    }
    val showBrowseButton = false
    val showBackButton = currentRoute == Screen.BowlingBallSearch.route
    Column {
        Scaffold(
            topBar = {
                AppTopBar(
                    screenName = screenName,
                    onBackClick = if (showBackButton) { { navController.popBackStack() } } else null,
                    onLeftActionClick = if (currentRoute == Screen.MyBallBag.route) { { navController.navigate(Screen.BowlingBallSearch.route) } } else null,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onExitApp = onExitApp
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.MyBallBag.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.MyBallBag.route) {
                    val doFadeIn = !myBallBagFadeInShown
                    if (!myBallBagFadeInShown) myBallBagFadeInShown = true
                    MyBallBagScreen(fadeIn = doFadeIn)
                }
                composable(Screen.BowlingBallSearch.route) {
                    com.studio32b.ballbag.ui.browse.BowlingBallSearchScreen()
                }
            }
        }
    }
}
