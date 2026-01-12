package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.Contact

interface ContactDetailProvider {
    suspend fun getContactByPhone(phone: String): Contact?
}
