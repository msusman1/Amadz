package com.talsk.amadz.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.util.getStartOfDay


@Composable
fun RecentsScreen(callLogs: List<CallLogData>, loadNextPage: () -> Unit) {
    val context = LocalContext.current
    Column {
        val (older, today) = callLogs.partition { it.time.before(getStartOfDay()) }
        LazyColumn {
            if (today.isNotEmpty()) {
                item { HeaderItem(text = "Today") }
            }
            items(today) { callLog ->
                CallLogItem(
                    logData = callLog, onCallClick = { context.dial(it.phone) })
            }
            if (older.isNotEmpty()) {
                item { HeaderItem(text = "Older") }
            }
            items(older) { callLog ->
                CallLogItem(
                    logData = callLog, onCallClick = { context.dial(it.phone) })
            }
            item { EmptyContactItem() }
            item {
                LaunchedEffect(Unit) {
                    loadNextPage()
                }
            }
        }
    }
}