package com.talsk.amadz.ui.home.contacts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.home.HeaderItemLargeBold


@Composable
fun ContactsScreen(
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit,
    vm: ContactsViewModel = hiltViewModel()
) {
    val contacts = vm.contacts.collectAsLazyPagingItems()
    ContactsScreenInternal(
        contacts = contacts,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick
    )
}

@Composable
fun ContactsScreenInternal(
    contacts: LazyPagingItems<ContactUiModel>,
    onContactDetailClick: (Contact) -> Unit,
    onCallClick: (Contact) -> Unit
) {
    LazyPagedColumn(
        modifier = Modifier.fillMaxSize(),
        pagingItems = contacts
    ) {
        items(contacts.itemCount, key = { index ->
            when (val item = contacts[index]) {
                is ContactUiModel.Header -> "header_${item.letter}"
                is ContactUiModel.Item -> "item_${item.contact.id}"
                null -> index
            }
        }) { index ->
            when (val model = contacts.peek(index)) {
                is ContactUiModel.Header -> {
                    HeaderItemLargeBold(text = model.letter.toString())
                }

                is ContactUiModel.Item -> {
                    ContactItem(
                        contact = model.contact,
                        onContactDetailClick = onContactDetailClick,
                        onCallClick = onCallClick
                    )
                }

                null -> Unit
            }
        }
    }
}