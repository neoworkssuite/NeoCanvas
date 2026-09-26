package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.brushes.BrushAssetRef
import com.neoworksuite.neocanvas.brushes.Sha256
import com.neoworksuite.neocanvas.renderer.BrushAsset
import com.neoworksuite.neocanvas.renderer.BrushAssetCodec
import com.neoworksuite.neocanvas.renderer.BrushAssetResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CompositeBrushAssetResolverTest {
    @Test
    fun installed_asset_with_exact_hash_precedes_builtin_without_id_collision() {
        val builtInAsset = BrushAsset("shared", 2, 2, byteArrayOf(0, 1, 1, 0))
        val packAsset = BrushAsset("shared", 2, 2, byteArrayOf(0, 2, 2, 0))
        val bytes = BrushAssetCodec.encode(packAsset)
        val ref = BrushAssetRef("shared", Sha256.hex(bytes))
        val resolver = CompositeBrushAssetResolver(
            builtIn = BrushAssetResolver { builtInAsset },
            installedAssets = { listOf(mapOf("shared.neomask" to bytes)) },
        )

        assertEquals(packAsset, assertNotNull(resolver.resolve(ref)))
    }

    @Test
    fun hash_mismatch_is_rejected_instead_of_falling_back_by_id() {
        val bytes = BrushAssetCodec.encode(BrushAsset("shared", 2, 2, byteArrayOf(0, 2, 2, 0)))
        val ref = BrushAssetRef("shared", "f".repeat(64))
        val resolver = CompositeBrushAssetResolver(
            builtIn = BrushAssetResolver { null },
            installedAssets = { listOf(mapOf("shared.neomask" to bytes)) },
        )

        assertFailsWith<IllegalArgumentException> { resolver.resolve(ref) }
    }
}
