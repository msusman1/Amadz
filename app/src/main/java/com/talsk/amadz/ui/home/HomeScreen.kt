package com.talsk.amadz.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.core.dial
import com.talsk.amadz.core.hasDefaultCallingSimConfigured
import com.talsk.amadz.domain.entity.SimInfo
import com.talsk.amadz.ui.callLogHistory.CallLogHistoryScreen
import com.talsk.amadz.ui.components.AnimatedBottomBar
import com.talsk.amadz.ui.components.DialFab
import com.talsk.amadz.ui.components.SimSelectionDialog
import com.talsk.amadz.ui.extensions.openContactAddScreen
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import com.talsk.amadz.ui.home.calllogs.CallLogsScreen
import com.talsk.amadz.ui.home.contacts.ContactsScreen
import com.talsk.amadz.ui.home.favourite.FavouritesScreen
import com.talsk.amadz.ui.home.searchbar.HomeSearchBar
import com.talsk.amadz.ui.home.searchbar.SearchBarState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val backStack = rememberNavBackStack(RecentsKey)
    val context = LocalContext.current
    val vm: HomeViewModel = hiltViewModel()
    var searchBarState by rememberSaveable { mutableStateOf(SearchBarState.COLLAPSED) }
    var pendingDialPhone by remember { mutableStateOf<String?>(null) }
    var simOptions by remember { mutableStateOf<List<SimInfo>>(emptyList()) }
    val currentDestination: NavKey? = backStack.lastOrNull()
    val isHomeTab =
        currentDestination == FavouritesKey ||
                currentDestination == RecentsKey ||
                currentDestination == ContactsKey

    fun requestDial(phone: String) {
        val sims = vm.getSimsInfo()
        if (sims.size > 1 && !context.hasDefaultCallingSimConfigured()) {
            pendingDialPhone = phone
            simOptions = sims.sortedBy { it.simSlotIndex }
        } else {
            context.dial(phone)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isHomeTab) {
                DialFab(
                    visible = !searchBarState.isActive(),
                    onClick = {
                        searchBarState = SearchBarState.EXPANDED_WITH_DIAL_PAD
                    }
                )
            }
        },
        topBar = {
            if (isHomeTab) {
                HomeSearchBar(
                    searchBarState = searchBarState,
                    onSearchBarClick = {
                        searchBarState = SearchBarState.EXPANDED
                    },
                    onSearchCloseClick = {
                        searchBarState = SearchBarState.COLLAPSED
                    },
                    onCallClick = ::requestDial
                )
            }
        },
        bottomBar = {
            if (isHomeTab) {
                AnimatedBottomBar(
                    visible = !searchBarState.isActive(),
                    backStack = backStack
                )
            }
        }
    ) { paddingValues ->

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry(FavouritesKey) {
                    FavouritesScreen(
                        onCallClick = { requestDial(it.phone) },
                        onContactDetailCLick = { context.openContactDetailScreen(it.id) }
                    )
                }
                entry(RecentsKey) {
                    CallLogsScreen(
                        onContactDetailClick = {
                            it.contactId?.let(context::openContactDetailScreen)
                        },
                        onCallClick = {
                            requestDial(it)
                        },
                        onCallLogClick = {
                            backStack.add(
                                CallLogHistoryKey(
                                    phone = it.phone,
                                    contactName = it.name,
                                    contactId = it.contactId
                                )
                            )
                        }
                    )
                }
                entry(ContactsKey) {
                    ContactsScreen(
                        onContactDetailClick = { context.openContactDetailScreen(it.id) },
                        onCallClick = { requestDial(it.phone) }
                    )
                }
                entry<CallLogHistoryKey> {
                    CallLogHistoryScreen(
                        phone = it.phone,
                        contactName = it.contactName,
                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        },
                        onCallClick = { phone ->
                            requestDial(phone)
                        },
                        onAddContactClick = { phone ->
                            context.openContactAddScreen(phone)
                        },
                    )
                }
            }
        )

    }

    if (pendingDialPhone != null && simOptions.isNotEmpty()) {
        SimSelectionDialog(
            sims = simOptions,
            onSimSelected = { sim ->
                val phone = pendingDialPhone ?: return@SimSelectionDialog
                context.dial(phone = phone, accountId = sim.accountId)
                pendingDialPhone = null
                simOptions = emptyList()
            },
            onDismiss = {
                pendingDialPhone = null
                simOptions = emptyList()
            }
        )
    }
}
