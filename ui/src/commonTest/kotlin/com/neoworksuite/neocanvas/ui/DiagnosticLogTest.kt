package com.neoworksuite.neocanvas.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiagnosticLogTest {
    @Test
    fun log_sanitizes_entries_bounds_history_and_persists() {
        var saved = ""
        val log = DiagnosticLog("older\nentry") { saved = it }

        repeat(240) { index -> log.append("event $index\ncontinued") }

        assertFalse(log.text.contains("event 0\n"))
        assertTrue(log.text.contains("event 239 continued"))
        assertEquals(log.text, saved)
        assertTrue(log.text.lineSequence().count() <= DiagnosticLog.MAX_ENTRIES)
    }

    @Test
    fun clearing_log_keeps_a_visible_marker() {
        var saved = ""
        val log = DiagnosticLog("old") { saved = it }

        log.clear()

        assertEquals("Diagnostic log cleared", log.text)
        assertEquals(log.text, saved)
    }
}
