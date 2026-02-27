package com.talsk.amadz.ui.home.searchbar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.core.dial
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.components.LazyPagedColumn
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
                    onExpandedChange = { if (it) onSearchBarClick() },
                    enabled = true,
                    placeholder = { Text("Search Contacts") },
                    leadingIcon = {
                        if (searchBarState.isActive()) {
                            IconButton(onClick = onSearchCloseClick) {
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
        onExpandedChange = { },
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
            SearchResults(
                filteredContacts = contacts,
                onContactDetailClick = { context.openContactDetailScreen(it.id) },
                onCallClick = { context.dial(it.phone) }
            )
            if (searchBarState == SearchBarState.EXPANDED_WITH_DIAL_PAD) {
                KeyPad(
                    modifier = Modifier.weight(1.0f),
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

    )
}

@Composable
private fun SearchResults(
    filteredContacts: LazyPagingItems<Contact>,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
) {
    if (filteredContacts.itemCount > 0) {
        HeaderItem(text = "All Contacts")
    }
    LazyPagedColumn(filteredContacts) {
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
