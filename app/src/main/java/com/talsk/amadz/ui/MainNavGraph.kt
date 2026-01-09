package com.talsk.amadz.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.core.dial
import com.talsk.amadz.ui.extensions.openContactAddScreen
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import com.talsk.amadz.ui.home.HomeScreen
import com.talsk.amadz.ui.home.dialpad.DialPadScreen
import kotlinx.serialization.Serializable


@Serializable
data class DialPadKey(val phoneNumber: String? = null) : NavKey

@Serializable
data object HomeKey : NavKey

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/16/2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph() {
    val backStack = rememberNavBackStack(HomeKey)
    val context = LocalContext.current
    NavDisplay(
        backStack = backStack, entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    dailButtonClicked = {
                        backStack.add(DialPadKey())
                    }
                )
            }
            entry<DialPadKey> {
                DialPadScreen(
                    onPhoneDialed = { context.dial(it) },
                    onContactDetailClicked = { context.openContactDetailScreen(it.id) },
                    onContactAddClicked = { context.openContactAddScreen(it) }
                )
            }
        })

}