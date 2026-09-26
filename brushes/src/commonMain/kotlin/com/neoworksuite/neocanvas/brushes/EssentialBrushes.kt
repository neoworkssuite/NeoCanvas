package com.neoworksuite.neocanvas.brushes

internal object EssentialBrushes {
    val brushes = listOf(
        authoredBrush("neo.essential.hard-round", "Hard Round", "Opaque round paint with a crisp responsive edge.", "essentials", BrushTip.Round, 18f, 1f, .10f, 1f, .25f, BrushDynamics(hardness = 1f)),
        authoredBrush("neo.soft-round", "Soft Airbrush", "A smooth feathered airbrush for light and shadow.", "essentials", BrushTip.SoftRound, 42f, .22f, .09f, .3f, 1f, BrushDynamics(hardness = .08f), listOf(BuiltInBrushAssetRefs.roundSoft), scaleX = 1.05f, scaleY = 1.05f),
        authoredBrush("neo.essential.monoline", "Monoline", "Even-width clean line for diagrams and lettering.", "essentials", BrushTip.Round, 8f, 1f, .08f, 0f, 0f, BrushDynamics(hardness = 1f), startTaper = .04f, endTaper = .04f),
        authoredBrush("neo.essential.block-fill", "Block Fill", "Solid chisel coverage for fast silhouettes and fills.", "essentials", BrushTip.Flat, 34f, 1f, .18f, .45f, .25f, BrushDynamics(shapeRatio = .62f, hardness = 1f), listOf(BuiltInBrushAssetRefs.chisel), angleMode = StampAngleMode.Direction, scaleY = .72f),
        authoredBrush("neo.essential.pixel", "Pixel Square", "One-pixel-hard square marks with no smoothing.", "essentials", BrushTip.Pixel, 8f, 1f, 1f, 0f, 0f, BrushDynamics(hardness = 1f)),
        authoredBrush("neo.essential.clean-shade", "Clean Shade", "Controlled soft shading that builds without texture.", "essentials", BrushTip.SoftRound, 58f, .12f, .08f, .2f, 1f, BrushDynamics(hardness = .02f), listOf(BuiltInBrushAssetRefs.roundSoft), scaleX = 1.3f, scaleY = .9f),
    )
}
