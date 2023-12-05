package com.talsk.amadz.ui.home

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Dialpad("", {}, {})
}

@Composable
fun Dialpad(phone: String, onDialChange: (String) -> Unit, onCallDialed: (String) -> Unit) {
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_DTMF, 100) }
    Column(
        modifier = Modifier
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
            IconButtonLongClickable(
                modifier = Modifier.align(Alignment.CenterEnd),
                onLongClick = { onDialChange("") },
                onClick = {
                    if (phone.isNotEmpty()) {
                        onDialChange(phone.dropLast(1))
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Backspace, contentDescription = "Call"
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                "1",
                ""
            ) { onDialChange(phone + "1");toneGenerator.startTone(ToneGenerator.TONE_DTMF_1);toneGenerator.stopTone() }
            DialButton(
                "2",
                "ABC"
            ) { onDialChange(phone + "2");toneGenerator.startTone(ToneGenerator.TONE_DTMF_2);toneGenerator.stopTone() }
            DialButton(
                "3",
                "DEF"
            ) { onDialChange(phone + "3");toneGenerator.startTone(ToneGenerator.TONE_DTMF_3);toneGenerator.stopTone() }
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                "4",
                "GHI"
            ) { onDialChange(phone + "4");toneGenerator.startTone(ToneGenerator.TONE_DTMF_4);toneGenerator.stopTone() }
            DialButton(
                "5",
                "JKL"
            ) { onDialChange(phone + "5");toneGenerator.startTone(ToneGenerator.TONE_DTMF_5);toneGenerator.stopTone() }
            DialButton(
                "6",
                "MNO"
            ) { onDialChange(phone + "6");toneGenerator.startTone(ToneGenerator.TONE_DTMF_6);toneGenerator.stopTone() }
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                "7",
                "PQRS"
            ) { onDialChange(phone + "7");toneGenerator.startTone(ToneGenerator.TONE_DTMF_7);toneGenerator.stopTone() }
            DialButton(
                "8",
                "TUV"
            ) { onDialChange(phone + "8");toneGenerator.startTone(ToneGenerator.TONE_DTMF_8);toneGenerator.stopTone() }
            DialButton(
                "9",
                "WXYZ"
            ) { onDialChange(phone + "9");toneGenerator.startTone(ToneGenerator.TONE_DTMF_9);toneGenerator.stopTone() }
        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton(
                "*",
                ""
            ) { onDialChange("$phone*");toneGenerator.startTone(ToneGenerator.TONE_DTMF_S);toneGenerator.stopTone() }
            DialButton(
                "0",
                "+"
            ) { onDialChange(phone + "0");toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);toneGenerator.stopTone() }
            DialButton(
                "#",
                ""
            ) { onDialChange("$phone#");toneGenerator.startTone(ToneGenerator.TONE_DTMF_P);toneGenerator.stopTone() }
        }
        Button(
            onClick = { onCallDialed(phone) },
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

@Composable
fun RowScope.DialButton(title: String, subtitle: String, onClick: () -> Unit) {

    Column(modifier = Modifier
        .weight(1f)
        .height(56.dp)
        .background(
            shape = ButtonDefaults.shape, color = MaterialTheme.colorScheme.background
        )
        .clip(RoundedCornerShape(16.dp))
        .clickable(indication = rememberRipple(color = MaterialTheme.colorScheme.secondaryContainer),
            interactionSource = remember { MutableInteractionSource() }) { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
    }

}