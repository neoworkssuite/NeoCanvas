package com.neoworksuite.neocanvas.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectPanelPresentationTest {
    @Test fun shape_editor_stays_compact_so_the_selected_object_remains_visible() {
        assertEquals(360, objectPanelWidthDp(compact = false))
        assertEquals(390, objectPanelWidthDp(compact = true))
        assertEquals(520, objectPanelHeightDp(textObject = false, compact = false))
    }

    @Test fun text_editor_can_scroll_in_a_taller_bounded_panel() {
        assertEquals(640, objectPanelHeightDp(textObject = true, compact = false))
        assertEquals(600, objectPanelHeightDp(textObject = true, compact = true))
    }
}
