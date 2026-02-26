package com.talsk.amadz.ui.home.searchbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.home.HeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    onSearchBarActiveChange: (Boolean) -> Unit,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
    vm: SearchViewModel = hiltViewModel()
) {
    val contacts = vm.contacts.collectAsLazyPagingItems()
    val query by vm.query.collectAsStateWithLifecycle()
    HomeSearchBarInternal(
        contacts = contacts,
        query = query,
        onSearchBarActiveChange = onSearchBarActiveChange,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick,
        onQueryChanged = vm::onSearchQueryChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBarInternal(
    contacts: LazyPagingItems<Contact>,
    query: String,
    onSearchBarActiveChange: (Boolean) -> Unit,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
    onQueryChanged: (String) -> Unit
) {

    var active by rememberSaveable { mutableStateOf(false) }
    val padding by animateDpAsState(if (active) 0.dp else 16.dp)
    LaunchedEffect(active) {
        onSearchBarActiveChange(active)
    }

    SearchBar(
        query = query,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        onQueryChange = onQueryChanged,
        onSearch = { active = false },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search Contacts") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (active) {
                IconButton(onClick = {
                    if (query.isNotEmpty()) {
                        onQueryChanged("")
                    } else {
                        active = false
                    }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        }) {
        // 🔥 Expanded content → full width
        SearchResults(
            filteredContacts = contacts,
            onContactDetailClick = onContactDetailClick,
            onCallClick = onCallClick
        )
    }
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
        items(filteredContacts.itemCount, key = {
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