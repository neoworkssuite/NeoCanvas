package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushMode
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IncrementalRasterStrokeTest {
    @Test
    fun graphite_preview_finishes_with_the_same_pixels_as_one_shot_rasterization() {
        val points = (0..80).map { index ->
            RasterPoint(30f + index * 5f, 180f + (index % 7 - 3) * 1.5f, .35f + index / 160f)
        }
        val expected = TileStore().also { store ->
            store.applyPatch(
                Rasterizer.stroke(
                    existing = store,
                    layerId = "paint",
                    points = points,
                    color = RasterColor(42, 55, 68),
                    size = 72f,
                    opacity = .78f,
                    mode = BrushMode.PAINT,
                    canvasWidth = 512,
                    canvasHeight = 384,
                    brush = BuiltInBrushes.pencil,
                    assetResolver = BuiltInBrushAssets.resolver,
                ),
            )
        }
        val actual = TileStore()
        val session = IncrementalRasterStroke(
            existing = actual,
            layerId = "paint",
            color = RasterColor(42, 55, 68),
            size = 72f,
            opacity = .78f,
            mode = BrushMode.PAINT,
            canvasWidth = 512,
            canvasHeight = 384,
            brush = BuiltInBrushes.pencil,
            assetResolver = BuiltInBrushAssets.resolver,
        )

        var live: IncrementalRasterStrokePreview? = null
        points.indices.drop(1).chunked(8).forEach { indices ->
            live = session.update(points.take(indices.last() + 1))
        }
        val liveStore = TileStore().also { it.applyPatch(assertNotNull(live).patch) }
        assertEquals(expected.keys, liveStore.keys)
        expected.keys.forEach { key ->
            assertContentEquals(expected.read(key), liveStore.read(key), "Live tile $key")
        }
        actual.applyPatch(session.finish(points).patch)

        assertEquals(expected.keys, actual.keys)
        expected.keys.forEach { key ->
            assertContentEquals(expected.read(key), actual.read(key), "Tile $key")
        }
    }

    @Test
    fun extending_a_live_stroke_only_rasterizes_newly_confirmed_stamps() {
        val points = (0..120).map { RasterPoint(20f + it * 4f, 120f, 1f) }
        val session = IncrementalRasterStroke(
            existing = TileStore(),
            layerId = "paint",
            color = RasterColor(10, 20, 30),
            size = 96f,
            opacity = 1f,
            mode = BrushMode.PAINT,
            canvasWidth = 640,
            canvasHeight = 256,
            brush = BuiltInBrushes.paintBrushes.first { it.id == "neo.essential.hard-round" },
            assetResolver = BuiltInBrushAssets.resolver,
        )

        val first = session.update(points.take(91))
        val extended = session.update(points)

        assertTrue(first.newStampCount > 1)
        assertTrue(extended.newStampCount in 1 until first.processedStampCount)
        assertNotNull(extended.confirmedPoint)
        assertEquals(points.last(), session.finish(points).confirmedPoint)
    }

    @Test
    fun tapered_ink_live_preview_rebuilds_to_match_its_current_full_length() {
        val points = (0..60).map { RasterPoint(20f + it * 5f, 90f + (it % 3), .8f) }
        val expected = TileStore().also { store ->
            store.applyPatch(Rasterizer.stroke(
                store, "ink", points, RasterColor(5, 10, 15), 28f, 1f, BrushMode.PAINT,
                384, 192, brush = BuiltInBrushes.ink, assetResolver = BuiltInBrushAssets.resolver,
            ))
        }
        val session = IncrementalRasterStroke(
            TileStore(), "ink", RasterColor(5, 10, 15), 28f, 1f, BrushMode.PAINT,
            384, 192, brush = BuiltInBrushes.ink, assetResolver = BuiltInBrushAssets.resolver,
        )

        session.update(points.take(25))
        val live = TileStore().also { it.applyPatch(session.update(points).patch) }

        assertEquals(expected.keys, live.keys)
        expected.keys.forEach { key -> assertContentEquals(expected.read(key), live.read(key), "Tapered tile $key") }
    }
}
