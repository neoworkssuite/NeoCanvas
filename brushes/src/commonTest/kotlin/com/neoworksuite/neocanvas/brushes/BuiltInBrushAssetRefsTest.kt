package com.neoworksuite.neocanvas.brushes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuiltInBrushAssetRefsTest {
    @Test
    fun built_in_asset_references_are_unique_complete_and_portable() {
        assertTrue(BuiltInBrushAssetRefs.all.size >= 32)
        assertEquals(BuiltInBrushAssetRefs.all.size, BuiltInBrushAssetRefs.all.map { it.id }.distinct().size)
        assertEquals(BuiltInBrushAssetRefs.all.size, BuiltInBrushAssetRefs.all.map { it.sha256 }.distinct().size)
        assertTrue(BuiltInBrushAssetRefs.shapes.isNotEmpty())
        assertTrue(BuiltInBrushAssetRefs.grains.isNotEmpty())
    }
}
