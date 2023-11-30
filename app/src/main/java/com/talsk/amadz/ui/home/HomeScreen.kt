package com.talsk.amadz.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talsk.amadz.R
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.openContactDetailScreen
import com.talsk.amadz.util.getStartOfDay

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */
@Composable
fun HomeScreen(
    contacts: List<ContactData>,
    callLogs: List<CallLogData>,
    onFavouriteToggle: (ContactData) -> Unit,
    onContactDetailClick: (ContactData) -> Unit,
    dailButtonClicked: () -> Unit,
) {
    val favourites: List<ContactData> =
        remember(key1 = contacts) { contacts.filter { it.isFavourite } }
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = dailButtonClicked) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_dialpad_24),
                contentDescription = "Dialpad"
            )
        }
    }, bottomBar = {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp
        ) {
            homeRoutes().forEach { menu ->

                val selected = currentDestination?.hierarchy?.any { it.route == menu.route } == true
                NavigationBarItem(label = { Text(menu.label) }, icon = {
                    Icon(
                        painter = painterResource(id = if (selected) menu.iconSelected else menu.icon),
                        contentDescription = menu.label
                    )
                }, selected = selected, onClick = {
                    navController.navigate(menu.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        }
    }


    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "recent",
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {

            composable("favourite") {

                LazyColumn {
                    item {
                        HeaderItem(text = "Favourites")
                    }
                    if (favourites.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    modifier = Modifier.size(96.dp),
                                    painter = painterResource(id = R.drawable.image_favourite),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Contact in Favourites",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(favourites.chunked(3)) { contacts ->
                            FavouriteItemGroup(contacts = contacts,
                                onCallClick = { context.dial(it.phone) })
                        }
                    }
                    item {
                        HeaderItem(text = "Frequents")
                    }
                    items(callLogs.filter { it.name.isNotEmpty() }
                        .groupBy { it.phone }.values.sortedBy { it.size }.take(5)
                        .mapNotNull { it.firstOrNull() }
                    ) { callLog ->
                        ContactItem(
                            contact = callLog.toContactData(),
                            onContactDetailClick = onContactDetailClick,
                            onCallClick = { contactData -> context.dial(contactData.phone) },
                            onFavouriteToggle = null
                        )
                    }
                    item { EmptyContactItem() }
                }
            }
            composable("recent") {
                Column {
                    val (older, today) = callLogs.partition { it.time.before(getStartOfDay()) }
                    LazyColumn {
                        if (today.isNotEmpty()) {
                            item { HeaderItem(text = "Today") }
                        }
                        items(today) { callLog ->
                            CallLogItem(logData = callLog,
                                onCallClick = { context.dial(it.phone) })
                        }
                        if (older.isNotEmpty()) {
                            item { HeaderItem(text = "Older") }
                        }
                        items(older) { callLog ->
                            CallLogItem(logData = callLog,
                                onCallClick = { context.dial(it.phone) })
                        }
                        item { EmptyContactItem() }
                    }
                }
            }
            composable("contact") {
                Column {
                    HeaderItem(text = "Contacts")
                    LazyColumn {
                        items(contacts) { contact ->
                            ContactItem(
                                contact = contact,
                                onContactDetailClick = {
                                    context.openContactDetailScreen(it.id)
                                },
                                onCallClick = { context.dial(it.phone) },
                                onFavouriteToggle = onFavouriteToggle
                            )
                        }
                        item { EmptyContactItem() }
                    }
                }


            }
        }


    }
}

