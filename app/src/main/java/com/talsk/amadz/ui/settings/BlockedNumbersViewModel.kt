package com.talsk.amadz.ui.settings

import androidx.lifecycle.ViewModel
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BlockedNumbersViewModel @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository
) : ViewModel() {

    private val _blockedNumbers = MutableStateFlow(emptyList<String>())
    val blockedNumbers: StateFlow<List<String>> = _blockedNumbers.asStateFlow()

    init {
        refreshBlockedNumbers()
    }

    fun addBlockedNumber(phone: String) {
        blockedNumberRepository.block(phone)
        refreshBlockedNumbers()
    }

    fun removeBlockedNumber(phone: String) {
        blockedNumberRepository.unblock(phone)
        refreshBlockedNumbers()
    }

    private fun refreshBlockedNumbers() {
        _blockedNumbers.value = blockedNumberRepository.getBlockedNumbers()
    }
}

