package com.neoworksuite.neocanvas.renderer

import com.neoworksuite.neocanvas.brushes.BuiltInBrushes
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrushReviewArtifactTest {
    @Test
    fun render_every_launch_brush_at_default_and_large_sizes() {
        val output = File(if (File("renderer").isDirectory) "renderer/build/brush-review" else "build/brush-review")
        output.mkdirs()
        output.listFiles()?.filter { it.isFile }?.forEach(File::delete)
        val lines = mutableListOf("category\tid\tname\tdescription\tstandard\tlarge")
        BuiltInBrushes.categories.forEach { category ->
            BuiltInBrushes.inCategory(category.id).forEach { brush ->
                val standard = BrushPreviewRenderer.render(brush, BuiltInBrushAssets.resolver, 360, 116, RasterColor(238, 241, 245))
                val largeBrush = brush.copy(baseSize = brush.baseSize * 1.8f, spacing = brush.spacing * 1.8f)
                val large = BrushPreviewRenderer.render(largeBrush, BuiltInBrushAssets.resolver, 360, 180, RasterColor(238, 241, 245))
                val stem = brush.id.replace('.', '-')
                File(output, "$stem-standard.rgba").writeBytes(standard.rgba)
                File(output, "$stem-large.rgba").writeBytes(large.rgba)
                lines += listOf(category.name, brush.id, brush.name, brush.description.replace('\t', ' '),
                    "$stem-standard.rgba", "$stem-large.rgba").joinToString("\t")
            }
        }
        File(output, "manifest.tsv").writeText(lines.joinToString("\n", postfix = "\n"))
        assertEquals(49, lines.size)
        assertTrue(File(output, "manifest.tsv").length() > 1_000)
    }
}
