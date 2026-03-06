package com.talsk.amadz.domain.repo

interface BlockedNumberRepository {
    fun isBlocked(phone: String): Boolean
    fun getBlockedNumbers(): List<String>
    fun block(phone: String)
    fun unblock(phone: String)
}
