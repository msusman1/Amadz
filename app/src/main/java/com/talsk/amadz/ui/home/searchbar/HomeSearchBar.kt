package com.talsk.amadz.ui.home.searchbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.home.HeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    onSearchBarActiveChange: (Boolean) -> Unit,
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit,
    vm: SearchViewModel = hiltViewModel()
) {
    val contacts by vm.contacts.collectAsStateWithLifecycle()
    var searchText by rememberSaveable { mutableStateOf("") }
    HomeSearchBarInternal(
        contacts = contacts,
        onSearchBarActiveChange = onSearchBarActiveChange,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick,
        onFavouriteToggle = vm::toggleFavourite,
        onQueryChanged = { query ->
            searchText = query
            vm.onSearchQueryChanged(query)
        },
        queryText = searchText
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBarInternal(
    contacts: List<ContactData>,
    onSearchBarActiveChange: (Boolean) -> Unit,
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit,
    onFavouriteToggle: ((ContactData) -> Unit)? = null,
    queryText: String,
    onQueryChanged: (String) -> Unit
) {

    var active by rememberSaveable { mutableStateOf(false) }
    val padding by animateDpAsState(if (active) 0.dp else 16.dp)
    LaunchedEffect(active) {
        onSearchBarActiveChange(active)
    }

    SearchBar(
        query = queryText,
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
                    if (queryText.isNotEmpty()) {
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
            onCallClick = onCallClick,
            onFavouriteToggle = onFavouriteToggle
        )
    }

}


@Composable
private fun SearchResults(
    filteredContacts: List<ContactData>,
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit,
    onFavouriteToggle: ((ContactData) -> Unit)? = null,
) {

    if (filteredContacts.isNotEmpty()) {
        HeaderItem(text = "All Contacts")
    }
    LazyColumn {
        items(filteredContacts) { contact ->
            ContactItem(
                contact = contact,
                onContactDetailClick = onContactDetailClick,
                onCallClick = onCallClick,
                onFavouriteToggle = onFavouriteToggle
            )
        }
    }
}