package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushMode
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NatureBrushQualityTest {
    @Test
    fun every_nature_brush_is_deterministic_non_empty_and_visually_distinct() {
        val signatures = linkedSetOf<Int>()
        BuiltInBrushes.inCategory("nature").forEach { brush ->
            val first = render(brush.id)
            val second = render(brush.id)
            assertContentEquals(first, second, brush.name)
            assertTrue(first.any { (it.toInt() and 255) != 0 }, brush.name)
            assertTrue(signatures.add(first.contentHashCode()), "${brush.name} duplicates another Nature mark")
        }
        assertEquals(16, signatures.size)
    }

    @Test
    fun detailed_silhouettes_retain_internal_separation() {
        listOf("neo.nature.grass-wild", "neo.nature.grass-meadow", "neo.nature.fern", "neo.nature.pine-needles").forEach { id ->
            val alpha = render(id)
            val occupied = alpha.indices.count { index -> index % 4 == 3 && (alpha[index].toInt() and 255) > 0 }
            val holes = alpha.indices.count { index -> index % 4 == 3 && (alpha[index].toInt() and 255) == 0 }
            assertTrue(occupied > 40, id)
            assertTrue(holes > occupied, "$id must retain separated blades or leaflets")
        }
    }

    private fun render(id: String): ByteArray {
        val brush = requireNotNull(BuiltInBrushes.find(id))
        val store = TileStore()
        store.applyPatch(
            Rasterizer.stroke(
                existing = store,
                layerId = "nature",
                points = listOf(RasterPoint(22f, 72f, .3f), RasterPoint(116f, 35f, .7f), RasterPoint(220f, 70f, 1f)),
                color = RasterColor(30, 90, 45),
                size = brush.baseSize.coerceAtMost(72f),
                opacity = brush.opacity,
                mode = BrushMode.PAINT,
                canvasWidth = 256,
                canvasHeight = 128,
                brush = brush,
                assetResolver = BuiltInBrushAssets.resolver,
            ),
        )
        return store.read(TileKey("nature", 0, 0)) ?: ByteArray(TileFormat.BYTES_PER_TILE)
    }
}
