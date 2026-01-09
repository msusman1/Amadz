package com.talsk.amadz.ui.onboarding

sealed class CallState {

    object Idle : CallState()

    data class Ringing(
        val direction: CallDirection
    ) : CallState()

    object Connecting : CallState()

    data class Active(
        val duration: Long,
        val isMuted: Boolean,
        val isSpeakerOn: Boolean,
        val isOnHold: Boolean
    ) : CallState()

    object OnHold : CallState()

    object CallDisconnected : CallState()

    fun toReadableStatus(): String =
        when (this) {
            is Ringing -> when (direction) {
                CallDirection.INCOMING -> "Call from"
                CallDirection.OUTGOING -> "Calling"
            }

            is Connecting -> "Connecting"
            is Active -> "In call"
            is OnHold -> "On hold"
            is CallDisconnected -> "Call ended"
            Idle -> ""
        }
}

enum class CallDirection {
    INCOMING,
    OUTGOING
}
