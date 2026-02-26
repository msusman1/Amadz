package com.talsk.amadz.ui.callLogHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogHistoryViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository
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
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = contact?.name ?: cachedName.ifBlank { phone },
                phone = contact?.phone ?: phone,
                logs = logs
            )
        }
    }

    fun deleteHistory() {
        val phone = _uiState.value.phone
        if (phone.isBlank()) return
        viewModelScope.launch {
            callLogRepository.deleteCallLogsByPhone(phone)
            _uiState.value = _uiState.value.copy(logs = emptyList())
        }
    }
}

data class CallLogHistoryUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val phone: String = "",
    val logs: List<CallLogData> = emptyList()
)
