package com.talsk.amadz.domain

interface DtmfToneGenerator {
    fun startTone(digit: Char)
    fun stopTone()
}