package com.talsk.amadz.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.talsk.amadz.R
import com.talsk.amadz.domain.entity.Contact

@Preview(showBackground = true)
@Composable
fun ContactAvatarPreview() {
    val contact = Contact.unknown("33445545").copy(name = "")
    ContactAvatar(contact = contact, modifier = Modifier.size(56.dp), onClick = {})
}

@Composable
fun ContactAvatar(
    contact: Contact, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val borderColor = Color.Black.copy(alpha = 0.08f)

    // Use a single Box as the "Container"
    Box(
        modifier = modifier
            .aspectRatio(1f) // Ensure it's always a square/circle
            .clip(CircleShape)
            .background(contact.getBackgroundColor())
            // Add the border here
            .border(width = 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick), contentAlignment = Alignment.Center
    ) {
        when {
            // Case 1: Photo available
            (contact.image != null && contact.image != Uri.EMPTY) -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(contact.image)
                        .crossfade(true).build(),
                    contentDescription = "Profile picture of ${contact.name}",
                    modifier = Modifier.fillMaxSize(), // Fill the size provided by the parent modifier
                    contentScale = ContentScale.Crop
                )
            }

            // Case 2: No photo, but name exists (Initials)
            contact.name.trim().isNotEmpty() -> {
                Text(
                    text = contact.getNamePlaceHolder(), // Better than first().toString()
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            // Case 3: Empty name and no photo (Placeholder)
            else -> {
                Icon(
                    painter = painterResource(id = R.drawable.outline_person_24),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}