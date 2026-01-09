package com.talsk.amadz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.data.ContactData


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
            ContactAvatar(
                contact = contact,
                modifier = Modifier.size(56.dp),
                onClick = { onContactDetailClick(contact) })
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