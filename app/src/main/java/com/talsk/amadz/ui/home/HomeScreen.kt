package com.talsk.amadz.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.core.dial
import com.talsk.amadz.ui.callLogHistory.CallLogHistoryScreen
import com.talsk.amadz.ui.components.AnimatedBottomBar
import com.talsk.amadz.ui.components.DialFab
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
    var searchBarState by rememberSaveable { mutableStateOf(SearchBarState.COLLAPSED) }
    val currentDestination: NavKey? = backStack.lastOrNull()
    val isHomeTab =
        currentDestination == FavouritesKey ||
                currentDestination == RecentsKey ||
                currentDestination == ContactsKey

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
                    }
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
                        onCallClick = { context.dial(it.phone) },
                        onContactDetailCLick = { context.openContactDetailScreen(it.id) }
                    )
                }
                entry(RecentsKey) {
                    CallLogsScreen(
                        onContactDetailClick = {
                            it.contactId?.let(context::openContactDetailScreen)
                        },
                        onCallClick = {
                            context.dial(it)
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
                        onCallClick = { context.dial(it.phone) }
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
                            context.dial(phone)
                        }
                    )
                }
            }
        )

    }
}
