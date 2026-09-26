package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushDefinition
import com.neoworksuite.neocanvas.brushes.BrushMode

data class BrushPreview(val width: Int, val height: Int, val rgba: ByteArray) {
    init {
        require(width > 0 && height > 0) { "Preview dimensions must be positive." }
        require(rgba.size == width * height * 4) { "Preview pixels must be RGBA8." }
    }
}

/** Renders the same raster brush used by the canvas into a small transparent preview. */
object BrushPreviewRenderer {
    fun render(
        brush: BrushDefinition,
        assetResolver: BrushAssetResolver,
        width: Int,
        height: Int,
        color: RasterColor,
    ): BrushPreview {
        require(width >= 32 && height >= 16) { "Brush previews must be at least 32 by 16 pixels." }
        val store = TileStore()
        val displaySize = brush.baseSize.coerceIn(3f, height * .7f)
        val finalDabSpace = (height * .72f).coerceIn(18f, width * .3f)
        val strokeEnd = (width - finalDabSpace).coerceAtLeast(width * .58f)
        val centerY = height * .58f
        val points = listOf(
            RasterPoint(width * .06f, centerY + height * .08f, .18f),
            RasterPoint(width * .28f, centerY - height * .2f, .42f),
            RasterPoint(width * .52f, centerY - height * .25f, .72f),
            RasterPoint(strokeEnd, centerY, 1f),
        )
        store.applyPatch(
            Rasterizer.stroke(
                existing = store,
                layerId = PREVIEW_LAYER,
                points = points,
                color = color,
                size = displaySize,
                opacity = brush.opacity,
                mode = BrushMode.PAINT,
                canvasWidth = width,
                canvasHeight = height,
                brush = brush.copy(mode = BrushMode.PAINT),
                assetResolver = assetResolver,
            ),
        )

        val untapered = brush.stamp?.let { brush.copy(stamp = it.copy(startTaper = 0f, endTaper = 0f)) } ?: brush
        store.applyPatch(
            Rasterizer.stroke(
                existing = store,
                layerId = PREVIEW_LAYER,
                points = listOf(RasterPoint(width - finalDabSpace * .48f, centerY, .82f)),
                color = color,
                size = displaySize * .72f,
                opacity = brush.opacity,
                mode = BrushMode.PAINT,
                canvasWidth = width,
                canvasHeight = height,
                brush = untapered.copy(mode = BrushMode.PAINT),
                assetResolver = assetResolver,
            ),
        )
        return BrushPreview(width, height, assemble(store, width, height))
    }

    private fun assemble(store: TileStore, width: Int, height: Int): ByteArray {
        val result = ByteArray(width * height * 4)
        for (y in 0 until height) for (x in 0 until width) {
            val key = TileKey(PREVIEW_LAYER, tileCoordinate(x), tileCoordinate(y))
            val tile = store.read(key) ?: continue
            val localX = x - key.x * TILE_SIZE_PIXELS
            val localY = y - key.y * TILE_SIZE_PIXELS
            val source = (localY * TILE_SIZE_PIXELS + localX) * 4
            val target = (y * width + x) * 4
            tile.copyInto(result, target, source, source + 4)
        }
        return result
    }

    private const val PREVIEW_LAYER = "brush-preview"
}
