package com.talsk.amadz.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> LazyPagedColumn(
    pagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    pagedContent: LazyListScope.(LazyPagingItems<T>) -> Unit
) {

    LazyColumn(
        modifier = modifier
    ) {
        /* ---------- REFRESH STATE ---------- */
        when (pagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                item { FullScreenLoader() }
                return@LazyColumn
            }

            is LoadState.Error -> {
                item {
                    FullScreenError(
                        message = "Failed to load call logs",
                        onRetry = { pagingItems.retry() }
                    )
                }
                return@LazyColumn
            }

            else -> Unit
        }

        /* ---------- PAGED CONTENT WITH STICKY HEADERS ---------- */
        pagedContent(pagingItems)

        /* ---------- APPEND STATE ---------- */
        when (pagingItems.loadState.append) {
            is LoadState.Loading -> {
                item { InlineLoader() }
            }

            is LoadState.Error -> {
                item {
                    InlineError(
                        message = "Failed to load more",
                        onRetry = { pagingItems.retry() })
                }
            }

            else -> Unit
        }
    }
}