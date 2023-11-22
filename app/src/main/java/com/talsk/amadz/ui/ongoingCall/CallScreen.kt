package com.talsk.amadz.ui.ongoingCall

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.ui.home.TextOrBitmapDrawable
import com.talsk.amadz.ui.onboarding.CallUiState
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.ui.theme.green
import com.talsk.amadz.ui.theme.red

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */


@Composable
fun CallScreen(
    contact: ContactData,
    uiState: CallUiState,
    callTime: String,
    onIncomingAccept: () -> Unit,
    onIncomingDecline: () -> Unit,
    onEnd: () -> Unit,
    setCallOnHold: (Boolean) -> Unit,
    setCallMute: (Boolean) -> Unit,
    setSpeakerOn: (Boolean) -> Unit,
) {
    AmadzTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(56.dp))
                    TextOrBitmapDrawable(modifier = Modifier.size(96.dp), contact = contact)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(contact.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(contact.phone, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(uiState.toStatus(), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState is CallUiState.InCall) {
                        Text(
                            text = callTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 56.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        ToggleFab(
                            icon = R.drawable.outline_pause_24,
                            text = "Hold",
                            onAction = setCallOnHold
                        )
                        ToggleFab(
                            icon = R.drawable.outline_mic_off_24,
                            text = "Mute",
                            onAction = setCallMute
                        )
                        ToggleFab(
                            icon = R.drawable.outline_volume_up_24,
                            text = "Speaker",
                            onAction = setSpeakerOn
                        )


                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (uiState is CallUiState.InComingCall) Arrangement.SpaceBetween else Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        if (uiState is CallUiState.InComingCall) {
                            FloatingActionButton(
                                onClick = onIncomingDecline,
                                containerColor = red,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_call_end_24),
                                    contentDescription = "Decline"
                                )
                            }
                            FloatingActionButton(
                                onClick = onIncomingAccept,
                                containerColor = green,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_call_24),
                                    contentDescription = "Accept"
                                )
                            }
                        }
                        if (uiState is CallUiState.InCall || uiState is CallUiState.OutgoingCall) {
                            FloatingActionButton(
                                onClick = onEnd,
                                containerColor = red,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_call_end_24),
                                    contentDescription = "End Call"
                                )
                            }
                        }


                    }
                }

            }

        }
    }
}


@Composable
fun ToggleFab(
    @DrawableRes icon: Int,
    text: String,
    onAction: (Boolean) -> Unit
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
                painter = painterResource(id = icon),
                contentDescription = text
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text)
    }
}