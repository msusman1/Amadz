package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.domain.entity.SimInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimInfoProviderImpl @Inject constructor(
    @ApplicationContext context: Context
) : SimInfoProvider {
    private val telecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    private var cache: MutableList<SimInfo>? = null

    @SuppressLint("MissingPermission")
    override fun getSimsInfo(): List<SimInfo> {
        if (cache != null) {
            return cache!!
        }
        val tempList: MutableList<SimInfo> = mutableListOf()
        telecomManager.callCapablePhoneAccounts.forEach { handle ->
            val subId = handle.id.toIntOrNull() ?: return@forEach
            val info = subscriptionManager.getActiveSubscriptionInfo(subId) ?: return@forEach
            tempList.add(
                SimInfo(
                    accountId = handle.id,
                    simSlotIndex = info.simSlotIndex,
                    displayName = info.displayName?.toString()
                )
            )
        }
        return tempList.also {
            cache = it
        }
    }
}