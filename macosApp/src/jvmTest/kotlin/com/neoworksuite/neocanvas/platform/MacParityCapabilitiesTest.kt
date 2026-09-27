package com.neoworksuite.neocanvas.platform

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacParityCapabilitiesTest {
    @Test
    fun professional_exports_expose_shared_macos_capabilities() {
        val actions = MacEditorFileActions()
        assertTrue(actions.supportsPsdImport)
        assertTrue(actions.supportsPsdExport)
        assertTrue(actions.supportsEditableObjectPsdFlattening)
        assertTrue(actions.supportsJpegExport)
        assertTrue(actions.supportsTiffExport)
        assertTrue(actions.supportsPdfExport)
    }

    @Test
    fun external_urls_require_https() {
        val actions = MacEditorFileActions()
        assertFalse(actions.openExternalUrl("http://example.invalid"))
    }

    @Test
    fun app_store_build_disables_the_direct_download_updater() {
        assertFalse(MacEditorFileActions(distribution = "app-store").supportsUpdateChecks)
        assertTrue(MacEditorFileActions(distribution = "direct").supportsUpdateChecks)
    }
}
