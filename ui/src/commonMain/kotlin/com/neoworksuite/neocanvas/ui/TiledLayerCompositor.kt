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
