package com.talsk.amadz.ui.callLogHistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talsk.amadz.R
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.ui.components.FullScreenLoader
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.ui.home.calllogs.CallLogUiModel
import com.talsk.amadz.util.startOfDay
import com.talsk.amadz.util.toDayCategory
import com.talsk.amadz.util.toReadableFormat

@Composable
fun CallLogHistoryScreen(
    phone: String,
    contactName: String,
    onBackClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onAddContactClick: (String) -> Unit,
    vm: CallLogHistoryViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(phone, contactName) {
        vm.load(phone = phone, cachedName = contactName)
    }
    CallLogHistoryScreenInternal(
        state = uiState,
        onBackClick = onBackClick,
        onCallClick = onCallClick,
        onAddContactClick = onAddContactClick,
        onDeleteHistory = vm::deleteHistory,
        onToggleBlocked = vm::toggleBlocked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallLogHistoryScreenInternal(
    state: CallLogHistoryUiState,
    onBackClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onAddContactClick: (String) -> Unit,
    onDeleteHistory: () -> Unit,
    onToggleBlocked: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = state.title, style = MaterialTheme.typography.titleMedium)
                        if (state.title != state.phone) {
                            Text(text = state.phone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!state.isSavedContact && state.phone.isNotBlank()) {
                        IconButton(onClick = { onAddContactClick(state.phone) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_person_add_alt_24),
                                contentDescription = "Add contact"
                            )
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (state.isBlocked) "Unblock number" else "Block number")
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleBlocked()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete history") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteHistory()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onCallClick(state.phone) }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_call_24),
                    contentDescription = "Call"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Call")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            FullScreenLoader()
        } else if (state.logs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No call history")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(
                    items = state.logs,
                    key = { item ->
                        when (item) {
                            is CallLogUiModel.Header -> "header_${item.date.time}"
                            is CallLogUiModel.Item -> "row_${item.log.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is CallLogUiModel.Header -> HeaderItem(text = item.date.toDayCategory())
                        is CallLogUiModel.Item -> CallLogHistoryItem(log = item.log)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallLogHistoryItem(log: CallLogData) {
    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(id = log.callTypeIconRes()),
                tint = if (log.callLogType == CallLogType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(text = log.callTypeReadable())
        },
        supportingContent = {
            Text(
                text = log.time.toReadableFormat(),
                color = if (log.callLogType == CallLogType.MISSED) MaterialTheme.colorScheme.error else Color.Unspecified
            )
        },
        trailingContent = {
            if (log.callLogType != CallLogType.MISSED) {
                Text(text = log.callDurationReadable())
            }
        }
    )
}

private fun CallLogData.callTypeIconRes(): Int = when (callLogType) {
    CallLogType.MISSED -> R.drawable.baseline_call_missed_24
    CallLogType.INCOMING -> R.drawable.baseline_call_received_24
    CallLogType.OUTGOING -> R.drawable.baseline_call_made_24
}

private fun CallLogData.callTypeReadable(): String = when (callLogType) {
    CallLogType.MISSED -> "Missed Call"
    CallLogType.INCOMING -> "Incoming Call"
    CallLogType.OUTGOING -> "Outgoing Call"
}
