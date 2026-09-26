package com.neoworksuite.neocanvas.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes

class ImmediateStrokePreviewTest {
    @Test
    fun live_width_matches_full_pressure_brush_dynamics() {
        assertEquals(100f, immediateStrokeWidth(100f, 1f, 1f), .001f)
        assertEquals(50f, immediateStrokeWidth(100f, .5f, 1f), .001f)
    }

    @Test
    fun disabled_pressure_response_keeps_configured_size() {
        assertEquals(100f, immediateStrokeWidth(100f, .2f, 0f), .001f)
    }

    @Test
    fun live_width_never_disappears() {
        assertEquals(1f, immediateStrokeWidth(.1f, .05f, 1f), .001f)
    }

    @Test
    fun live_alpha_matches_brush_pressure_without_segment_accumulation() {
        assertEquals(.4f, immediateStrokeAlpha(.8f, .5f, 1f), .001f)
        assertEquals(.8f, immediateStrokeAlpha(.8f, .5f, 0f), .001f)
    }

    @Test
    fun graphite_live_trace_accounts_for_grain_before_commit() {
        val coverage = immediateStrokeCoverage(BuiltInBrushes.pencil)

        assertTrue(coverage in .3f..<.7f)
        assertTrue(BuiltInBrushes.pencil.opacity * coverage < BuiltInBrushes.pencil.opacity)
    }
}
