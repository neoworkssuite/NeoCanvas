package com.neoworksuite.neocanvas.brushes

/** Curated original NeoCanvas launch library. */
internal object NeoBrushLibrary {
    val categories: List<BrushCategory> = listOf(
        BrushCategory("essentials", "Essentials"),
        BrushCategory("sketching", "Sketching"),
        BrushCategory("inking", "Inking"),
        BrushCategory("painting", "Painting"),
        BrushCategory("textures", "Textures"),
        BrushCategory("nature", "Nature"),
    )
    val brushes: List<BrushDefinition> =
        EssentialBrushes.brushes + SketchingBrushes.brushes + InkingBrushes.brushes +
            PaintingBrushes.brushes + TextureBrushes.brushes + NatureBrushes.brushes
}

internal fun authoredBrush(
    id: String,
    name: String,
    description: String,
    category: String,
    tip: BrushTip,
    size: Float,
    opacity: Float,
    spacingRatio: Float,
    pressureSize: Float = .7f,
    pressureOpacity: Float = .7f,
    dynamics: BrushDynamics = BrushDynamics(),
    shapes: List<BrushAssetRef> = emptyList(),
    grain: BrushAssetRef? = null,
    angleMode: StampAngleMode = StampAngleMode.Fixed,
    angleDegrees: Float = 0f,
    angleJitter: Float = 0f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    scatterAlong: Float = 0f,
    scatterAcross: Float = 0f,
    stampCount: Int = 1,
    stampCountJitter: Float = 0f,
    grainScale: Float = 1f,
    grainMovement: GrainMovement = GrainMovement.Stamp,
    pressureScatter: Float = 0f,
    pressureStampCount: Float = 0f,
    startTaper: Float = 0f,
    endTaper: Float = 0f,
): BrushDefinition {
    val needsStamp = shapes.isNotEmpty() || grain != null || angleMode != StampAngleMode.Fixed || angleDegrees != 0f ||
        angleJitter != 0f || scaleX != 1f || scaleY != 1f || scatterAlong != 0f || scatterAcross != 0f ||
        stampCount != 1 || stampCountJitter != 0f || grainScale != 1f || grainMovement != GrainMovement.Stamp ||
        pressureScatter != 0f || pressureStampCount != 0f || startTaper != 0f || endTaper != 0f
    return BrushDefinition(
    id = id,
    name = name,
    description = description,
    spacing = size * spacingRatio,
    baseSize = size,
    opacity = opacity,
    version = 3,
    tip = tip,
    pressureSize = pressureSize,
    pressureOpacity = pressureOpacity,
    categoryId = category,
    dynamics = dynamics,
    stamp = if (needsStamp) BrushStamp(
        shape = shapes.firstOrNull(),
        shapeVariants = shapes,
        grain = grain,
        angleMode = angleMode,
        angleDegrees = angleDegrees,
        angleJitter = angleJitter,
        scaleX = scaleX,
        scaleY = scaleY,
        spacingRatio = spacingRatio,
        scatterAlong = scatterAlong,
        scatterAcross = scatterAcross,
        stampCount = stampCount,
        stampCountJitter = stampCountJitter,
        grainScale = grainScale,
        grainMovement = grainMovement,
        pressureScatter = pressureScatter,
        pressureStampCount = pressureStampCount,
        startTaper = startTaper,
        endTaper = endTaper,
    ) else null,
    )
}
