package com.talsk.amadz.ui.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.extensions.openContactDetailScreen
import kotlin.collections.chunked

@Composable
fun FavouritesScreen(favourites: List<ContactData>, callLogs: List<CallLogData>) {
    val context = LocalContext.current
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
                    contacts = contacts, onCallClick = { context.dial(it.phone) })
            }
        }
        item {
            HeaderItem(text = "Frequents")
        }
        items(
            callLogs.filter { it.name.isNotEmpty() }
                .groupBy { it.phone }.values.sortedBy { it.size }.take(5)
                .mapNotNull { it.firstOrNull() }) { callLog ->
            ContactItem(
                contact = callLog.toContactData(),
                onContactDetailClick = {
                    context.openContactDetailScreen(it.id)
                },
                onCallClick = { contactData -> context.dial(contactData.phone) },
                onFavouriteToggle = null
            )
        }
        item { EmptyContactItem() }
    }
}