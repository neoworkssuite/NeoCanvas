package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BrushMode
import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class LargeBrushPerformanceTest {
    @Test
    fun large_opaque_round_stroke_rasterizes_within_an_interactive_budget() {
        val hardRound = BuiltInBrushes.paintBrushes.first { it.id == "neo.essential.hard-round" }
        fun measuredRun() = measureTime {
            val store = TileStore()
            store.applyPatch(Rasterizer.stroke(
                existing = store,
                layerId = "paint",
                points = listOf(RasterPoint(100f, 512f), RasterPoint(924f, 512f)),
                color = RasterColor(45, 90, 135),
                size = 512f,
                opacity = 1f,
                mode = BrushMode.PAINT,
                canvasWidth = 1024,
                canvasHeight = 1024,
                brush = hardRound.copy(pressureSize = 0f, pressureOpacity = 0f),
                assetResolver = BuiltInBrushAssets.resolver,
            ))
        }

        measuredRun() // Warm Kotlin/JVM code paths before recording the regression budget.
        val samples = List(3) { measuredRun() }
        val best = samples.minOrNull()!!

        assertTrue(best < 325.milliseconds, "Large round stroke samples were $samples")
    }
}
