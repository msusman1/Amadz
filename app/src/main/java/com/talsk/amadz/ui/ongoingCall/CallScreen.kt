package com.talsk.amadz.ui.ongoingCall

import android.telephony.TelephonyManager
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.ui.components.ContactAvatar
import com.talsk.amadz.ui.components.ToggleFab
import com.talsk.amadz.ui.home.KeyPad
import com.talsk.amadz.ui.onboarding.CallDirection
import com.talsk.amadz.ui.onboarding.CallState
import com.talsk.amadz.ui.onboarding.ContactWithCompanyName
import com.talsk.amadz.ui.theme.green
import com.talsk.amadz.ui.theme.red
import com.talsk.amadz.util.secondsToReadableTime

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */

@Preview
@Composable
private fun CallScreenPrev() {
    CallScreen(
        contact = ContactWithCompanyName(
            contact = Contact(
                id = 232L,
                name = "ALi",
                phone = "45678o",
                image = null,

                ),
            companyName = "Visa",
        ),
        uiState = CallState.Active(
            duration = 12,
            isMuted = false,
            isSpeakerOn = false,
            isOnHold = false
        ),
        onAction = {}
    )
}

fun Int.toSimStateReadable(): String {
    return when (this) {
        TelephonyManager.SIM_STATE_ABSENT -> "No Sim available"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card Io error"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Sim card restricted"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
        TelephonyManager.SIM_STATE_NOT_READY -> "Sim not ready"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM disabled"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
        TelephonyManager.SIM_STATE_READY -> "Sim is ready"
        else -> "Unknown"
    }
}

@Composable
fun CallScreen(
    contact: ContactWithCompanyName,
    uiState: CallState,
    onAction: (CallAction) -> Unit,
) {

    var keyboardOpen by rememberSaveable { mutableStateOf(false) }

    /* val telephonyManager = getSystemService(TelephonyManager::class.java)
     if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY){
         if (uiState is CallState.SimError) {
             SimErrorDialog(
                 message = uiState.message,
                 onDismiss = {
                     if(vm.callState.value is CallState.CallDisconnected){
                         finishAndRemoveTask()
                     }

                 }
             )
         }
     }*/




    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CallHeader(
                    contact = contact.contact,
                    companyName = contact.companyName,
                    uiState = uiState,
                    onContactDetailClick = {}
                )
                KeyPad(
                    keyboardOpen = keyboardOpen,
                    startTone = { onAction(CallAction.StartDialTone(it)) },
                    stopTone = { onAction(CallAction.StopDialTone) }
                )
            }

        }
        Column(
            modifier = Modifier
                .weight(0.4f)
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 32.dp), verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToggleFab(
                    icon = R.drawable.baseline_dialpad_24,
                    text = "Keyboard",
                    onAction = {
                        keyboardOpen = it
                    }
                )
                ToggleFab(
                    icon = R.drawable.outline_pause_24,
                    text = "Hold",
                    onAction = { onAction(CallAction.Hold(it)) }
                )
                ToggleFab(
                    icon = R.drawable.outline_mic_off_24,
                    text = "Mute",
                    onAction = { onAction(CallAction.Mute(it)) }
                )
                ToggleFab(
                    icon = R.drawable.outline_volume_up_24,
                    text = "Speaker",
                    onAction = { onAction(CallAction.Speaker(it)) }
                )
            }

            CallActionButtons(
                uiState = uiState,
                onAnswer = { onAction(CallAction.Answer) },
                onHangup = { onAction(CallAction.Hangup) }
            )

        }

    }
}

@Composable
fun CallHeader(
    contact: Contact,
    companyName: String?,
    uiState: CallState,
    onContactDetailClick: (Contact) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(56.dp))
        ContactAvatar(
            modifier = Modifier.size(96.dp),
            contact = contact,
            onClick = { onContactDetailClick(contact) })
        Spacer(modifier = Modifier.height(16.dp))
        Text(contact.name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(companyName ?: "", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(contact.phone, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            uiState.toReadableStatus(),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState is CallState.Active) {
            Text(
                text = secondsToReadableTime(uiState.duration.toInt()),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        } else if (uiState is CallState.OnHold) {
            Text(
                text = "Call On Hold",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BoxScope.KeyPad(keyboardOpen: Boolean, startTone: (Char) -> Unit, stopTone: () -> Unit) {
    var dialed by rememberSaveable { mutableStateOf("") }
    androidx.compose.animation.AnimatedVisibility(
        visible = keyboardOpen,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = Modifier
            .align(alignment = Alignment.BottomCenter),
    ) {

        KeyPad(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .align(alignment = Alignment.BottomCenter),
            phone = dialed,
            onTapDown = {
                dialed += it
                startTone(it)
            },
            onTapUp = stopTone,
            onBackSpaceClicked = {
                dialed = dialed.dropLast(1)
            },
            onClearClicked = {
                dialed = ""
            },
            onCallClicked = {},
            showCallButton = false,
            showClearButton = false,
        )
    }
}


@Composable
fun CallActionButtons(
    uiState: CallState,
    onAnswer: () -> Unit,
    onHangup: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (uiState is CallState.Ringing) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (uiState is CallState.Ringing) {
            FloatingActionButton(
                onClick = onHangup,
                containerColor = red,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_end_24),
                    contentDescription = "Decline"
                )
            }
            FloatingActionButton(
                onClick = onAnswer,
                containerColor = green,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_24),
                    contentDescription = "Accept"
                )
            }
        }
        if (uiState is CallState.Active
            || (uiState is CallState.Ringing && uiState.direction == CallDirection.OUTGOING)
            || uiState is CallState.OnHold
        ) {
            FloatingActionButton(
                onClick = onHangup, containerColor = red, contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_end_24),
                    contentDescription = "End Call"
                )
            }
        }
    }
}