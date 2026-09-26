package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.brushes.BuiltInBrushAssetRefs
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import com.neoworksuite.neocanvas.renderer.BrushPreview
import com.neoworksuite.neocanvas.renderer.BuiltInBrushAssets
import com.neoworksuite.neocanvas.renderer.RasterColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertNull

class BrushPreviewCacheTest {
    @Test
    fun preview_can_render_off_path_then_enter_the_cache() {
        val cache = BrushPreviewCache(capacity = 4)
        val color = RasterColor(240, 240, 240)
        assertNull(cache.cachedImage(BuiltInBrushes.pencil, 80, 32, color))
        val preview = cache.renderPreview(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 80, 32, color)
        assertEquals(0, cache.renderCount)
        val stored = cache.storeImage(BuiltInBrushes.pencil, 80, 32, color, preview)
        assertSame(stored, cache.cachedImage(BuiltInBrushes.pencil, 80, 32, color))
        assertEquals(1, cache.renderCount)
    }

    @Test
    fun reuses_identical_settings_and_invalidates_changes() {
        val cache = BrushPreviewCache(capacity = 4)
        val first = cache.image(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 120, 40, RasterColor(240, 240, 240))
        val same = cache.image(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 120, 40, RasterColor(240, 240, 240))
        val changed = cache.image(BuiltInBrushes.pencil.copy(opacity = .41f), BuiltInBrushAssets.resolver, 120, 40, RasterColor(240, 240, 240))
        assertSame(first, same)
        assertNotSame(first, changed)
        assertEquals(2, cache.renderCount)
    }

    @Test
    fun referenced_asset_hash_is_part_of_the_key() {
        val cache = BrushPreviewCache(capacity = 4) { _, _, width, height, _ -> BrushPreview(width, height, ByteArray(width * height * 4)) }
        val brush = requireNotNull(BuiltInBrushes.find("neo.nature.leaf-broad"))
        val first = cache.image(brush, BuiltInBrushAssets.resolver, 120, 40, RasterColor(240, 240, 240))
        val changedRef = BuiltInBrushAssetRefs.leafBroadA.copy(sha256 = "0".repeat(64))
        val changedBrush = brush.copy(stamp = brush.stamp!!.copy(shape = changedRef, shapeVariants = listOf(changedRef)))
        val second = cache.image(changedBrush, BuiltInBrushAssets.resolver, 120, 40, RasterColor(240, 240, 240))
        assertNotSame(first, second)
        assertEquals(2, cache.renderCount)
    }

    @Test
    fun least_recently_used_entry_is_evicted_at_capacity() {
        val cache = BrushPreviewCache(capacity = 2)
        val color = RasterColor(240, 240, 240)
        val first = cache.image(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 80, 32, color)
        cache.image(BuiltInBrushes.ink, BuiltInBrushAssets.resolver, 80, 32, color)
        cache.image(BuiltInBrushes.pencil, BuiltInBrushAssets.resolver, 80, 32, color)
        cache.image(BuiltInBrushes.softRound, BuiltInBrushAssets.resolver, 80, 32, color)
        val rerenderedInk = cache.image(BuiltInBrushes.ink, BuiltInBrushAssets.resolver, 80, 32, color)
        assertNotSame(first, rerenderedInk)
        assertEquals(4, cache.renderCount)
        assertEquals(2, cache.size)
    }
}
