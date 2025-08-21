package com.studio32b.ballbag.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.studio32b.ballbag.R

sealed class BottomNavItem(val route: String, val label: String) {
    class VectorIcon(route: String, label: String, val icon: ImageVector) : BottomNavItem(route, label)
    class DrawableIcon(route: String, label: String, val iconRes: Int) : BottomNavItem(route, label)

    companion object {
        val MyBallBag = DrawableIcon("my_ball_bag", "My Ball Bag", R.drawable.ic_arsenal)
        val AllBowlingBalls = VectorIcon("bowling_ball_search", "Ball Search", Icons.Filled.Search)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.MyBallBag,
        BottomNavItem.AllBowlingBalls
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findNode(item.route)?.id ?: navController.graph.startDestinationId) {
                                inclusive = false
                            }
                        }
                    }
                },
                icon = {
                    when (item) {
                        is BottomNavItem.VectorIcon -> Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp))
                        is BottomNavItem.DrawableIcon -> Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
            )
            // Add a vertical divider between buttons, except after the last one
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(32.dp) // Use a fixed height instead of fillMaxHeight
                        .width(1.dp)
                        .background(Color(0xFFDDDDDD))
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}
