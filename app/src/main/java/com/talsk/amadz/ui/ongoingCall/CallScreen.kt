package com.talsk.amadz.ui.ongoingCall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talsk.amadz.R
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.entity.CallState
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactAvatar
import com.talsk.amadz.ui.components.SimErrorDialog
import com.talsk.amadz.ui.components.ToggleFab
import com.talsk.amadz.ui.home.KeyPad
import com.talsk.amadz.ui.home.deleteBeforeCursor
import com.talsk.amadz.ui.home.insertAtSelection
import com.talsk.amadz.ui.theme.AmadzTheme
import com.talsk.amadz.ui.theme.green
import com.talsk.amadz.ui.theme.red
import com.talsk.amadz.util.secondsToReadableTime

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */

@Preview
@Composable
private fun CallScreenPrev() {
    AmadzTheme(darkTheme = false) {
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
            uiState = CallState.Idle,
            onAction = {},
            onFinish = {}
        )
    }
}


@Composable
fun CallScreen(
    contact: ContactWithCompanyName,
    uiState: CallState,
    onAction: (CallAction) -> Unit,
    onFinish: () -> Unit,
) {

    var keyboardOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState !is CallState.Active) {
            if (keyboardOpen) {
                onAction(CallAction.StopDialTone)
            }
            keyboardOpen = false
        }
        if (uiState == CallState.CallDisconnected) {
            onFinish()
        }
    }
    if (uiState is CallState.SimError) {
        SimErrorDialog(
            message = uiState.message,
            onDismiss = {
                onFinish()
            }
        )
    }

    val canUseCallControls = uiState is CallState.Active || uiState is CallState.OnHold
    val canToggleKeyboard = uiState is CallState.Active
    val canToggleSpeaker = uiState is CallState.Active || uiState is CallState.Ringing
    val isOnHold = uiState is CallState.OnHold
    val isMuted = (uiState as? CallState.Active)?.isMuted == true
    val isSpeakerOn = when (uiState) {
        is CallState.Active -> uiState.isSpeakerOn
        is CallState.Ringing -> uiState.isSpeakerOn
        else -> false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Top
    ) {
        CallHeader(
            modifier = Modifier.weight(1f),
            contact = contact.contact,
            companyName = contact.companyName,
            uiState = uiState,
            compact = keyboardOpen,
        )

        KeyPad(
            keyboardOpen = keyboardOpen,
            startTone = { onAction(CallAction.StartDialTone(it)) },
            stopTone = { onAction(CallAction.StopDialTone) }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToggleFab(
                    icon = R.drawable.baseline_dialpad_24,
                    text = "Keyboard",
                    isActive = keyboardOpen,
                    modifier = Modifier.weight(1f),
                    enabled = canToggleKeyboard,
                    onAction = {
                        keyboardOpen = it
                    }
                )
                ToggleFab(
                    icon = if (isOnHold) R.drawable.baseline_play_arrow_24 else R.drawable.outline_pause_24,
                    text = if (isOnHold) "Resume" else "Hold",
                    isActive = isOnHold,
                    modifier = Modifier.weight(1f),
                    enabled = canUseCallControls,
                    onAction = { onAction(CallAction.Hold(it)) }
                )
                ToggleFab(
                    icon = if (isMuted) {
                        R.drawable.outline_mic_off_24
                    } else {
                        R.drawable.outline_mic_24
                    },
                    text = "Mute",
                    isActive = isMuted,
                    modifier = Modifier.weight(1f),
                    enabled = uiState is CallState.Active,
                    onAction = { onAction(CallAction.Mute(it)) }
                )
                ToggleFab(
                    icon = if (isSpeakerOn) {
                        R.drawable.outline_volume_up_24
                    } else {
                        R.drawable.outline_volume_off_24
                    },
                    text = "Speaker",
                    isActive = isSpeakerOn,
                    modifier = Modifier.weight(1f),
                    enabled = canToggleSpeaker,
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
    modifier: Modifier = Modifier,
    contact: Contact,
    companyName: String?,
    uiState: CallState,
    compact: Boolean = false,
) {
    val compactTransition = updateTransition(targetState = compact, label = "callHeaderCompact")
    val avatarSize by compactTransition.animateDp(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderAvatarSize",
    ) { isCompact ->
        if (isCompact) 48.dp else 96.dp
    }
    val nameFontSize by compactTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderNameSize",
    ) { isCompact ->
        if (isCompact) 16f else 22f
    }
    val phoneFontSize by compactTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderPhoneSize",
    ) { isCompact ->
        if (isCompact) 14f else 16f
    }
    val statusFontSize by compactTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderStatusSize",
    ) { isCompact ->
        if (isCompact) 12f else 14f
    }
    val durationFontSize by compactTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderDurationSize",
    ) { isCompact ->
        if (isCompact) 16f else 24f
    }
    val avatarTextSpacing by compactTransition.animateDp(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderAvatarSpacing",
    ) { isCompact ->
        if (isCompact) 8.dp else 16.dp
    }
    val detailSpacing by compactTransition.animateDp(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderDetailSpacing",
    ) { isCompact ->
        if (isCompact) 4.dp else 8.dp
    }
    val statusSpacing by compactTransition.animateDp(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderStatusSpacing",
    ) { isCompact ->
        if (isCompact) 4.dp else 12.dp
    }
    val contentVerticalPadding by compactTransition.animateDp(
        transitionSpec = { tween(durationMillis = 300, easing = LinearEasing) },
        label = "callHeaderVerticalPadding",
    ) { isCompact ->
        if (isCompact) 8.dp else 24.dp
    }

    Surface(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                    )
                    .padding(horizontal = 24.dp, vertical = contentVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ContactAvatar(
                    modifier = Modifier.size(avatarSize),
                    contact = contact,
                )
                Spacer(modifier = Modifier.height(avatarTextSpacing))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = nameFontSize.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AnimatedVisibility(
                    visible = !compact && !companyName.isNullOrBlank(),
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                    ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = LinearEasing)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                    ) + fadeOut(animationSpec = tween(durationMillis = 300, easing = LinearEasing)),
                ) {
                    Text(
                        text = companyName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(detailSpacing))
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = phoneFontSize.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (uiState == CallState.Idle) {
                        "Waiting for call"
                    } else {
                        uiState.toReadableStatus()
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = statusFontSize.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = statusSpacing),
                )
                if (uiState is CallState.Active) {
                    Text(
                        text = secondsToReadableTime(uiState.duration),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = durationFontSize.sp,
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = statusSpacing)
                            .semantics {
                                contentDescription =
                                    "Call duration ${secondsToReadableTime(uiState.duration)}"
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun KeyPad(keyboardOpen: Boolean, startTone: (Char) -> Unit, stopTone: () -> Unit) {
    var dialed by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = keyboardOpen,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = slideOutVertically { it / 2 } + fadeOut(),

        ) {

        KeyPad(
            phone = dialed,
            onPhoneChange = { dialed = it },
            onTapDown = {
                dialed = dialed.insertAtSelection(it.toString())
                startTone(it)
            },
            onTapUp = stopTone,
            onBackSpaceClicked = {
                dialed = dialed.deleteBeforeCursor()
            },
            onClearClicked = {
                dialed = TextFieldValue()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .heightIn(min = 56.dp),
        horizontalArrangement = if (uiState.isIncomingCall()) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (uiState.isIncomingCall()) {
            FloatingActionButton(
                onClick = onHangup,
                modifier = Modifier.size(56.dp),
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
                modifier = Modifier.size(56.dp),
                containerColor = green,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_24),
                    contentDescription = "Accept"
                )
            }
        } else {
            FloatingActionButton(
                onClick = onHangup,
                modifier = Modifier.size(56.dp),
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