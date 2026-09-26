package com.neoworksuite.neocanvas.brushes

internal object SketchingBrushes {
    val brushes = listOf(
        authoredBrush("neo.pencil", "Graphite Pencil", "Fine graphite with paper tooth and natural pressure.", "sketching", BrushTip.Pencil, 5f, .78f, .11f, 1f, .8f, BrushDynamics(grain = .35f, hardness = .8f), listOf(BuiltInBrushAssetRefs.roundSoft), BuiltInBrushAssetRefs.grainGraphite, grainScale = .7f, grainMovement = GrainMovement.Canvas),
        authoredBrush("neo.sketching.technical-pencil", "Technical Pencil", "A narrow firm lead for construction and detail.", "sketching", BrushTip.Pencil, 3f, .9f, .08f, .65f, .45f, BrushDynamics(grain = .14f, hardness = .95f), listOf(BuiltInBrushAssetRefs.roundSoft), BuiltInBrushAssetRefs.grainPaper, grainScale = .55f, grainMovement = GrainMovement.Canvas),
        authoredBrush("neo.sketching.soft-pencil", "Soft Pencil", "Broad dark graphite for loose studies and shading.", "sketching", BrushTip.Pencil, 11f, .58f, .12f, 1f, 1f, BrushDynamics(grain = .62f, hardness = .52f), listOf(BuiltInBrushAssetRefs.charcoal), BuiltInBrushAssetRefs.grainGraphite, angleMode = StampAngleMode.DirectionJitter, angleJitter = .1f, scaleY = .62f),
        authoredBrush("neo.sketching.vine-charcoal", "Vine Charcoal", "Broken organic charcoal with airy grain and soft edges.", "sketching", BrushTip.Chalk, 24f, .48f, .16f, .8f, .9f, BrushDynamics(grain = .8f, hardness = .35f, jitter = .08f), listOf(BuiltInBrushAssetRefs.charcoal), BuiltInBrushAssetRefs.grainChalk, angleMode = StampAngleMode.DirectionJitter, angleJitter = .24f, scaleX = 1.25f, scaleY = .5f),
        authoredBrush("neo.sketching.chalk", "Studio Chalk", "Dense chalk with a dry edge and visible paper breakup.", "sketching", BrushTip.Chalk, 18f, .64f, .15f, .6f, .85f, BrushDynamics(grain = .7f, hardness = .58f), listOf(BuiltInBrushAssetRefs.bristleFlat), BuiltInBrushAssetRefs.grainChalk, angleMode = StampAngleMode.DirectionJitter, angleJitter = .12f, scaleY = .55f),
        authoredBrush("neo.sketching.wax-crayon", "Wax Crayon", "Waxy rounded colour with resistant paper flecks.", "sketching", BrushTip.DryPaint, 16f, .78f, .13f, .7f, .7f, BrushDynamics(grain = .5f, hardness = .7f), listOf(BuiltInBrushAssetRefs.charcoal), BuiltInBrushAssetRefs.grainPaper, angleMode = StampAngleMode.DirectionJitter, angleJitter = .18f, scaleX = .9f, scaleY = .7f),
    )
}
