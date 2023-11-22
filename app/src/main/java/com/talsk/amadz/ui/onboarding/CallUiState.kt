package com.talsk.amadz.ui.onboarding



sealed class CallUiState {

    object Initial : CallUiState()
    object InComingCall : CallUiState()
    object OutgoingCall : CallUiState()
    object CallDisconnected : CallUiState()
    object InCall : CallUiState()






    fun toStatus(): String {
        return when (this) {
            is InComingCall -> "Call From"
            is OutgoingCall -> "Calling"
            is InCall -> "In Call"
            else -> ""
        }
    }
}