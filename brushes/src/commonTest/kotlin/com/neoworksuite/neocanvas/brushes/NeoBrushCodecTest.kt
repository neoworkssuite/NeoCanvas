package com.neoworksuite.neocanvas.brushes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NeoBrushCodecTest {
    @Test
    fun round_trip_preserves_every_brush_field_and_unicode_name() {
        val brush = BrushDefinition(
            id = "user.123",
            name = "Étoile 水彩",
            spacing = 3.25f,
            baseSize = 27f,
            opacity = .62f,
            mode = BrushMode.PAINT,
            version = 3,
            tip = BrushTip.Water,
            pressureSize = .72f,
            pressureOpacity = .41f,
            categoryId = "custom",
            dynamics = BrushDynamics(.2f, .3f, .4f, .5f, .6f, .7f, .8f),
        )

        assertEquals(brush, NeoBrushCodec.decode(NeoBrushCodec.encode(brush)))
    }

    @Test
    fun malformed_unknown_or_unsafe_files_are_rejected() {
        assertFailsWith<IllegalArgumentException> { NeoBrushCodec.decode("not a brush".encodeToByteArray()) }
        assertFailsWith<IllegalArgumentException> {
            NeoBrushCodec.decode("NEOCANVAS_BRUSH=99\nid=x".encodeToByteArray())
        }
        val encoded = NeoBrushCodec.encode(BuiltInBrushes.ink).decodeToString()
        assertFailsWith<IllegalArgumentException> {
            NeoBrushCodec.decode((encoded + "unknown=value\n").encodeToByteArray())
        }
    }

    @Test
    fun v2_round_trip_preserves_stamp_while_v1_stays_stamp_free() {
        val stamp = BrushStamp(
            shape = BrushAssetRef("oak-leaf", "0".repeat(64)),
            grain = BrushAssetRef("paper-grain", "1".repeat(64)),
            angleMode = StampAngleMode.DirectionJitter,
            angleDegrees = 12f,
            angleJitter = .35f,
            scaleX = 1.25f,
            scaleY = .7f,
            spacingRatio = .24f,
            scatterAlong = .2f,
            scatterAcross = .55f,
            stampCount = 4,
            stampCountJitter = .25f,
            grainScale = 1.4f,
            grainMovement = GrainMovement.Canvas,
            hueJitter = .03f,
            saturationJitter = .08f,
            brightnessJitter = .06f,
            pressureScatter = .5f,
            pressureStampCount = .7f,
            startTaper = .2f,
            endTaper = .15f,
        )
        val brush = BuiltInBrushes.ink.copy(version = 2, description = "", stamp = stamp)

        assertEquals(brush, NeoBrushCodec.decode(NeoBrushCodec.encode(brush)))
        assertNull(NeoBrushCodec.decode(NeoBrushCodec.encode(BuiltInBrushes.ink)).stamp)
    }
    @Test
    fun v3_round_trip_preserves_description_shape_variants_and_grain() {
        val first = BrushAssetRef("leaf-a", "a".repeat(64))
        val second = BrushAssetRef("leaf-b", "b".repeat(64))
        val stamp = BrushStamp(
            shape = first,
            shapeVariants = listOf(first, second),
            grain = BrushAssetRef("paper-grain", "c".repeat(64)),
            angleMode = StampAngleMode.DirectionJitter,
        )
        val brush = BuiltInBrushes.ink.copy(
            version = 3,
            description = "Directional leaves with a dry paper grain.",
            stamp = stamp,
        )

        val encoded = NeoBrushCodec.encode(brush)
        assertTrue(encoded.decodeToString().startsWith("NEOCANVAS_BRUSH=3\n"))
        assertEquals(brush, NeoBrushCodec.decode(encoded))
    }

    @Test
    fun v1_and_v2_decode_with_v3_defaults() {
        val v1 = NeoBrushCodec.decode(NeoBrushCodec.encode(BuiltInBrushes.ink.copy(version = 1, description = "")))
        val shape = BrushAssetRef("legacy-shape", "d".repeat(64))
        val v2Brush = BuiltInBrushes.ink.copy(version = 2, description = "", stamp = BrushStamp(shape = shape))
        val v2 = NeoBrushCodec.decode(NeoBrushCodec.encode(v2Brush))

        assertEquals("", v1.description)
        assertEquals(emptyList(), v1.stamp?.resolvedShapes ?: emptyList())
        assertEquals("", v2.description)
        assertEquals(listOf(shape), v2.stamp?.resolvedShapes)
    }

    @Test
    fun malformed_v3_shape_variants_are_rejected() {
        val shape = BrushAssetRef("leaf-a", "a".repeat(64))
        val brush = BuiltInBrushes.ink.copy(
            version = 3,
            description = "Leaf",
            stamp = BrushStamp(shape = shape, shapeVariants = listOf(shape)),
        )
        val encoded = NeoBrushCodec.encode(brush).decodeToString()

        assertFailsWith<IllegalArgumentException> {
            NeoBrushCodec.decode(encoded.replace(
                "stamp.shapeVariants=leaf-a:${"a".repeat(64)}",
                "stamp.shapeVariants=leaf-a:",
            ).encodeToByteArray())
        }
        assertFailsWith<IllegalArgumentException> {
            NeoBrushCodec.decode(encoded.replace(
                "stamp.shapeVariants=leaf-a:${"a".repeat(64)}",
                "stamp.shapeVariants=",
            ).encodeToByteArray())
        }
    }

}
