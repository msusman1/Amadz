package com.talsk.amadz.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talsk.amadz.domain.entity.BlockedNumber
import java.util.regex.PatternSyntaxException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedNumbersScreen(
    onBackClick: () -> Unit,
    vm: BlockedNumbersViewModel = hiltViewModel()
) {
    val blockedNumbers by vm.blockedNumbers.collectAsStateWithLifecycle()
    var newNumber by rememberSaveable { mutableStateOf("") }
    var isRegex by rememberSaveable { mutableStateOf(false) }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Blocked numbers") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = newNumber,
                onValueChange = {
                    newNumber = it
                    inputError = null
                },
                singleLine = true,
                label = { Text(if (isRegex) "Phone number regex" else "Phone number") },
                supportingText = {
                    Text(
                        inputError ?: if (isRegex) {
                            "Regex matches normalized phone digits."
                        } else {
                            "Numbers are normalized before matching."
                        }
                    )
                },
                isError = inputError != null
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isRegex,
                    onClick = {
                        isRegex = false
                        inputError = null
                    },
                    label = { Text("Exact number") }
                )
                FilterChip(
                    selected = isRegex,
                    onClick = {
                        isRegex = true
                        inputError = null
                    },
                    label = { Text("Regex") }
                )
            }
            Button(
                onClick = {
                    val value = newNumber.trim()
                    if (isRegex) {
                        try {
                            Regex(value)
                            vm.addBlockedPattern(value)
                            newNumber = ""
                        } catch (exception: PatternSyntaxException) {
                            inputError = exception.description
                        }
                    } else {
                        vm.addBlockedNumber(value)
                        newNumber = ""
                    }
                },
                enabled = newNumber.trim().isNotEmpty()
            ) {
                Text(if (isRegex) "Add regex block" else "Add blocked number")
            }

            if (blockedNumbers.isEmpty()) {
                Text(
                    text = "No blocked numbers yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn {
                    items(blockedNumbers) { blockedNumber ->
                        ListItem(
                            headlineContent = { Text(blockedNumber.value) },
                            supportingContent = {
                                Text(
                                    if (blockedNumber.type == BlockedNumber.Type.REGEX) "Regex"
                                    else "Exact number"
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { vm.removeBlockedNumber(blockedNumber) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove blocked number"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
