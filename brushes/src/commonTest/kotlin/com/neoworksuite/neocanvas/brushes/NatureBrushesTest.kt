package com.neoworksuite.neocanvas.brushes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NatureBrushesTest {
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
    fun nature_collection_is_explicit_asset_backed_and_described() {
        val expected = listOf(
            "Broad Leaf", "Fine Leaves", "Oak Cluster", "Tropical Foliage", "Leaf Canopy",
            "Wild Grass", "Meadow Grass", "Fern Frond", "Pine Needles", "Pine Bough",
            "Hedge", "Branch", "Twig", "Bark", "Moss", "Ground Cover",
        )
        val brushes = BuiltInBrushes.inCategory("nature")
        assertEquals(expected, brushes.map { it.name })
        brushes.forEach { brush ->
            assertEquals(3, brush.version, brush.id)
            assertTrue(brush.description.isNotBlank(), brush.id)
            assertTrue(brush.stamp?.resolvedShapes.orEmpty().isNotEmpty(), brush.id)
        }
    }

    @Test
    fun organic_repetition_uses_variants_and_directional_forms_follow_the_stroke() {
        val nature = BuiltInBrushes.inCategory("nature")
        val variantBrushes = nature.filter { it.stamp!!.resolvedShapes.size > 1 }
        assertTrue(variantBrushes.size >= 14)
        listOf("neo.nature.grass-wild", "neo.nature.grass-meadow", "neo.nature.fern", "neo.nature.pine-needles",
            "neo.nature.pine-bough", "neo.nature.branch", "neo.nature.twig", "neo.nature.bark")
            .forEach { id ->
                assertTrue(requireNotNull(BuiltInBrushes.find(id)).stamp?.angleMode in setOf(StampAngleMode.Direction, StampAngleMode.DirectionJitter), id)
            }
    }

    @Test
    fun nature_asset_families_are_all_represented() {
        val ids = BuiltInBrushes.inCategory("nature").flatMap { brush ->
            brush.stamp!!.resolvedShapes.map { it.id } + listOfNotNull(brush.stamp.grain?.id)
        }.toSet()
        listOf("leaf-broad-a", "leaf-fine-a", "leaf-oak-a", "leaf-tropical-a", "grass-wild-a",
            "grass-meadow-a", "fern-a", "pine-needle-a", "pine-bough-a", "hedge-a", "branch-a",
            "twig-a", "bark-fragment-a", "moss-a", "grain-organic", "grain-bark")
            .forEach { assertTrue(it in ids, it) }
    }
}
