package com.talsk.amadz.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/29/2023.
 */

@Composable
fun InfoDialog(
    message: String,
    onButtonClick: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            // Prevent dismissing the dialog when the effect is disposed
        }
    }
    AlertDialog(
        onDismissRequest = {},
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onButtonClick) {
                Text(text = "Ok")
            }
        },
        dismissButton = {
        }
    )

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconButtonLongClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.0.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
                enabled = true,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = 40.0.dp / 2
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
