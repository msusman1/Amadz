package com.talsk.amadz.domain.entity

data class BlockedNumber(
    val value: String,
    val type: Type
) {
    enum class Type {
        EXACT,
        REGEX
    }
}
