package com.talsk.amadz.ui.home.favourite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talsk.amadz.R
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.home.EmptyContactItem
import com.talsk.amadz.ui.home.FavouriteItemGroup
import com.talsk.amadz.ui.home.HeaderItem


@Composable
fun FavouritesScreen(
    onCallClick: (Contact) -> Unit,
    onContactDetailCLick: (Contact) -> Unit,
    vm: FavouritesViewModel = hiltViewModel()
) {
    val favourites by vm.favourites.collectAsStateWithLifecycle()
    val frequentCalledContacts by vm.frequentCalledContacts.collectAsStateWithLifecycle()
    FavouritesScreenInternal(
        favourites = favourites,
        frequents = frequentCalledContacts,
        onCallClick = onCallClick,
        onContactDetailCLick = onContactDetailCLick
    )
}

@Composable
fun FavouritesScreenInternal(
    favourites: List<Contact>,
    frequents: List<Contact>,
    onCallClick: (Contact) -> Unit,
    onContactDetailCLick: (Contact) -> Unit,
) {
    LazyColumn {
        item {
            HeaderItem(text = "Favourites")
        }
        if (favourites.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(id = R.drawable.image_favourite),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Contact in Favourites",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(favourites.chunked(3)) { contacts ->
                FavouriteItemGroup(
                    contacts = contacts,
                    onCallClick = onCallClick,
                    onContactDetailClick = onContactDetailCLick
                )
            }
        }
        item {
            HeaderItem(text = "Frequents")
        }
        items(
            frequents, key = { it.id }) { contact ->
            ContactItem(
                contact = contact,
                onContactDetailClick = onContactDetailCLick,
                onCallClick = onCallClick,
            )
        }
        item { EmptyContactItem() }
    }
}