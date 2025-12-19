package com.talsk.amadz.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/16/2023.
 */

@Preview(name = "Onboarding", showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen({})
}


@Composable
fun OnboardingScreen(onRequestDialerPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(140.dp),
                painter = painterResource(id = R.drawable.app_logo_short),
                contentDescription = "App Icon"
            )
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                style = MaterialTheme.typography.titleMedium,
                text = "Set Amadz as your default phone app to enjoy a tailored communication experience, Enjoy a streamlined and efficient way to manage calls, contacts, and essential phone functions.",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal

            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestDialerPermission) {
                Text(text = "Set as Default")
            }
        }
    }

}

