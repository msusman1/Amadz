package com.talsk.amadz.ui.home

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DialpadPrew() {
    Dialpad({})
}

@Composable
fun Dialpad(onCallDialed:(String)->Unit) {
    var phone: String by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    if (phone.isNotEmpty()) {
                        phone = phone.dropLast(1)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Backspace,
                    contentDescription = "Call"
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton("1", "") { phone += "1" }
            DialButton("2", "ABC") { phone += "2" }
            DialButton("3", "DEF") { phone += "3" }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton("4", "GHI") { phone += "4" }
            DialButton("5", "JKL") { phone += "5" }
            DialButton("6", "MNO") { phone += "6" }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton("7", "PQRS") { phone += "7" }
            DialButton("8", "TUV") { phone += "8" }
            DialButton("9", "WXYZ") { phone += "9" }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialButton("*", "") { phone += "*" }
            DialButton("0", "+") { phone += "0" }
            DialButton("#", "") { phone += "#" }
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

    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .background(
                shape = ButtonDefaults.shape,
                color = MaterialTheme.colorScheme.background
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