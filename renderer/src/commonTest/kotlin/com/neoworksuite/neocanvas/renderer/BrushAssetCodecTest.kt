package com.neoworksuite.neocanvas.renderer

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BrushAssetCodecTest {
    @Test
    fun neomask_round_trip_is_deterministic() {
        val asset = BrushAsset("test", 4, 3, byteArrayOf(0, 0, 20, 20, 20, 255.toByte(), 255.toByte(), 0, 0, 80, 80, 0))
        val encoded = BrushAssetCodec.encode(asset)
        val decoded = BrushAssetCodec.decode(encoded, "test")

        assertEquals(asset.width, decoded.width)
        assertEquals(asset.height, decoded.height)
        assertContentEquals(asset.coverage, decoded.coverage)
        assertContentEquals(encoded, BrushAssetCodec.encode(decoded))
    }

    @Test
    fun neomask_rejects_malformed_unbounded_and_empty_data() {
        assertFailsWith<IllegalArgumentException> { BrushAssetCodec.decode(byteArrayOf(), "test") }
        assertFailsWith<IllegalArgumentException> { BrushAssetCodec.decode(ByteArray(16) { 0 }, "test") }
        assertFailsWith<IllegalArgumentException> {
            BrushAssetCodec.encode(BrushAsset("empty", 2, 2, ByteArray(4)))
        }
        val trailing = BrushAssetCodec.encode(BrushAsset("full", 2, 2, ByteArray(4) { 1 })) + 1
        assertFailsWith<IllegalArgumentException> { BrushAssetCodec.decode(trailing, "full") }
    }
}
