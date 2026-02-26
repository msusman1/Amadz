package com.talsk.amadz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.talsk.amadz.ui.home.homeRoutes

@Composable
fun AnimatedBottomBar(visible: Boolean, backStack: NavBackStack<NavKey>) {
    val currentDestination: NavKey? = backStack.lastOrNull()
    val routes = remember { homeRoutes() }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        NavigationBar {
            routes.forEach { menu ->
                val selected = currentDestination == menu.navKey
                NavigationBarItem(
                    label = { Text(menu.label) },
                    icon = {
                        Icon(
                            painter = painterResource(id = if (selected) menu.iconSelected else menu.icon),
                            contentDescription = menu.label
                        )
                    },
                    selected = selected,
                    onClick = {
                        val current = backStack.lastOrNull()
                        if (current != menu.navKey) {
                            backStack.clear()
                            backStack.add(menu.navKey)
                        }
                    }
                )
            }
        }
    }
}