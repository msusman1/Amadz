package com.talsk.amadz.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.core.dial
import com.talsk.amadz.ui.callLogHistory.CallLogHistoryScreen
import com.talsk.amadz.ui.components.AnimatedBottomBar
import com.talsk.amadz.ui.components.DialFab
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import com.talsk.amadz.ui.home.calllogs.CallLogsScreen
import com.talsk.amadz.ui.home.contacts.ContactsScreen
import com.talsk.amadz.ui.home.favourite.FavouritesScreen
import com.talsk.amadz.ui.home.searchbar.HomeSearchBar
import com.talsk.amadz.ui.home.searchbar.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: SearchViewModel = hiltViewModel()
) {
    val backStack = rememberNavBackStack(RecentsKey)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val contacts = vm.contacts.collectAsLazyPagingItems()
    val query by vm.query.collectAsStateWithLifecycle()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var dialPadPhone by rememberSaveable { mutableStateOf("") }
    val currentDestination: NavKey? = backStack.lastOrNull()
    val isHomeTab =
        currentDestination == FavouritesKey ||
                currentDestination == RecentsKey ||
                currentDestination == ContactsKey

    Scaffold(
        floatingActionButton = {
            if (isHomeTab) {
                DialFab(
                    visible = !isSearchActive,
                    onClick = {
                        isSearchActive = true
                        coroutineScope.launch { bottomSheetState.expand() }
                    }
                )
            }
        },
        topBar = {
            if (isHomeTab) {
                HomeSearchBar(
                    contacts = contacts,
                    query = query,
                    searchActive = isSearchActive,
                    onSearchBarClick = {
                        isSearchActive = true
                    },
                    onSearchCloseClick = {
                        isSearchActive = false
                    },
                    onContactDetailClick = { context.openContactDetailScreen(it.id) },
                    onCallClick = { context.dial(it.phone) },
                    onQueryChanged = vm::onSearchQueryChanged
                )
            }
        },
        bottomBar = {
            if (isHomeTab) {
                AnimatedBottomBar(
                    visible = !isSearchActive,
                    backStack = backStack
                )
            }
        }
    ) { paddingValues ->
        BottomSheetScaffold(
            modifier = Modifier.padding(paddingValues),
            scaffoldState = scaffoldState,
            sheetContent = {
                KeyPad(
                    phone = dialPadPhone,
                    onTapDown = { char ->
                        dialPadPhone += char
                        vm.onSearchQueryChanged(dialPadPhone)
                    },
                    onTapUp = {},
                    onBackSpaceClicked = {
                        dialPadPhone = dialPadPhone.dropLast(1)
                        vm.onSearchQueryChanged(dialPadPhone)
                    },
                    onClearClicked = {
                        dialPadPhone = ""
                        vm.onSearchQueryChanged("")
                    },
                    onCallClicked = {
                        if (dialPadPhone.isNotBlank()) {
                            context.dial(dialPadPhone)
                        }
                    },
                    showCallButton = true,
                    showClearButton = true
                )
            },
            sheetPeekHeight = 0.dp,
            sheetDragHandle = null,
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
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
}
