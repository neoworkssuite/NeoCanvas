package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.GrainMovement
import kotlin.math.floor

internal class BrushGrainSampler(
    private val asset: BrushAsset,
    private val scale: Float,
    private val movement: GrainMovement,
) {
    init { require(scale.isFinite() && scale > 0f) }

    fun coverage(canvasX: Float, canvasY: Float, stampX: Float, stampY: Float): Float {
        val sourceX: Float
        val sourceY: Float
        when (movement) {
            GrainMovement.Canvas -> {
                sourceX = canvasX / scale
                sourceY = canvasY / scale
            }
            GrainMovement.Stamp -> {
                sourceX = (stampX + 1f) * .5f * asset.width / scale
                sourceY = (stampY + 1f) * .5f * asset.height / scale
            }
        }
        return sampleWrapped(sourceX, sourceY)
    }

    private fun sampleWrapped(x: Float, y: Float): Float {
        fun wrapped(value: Int, size: Int): Int = ((value % size) + size) % size
        val x0Raw = floor(x).toInt()
        val y0Raw = floor(y).toInt()
        val x0 = wrapped(x0Raw, asset.width)
        val y0 = wrapped(y0Raw, asset.height)
        val x1 = wrapped(x0Raw + 1, asset.width)
        val y1 = wrapped(y0Raw + 1, asset.height)
        val fx = x - floor(x)
        val fy = y - floor(y)
        fun value(px: Int, py: Int) = (asset.coverage[py * asset.width + px].toInt() and 255) / 255f
        val top = value(x0, y0) * (1f - fx) + value(x1, y0) * fx
        val bottom = value(x0, y1) * (1f - fx) + value(x1, y1) * fx
        return top * (1f - fy) + bottom * fy
    }
}
