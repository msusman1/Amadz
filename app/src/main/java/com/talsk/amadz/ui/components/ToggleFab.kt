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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun ToggleFab(
    @DrawableRes icon: Int, text: String, onAction: (Boolean) -> Unit
) {
    var isActive: Boolean by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = {
                isActive = !isActive
                onAction(isActive)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.background,
                contentColor = if (isActive) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
            ),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = painterResource(id = icon), contentDescription = text
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text)
    }
}