package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrushPreviewRendererTest {
    @Test
    fun preview_is_repeatable_and_preserves_the_final_dab() {
        val brush = requireNotNull(BuiltInBrushes.find("neo.nature.leaf-broad"))
        val first = BrushPreviewRenderer.render(brush, BuiltInBrushAssets.resolver, 180, 56, RasterColor(238, 241, 245))
        val second = BrushPreviewRenderer.render(brush, BuiltInBrushAssets.resolver, 180, 56, RasterColor(238, 241, 245))
        assertContentEquals(first.rgba, second.rgba)
        assertTrue(alphaCount(first, 145, 180) > 0, "final dab should be visible at the right edge")
    }

    @Test
    fun pressure_ramp_changes_width_and_opacity() {
        val preview = BrushPreviewRenderer.render(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 180, 56, RasterColor(238, 241, 245))
        val early = alphaCount(preview, 10, 60)
        val late = alphaCount(preview, 90, 140)
        assertTrue(late > early, "higher pressure should produce a stronger wider mark")
    }

    @Test
    fun nature_previews_use_their_actual_silhouettes() {
        val grass = BrushPreviewRenderer.render(requireNotNull(BuiltInBrushes.find("neo.nature.grass-wild")), BuiltInBrushAssets.resolver, 180, 56, RasterColor(238, 241, 245))
        val branch = BrushPreviewRenderer.render(requireNotNull(BuiltInBrushes.find("neo.nature.branch")), BuiltInBrushAssets.resolver, 180, 56, RasterColor(238, 241, 245))
        assertFalse(grass.rgba.contentEquals(branch.rgba))
        assertTrue(grass.rgba.any { (it.toInt() and 255) != 0 })
        assertTrue(branch.rgba.any { (it.toInt() and 255) != 0 })
    }

    private fun alphaCount(preview: BrushPreview, fromX: Int, untilX: Int): Int {
        var count = 0
        for (y in 0 until preview.height) for (x in fromX until untilX) {
            if ((preview.rgba[(y * preview.width + x) * 4 + 3].toInt() and 255) > 0) count++
        }
        return count
    }
}
