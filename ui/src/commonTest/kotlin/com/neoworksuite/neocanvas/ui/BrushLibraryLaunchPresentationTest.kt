package com.neoworksuite.neocanvas.ui

import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrushLibraryLaunchPresentationTest {
    @Test
    fun launch_presentation_is_six_curated_collections_with_the_approved_footer() {
        val state = BrushLibraryState()
        assertEquals(listOf("Essentials", "Sketching", "Inking", "Painting", "Textures", "Nature"), state.categories.map { it.name })
        assertEquals(48, state.allBrushes.size)
        assertEquals("48 launch brushes · More original brushes will arrive in future updates.", brushLaunchMessage(48))
    }

    @Test
    fun removed_generated_presets_are_absent_while_legacy_brushes_remain() {
        val state = BrushLibraryState()
        state.showAll()
        assertFalse(state.visibleBrushes.any { it.id == "neo.pencils.precision-pencil" })
        assertFalse(state.visibleBrushes.any { it.id == "neo.foliage.leaf-cluster" })
        listOf("neo.pencil", "neo.ink", "neo.soft-round", "neo.dry-paint", "neo.flat-marker", "neo.eraser")
            .forEach { assertTrue(BuiltInBrushes.find(it) != null, it) }
    }

    @Test
    fun search_uses_authored_descriptions() {
        val state = BrushLibraryState()
        state.query = "canvas-anchored"
        assertEquals(listOf("neo.nature.bark"), state.visibleBrushes.map { it.id })
    }
}
