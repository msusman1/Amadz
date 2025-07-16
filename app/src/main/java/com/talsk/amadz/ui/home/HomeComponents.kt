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
import coil3.compose.AsyncImage
import com.talsk.amadz.R
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.CallLogType
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.toReadableFormat

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


fun homeRoutes(): List<BottomNavMenu> {

    return listOf(
        BottomNavMenu(
            icon = R.drawable.baseline_star_border_24,
            iconSelected = R.drawable.baseline_star_24,
            label = "Favourites",
            route = "favourite"
        ),
        BottomNavMenu(
            icon = R.drawable.baseline_access_time_24,
            iconSelected = R.drawable.baseline_access_time_filled_24,
            label = "Recents",
            route = "recent"
        ),

        BottomNavMenu(
            icon = R.drawable.outline_people_alt_24,
            iconSelected = R.drawable.baseline_people_alt_24,
            label = "Contacts",
            route = "contact"
        ),

        )
}

data class BottomNavMenu(
    val icon: Int, val iconSelected: Int, val label: String, val route: String
)

@Composable
fun HeaderItem(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
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
fun FavouriteItemGroup(contacts: List<ContactData>, onCallClick: (ContactData) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val first = contacts.getOrNull(0)
        val second = contacts.getOrNull(1)
        val third = contacts.getOrNull(2)
        if (first != null) {
            FavouriteItem(first, onCallClick)
        }
        if (second != null) {
            FavouriteItem(second, onCallClick)
        } else {
            Spacer(modifier = Modifier.size(96.dp))
        }
        if (third != null) {
            FavouriteItem(third, onCallClick)
        } else {
            Spacer(modifier = Modifier.size(96.dp))
        }
    }

}

@Composable
fun FavouriteItem(contact: ContactData, onCallClick: (ContactData) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .clickable() { onCallClick(contact) }) {
        Spacer(modifier = Modifier.height(12.dp))
        TextOrBitmapDrawable(modifier = Modifier.size(96.dp), contact = contact) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_call_24),
                contentDescription = "Call",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .padding(4.dp)
            )
        }

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
fun TextOrBitmapDrawable(
    modifier: Modifier, contact: ContactData, inner: @Composable BoxScope.() -> Unit = {}
) {
    if (contact.image != null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,

            ) {
            AsyncImage(
                model = contact.image,
                contentDescription = null,
                modifier = modifier.clip(CircleShape)
            )
            inner(this)
        }
    } else if (contact.name.isNullOrEmpty().not()) {
        Box(
            modifier = modifier.background(color = contact.bgColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = contact.name.first().toString(),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
            )
            inner(this)
        }
    } else {
        Box(
            modifier = modifier.background(color = contact.bgColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(id = R.drawable.outline_person_24), contentDescription = null)
            inner(this)
        }
    }

}

@Composable
fun ContactItem(
    contact: ContactData,
    onContactDetailClick: (ContactData) -> Unit,
    onCallClick: (ContactData) -> Unit,
    onFavouriteToggle: ((ContactData) -> Unit)? = null
) {
    ListItem(
        modifier = Modifier.clickable { onContactDetailClick(contact) },
        leadingContent = {
            TextOrBitmapDrawable(modifier = Modifier.size(56.dp), contact = contact)
        },
        headlineContent = { Text(text = contact.name) },
        supportingContent = { Text(text = contact.phone) },
        trailingContent = {
            Row {
                if (onFavouriteToggle != null) {
                    IconButton(onClick = { onFavouriteToggle(contact) }) {
                        Icon(
                            painter = painterResource(id = if (contact.isFavourite) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24),
                            contentDescription = "Call"
                        )
                    }
                }
                IconButton(onClick = { onCallClick(contact) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_call_24),
                        contentDescription = "Call"
                    )
                }
            }

        })
}


@Composable
fun CallLogItem(
    logData: CallLogData,
    onCallClick: (CallLogData) -> Unit
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
            TextOrBitmapDrawable(
                modifier = Modifier
                    .size(56.dp),
                contact = logData.toContactData()
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

@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier.padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CircularProgressIndicator()
    }
}


