package com.talsk.amadz.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.domain.entity.BlockedNumber
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedNumbersViewModel @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository
) : ViewModel() {

    val blockedNumbers: StateFlow<List<BlockedNumber>> = blockedNumberRepository.getBlockedNumbers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addBlockedNumber(phone: String) = viewModelScope.launch {
        blockedNumberRepository.block(phone)

    }

    fun addBlockedPattern(pattern: String) = viewModelScope.launch {
        blockedNumberRepository.blockPattern(pattern)
    }

    fun removeBlockedNumber(blockedNumber: BlockedNumber) = viewModelScope.launch {
        blockedNumberRepository.unblock(blockedNumber)
    }
}
