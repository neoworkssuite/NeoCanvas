package com.neoworksuite.neocanvas.ui

import androidx.compose.ui.graphics.FilterQuality
import kotlin.test.Test
import kotlin.test.assertEquals

class CanvasTileSamplingTest {
    @Test
    fun seamless_compositor_uses_non_interpolating_tile_sampling() {
        assertEquals(FilterQuality.None, seamlessRasterTileFilterQuality())
    }
}
