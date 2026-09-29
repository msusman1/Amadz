package com.talsk.amadz.ui.home

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyPadEditingTest {
    @Test
    fun insertsAtCursor() {
        val edited = TextFieldValue("1234", TextRange(2)).insertAtSelection("9")

        assertEquals("12934", edited.text)
        assertEquals(TextRange(3), edited.selection)
    }

    @Test
    fun replacesSelectedText() {
        val edited = TextFieldValue("1234", TextRange(1, 3)).insertAtSelection("9")

        assertEquals("194", edited.text)
        assertEquals(TextRange(2), edited.selection)
    }

    @Test
    fun backspacesAtCursor() {
        val edited = TextFieldValue("1234", TextRange(2)).deleteBeforeCursor()

        assertEquals("134", edited.text)
        assertEquals(TextRange(1), edited.selection)
    }

    @Test
    fun backspacesSelectionAndLeavesTextUnchangedAtStart() {
        val selected = TextFieldValue("1234", TextRange(1, 3)).deleteBeforeCursor()
        val atStart = TextFieldValue("1234", TextRange(0)).deleteBeforeCursor()

        assertEquals("14", selected.text)
        assertEquals(TextRange(1), selected.selection)
        assertEquals("1234", atStart.text)
        assertEquals(TextRange(0), atStart.selection)
    }
}
