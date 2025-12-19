package com.talsk.amadz.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.home.DialPadScreen
import com.talsk.amadz.ui.home.HomeScreen
import com.talsk.amadz.ui.home.MainViewModel
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
fun MainNavGraph(
    vm: MainViewModel
) {
    val contacts by vm.contacts.collectAsStateWithLifecycle()
    val callLogs by vm.callLogs.collectAsStateWithLifecycle()
    val dialedNumber by vm.dialedNumber.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(HomeKey)
    LaunchedEffect(dialedNumber) {
        if (dialedNumber != null) {
            backStack.add(DialPadKey(dialedNumber))
        }
    }

    NavDisplay(
        backStack = backStack, entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    contacts = contacts,
                    callLogs = callLogs,
                    onFavouriteToggle = vm::toggleFavourite,
                    dailButtonClicked = {
                        backStack.add(DialPadKey())
                    },
                    loadNextPage = vm::loadNextPage,
                )
            }
            entry<DialPadKey> {
                DialPadScreen(contacts, it.phoneNumber, vm::consumeDialedNumber)
            }
        })

}