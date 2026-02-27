package com.talsk.amadz.ui.home.searchbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    searchActive: Boolean,
    contacts: LazyPagingItems<Contact>,
    query: String,
    onSearchBarClick: () -> Unit,
    onSearchCloseClick: () -> Unit,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val padding by animateDpAsState(if (searchActive) 0.dp else 16.dp)

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChanged,
                onSearch = onQueryChanged,
                expanded = searchActive,
                onExpandedChange = {
                    if (it) {
                        onSearchBarClick()
                    }
                },
                enabled = true,
                placeholder = { Text("Search Contacts") },
                leadingIcon = {
                    if (searchActive) {
                        IconButton(onClick = onSearchCloseClick) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                },
                trailingIcon = {
                    if (searchActive && query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                interactionSource = null,
            )
        },
        expanded = searchActive,
        onExpandedChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        shape = if (searchActive) RoundedCornerShape(0.dp) else SearchBarDefaults.inputFieldShape,
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
                onContactDetailClick = onContactDetailClick,
                onCallClick = onCallClick
            )
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
