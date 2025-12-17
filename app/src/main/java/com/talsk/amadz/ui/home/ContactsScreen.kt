package com.talsk.amadz.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.extensions.openContactDetailScreen

@Composable
fun ContactsScreen(contacts: List<ContactData>, onFavouriteToggle: (ContactData) -> Unit) {
    val context = LocalContext.current
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