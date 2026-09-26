package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.renderer.TileKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TiledLayerCompositorGeometryTest {
    @Test
    fun fractional_scale_adds_less_than_one_document_pixel_of_bleed() {
        val bounds = seamSafeTileBounds(
            key = TileKey("paint", 1, 1),
            documentScale = 0.75f,
            documentWidth = 1024,
            documentHeight = 1024,
        )

        assertTrue(bounds.left < 256f)
        assertTrue(bounds.top < 256f)
        assertTrue(bounds.right > 512f)
        assertTrue(bounds.bottom > 512f)
        assertTrue(256f - bounds.left < 1f)
        assertTrue(bounds.right - 512f < 1f)
    }

    @Test
    fun document_edges_are_clipped() {
        val first = seamSafeTileBounds(TileKey("paint", 0, 0), 0.75f, 500, 500)
        val last = seamSafeTileBounds(TileKey("paint", 1, 1), 0.75f, 500, 500)

        assertEquals(0f, first.left)
        assertEquals(0f, first.top)
        assertEquals(500f, last.right)
        assertEquals(500f, last.bottom)
    }

    @Test
    fun negative_tile_coordinates_remain_ordered() {
        val outside = seamSafeTileBounds(TileKey("paint", -1, -1), 1.25f, 512, 512)
        val intersecting = seamSafeTileBounds(TileKey("paint", 0, 0), 1.25f, 512, 512)

        assertTrue(outside.left <= outside.right)
        assertTrue(outside.top <= outside.bottom)
        assertTrue(intersecting.right > intersecting.left)
        assertTrue(intersecting.bottom > intersecting.top)
    }
}
