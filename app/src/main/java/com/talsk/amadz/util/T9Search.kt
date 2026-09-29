package com.talsk.amadz.util

private val t9LetterGroups = mapOf(
    '2' to "abc",
    '3' to "def",
    '4' to "ghi",
    '5' to "jkl",
    '6' to "mno",
    '7' to "pqrs",
    '8' to "tuv",
    '9' to "wxyz",
)

fun String.toT9GlobPattern(): String? {
    if (isEmpty()) return null
    val characterClasses = map { digit ->
        val letters = t9LetterGroups[digit] ?: return null
        "[${letters}${letters.uppercase()}]"
    }
    return "*${characterClasses.joinToString("*")}*"
}
