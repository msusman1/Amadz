package com.talsk.amadz.domain

sealed class CallAction {
    object Answer : CallAction()
    object Hangup : CallAction()
    data class Hold(val enabled: Boolean) : CallAction()
    data class Mute(val enabled: Boolean) : CallAction()
    data class Speaker(val enabled: Boolean) : CallAction()
    data class StartDialTone(val char: Char) : CallAction()
    data object StopDialTone : CallAction()
}