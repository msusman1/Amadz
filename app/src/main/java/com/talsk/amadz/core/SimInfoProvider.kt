package com.talsk.amadz.core

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimInfoProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val telecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    private var cache: Map<String, Pair<Int, String?>>? = null

    @SuppressLint("MissingPermission")
    fun getSimInfo(): Map<String, Pair<Int, String?>> {
        return cache ?: buildMap {
            telecomManager.callCapablePhoneAccounts.forEach { handle ->
                val subId = handle.id.toIntOrNull() ?: return@forEach
                val info = subscriptionManager.getActiveSubscriptionInfo(subId) ?: return@forEach
                put(handle.id, info.simSlotIndex to info.displayName?.toString())
            }
        }.also { cache = it }
    }
}
