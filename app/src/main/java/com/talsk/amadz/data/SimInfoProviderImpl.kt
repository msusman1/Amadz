package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.talsk.amadz.core.toSimStateReadable
import com.talsk.amadz.domain.entity.CallState
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.domain.entity.SimInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimInfoProviderImpl @Inject constructor(
    @ApplicationContext context: Context
) : SimInfoProvider {
    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)
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
        return runCatching {
            val tempList: MutableList<SimInfo> = mutableListOf()
            val activeInfos = runCatching { subscriptionManager.activeSubscriptionInfoList }.getOrNull().orEmpty()
            telecomManager.callCapablePhoneAccounts?.forEach { handle ->
                val subId = handle.id.toIntOrNull()
                val info = if (subId != null) {
                    subscriptionManager.getActiveSubscriptionInfo(subId)
                } else {
                    activeInfos.firstOrNull { it.iccId == handle.id || it.subscriptionId.toString() == handle.id }
                }
                val slotIndex = info?.simSlotIndex ?: tempList.size
                val name = info?.displayName?.toString()
                tempList.add(
                    SimInfo(
                        accountId = handle.id,
                        simSlotIndex = slotIndex,
                        displayName = name
                    )
                )
            }
            tempList
        }.getOrDefault(emptyList<SimInfo>()).also {
            if (it.isNotEmpty()) {
                cache = it.toMutableList()
            }
        }
    }

   @SuppressLint("MissingPermission")
   override fun checkSimState(): CallState.SimError? {
        val simState = runCatching {
            this.telephonyManager?.simState
        }.getOrNull() ?: TelephonyManager.SIM_STATE_UNKNOWN

        val hasPhoneAccounts = runCatching {
            !telecomManager.callCapablePhoneAccounts.isNullOrEmpty()
        }.getOrDefault(false)

        if (!hasPhoneAccounts && simState != TelephonyManager.SIM_STATE_READY && simState != TelephonyManager.SIM_STATE_UNKNOWN) {
            return CallState.SimError(
                simState = simState,
                message = simState.toSimStateReadable()
            )
        }
        return null
    }
}