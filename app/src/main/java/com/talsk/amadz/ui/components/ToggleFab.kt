package com.talsk.amadz.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp


@Composable
fun ToggleFab(
    @DrawableRes icon: Int,
    text: String,
    isActive: Boolean,
    onAction: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onAction(!isActive) },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.background,
                contentColor = if (isActive) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
            ),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .semantics {
                    contentDescription = text
                    role = Role.Switch
                    stateDescription = if (isActive) "On" else "Off"
                },
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}