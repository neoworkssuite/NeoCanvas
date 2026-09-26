package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.renderer.TILE_SIZE_PIXELS
import com.neoworksuite.neocanvas.renderer.TileKey
import kotlin.math.max
import kotlin.math.min

internal data class TileCompositeBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

internal fun seamSafeTileBounds(
    key: TileKey,
    documentScale: Float,
    documentWidth: Int,
    documentHeight: Int,
): TileCompositeBounds {
    require(documentScale > 0f && documentScale.isFinite()) { "Document scale must be finite and positive." }
    require(documentWidth >= 0 && documentHeight >= 0) { "Document dimensions must not be negative." }

    val bleed = min(0.999f, 1f / documentScale)
    val rawLeft = key.x * TILE_SIZE_PIXELS.toFloat()
    val rawTop = key.y * TILE_SIZE_PIXELS.toFloat()
    val rawRight = rawLeft + TILE_SIZE_PIXELS
    val rawBottom = rawTop + TILE_SIZE_PIXELS

    val left = (rawLeft - if (rawLeft > 0f) bleed else 0f).coerceIn(0f, documentWidth.toFloat())
    val top = (rawTop - if (rawTop > 0f) bleed else 0f).coerceIn(0f, documentHeight.toFloat())
    val right = (rawRight + if (rawRight < documentWidth) bleed else 0f).coerceIn(0f, documentWidth.toFloat())
    val bottom = (rawBottom + if (rawBottom < documentHeight) bleed else 0f).coerceIn(0f, documentHeight.toFloat())

    return TileCompositeBounds(
        left = min(left, right),
        top = min(top, bottom),
        right = max(left, right),
        bottom = max(top, bottom),
    )
}

internal data class RasterTileImage(
    val key: TileKey,
    val image: androidx.compose.ui.graphics.ImageBitmap,
)

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeamlessRasterTiles(
    tiles: List<RasterTileImage>,
    documentWidth: Int,
    documentHeight: Int,
    documentScale: Float,
    alpha: Float,
    blendMode: androidx.compose.ui.graphics.BlendMode,
) {
    if (tiles.isEmpty() || documentWidth <= 0 || documentHeight <= 0 || alpha <= 0f) return

    val documentBounds = androidx.compose.ui.geometry.Rect(
        left = 0f,
        top = 0f,
        right = documentWidth.toFloat(),
        bottom = documentHeight.toFloat(),
    )
    val canvas = drawContext.canvas
    val layerPaint = androidx.compose.ui.graphics.Paint().apply {
        this.alpha = alpha.coerceIn(0f, 1f)
        this.blendMode = blendMode
    }
    val tilePaint = androidx.compose.ui.graphics.Paint().apply {
        this.alpha = 1f
        this.blendMode = androidx.compose.ui.graphics.BlendMode.Src
        this.filterQuality = androidx.compose.ui.graphics.FilterQuality.None
        this.isAntiAlias = false
    }

    canvas.save()
    canvas.clipRect(documentBounds)
    canvas.saveLayer(documentBounds, layerPaint)
    try {
        tiles.forEach { tile ->
            val bounds = seamSafeTileBounds(
                key = tile.key,
                documentScale = documentScale,
                documentWidth = documentWidth,
                documentHeight = documentHeight,
            )
            val width = bounds.right - bounds.left
            val height = bounds.bottom - bounds.top
            if (width <= 0f || height <= 0f) return@forEach

            canvas.save()
            try {
                canvas.translate(bounds.left, bounds.top)
                canvas.scale(width / tile.image.width, height / tile.image.height)
                canvas.drawImage(tile.image, androidx.compose.ui.geometry.Offset.Zero, tilePaint)
            } finally {
                canvas.restore()
            }
        }
    } finally {
        canvas.restore()
        canvas.restore()
    }
}
