package com.talsk.amadz.ui.home.searchbar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.R
import com.talsk.amadz.core.dial
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.extensions.openContactAddScreen
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.ui.home.KeyPad


enum class SearchBarState {
    COLLAPSED,
    EXPANDED,
    EXPANDED_WITH_DIAL_PAD;

    fun isActive(): Boolean {
        return this == EXPANDED || this == EXPANDED_WITH_DIAL_PAD
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    searchBarState: SearchBarState,
    onSearchBarClick: () -> Unit,
    onSearchCloseClick: () -> Unit,
    vm: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contacts = vm.contacts.collectAsLazyPagingItems()
    val query by vm.query.collectAsStateWithLifecycle()
    val padding by animateDpAsState(if (searchBarState == SearchBarState.COLLAPSED) 16.dp else 0.dp)
    var dialPadPhone by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = searchBarState.isActive()) {
        vm.onSearchQueryChanged("")
        onSearchCloseClick()
    }
    SearchBar(
        inputField = {
            if (searchBarState != SearchBarState.EXPANDED_WITH_DIAL_PAD) {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = vm::onSearchQueryChanged,
                    onSearch = vm::onSearchQueryChanged,
                    expanded = searchBarState.isActive(),
                    onExpandedChange = { expanded ->
                        if (expanded) onSearchBarClick() else {
                            vm.onSearchQueryChanged("")
                            onSearchCloseClick()
                        }
                    },
                    enabled = true,
                    placeholder = { Text("Search Contacts") },
                    leadingIcon = {
                        if (searchBarState.isActive()) {
                            IconButton(onClick = {
                                vm.onSearchQueryChanged("")
                                onSearchCloseClick()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                    },
                    trailingIcon = {
                        if (searchBarState.isActive() && query.isNotEmpty()) {
                            IconButton(onClick = { vm.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    interactionSource = null,
                )
            }
        },
        expanded = searchBarState.isActive(),
        onExpandedChange = { expanded ->
            if (expanded) onSearchBarClick() else {
                vm.onSearchQueryChanged("")
                onSearchCloseClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        shape = if (searchBarState.isActive()) RoundedCornerShape(0.dp) else SearchBarDefaults.inputFieldShape,
        colors = SearchBarDefaults.colors(
            dividerColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.background,
            inputFieldColors = inputFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ),
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchResults(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    dialPadPhone = dialPadPhone,
                    filteredContacts = contacts,
                    onContactDetailClick = {
                        if (it.id > 0) context.openContactDetailScreen(it.id)
                        else context.openContactAddScreen(it.phone)
                    },
                    onCallClick = { context.dial(it.phone) }
                )
                if (searchBarState == SearchBarState.EXPANDED_WITH_DIAL_PAD) {
                    KeyPad(
                        modifier = Modifier.fillMaxWidth(),
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
                }
            }
        }

    )
}

@Composable
private fun SearchResults(
    dialPadPhone: String,
    modifier: Modifier = Modifier,
    filteredContacts: LazyPagingItems<Contact>,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        if (dialPadPhone.isNotEmpty()) {
            NewContactHeader({ context.openContactAddScreen(dialPadPhone) }, dialPadPhone)
        }
        if (filteredContacts.itemCount > 0) {
            HeaderItem(text = "Suggestions")
        }
        LazyPagedColumn(
            pagingItems = filteredContacts,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(
                count = filteredContacts.itemCount,
                key = {
                    filteredContacts[it]?.let { contact -> "${contact.id}_${contact.phone}" } ?: it
                }) { index ->
                filteredContacts[index]?.let {
                    ContactItem(
                        contact = it,
                        onContactDetailClick = onContactDetailClick,
                        onCallClick = onCallClick
                    )
                }
            }
        }
    }
}


@Composable
private fun NewContactHeader(
    onContactAddCLicked: (String) -> Unit, dialPhone: String
) {
    ListItem(
        modifier = Modifier.clickable { onContactAddCLicked(dialPhone) },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_add_alt_24),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Call"
            )
        },
        headlineContent = {
            Text(
                text = "Create new contact", color = MaterialTheme.colorScheme.primary
            )
        },
    )
}