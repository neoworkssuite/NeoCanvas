package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BuiltInBrushAssetRefs
import com.neoworksuite.neocanvas.brushes.Sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BuiltInBrushAssetsTest {
    @Test
    fun every_built_in_reference_resolves_with_matching_hash_and_dimensions() {
        BuiltInBrushAssetRefs.all.forEach { ref ->
            val bytes = assertNotNull(BuiltInBrushAssets.encoded(ref))
            assertEquals(ref.sha256, Sha256.hex(bytes), ref.id)
            val asset = assertNotNull(BuiltInBrushAssets.resolver.resolve(ref))
            assertTrue(asset.width in 1..512 && asset.height in 1..512)
            assertTrue(asset.coverage.any { (it.toInt() and 255) != 0 })
        }
    }

    @Test
    fun organic_shapes_have_a_transparent_border() {
        BuiltInBrushAssetRefs.shapes.forEach { ref ->
            val asset = assertNotNull(BuiltInBrushAssets.resolver.resolve(ref))
            val edge = buildList {
                repeat(asset.width) { x -> add(asset.coverage[x]); add(asset.coverage[(asset.height - 1) * asset.width + x]) }
                repeat(asset.height) { y -> add(asset.coverage[y * asset.width]); add(asset.coverage[y * asset.width + asset.width - 1]) }
            }
            assertTrue(edge.all { (it.toInt() and 255) == 0 }, ref.id)
        }
    }
}
