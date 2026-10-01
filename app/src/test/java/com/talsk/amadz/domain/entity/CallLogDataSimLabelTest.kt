package com.talsk.amadz.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class CallLogDataSimLabelTest {
    @Test
    fun usesSubscriptionDisplayNameWhenAvailable() {
        assertEquals("Carrier", callLog(simSlot = 1, simDisplayName = "Carrier").simLabel())
    }

    @Test
    fun fallsBackToSimSlotWhenDisplayNameIsBlank() {
        assertEquals("SIM 2", callLog(simSlot = 1, simDisplayName = " ").simLabel())
    }

    @Test
    fun omitsLabelWhenSimSlotIsUnknown() {
        assertNull(callLog(simSlot = -1, simDisplayName = "Carrier").simLabel())
    }

    private fun callLog(simSlot: Int, simDisplayName: String?) = CallLogData(
        id = 1,
        contactId = null,
        name = "Contact",
        phone = "123",
        image = null,
        callLogType = CallLogType.OUTGOING,
        time = Date(0),
        callDuration = 0,
        simSlot = simSlot,
        simDisplayName = simDisplayName
    )
}
