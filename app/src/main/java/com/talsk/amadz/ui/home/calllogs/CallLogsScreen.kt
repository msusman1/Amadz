package com.talsk.amadz.ui.home.calllogs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.home.CallLogItem
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.util.toDayCategory


@Composable
fun CallLogsScreen(
    onCallClick: (String) -> Unit,
    onCallLogClick: (CallLogData) -> Unit,
    onContactDetailClick: (CallLogData) -> Unit,
    vm: CallLogsViewModel = hiltViewModel()
) {
    val callLogs: LazyPagingItems<CallLogUiModel> = vm.callLogs.collectAsLazyPagingItems()

    CallLogsScreenInternal(
        callLogs = callLogs,
        onContactDetailClick = onContactDetailClick,
        onCallClick = onCallClick,
        onCallLogClick = onCallLogClick
    )
}

@Composable
fun CallLogsScreenInternal(
    callLogs: LazyPagingItems<CallLogUiModel>,
    onContactDetailClick: (CallLogData) -> Unit,
    onCallClick: (String) -> Unit,
    onCallLogClick: (CallLogData) -> Unit,
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
                        onCallLogClick = onCallLogClick,
                        onCallClick = { onCallClick(model.log.phone) },
                        onContactDetailClick = onContactDetailClick
                    )
                }

                null -> Unit
            }
        }
    }

}

