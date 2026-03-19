package com.talsk.amadz.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.domain.repo.CallLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    fun clearAllCallLogs() {
        viewModelScope.launch {
            callLogRepository.deleteAllCallLogs()
            App.needCallLogRefresh = true
        }
    }
}
