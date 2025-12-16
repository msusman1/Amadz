package com.talsk.amadz.ui.home

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.ui.IconButtonLongClickable

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DialpadPrew() {
    Column {

        Spacer(Modifier.height(48.dp))
        Dialpad(
            phone = "2345",
            onTapDown = {},
            onTapUp = {},
            onBackSpaceClicked = {},
            onClearClicked = {},
            onCallClicked = {},
            showClearButton = false,
            showCallButton = false,
        )
    }
}


@Composable
fun Dialpad(
    modifier: Modifier = Modifier,
    phone: String,
    onTapDown: (Char) -> Unit,
    onTapUp: () -> Unit,
    onBackSpaceClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onCallClicked: () -> Unit,
    showCallButton: Boolean,
    showClearButton: Boolean,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 56.dp),
                text = phone,
                maxLines = 1,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            if (showClearButton) {
                IconButtonLongClickable(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onLongClick = onClearClicked,
                    onClick = {
                        if (phone.isNotEmpty()) {
                            onBackSpaceClicked()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Backspace, contentDescription = "Call"
                    )
                }
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                title = '1',
                subtitle = "",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '2',
                subtitle = "ABC",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '3',
                subtitle = "DEF",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                title = '4',
                subtitle = "GHI",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '5',
                subtitle = "JKL",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '6',
                subtitle = "MNO",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                title = '7',
                subtitle = "PQRS",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '8',
                subtitle = "TUV",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '9',
                subtitle = "WXYZ",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                title = '*',
                subtitle = "",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '0',
                subtitle = "+",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
            DialButton(
                title = '#',
                subtitle = "",
                onTapDown = onTapDown,
                onTapUp = onTapUp
            )
        }
        if (showCallButton) {
            Button(
                onClick = { onCallClicked() },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier
                    .height(56.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_24),
                    contentDescription = "phone"
                )
                Text(text = "Call", modifier = Modifier.padding(start = 16.dp))
            }
        }

    }
}

@Composable
fun RowScope.DialButton(
    title: Char,
    subtitle: String,
    onTapDown: (Char) -> Unit,
    onTapUp: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,

                )
            .clip(RoundedCornerShape(16.dp))
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        onTapDown(title)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                        onTapUp()
                    }
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
    }
}