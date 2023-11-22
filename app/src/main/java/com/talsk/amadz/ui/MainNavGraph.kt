package com.talsk.amadz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.filterContacts
import com.talsk.amadz.data.openContactDetailScreen
import com.talsk.amadz.ui.home.ContactItem
import com.talsk.amadz.ui.home.Dialpad
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.ui.home.HomeScreen
import com.talsk.amadz.ui.home.LoadingIndicator
import kotlinx.coroutines.launch

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/16/2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(contacts: List<ContactData>, callLogs: List<CallLogData>,onFavouriteToggle:(ContactData)->Unit) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))
    val context = LocalContext.current

    BackHandler(enabled = bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch { bottomSheetScaffoldState.bottomSheetState.hide() }
    }
    BottomSheetScaffold(
        sheetContent = { Dialpad { context.dial(it) } },
        sheetShape = BottomSheetDefaults.HiddenShape,
        sheetPeekHeight = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 0.dp,
        sheetDragHandle = null,
        scaffoldState = bottomSheetScaffoldState,
    ) {
        NavHost(navController = navController, "home") {

            composable("home") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var text by rememberSaveable { mutableStateOf("") }
                    var active by rememberSaveable { mutableStateOf(false) }

                    SearchBar(
                        query = text,
                        onQueryChange = { text = it },
                        onSearch = { active = false },
                        active = active,
                        onActiveChange = {
                            active = it
                        },
                        placeholder = { Text("Search Contacts") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (active) {
                                IconButton(onClick = {
                                    if (text.isEmpty().not()) {
                                        text = ""
                                    } else {
                                        active = false
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                    ) {

                        HeaderItem(text = "All Contacts")

                        LazyColumn {
                            items(contacts.filterContacts(text)) { contact ->
                                ContactItem(
                                    contact = contact,
                                    onContactDetailClick = {
                                        context.openContactDetailScreen(it.id)
                                    },
                                    onCallClick = { context.dial(it.phone) },
                                    onFavouriteToggle=onFavouriteToggle
                                    )
                            }
                        }
                    }
                    HomeScreen(
                        contacts = contacts,
                        callLogs = callLogs,
                        onFavouriteToggle=onFavouriteToggle
                    ) {
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            }
        }
    }
}