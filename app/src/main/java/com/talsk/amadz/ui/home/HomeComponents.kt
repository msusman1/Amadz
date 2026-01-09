package com.talsk.amadz.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.talsk.amadz.R
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.CallLogType
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.components.ContactAvatar
import com.talsk.amadz.util.toReadableFormat
import kotlinx.serialization.Serializable

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */

@Serializable
data object FavouritesKey : NavKey

@Serializable
data object RecentsKey : NavKey

@Serializable
data object ContactsKey : NavKey

fun homeRoutes(): List<BottomNavMenu> {

    return listOf(
        BottomNavMenu(
            icon = R.drawable.baseline_star_border_24,
            iconSelected = R.drawable.baseline_star_24,
            label = "Favourites",
            navKey = FavouritesKey
        ),
        BottomNavMenu(
            icon = R.drawable.baseline_access_time_24,
            iconSelected = R.drawable.baseline_access_time_filled_24,
            label = "Recents",
            navKey = RecentsKey
        ),

        BottomNavMenu(
            icon = R.drawable.outline_people_alt_24,
            iconSelected = R.drawable.baseline_people_alt_24,
            label = "Contacts",
            navKey = ContactsKey
        ),

        )
}

data class BottomNavMenu(
    val icon: Int, val iconSelected: Int, val label: String, val navKey: NavKey
)


@Composable
fun HeaderItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun EmptyContactItem() {
    Spacer(
        modifier = Modifier.size(56.dp),
    )
}


@Composable
fun FavouriteItemGroup(
    contacts: List<ContactData>,
    onCallClick: (ContactData) -> Unit,
    onContactDetailClick: (ContactData) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val first = contacts.getOrNull(0)
        val second = contacts.getOrNull(1)
        val third = contacts.getOrNull(2)
        if (first != null) {
            FavouriteItem(first, onCallClick, onContactDetailClick)
        }
        if (second != null) {
            FavouriteItem(second, onCallClick, onContactDetailClick)
        } else {
            Spacer(modifier = Modifier.size(96.dp))
        }
        if (third != null) {
            FavouriteItem(third, onCallClick, onContactDetailClick)
        } else {
            Spacer(modifier = Modifier.size(96.dp))
        }
    }

}

@Composable
fun FavouriteItem(
    contact: ContactData,
    onCallClick: (ContactData) -> Unit,
    onContactDetailClick: (ContactData) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .clickable { onCallClick(contact) }) {
        Spacer(modifier = Modifier.height(12.dp))
        ContactAvatar(
            modifier = Modifier.size(96.dp),
            contact = contact,
            onClick = { onContactDetailClick(contact) }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = contact.name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

}


@Composable
fun CallLogItem(
    logData: CallLogData,
    onCallClick: (CallLogData) -> Unit,
    onContactDetailClick: (CallLogData) -> Unit
) {
    fun getCallIcon(): Int {
        return when (logData.callLogType) {
            CallLogType.MISSED -> R.drawable.baseline_call_missed_24
            CallLogType.INCOMING -> R.drawable.baseline_call_received_24
            CallLogType.OUTGOING -> R.drawable.baseline_call_made_24
        }
    }
    ListItem(
        leadingContent = {
            ContactAvatar(
                modifier = Modifier
                    .size(56.dp),
                contact = logData.toContactData(),
                onClick = { onContactDetailClick(logData) }
            )
        },
        headlineContent = { Text(text = logData.name.takeIf { it.isNotEmpty() } ?: logData.phone) },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp),
                    painter = painterResource(id = getCallIcon()),
                    tint = if (logData.callLogType == CallLogType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    contentDescription = null
                )
                Text(text = logData.time.toReadableFormat())
                if (logData.simSlot >= 0) {
                    Text(
                        text = "SIM ${logData.simSlot + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

        },
        trailingContent = {
            IconButton(onClick = { onCallClick(logData) }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_24),
                    contentDescription = "Call"
                )
            }

        })
}




