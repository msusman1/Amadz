package com.talsk.amadz.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.R
import com.talsk.amadz.core.dial
import com.talsk.amadz.ui.callLogHistory.CallLogHistoryScreen
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import com.talsk.amadz.ui.home.contacts.ContactsScreen
import com.talsk.amadz.ui.home.favourite.FavouritesScreen
import com.talsk.amadz.ui.home.calllogs.CallLogsScreen
import com.talsk.amadz.ui.home.searchbar.HomeSearchBar

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


@Composable
fun HomeScreen(
    dailButtonClicked: () -> Unit,
) {
    val backStack = rememberNavBackStack(RecentsKey)
    val routes = remember { homeRoutes() }
    val context = LocalContext.current
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val currentDestination: NavKey? = backStack.lastOrNull()
    val isHomeTab =
        currentDestination == FavouritesKey ||
            currentDestination == RecentsKey ||
            currentDestination == ContactsKey

    Scaffold(floatingActionButton = {
        if (isHomeTab) {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(onClick = dailButtonClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_dialpad_24),
                        contentDescription = "DialPad"
                    )
                }
            }
        }
    }, topBar = {
        if (isHomeTab) {
            HomeSearchBar(
                onSearchBarActiveChange = { active -> isSearchActive = active },
                onContactDetailClick = {
                    context.openContactDetailScreen(it.id)
                },
                onCallClick = {
                    context.dial(it.phone)
                })
        }
    }, bottomBar = {
        if (isHomeTab) {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()) {
                NavigationBar {
                    routes.forEach { menu ->
                        val selected = currentDestination == menu.navKey
                        NavigationBarItem(label = { Text(menu.label) }, icon = {
                            Icon(
                                painter = painterResource(id = if (selected) menu.iconSelected else menu.icon),
                                contentDescription = menu.label
                            )
                        }, selected = selected, onClick = {
                            val current = backStack.lastOrNull()
                            if (current != menu.navKey) {
                                backStack.clear()
                                backStack.add(menu.navKey)
                            }
                        })
                    }
                }
            }
        }
    }) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry(FavouritesKey) {
                    FavouritesScreen(onCallClick = {
                        context.dial(it.phone)
                    }, onContactDetailCLick = {
                        context.openContactDetailScreen(it.id)
                    })
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
                    ContactsScreen(onContactDetailClick = {
                        context.openContactDetailScreen(it.id)
                    }, onCallClick = {
                        context.dial(it.phone)
                    })
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

            })
    }
}
