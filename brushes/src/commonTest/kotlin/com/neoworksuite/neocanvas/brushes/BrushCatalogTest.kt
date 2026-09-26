package com.neoworksuite.neocanvas.brushes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BrushCatalogTest {
    @Test
    fun launch_library_has_six_ordered_collections_and_forty_eight_brushes() {
        assertEquals(
            listOf("Essentials", "Sketching", "Inking", "Painting", "Textures", "Nature"),
            BuiltInBrushes.categories.map { it.name },
        )
        assertEquals(listOf(6, 6, 6, 8, 6, 16), BuiltInBrushes.categories.map { BuiltInBrushes.inCategory(it.id).size })
        assertEquals(48, BuiltInBrushes.paintBrushes.size)
    }

    @Test
    fun every_core_brush_is_authored_described_and_asset_references_are_valid() {
        val refs = BuiltInBrushAssetRefs.all.toSet()
        BuiltInBrushes.paintBrushes.forEach { brush ->
            assertEquals(3, brush.version, brush.id)
            assertTrue(brush.description.isNotBlank(), brush.id)
            brush.stamp?.resolvedShapes.orEmpty().forEach { assertTrue(it in refs, "${brush.id}: ${it.id}") }
            brush.stamp?.grain?.let { assertTrue(it in refs, "${brush.id}: ${it.id}") }
        }
    }

    @Test
    fun brush_and_category_ids_are_unique_and_legacy_ids_remain_resolvable() {
        assertEquals(BuiltInBrushes.categories.size, BuiltInBrushes.categories.map { it.id }.toSet().size)
        assertEquals(BuiltInBrushes.brushes.size, BuiltInBrushes.brushes.map { it.id }.toSet().size)
        listOf("neo.pencil", "neo.ink", "neo.soft-round", "neo.dry-paint", "neo.flat-marker", "neo.eraser")
            .forEach { assertTrue(BuiltInBrushes.find(it) != null, it) }
    }

    @Test
    fun search_matches_names_descriptions_and_categories_without_case_sensitivity() {
        assertTrue(BuiltInBrushes.search("GRAPHITE").any { it.id == "neo.pencil" })
        assertEquals(8, BuiltInBrushes.search("painting").size)
        assertEquals(BuiltInBrushes.paintBrushes, BuiltInBrushes.search(""))
    }

    @Test
    fun dynamics_reject_values_outside_supported_ranges() {
        assertFailsWith<IllegalArgumentException> { BrushDynamics(grain = 1.1f) }
        assertFailsWith<IllegalArgumentException> { BrushDynamics(shapeRatio = 0.05f) }
        assertFailsWith<IllegalArgumentException> { BrushDynamics(scatter = -0.1f) }
    }
}
