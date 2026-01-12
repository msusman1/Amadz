package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.SimInfo

interface SimInfoProvider {
    fun getSimsInfo(): List<SimInfo>
}