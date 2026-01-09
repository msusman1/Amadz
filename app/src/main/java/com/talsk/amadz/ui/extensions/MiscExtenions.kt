package com.talsk.amadz.ui.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

context (vm: ViewModel)
inline fun <reified T> Flow<T>.stateInScoped(initialValue: T): StateFlow<T> {
    return stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)
}