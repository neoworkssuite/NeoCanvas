package com.neoworksuite.neocanvas.brushes

internal object InkingBrushes {
    val brushes = listOf(
        authoredBrush("neo.ink", "Studio Ink", "Pressure-tapered black ink for confident finished lines.", "inking", BrushTip.Round, 8f, 1f, .08f, 1f, .2f, BrushDynamics(hardness = .98f), startTaper = .12f, endTaper = .18f),
        authoredBrush("neo.inking.technical", "Technical Ink", "A precise constant-flow nib for panels and linework.", "inking", BrushTip.Round, 4f, 1f, .07f, .25f, .1f, BrushDynamics(hardness = 1f), startTaper = .03f, endTaper = .05f),
        authoredBrush("neo.inking.dry-nib", "Dry Nib", "A narrow direction-led nib with fibrous dry breaks.", "inking", BrushTip.DryPaint, 10f, .9f, .12f, .9f, .7f, BrushDynamics(grain = .55f, shapeRatio = .45f, hardness = .85f), listOf(BuiltInBrushAssetRefs.bristleFlat), BuiltInBrushAssetRefs.grainDryPaint, angleMode = StampAngleMode.DirectionJitter, angleJitter = .08f, scaleX = 1.2f, scaleY = .36f, startTaper = .08f, endTaper = .14f),
        authoredBrush("neo.inking.brush", "Brush Ink", "Flexible brush-pen stroke with a broad expressive belly.", "inking", BrushTip.Bristle, 18f, .95f, .09f, 1f, .45f, BrushDynamics(shapeRatio = .5f, hardness = .9f), listOf(BuiltInBrushAssetRefs.chisel), angleMode = StampAngleMode.Direction, scaleX = 1.15f, scaleY = .48f, startTaper = .18f, endTaper = .28f),
        authoredBrush("neo.inking.comic", "Comic Inker", "Bold clean contour ink with decisive start and end taper.", "inking", BrushTip.Round, 13f, 1f, .075f, 1f, .2f, BrushDynamics(hardness = 1f), startTaper = .22f, endTaper = .32f),
        authoredBrush("neo.flat-marker", "Flat Marker", "Broad felt marker with a clean rectangular face.", "inking", BrushTip.Flat, 24f, .72f, .13f, .8f, .75f, BrushDynamics(shapeRatio = .38f, hardness = 1f)),
    )
}
