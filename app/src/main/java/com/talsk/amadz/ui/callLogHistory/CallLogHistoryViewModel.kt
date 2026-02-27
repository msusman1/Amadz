package com.talsk.amadz.ui.callLogHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.home.calllogs.CallLogUiModel
import com.talsk.amadz.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogHistoryViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val blockedNumberRepository: BlockedNumberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallLogHistoryUiState())
    val uiState: StateFlow<CallLogHistoryUiState> = _uiState.asStateFlow()

    fun load(phone: String, cachedName: String) {
        if (_uiState.value.phone == phone && !_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                phone = phone,
                title = cachedName.ifBlank { phone }
            )

            val contact = contactRepository.getContactByPhone(phone)
            val logs = callLogRepository.getCallLogsByPhone(phone)

            val groupedLogs = buildList {
                var lastHeaderDayMillis: Long? = null
                logs.forEach { log ->
                    val day = log.time.startOfDay()
                    if (lastHeaderDayMillis != day.time) {
                        add(CallLogUiModel.Header(day))
                        lastHeaderDayMillis = day.time
                    }
                    add(CallLogUiModel.Item(log))
                }
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = contact?.name ?: cachedName.ifBlank { phone },
                phone = contact?.phone ?: phone,
                logs = groupedLogs,
                isBlocked = blockedNumberRepository.isBlocked(contact?.phone ?: phone),
                isSavedContact = contact != null
            )
        }
    }

    fun deleteHistory() {
        val phone = _uiState.value.phone
        if (phone.isBlank()) return
        viewModelScope.launch {
            callLogRepository.deleteCallLogsByPhone(phone)
            App.needCallLogRefresh = true
            _uiState.value = _uiState.value.copy(logs = emptyList())
        }
    }

    fun toggleBlocked() {
        val currentPhone = _uiState.value.phone
        if (currentPhone.isBlank()) return
        val blocked = blockedNumberRepository.isBlocked(currentPhone)
        if (blocked) {
            blockedNumberRepository.unblock(currentPhone)
        } else {
            blockedNumberRepository.block(currentPhone)
        }
        _uiState.value = _uiState.value.copy(isBlocked = !blocked)
    }
}

data class CallLogHistoryUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val phone: String = "",
    val logs: List<CallLogUiModel> = emptyList(),
    val isBlocked: Boolean = false,
    val isSavedContact: Boolean = false
)
