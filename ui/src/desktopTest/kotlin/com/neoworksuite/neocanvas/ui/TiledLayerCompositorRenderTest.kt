package com.neoworksuite.neocanvas.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.neoworksuite.neocanvas.renderer.TileKey
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals

class TiledLayerCompositorRenderTest {
    @Test
    fun translucent_tiles_have_no_fractional_zoom_boundary() {
        val target = ImageBitmap(512, 256)
        val tile = solidTile(Color(0x80804020))
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(target), Size(512f, 256f)) {
            withTransform({ scale(0.75f, 0.75f, Offset.Zero) }) {
                drawSeamlessRasterTiles(
                    tiles = listOf(
                        RasterTileImage(TileKey("paint", 0, 0), tile),
                        RasterTileImage(TileKey("paint", 1, 0), tile),
                    ),
                    documentWidth = 512,
                    documentHeight = 256,
                    documentScale = 0.75f,
                    alpha = 0.8f,
                    blendMode = BlendMode.SrcOver,
                )
            }
        }

        val pixels = target.toPixelMap()
        assertColorNear(pixels[191, 96], pixels[192, 96])
        assertColorNear(pixels[190, 96], pixels[193, 96])
    }

    @Test
    fun rotated_tiles_have_no_boundary() {
        val target = ImageBitmap(620, 620)
        val tile = solidTile(Color(0x996040B0))
        val degrees = 7f
        val radians = Math.toRadians(degrees.toDouble())
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(target), Size(620f, 620f)) {
            withTransform({
                translate(54f, 54f)
                rotate(degrees, Offset(256f, 256f))
            }) {
                drawSeamlessRasterTiles(
                    tiles = listOf(
                        RasterTileImage(TileKey("paint", 0, 0), tile),
                        RasterTileImage(TileKey("paint", 1, 0), tile),
                    ),
                    documentWidth = 512,
                    documentHeight = 256,
                    documentScale = 1f,
                    alpha = 0.7f,
                    blendMode = BlendMode.SrcOver,
                )
            }
        }

        fun transformed(x: Double, y: Double): Pair<Int, Int> {
            val dx = x - 256.0
            val dy = y - 256.0
            val px = 54.0 + 256.0 + dx * cos(radians) - dy * sin(radians)
            val py = 54.0 + 256.0 + dx * sin(radians) + dy * cos(radians)
            return px.roundToInt() to py.roundToInt()
        }
        val left = transformed(255.0, 128.0)
        val right = transformed(257.0, 128.0)
        val pixels = target.toPixelMap()
        assertColorNear(pixels[left.first, left.second], pixels[right.first, right.second])
    }

    @Test
    fun missing_neighbor_remains_transparent() {
        val target = ImageBitmap(512, 256)
        val tile = solidTile(Color.Red)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(target), Size(512f, 256f)) {
            withTransform({ scale(0.75f, 0.75f, Offset.Zero) }) {
                drawSeamlessRasterTiles(
                    tiles = listOf(RasterTileImage(TileKey("paint", 0, 0), tile)),
                    documentWidth = 512,
                    documentHeight = 256,
                    documentScale = 0.75f,
                    alpha = 1f,
                    blendMode = BlendMode.SrcOver,
                )
            }
        }

        val pixels = target.toPixelMap()
        assertEquals(0f, pixels[195, 96].alpha, 0.001f)
    }

    private fun solidTile(color: Color): ImageBitmap = ImageBitmap(256, 256).also { image ->
        Canvas(image).drawRect(0f, 0f, 256f, 256f, Paint().apply { this.color = color })
    }

    private fun assertColorNear(expected: Color, actual: Color) {
        val tolerance = 1f / 255f
        assertEquals(expected.red, actual.red, tolerance)
        assertEquals(expected.green, actual.green, tolerance)
        assertEquals(expected.blue, actual.blue, tolerance)
        assertEquals(expected.alpha, actual.alpha, tolerance)
    }
}
