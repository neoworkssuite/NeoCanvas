package com.neoworksuite.neocanvas.ui

import androidx.compose.ui.unit.dp
import com.neoworksuite.neocanvas.core.model.CanvasDocument
import com.neoworksuite.neocanvas.core.model.DocumentHistory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayerPanelInteractionTest {
    @Test fun layer_panel_uses_the_compact_width_on_ipad_and_narrow_layouts() {
        assertEquals(280, layerPanelMaxWidthDp(compact = false))
        assertEquals(300, layerPanelMaxWidthDp(compact = true))
    }

    @Test fun layer_panel_begins_below_the_top_toolbar() {
        assertEquals(72, layerPanelTopInsetDp())
    }

    @Test fun panel_dimension_is_resolved_once_from_viewport_and_cap() {
        assertEquals(280.dp, cappedPanelDimension(1800.dp, .32f, 280.dp))
        assertEquals(224.dp, cappedPanelDimension(700.dp, .32f, 280.dp))
    }

    @Test fun layer_header_icons_keep_an_accessible_touch_target() {
        assertEquals(44, layerHeaderTouchTargetDp())
    }

    @Test fun dragging_a_displayed_layer_down_reorders_it_and_undo_restores_the_stack() {
        val state = EditorState(DocumentHistory(CanvasDocument.blank(16, 16)))
        state.addLayer() // layer-1, bottom
        state.addLayer() // layer-2
        state.addLayer() // layer-3, top and active

        assertTrue(state.reorderActiveLayerInDisplay(1))

        assertEquals(listOf("layer-1", "layer-3", "layer-2"), state.document.layers.map { it.id })
        assertTrue(state.undo())
        assertEquals(listOf("layer-1", "layer-2", "layer-3"), state.document.layers.map { it.id })
    }

    @Test fun dragging_past_the_display_edge_is_a_no_op() {
        val state = EditorState(DocumentHistory(CanvasDocument.blank(16, 16)))
        state.addLayer()
        state.addLayer()
        val original = state.document

        assertEquals(false, state.reorderActiveLayerInDisplay(-1))
        assertEquals(original, state.document)
    }

    @Test fun only_the_handle_can_reorder_a_layer() {
        assertTrue(allowsLayerReorder(LayerDragRegion.Handle))
        assertEquals(false, allowsLayerReorder(LayerDragRegion.Body))
        assertEquals(false, allowsLayerReorder(LayerDragRegion.Controls))
    }
    @Test fun selected_layer_controls_remain_actions_not_reorder_regions() {
        assertEquals(false, allowsLayerReorder(LayerDragRegion.Controls))
        assertTrue(allowsLayerReorder(LayerDragRegion.Handle))
    }
}
