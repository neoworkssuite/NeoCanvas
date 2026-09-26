package com.neoworksuite.neocanvas.ui

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.neoworksuite.neocanvas.brushes.BrushDefinition
import com.neoworksuite.neocanvas.renderer.BrushAssetResolver
import com.neoworksuite.neocanvas.renderer.BrushPreview
import com.neoworksuite.neocanvas.renderer.BrushPreviewRenderer
import com.neoworksuite.neocanvas.renderer.RasterColor

internal class BrushPreviewCache(
    private val capacity: Int = 96,
    private val renderer: (BrushDefinition, BrushAssetResolver, Int, Int, RasterColor) -> BrushPreview =
        BrushPreviewRenderer::render,
) {
    init { require(capacity > 0) }

    private data class Key(
        val brush: BrushDefinition,
        val width: Int,
        val height: Int,
        val color: RasterColor,
    )

    private val entries = linkedMapOf<Key, ImageBitmap>()
    internal var renderCount: Int = 0
        private set
    internal val size: Int get() = entries.size

    fun cachedImage(
        brush: BrushDefinition,
        width: Int,
        height: Int,
        color: RasterColor,
    ): ImageBitmap? {
        val key = Key(brush, width, height, color)
        return entries.remove(key)?.also { entries[key] = it }
    }

    fun renderPreview(
        brush: BrushDefinition,
        assetResolver: BrushAssetResolver,
        width: Int,
        height: Int,
        color: RasterColor,
    ): BrushPreview = renderer(brush, assetResolver, width, height, color)

    fun storeImage(
        brush: BrushDefinition,
        width: Int,
        height: Int,
        color: RasterColor,
        preview: BrushPreview,
    ): ImageBitmap {
        val key = Key(brush, width, height, color)
        cachedImage(brush, width, height, color)?.let { return it }
        val image = preview.toImageBitmap()
        renderCount++
        entries[key] = image
        while (entries.size > capacity) entries.remove(entries.keys.first())
        return image
    }

    fun image(
        brush: BrushDefinition,
        assetResolver: BrushAssetResolver,
        width: Int,
        height: Int,
        color: RasterColor,
    ): ImageBitmap {
        cachedImage(brush, width, height, color)?.let { return it }
        return storeImage(brush, width, height, color, renderPreview(brush, assetResolver, width, height, color))
    }
}

private fun BrushPreview.toImageBitmap(): ImageBitmap {
    val image = ImageBitmap(width, height)
    val canvas = Canvas(image)
    val paint = Paint().apply { isAntiAlias = false }
    fun packed(x: Int, y: Int): Int {
        val offset = (y * width + x) * 4
        return ((rgba[offset + 3].toInt() and 255) shl 24) or
            ((rgba[offset].toInt() and 255) shl 16) or
            ((rgba[offset + 1].toInt() and 255) shl 8) or
            (rgba[offset + 2].toInt() and 255)
    }
    for (y in 0 until height) {
        var x = 0
        while (x < width) {
            val start = x
            val color = packed(x, y)
            x++
            while (x < width && packed(x, y) == color) x++
            if ((color ushr 24) != 0) {
                paint.color = Color(color)
                canvas.drawRect(start.toFloat(), y.toFloat(), x.toFloat(), (y + 1).toFloat(), paint)
            }
        }
    }
    return image
}
