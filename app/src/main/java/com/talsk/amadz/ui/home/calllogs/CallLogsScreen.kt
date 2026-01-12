package com.talsk.amadz.ui.home.calllogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.ui.components.FullScreenError
import com.talsk.amadz.ui.components.FullScreenLoader
import com.talsk.amadz.ui.components.InlineError
import com.talsk.amadz.ui.components.InlineLoader
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.home.CallLogItem
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.util.toDayCategory


@Composable
fun CallLogsScreen(
    onCallClick: (String) -> Unit,
    onContactDetailClick: (CallLogData) -> Unit,
    vm: CallLogsViewModel = hiltViewModel()
) {
    val callLogs: LazyPagingItems<CallLogUiModel> = vm.callLogs.collectAsLazyPagingItems()

    CallLogsScreenInternal(
        callLogs = callLogs,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick
    )
}

@Composable
fun CallLogsScreenInternal(
    callLogs: LazyPagingItems<CallLogUiModel>,
    onContactDetailClick: (CallLogData) -> Unit,
    onCallClick: (String) -> Unit,
) {
    LazyPagedColumn(
        modifier = Modifier.fillMaxSize(),
        pagingItems = callLogs
    ) {
        items(callLogs.itemCount, key = { index ->
            when (val item = callLogs[index]) {
                is CallLogUiModel.Header -> "header_${item.date.time}"
                is CallLogUiModel.Item -> "item_${item.log.id}"
                null -> index
            }
        }) { index ->
            when (val model = callLogs.peek(index)) {
                is CallLogUiModel.Header -> {
                    HeaderItem(text = model.date.toDayCategory())
                }

                is CallLogUiModel.Item -> {
                    CallLogItem(
                        logData = model.log,
                        onCallClick = { onCallClick(model.log.phone) },
                        onContactDetailClick = onContactDetailClick
                    )
                }

                null -> Unit
            }
        }
    }

}

