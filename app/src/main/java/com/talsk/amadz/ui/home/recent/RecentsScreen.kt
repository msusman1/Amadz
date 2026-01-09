package com.talsk.amadz.ui.home.recent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.ui.components.FullScreenError
import com.talsk.amadz.ui.components.FullScreenLoader
import com.talsk.amadz.ui.components.InlineError
import com.talsk.amadz.ui.components.InlineLoader
import com.talsk.amadz.ui.home.CallLogItem
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.util.toDayCategory


@Composable
fun RecentsScreen(
    onCallClick: (String) -> Unit,
    onContactDetailClick: (CallLogData) -> Unit,
    vm: RecentViewModel = hiltViewModel()
) {
    val callLogs: LazyPagingItems<CallLogData> = vm.callLogs.collectAsLazyPagingItems()

    RecentsScreenInternal(
        callLogs = callLogs,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick
    )
}

@Composable
fun RecentsScreenInternal(
    callLogs: LazyPagingItems<CallLogData>,
    onContactDetailClick: (CallLogData) -> Unit,
    onCallClick: (String) -> Unit,
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        /* ---------- REFRESH STATE ---------- */
        when (callLogs.loadState.refresh) {
            is LoadState.Loading -> {
                item { FullScreenLoader() }
                return@LazyColumn
            }

            is LoadState.Error -> {
                item {
                    FullScreenError(
                        message = "Failed to load call logs",
                        onRetry = { callLogs.retry() }
                    )
                }
                return@LazyColumn
            }

            else -> Unit
        }

        /* ---------- PAGED CONTENT WITH STICKY HEADERS ---------- */
        var lastHeader: String? = null

        items(
            count = callLogs.itemCount,
            key = { index -> callLogs[index]?.id ?: index }
        ) { index ->

            val log = callLogs[index] ?: return@items
            val currentHeader = log.time.toDayCategory()

            if (currentHeader != lastHeader) {
                lastHeader = currentHeader

                this@LazyColumn.stickyHeader {
                    HeaderItem(
                        text = currentHeader,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }

            CallLogItem(
                logData = log,
                onCallClick = { onCallClick(log.phone) },
                onContactDetailClick = onContactDetailClick
            )
        }

        /* ---------- APPEND STATE ---------- */
        when (callLogs.loadState.append) {
            is LoadState.Loading -> {
                item { InlineLoader() }
            }

            is LoadState.Error -> {
                item {
                    InlineError(
                        message = "Failed to load more",
                        onRetry = { callLogs.retry() }
                    )
                }
            }

            else -> Unit
        }
    }
}

