package com.talsk.amadz.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.core.DtmfTonePrefs
import com.talsk.amadz.domain.repo.CallLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val dtmfTonePrefs: DtmfTonePrefs
) : ViewModel() {

    val dtmfTonesEnabled = dtmfTonePrefs.isEnabled

    fun setDtmfTonesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dtmfTonePrefs.setEnabled(enabled)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            callLogRepository.deleteAllCallLogs()
        }
    }
}
