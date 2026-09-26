package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushAssetRef
import com.neoworksuite.neocanvas.brushes.BrushMode
import com.neoworksuite.neocanvas.brushes.BrushStamp
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import com.neoworksuite.neocanvas.brushes.GrainMovement
import com.neoworksuite.neocanvas.brushes.StampAngleMode
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BrushDynamicsV3Test {
    private val shapeA = BrushAssetRef("shape-a", "a".repeat(64))
    private val shapeB = BrushAssetRef("shape-b", "b".repeat(64))
    private val grainRef = BrushAssetRef("grain", "c".repeat(64))
    private val full = BrushAsset("full", 3, 3, ByteArray(9) { 255.toByte() })
    private val grain = BrushAsset("grain", 2, 2, byteArrayOf(255.toByte(), 64, 64, 255.toByte()))

    @Test
    fun variant_selection_is_deterministic_and_uses_the_declared_set() {
        val refs = listOf(shapeA, shapeB)
        val first = (0 until 12).map { selectShapeVariant(refs, it, 0) }
        val second = (0 until 12).map { selectShapeVariant(refs, it, 0) }

        assertEquals(first, second)
        assertEquals(refs.toSet(), first.toSet())
    }

    @Test
    fun interpolation_tracks_forward_reverse_and_curved_tangents() {
        val forward = plannedRasterStampSamples(listOf(RasterPoint(0f, 0f), RasterPoint(20f, 0f)), 10f, null)
        val reverse = plannedRasterStampSamples(listOf(RasterPoint(20f, 0f), RasterPoint(0f, 0f)), 10f, null)
        val curved = plannedRasterStampSamples(
            listOf(RasterPoint(0f, 0f), RasterPoint(20f, 0f), RasterPoint(20f, 20f)),
            10f,
            null,
        )

        assertEquals(0f, forward.first().tangentRadians, .001f)
        assertEquals(PI.toFloat(), kotlin.math.abs(reverse.first().tangentRadians), .001f)
        assertEquals((PI / 2).toFloat(), curved.last().tangentRadians, .001f)
        assertEquals(0f, curved.first().progress, .001f)
        assertEquals(1f, curved.last().progress, .001f)
    }

    @Test
    fun grain_movement_is_stable_in_stamp_and_canvas_space() {
        val stamp = BrushGrainSampler(grain, 1f, GrainMovement.Stamp)
        val canvas = BrushGrainSampler(grain, 1f, GrainMovement.Canvas)

        assertEquals(stamp.coverage(10f, 20f, .25f, .25f), stamp.coverage(40f, 50f, .25f, .25f))
        assertEquals(canvas.coverage(255.25f, 10f, .1f, .1f), canvas.coverage(255.25f, 10f, .8f, .8f))
        assertEquals(canvas.coverage(255.25f, 10f, .1f, .1f), canvas.coverage(255.25f + 2f, 10f, .1f, .1f))
    }

    @Test
    fun pressure_scatter_and_stamp_count_remain_bounded() {
        val spec = BrushStamp(
            stampCount = 8,
            stampCountJitter = 1f,
            pressureScatter = 1f,
            pressureStampCount = 1f,
        )
        val low = plannedSubStampCount(spec, pressure = .05f, stampIndex = 3)
        val high = plannedSubStampCount(spec, pressure = 1f, stampIndex = 3)

        assertTrue(low in 1..8)
        assertTrue(high in 1..8)
        assertTrue(low <= high)
    }

    @Test
    fun taper_reduces_stroke_ends_and_color_jitter_is_repeatable() {
        val brush = BuiltInBrushes.ink.copy(
            version = 3,
            stamp = BrushStamp(
                shape = shapeA,
                shapeVariants = listOf(shapeA),
                startTaper = .3f,
                endTaper = .3f,
                hueJitter = .2f,
                saturationJitter = .3f,
                brightnessJitter = .3f,
                angleMode = StampAngleMode.Direction,
            ),
        )
        val resolver = BrushAssetResolver { if (it == shapeA) full else null }
        val first = render(brush, resolver)
        val second = render(brush, resolver)

        assertContentEquals(first, second)
        fun alpha(x: Int) = first[(32 * 256 + x) * 4 + 3].toInt() and 255
        assertTrue(alpha(32) > alpha(8))
        assertTrue(alpha(32) > alpha(56))
        val centre = (32 * 256 + 32) * 4
        assertNotEquals(90, first[centre].toInt() and 255)
    }

    @Test
    fun every_referenced_asset_must_resolve_before_rendering() {
        val brush = BuiltInBrushes.ink.copy(
            version = 3,
            stamp = BrushStamp(
                shape = shapeA,
                shapeVariants = listOf(shapeA, shapeB),
                grain = grainRef,
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            render(brush, BrushAssetResolver { if (it == shapeA) full else null })
        }
    }

    private fun render(brush: com.neoworksuite.neocanvas.brushes.BrushDefinition, resolver: BrushAssetResolver): ByteArray {
        val store = TileStore()
        val points = listOf(RasterPoint(8f, 32f), RasterPoint(32f, 32f), RasterPoint(56f, 32f))
        store.applyPatch(
            Rasterizer.stroke(
                existing = store,
                layerId = "paint",
                points = points,
                color = RasterColor(90, 130, 170),
                size = 14f,
                opacity = 1f,
                mode = BrushMode.PAINT,
                canvasWidth = 64,
                canvasHeight = 64,
                brush = brush,
                assetResolver = resolver,
            ),
        )
        return store.read(TileKey("paint", 0, 0)) ?: ByteArray(TileFormat.BYTES_PER_TILE)
    }
}
