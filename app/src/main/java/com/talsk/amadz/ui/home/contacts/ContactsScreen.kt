package com.talsk.amadz.ui.home.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.home.EmptyContactItem
import com.talsk.amadz.ui.home.HeaderItem


@Preview
@Composable
private fun ContactsScreenPrev() {
    ContactsScreenInternal(
        contacts = listOf(
            ContactData.unknown("434344"),
            ContactData.unknown("43434444").copy(id = 3)
        ),
        onFavouriteToggle = {},
        onContactDetailClick = {},
        onCallClick = {}
    )
}

@Composable
fun ContactsScreen(
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit,
    vm: ContactsViewModel = hiltViewModel()
) {
    val contacts by vm.contacts.collectAsStateWithLifecycle()
    ContactsScreenInternal(
        contacts = contacts,
        onFavouriteToggle = vm::toggleFavourite,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick
    )
}

@Composable
fun ContactsScreenInternal(
    contacts: List<ContactData>,
    onFavouriteToggle: (ContactData) -> Unit,
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit
) {
    Column {
        HeaderItem(text = "Contacts")
        LazyColumn {
            items(contacts, key = { it.id }) { contact ->
                ContactItem(
                    contact = contact,
                    onContactDetailClick = onContactDetailClick,
                    onCallClick = onCallClick,
                    onFavouriteToggle = onFavouriteToggle
                )
            }
            item { EmptyContactItem() }
        }
    }
}