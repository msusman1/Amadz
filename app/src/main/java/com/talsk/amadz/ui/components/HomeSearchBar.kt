package com.talsk.amadz.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.filterContacts
import com.talsk.amadz.ui.home.ContactItem
import com.talsk.amadz.ui.home.HeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    contacts: List<ContactData>, onSearchBarActiveChange: (Boolean) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    val filteredContacts by remember(text, contacts) {
        derivedStateOf {
            contacts.filterContacts(text)
        }
    }
    val padding by animateDpAsState(if (active) 0.dp else 16.dp)
    LaunchedEffect(active) {
        onSearchBarActiveChange(active)
    }

    SearchBar(
        query = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        onQueryChange = { text = it },
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
                    if (text.isNotEmpty()) {
                        text = ""
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
            filteredContacts = filteredContacts,
            onContactDetailClick = {},
            onCallClick = {},
            onFavouriteToggle = {})
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