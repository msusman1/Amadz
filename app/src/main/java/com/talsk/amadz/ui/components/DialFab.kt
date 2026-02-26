package com.talsk.amadz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.talsk.amadz.R

@Composable
fun DialFab(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        FloatingActionButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_dialpad_24),
                contentDescription = "DialPad"
            )
        }
    }
}