package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.brushes.BrushAssetRef
import com.neoworksuite.neocanvas.brushes.Sha256
import com.neoworksuite.neocanvas.renderer.BrushAsset
import com.neoworksuite.neocanvas.renderer.BrushAssetCodec
import com.neoworksuite.neocanvas.renderer.BrushAssetResolver

internal class CompositeBrushAssetResolver(
    private val builtIn: BrushAssetResolver,
    private val installedAssets: () -> List<Map<String, ByteArray>>,
) : BrushAssetResolver {
    override fun resolve(ref: BrushAssetRef): BrushAsset? {
        var mismatchedPackAsset = false
        installedAssets().forEach { assets ->
            val bytes = assets["${ref.id}.neomask"] ?: return@forEach
            if (Sha256.hex(bytes) != ref.sha256) {
                mismatchedPackAsset = true
                return@forEach
            }
            return BrushAssetCodec.decode(bytes, ref.id)
        }
        builtIn.resolve(ref)?.let { return it }
        require(!mismatchedPackAsset) { "Brush asset '${ref.id}' does not match its declared hash." }
        return null
    }
}
