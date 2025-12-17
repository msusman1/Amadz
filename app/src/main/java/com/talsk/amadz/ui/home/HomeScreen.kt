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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.talsk.amadz.R
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.components.HomeSearchBar

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


@Composable
fun HomeScreen(
    contacts: List<ContactData>,
    callLogs: List<CallLogData>,
    onFavouriteToggle: (ContactData) -> Unit,
    dailButtonClicked: () -> Unit,
    loadNextPage: () -> Unit,
) {
    val favourites by remember {
        derivedStateOf { contacts.filter { it.isFavourite } }
    }
    val backStack = rememberNavBackStack(RecentsKey)
    val routes = remember { homeRoutes() }

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(onClick = dailButtonClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_dialpad_24),
                        contentDescription = "Dialpad"
                    )
                }
            }
        },
        topBar = {
            HomeSearchBar(contacts, onSearchBarActiveChange = { active ->
                isSearchActive = active
            })
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar {
                    routes.forEach { menu ->
                        val currentDestination: NavKey? = backStack.lastOrNull()
                        val selected = currentDestination == menu.navKey
                        NavigationBarItem(label = { Text(menu.label) }, icon = {
                            Icon(
                                painter = painterResource(id = if (selected) menu.iconSelected else menu.icon),
                                contentDescription = menu.label
                            )
                        }, selected = selected, onClick = {
                            backStack.add(menu.navKey)
                        })
                    }
                }
            }
        }


    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            entryProvider = entryProvider {
                entry(FavouritesKey) {
                    FavouritesScreen(favourites = favourites, callLogs = callLogs)
                }
                entry(RecentsKey) {
                    RecentsScreen(callLogs = callLogs, loadNextPage = loadNextPage)
                }
                entry(ContactsKey) {
                    ContactsScreen(contacts = contacts, onFavouriteToggle = onFavouriteToggle)
                }

            }
        )
    }
}

