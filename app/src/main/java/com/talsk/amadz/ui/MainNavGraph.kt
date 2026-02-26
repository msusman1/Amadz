package com.talsk.amadz.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.ui.home.HomeScreen
import kotlinx.serialization.Serializable


@Serializable
data object HomeKey : NavKey

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/16/2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph() {
    val backStack = rememberNavBackStack(HomeKey)
    NavDisplay(
        backStack = backStack, entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen()
            }
        })

}
