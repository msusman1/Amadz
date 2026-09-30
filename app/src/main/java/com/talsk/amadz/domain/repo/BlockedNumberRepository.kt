package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockedNumberRepository {
    suspend fun isBlocked(phone: String): Boolean
    fun getBlockedNumbers(): Flow<List<BlockedNumber>>
    suspend fun block(phone: String)
    suspend fun blockPattern(pattern: String)
    suspend fun unblock(phone: String)
    suspend fun unblock(blockedNumber: BlockedNumber)
}
