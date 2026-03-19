package com.talsk.amadz.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.talsk.amadz.core.DtmfTonePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onBlockedNumbersClick: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(DtmfTonePrefs.PREFS_NAME, Context.MODE_PRIVATE)
    }
    var dtmfTonesEnabled by remember {
        mutableStateOf(prefs.getBoolean(DtmfTonePrefs.KEY_DTMF_TONE_ENABLED, true))
    }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(paddingValues)) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Dial pad tones"
                    )
                },
                headlineContent = { Text("Dial pad tones") },
                supportingContent = { Text("Respect system sound settings") },
                trailingContent = {
                    Switch(
                        checked = dtmfTonesEnabled,
                        onCheckedChange = { enabled ->
                            dtmfTonesEnabled = enabled
                            prefs.edit()
                                .putBoolean(DtmfTonePrefs.KEY_DTMF_TONE_ENABLED, enabled)
                                .apply()
                        }
                    )
                }
            )

            ListItem(
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Sound and vibration"
                    )
                },
                headlineContent = { Text("Sound and vibration") }
            )

            ListItem(
                modifier = Modifier.clickable(onClick = onBlockedNumbersClick),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Blocked numbers"
                    )
                },
                headlineContent = { Text("Blocked numbers") }
            )

            ListItem(
                modifier = Modifier.clickable { showClearLogsDialog = true },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Clear call logs"
                    )
                },
                headlineContent = { Text("Clear call logs") },
                supportingContent = { Text("Delete all call history") }
            )
        }
    }

    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = { Text("Clear call logs?") },
            text = { Text("This will permanently delete all call history from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearLogsDialog = false
                        vm.clearAllCallLogs()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
