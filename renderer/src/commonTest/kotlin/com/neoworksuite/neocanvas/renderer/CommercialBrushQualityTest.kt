package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushMode
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommercialBrushQualityTest {
    @Test
    fun every_launch_brush_is_non_empty_repeatable_and_not_an_exact_duplicate() {
        val signatures = linkedMapOf<Int, String>()
        BuiltInBrushes.paintBrushes.forEach { brush ->
            val first = render(brush.id)
            val second = render(brush.id)
            assertContentEquals(first, second, brush.id)
            assertTrue(first.any { (it.toInt() and 255) != 0 }, brush.id)
            val signature = first.contentHashCode()
            assertTrue(signatures.put(signature, brush.id) == null, "${brush.id} duplicates ${signatures[signature]}")
        }
        assertEquals(48, signatures.size)
    }

    private fun render(id: String): ByteArray {
        val brush = requireNotNull(BuiltInBrushes.find(id))
        val store = TileStore()
        store.applyPatch(
            Rasterizer.stroke(
                existing = store,
                layerId = "paint",
                points = listOf(
                    RasterPoint(18f, 56f, .25f),
                    RasterPoint(72f, 24f, .7f),
                    RasterPoint(132f, 62f, 1f),
                    RasterPoint(180f, 38f, .55f),
                ),
                color = RasterColor(42, 86, 134),
                size = brush.baseSize.coerceIn(6f, 48f),
                opacity = brush.opacity,
                mode = BrushMode.PAINT,
                canvasWidth = 200,
                canvasHeight = 96,
                brush = brush,
                assetResolver = BuiltInBrushAssets.resolver,
            ),
        )
        return store.read(TileKey("paint", 0, 0)) ?: ByteArray(TileFormat.BYTES_PER_TILE)
    }
}
