package com.neoworksuite.neocanvas.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GalleryPresentationTest {
    @Test
    fun gallery_has_no_kids_destination_or_placeholder_copy() {
        assertFalse("Kids" in galleryPrimaryActionLabels())
        assertEquals("Create a canvas. Everything stays local.", galleryEmptyStateMessage())
    }

    @Test
    fun testflight_preview_distinguishes_available_features_from_roadmap_candidates() {
        val preview = galleryReleasePreviewContent()
        assertEquals("TestFlight Preview", preview.title)
        assertTrue(preview.available.any { "48" in it && "brush" in it.lowercase() })
        assertTrue(preview.candidates.any { "Procreate" in it && "investigation" in it.lowercase() })
        assertTrue("not a promise" in preview.disclaimer.lowercase())
        assertFalse(preview.candidates.any { "will include" in it.lowercase() })
    }
}
