package com.neoworksuite.neocanvas.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class GalleryStackPersistenceTest {
    @Test
    fun stack_name_is_trimmed_bounded_and_has_a_default() {
        assertEquals("Stack", galleryStackName(null))
        assertEquals("Stack", galleryStackName("   "))
        assertEquals("Landscapes", galleryStackName("  Landscapes  "))
        assertEquals(60, galleryStackName("x".repeat(80)).length)
    }

    @Test
    fun persisted_stack_drops_artwork_that_no_longer_exists() {
        val stack = linkedSetOf("one.neocanvas", "missing.neocanvas", "two.neocanvas")
        assertEquals(
            linkedSetOf("one.neocanvas", "two.neocanvas"),
            reconcileGalleryStack(stack, listOf("two.neocanvas", "one.neocanvas")),
        )
    }

    @Test
    fun rename_updates_stack_membership_without_changing_other_members() {
        assertEquals(
            linkedSetOf("renamed.neocanvas", "two.neocanvas"),
            renameGalleryStackMember(
                linkedSetOf("one.neocanvas", "two.neocanvas"),
                "one.neocanvas",
                "renamed.neocanvas",
            ),
        )
    }

    @Test
    fun rename_of_unstacked_artwork_leaves_stack_unchanged() {
        val original = linkedSetOf("one.neocanvas", "two.neocanvas")
        assertEquals(
            original,
            renameGalleryStackMember(original, "other.neocanvas", "new.neocanvas"),
        )
    }
}
