package com.talsk.amadz.domain.entity

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

    data class SimError(
        val simState: Int,
        val message: String
    ) : CallState()

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
            is SimError -> message
            Idle -> ""
        }


    fun isIncomingCall(): Boolean {
        if (this is Ringing && direction == CallDirection.INCOMING) {
            return true
        }
        return false
    }
}

enum class CallDirection {
    INCOMING,
    OUTGOING
}
