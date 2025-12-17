package com.talsk.amadz.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.home.DialPadScreen
import com.talsk.amadz.ui.home.HomeScreen
import kotlinx.serialization.Serializable


@Serializable
data object DialPadKey : NavKey

@Serializable
data object HomeKey : NavKey

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/16/2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(
    contacts: List<ContactData>,
    callLogs: List<CallLogData>,
    onFavouriteToggle: (ContactData) -> Unit,
    phoneNumber: String? = null,
    loadNextPage: () -> Unit,
) {

    val backStack = rememberNavBackStack(HomeKey)
    LaunchedEffect(phoneNumber) {
        if (phoneNumber != null) {
            backStack.add(DialPadKey)
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    contacts = contacts,
                    callLogs = callLogs,
                    onFavouriteToggle = onFavouriteToggle,
                    dailButtonClicked = {
                        backStack.add(DialPadKey)
                    },
                    loadNextPage = loadNextPage,
                )
            }
            entry<DialPadKey> {
                DialPadScreen(contacts, phoneNumber)
            }
        }
    )

}