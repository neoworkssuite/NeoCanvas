package com.neoworksuite.neocanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import com.neoworksuite.neocanvas.renderer.DrawingSymmetry
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StudioTopBar(state: EditorState, compact: Boolean, modifier: Modifier = Modifier, onGallery: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).background(NeoCanvasColors.chrome).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!compact) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                BrandMark()
                Text(
                    "NEOCANVAS",
                    color = NeoCanvasColors.paper,
                    fontSize = 12.sp,
                    letterSpacing = 1.35.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }

        StudioButton(Glyph.Gallery, "Gallery") { onGallery() }
        StudioActionsMenu(state)
        StudioButton(
            Glyph.Fx,
            "FX and adjustments",
            state.inspectorVisible && state.inspectorPanel == InspectorPanel.Effects,
        ) { state.toggleInspector(InspectorPanel.Effects) }
        StudioButton(Glyph.Select, "Selection tools", state.tool == Tool.Select) {
            state.activateTool(Tool.Select)
        }
        StudioButton(
            Glyph.Transform,
            "Transform artwork",
            state.transformSession != null || state.tool == Tool.MoveSelection,
        ) { state.activateTransformTool() }
        StudioButton(Glyph.Settings, "Settings") { state.openSettings() }

        Spacer(Modifier.weight(1f))

        StudioButton(
            Glyph.Brush,
            "Brush library",
            state.tool == Tool.Brush &&
                state.inspectorVisible &&
                state.inspectorPanel == InspectorPanel.Brushes,
        ) {
            state.toggleBrushLibrary()
        }
        StudioButton(Glyph.Smudge, "Smudge", state.tool == Tool.Smudge) {
            state.activateTool(Tool.Smudge)
        }
        StudioButton(Glyph.Eraser, "Eraser", state.tool == Tool.Eraser) {
            state.activateTool(Tool.Eraser)
        }
        StudioColourButton(state)
        StudioButton(
            Glyph.Layers,
            "Layers",
            state.inspectorVisible && state.inspectorPanel == InspectorPanel.Layers,
        ) { state.toggleInspector(InspectorPanel.Layers) }
    }
}

@Composable
private fun StudioActionsMenu(state: EditorState) {
    var expanded by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(StudioActionSection.Add) }

    fun closeMenu() {
        expanded = false
    }

    Box {
        StudioTooltip("Actions") {
            Box(
                Modifier.size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (expanded) NeoCanvasColors.accent else NeoCanvasColors.panelRaised)
                    .clickable {
                        if (expanded) closeMenu() else expanded = true
                    }
                    .semantics { contentDescription = "Actions menu" },
                contentAlignment = Alignment.Center,
            ) {
                StudioGlyph(Glyph.Wrench, if (expanded) NeoCanvasColors.ink else NeoCanvasColors.muted)
                if (state.hasUnsavedChanges) {
                    Box(
                        Modifier.size(7.dp).clip(CircleShape)
                            .background(if (expanded) NeoCanvasColors.ink else NeoCanvasColors.accent)
                            .align(Alignment.TopEnd).padding(7.dp),
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { closeMenu() },
            containerColor = NeoCanvasColors.panelRaised,
            modifier = Modifier.width(360.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                studioActionTabs().forEach { tab ->
                    ActionSectionTab(tab, selected = section == tab.section) { section = tab.section }
                }
            }
            HorizontalDivider(color = NeoCanvasColors.line)
            when (section) {
                StudioActionSection.Add -> {
                    ActionItem("Add Text", Glyph.Text) { closeMenu(); state.addTextObject() }
                    ActionItem("Add Rectangle", Glyph.Shape) { closeMenu(); state.addShapeObject(com.neoworksuite.neocanvas.core.model.ShapeKind.Rectangle) }
                    ActionItem("Add Ellipse", Glyph.Shape) { closeMenu(); state.addShapeObject(com.neoworksuite.neocanvas.core.model.ShapeKind.Ellipse) }
                    ActionItem("Add Line", Glyph.Stroke) { closeMenu(); state.addShapeObject(com.neoworksuite.neocanvas.core.model.ShapeKind.Line) }
                    ActionItem("Import Image…", Glyph.ImportImage) { closeMenu(); state.importImage() }
                    if (state.supportsPsdImport) {
                        ActionItem("Import Photoshop PSD…", Glyph.Import) { closeMenu(); state.importPsd() }
                    }
                }
                StudioActionSection.Canvas -> {
                    ActionItem("New Canvas…", Glyph.New) { closeMenu(); state.newCanvasDialogVisible = true }
                    ActionItem("Fit Canvas", Glyph.Fit) { closeMenu(); state.resetView() }
                    HorizontalDivider(color = NeoCanvasColors.line)
                    ActionItem("Liquify…", Glyph.Fx) { closeMenu(); state.activateLiquifyTool() }
                    ActionItem("Fill", Glyph.Fill) { closeMenu(); state.activateTool(Tool.Fill) }
                    ActionItem("Colour Picker", Glyph.Eyedropper) { closeMenu(); state.activateTool(Tool.Eyedropper) }
                    ActionItem("Pan / Move Canvas", Glyph.Transform) { closeMenu(); state.activateTool(Tool.Pan) }
                }
                StudioActionSection.Guides -> {
                    ActionItem("Grid Guide", Glyph.Grid, selected = state.gridGuideVisible) {
                        closeMenu()
                        state.gridGuideVisible = !state.gridGuideVisible
                        state.persistPreferences()
                    }
                    ActionItem("Perspective Guide", Glyph.Assist, selected = state.perspectiveGuideVisible) {
                        closeMenu()
                        state.perspectiveGuideVisible = !state.perspectiveGuideVisible
                        state.persistPreferences()
                    }
                    ActionItem("Symmetry Off", Glyph.Assist, selected = state.symmetry == DrawingSymmetry.None) { closeMenu(); state.symmetry = DrawingSymmetry.None }
                    ActionItem("Vertical Symmetry", Glyph.Assist, selected = state.symmetry == DrawingSymmetry.Vertical) { closeMenu(); state.symmetry = DrawingSymmetry.Vertical }
                    ActionItem("Horizontal Symmetry", Glyph.Assist, selected = state.symmetry == DrawingSymmetry.Horizontal) { closeMenu(); state.symmetry = DrawingSymmetry.Horizontal }
                    ActionItem("Four-way Symmetry", Glyph.Assist, selected = state.symmetry == DrawingSymmetry.Both) { closeMenu(); state.symmetry = DrawingSymmetry.Both }
                }
                StudioActionSection.Studio -> {
                    if (state.supportsVersions) {
                        ActionItem("Versions…", Glyph.Versions) { closeMenu(); state.openVersions() }
                    }
                    if (state.supportsWorkbench) {
                        ActionItem("Workbench…", Glyph.Workbench) { closeMenu(); state.openWorkbench() }
                    }
                    if (state.recentEditableStrokes.isNotEmpty()) {
                        ActionItem("Recent Strokes…", Glyph.Stroke) { closeMenu(); state.openRecentStrokes() }
                    }
                    if (!state.supportsVersions && !state.supportsWorkbench && state.recentEditableStrokes.isEmpty()) {
                        ActionItem("No studio tools available") { closeMenu() }
                    }
                }
                StudioActionSection.Export -> {
                    ActionItem("Open…", Glyph.Open) { closeMenu(); state.open() }
                    ActionItem("Save", Glyph.Save) { closeMenu(); state.save() }
                    if (state.supportsSaveAs) {
                        ActionItem("Save As…", Glyph.Save) { closeMenu(); state.saveAs() }
                    }
                    ActionItem("Export PNG…", Glyph.Export) { closeMenu(); state.exportPng() }
                    if (state.supportsJpegExport) {
                        ActionItem("Export JPEG…", Glyph.Export) { closeMenu(); state.exportJpeg() }
                    }
                    if (state.supportsPdfExport) {
                        ActionItem("Export PDF…", Glyph.Export) { closeMenu(); state.exportPdf() }
                    }
                    if (state.supportsTiffExport) {
                        ActionItem("Export TIFF…", Glyph.Export) { closeMenu(); state.exportTiff() }
                    }
                    if (state.supportsPsdExport) {
                        ActionItem("PSD Compatibility…", Glyph.Layers) { closeMenu(); state.openPsdCompatibility() }
                        ActionItem("Export Photoshop PSD…", Glyph.Export) { closeMenu(); state.exportPsd() }
                    }
                }
                StudioActionSection.Preferences -> {
                    ActionItem("Settings…", Glyph.Settings) { closeMenu(); state.openSettings() }
                    ActionItem("Fit Canvas", Glyph.Fit) { closeMenu(); state.resetView() }
                }
            }
        }
    }
}

@Composable
private fun ActionSectionTab(tab: StudioActionTab, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) NeoCanvasColors.ink else NeoCanvasColors.muted
    Column(
        Modifier.width(54.dp).height(52.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) NeoCanvasColors.accent else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { contentDescription = tab.label; this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StudioGlyph(tab.glyph, tint, Modifier.size(22.dp))
        Text(tab.label, color = tint, fontSize = 7.sp, maxLines = 1)
    }
}

@Composable
private fun ActionItem(
    label: String,
    glyph: Glyph? = null,
    selected: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val tint = when {
        destructive -> Color(0xffff5b5b)
        selected -> NeoCanvasColors.accent
        else -> NeoCanvasColors.muted
    }
    DropdownMenuItem(
        text = { Text(label, color = if (destructive) tint else NeoCanvasColors.paper, fontSize = 14.sp) },
        leadingIcon = glyph?.let { icon -> { StudioGlyph(icon, tint, Modifier.size(21.dp)) } },
        trailingIcon = when {
            selected -> ({ Text("✓", color = NeoCanvasColors.accent) })
            else -> null
        },
        modifier = Modifier.semantics { this.selected = selected },
        onClick = onClick,
    )
}

@Composable
private fun StudioColourButton(state: EditorState) {
    val active = state.inspectorVisible && state.inspectorPanel == InspectorPanel.Colors
    StudioTooltip("Colour " + colorHex(state.color)) {
        Box(
            modifier = Modifier.size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (active) NeoCanvasColors.panelRaised else Color.Transparent)
                .clickable { state.toggleInspector(InspectorPanel.Colors) }
                .semantics { contentDescription = colourButtonDescription(state.color) },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(36.dp)) {
                drawCircle(state.color, radius = size.minDimension * .36f)
                drawCircle(if (active) NeoCanvasColors.accent else NeoCanvasColors.paper,
                    radius = size.minDimension * .38f, style = Stroke(if (active) 2.dp.toPx() else 1.dp.toPx()))
                drawCircle(state.secondaryColor, radius = size.minDimension * .13f,
                    center = Offset(size.width * .76f, size.height * .76f))
                drawCircle(NeoCanvasColors.paper, radius = size.minDimension * .14f,
                    center = Offset(size.width * .76f, size.height * .76f), style = Stroke(1.dp.toPx()))
            }
        }
    }
}

internal fun colourButtonDescription(colour: Color): String =
    "Colour ${colorHex(colour)}. Open Colour Studio"

@Composable
private fun StudioMenu(label: String, actions: List<Pair<String, () -> Unit>>, active: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(Modifier.height(52.dp).clip(RoundedCornerShape(10.dp))
            .background(if (active || expanded) NeoCanvasColors.accent else NeoCanvasColors.panelRaised)
            .clickable { expanded = !expanded }
            .semantics { contentDescription = "$label menu" }
            .padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text("$label ▾", color = if (active || expanded) NeoCanvasColors.ink else NeoCanvasColors.paper, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            containerColor = NeoCanvasColors.panelRaised) {
            actions.forEach { (title, action) ->
                DropdownMenuItem(text = { Text(title, color = NeoCanvasColors.paper, fontSize = 14.sp) }, onClick = {
                    expanded = false
                    action()
                })
            }
        }
    }
}

@Composable
private fun SymmetryMenu(state: EditorState) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(DrawingSymmetry.None to "Symmetry off", DrawingSymmetry.Vertical to "Vertical mirror",
        DrawingSymmetry.Horizontal to "Horizontal mirror", DrawingSymmetry.Both to "Four-way mirror")
    val active = state.symmetry != DrawingSymmetry.None
    Box {
        StudioTooltip("Symmetry: ${options.first { it.first == state.symmetry }.second}") {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(11.dp))
                .background(if (active) NeoCanvasColors.accent else NeoCanvasColors.panelRaised)
                .clickable { expanded = !expanded }
                .semantics { contentDescription = "Choose symmetry mode"; selected = active }, contentAlignment = Alignment.Center) {
                SymmetryIcon(state.symmetry, if (active) NeoCanvasColors.ink else NeoCanvasColors.muted)
                Text("▾", color = if (active) NeoCanvasColors.ink else NeoCanvasColors.muted,
                    fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))
            }
        }
        DropdownMenu(expanded, { expanded = false }, containerColor = NeoCanvasColors.panelRaised) {
            options.forEach { (mode, label) ->
                val chosen = state.symmetry == mode
                DropdownMenuItem(
                    text = { Text(label, color = if (chosen) NeoCanvasColors.accent else NeoCanvasColors.paper) },
                    leadingIcon = { SymmetryIcon(mode, if (chosen) NeoCanvasColors.accent else NeoCanvasColors.muted) },
                    trailingIcon = { if (chosen) Text("✓", color = NeoCanvasColors.accent) },
                    modifier = Modifier.semantics { selected = chosen },
                    onClick = { state.symmetry = mode; expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SymmetryIcon(mode: DrawingSymmetry, tint: Color) = Canvas(Modifier.size(24.dp)) {
    val w = size.width
    val h = size.height
    val stroke = 1.5.dp.toPx()
    drawRect(tint.copy(alpha = .6f), Offset(w * .12f, h * .12f), Size(w * .76f, h * .76f), style = Stroke(stroke))
    if (mode == DrawingSymmetry.None) {
        drawLine(tint, Offset(w * .12f, h * .88f), Offset(w * .88f, h * .12f), stroke, StrokeCap.Round)
    } else {
        if (mode == DrawingSymmetry.Vertical || mode == DrawingSymmetry.Both)
            drawLine(tint, Offset(w / 2, 0f), Offset(w / 2, h), stroke)
        if (mode == DrawingSymmetry.Horizontal || mode == DrawingSymmetry.Both)
            drawLine(tint, Offset(0f, h / 2), Offset(w, h / 2), stroke)
        val centres = when (mode) {
            DrawingSymmetry.Vertical -> listOf(.3f to .5f, .7f to .5f)
            DrawingSymmetry.Horizontal -> listOf(.5f to .3f, .5f to .7f)
            else -> listOf(.3f to .3f, .7f to .3f, .3f to .7f, .7f to .7f)
        }
        centres.forEach { (x, y) -> drawCircle(tint, w * .07f, Offset(w * x, h * y)) }
    }
}

@Composable
fun StudioRail(state: EditorState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(NeoCanvasColors.rail).padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state.tool) {
            Tool.Fill -> VerticalRailControl("TOL", state.fillTolerance.toFloat(), 0f..255f, { it.toInt().toString() }) {
                state.fillTolerance = it.toInt()
            }
            Tool.Liquify -> VerticalRailControl("SIZE", state.liquifySize, 8f..320f, { it.toInt().toString() }) {
                state.liquifySize = it
            }
            else -> VerticalRailControl("SIZE", state.brushSize, 1f..192f, { it.toInt().toString() }) {
                state.brushSize = it
            }
        }

        when (state.tool) {
            Tool.Smudge -> VerticalRailControl("POWER", state.smudgeStrength, 0.01f..1f, { ((it * 100).toInt()).toString() }) {
                state.smudgeStrength = it
            }
            Tool.Liquify -> VerticalRailControl("POWER", state.liquifyStrength, 0.01f..1f, { ((it * 100).toInt()).toString() }) {
                state.liquifyStrength = it
            }
            else -> VerticalRailControl("OPACITY", state.brushOpacity, 0.05f..1f, { ((it * 100).toInt()).toString() }) {
                state.brushOpacity = it
            }
        }

        Spacer(Modifier.weight(1f))
        StudioColourPickerButton(state)
        StudioButton(Glyph.Undo, "Undo", enabled = state.canUndo) { state.undo() }
        StudioButton(Glyph.Redo, "Redo", enabled = state.canRedo) { state.redo() }
    }
}

@Composable
private fun StudioColourPickerButton(state: EditorState) {
    val selected = state.tool == Tool.Eyedropper
    val tint = if (selected) NeoCanvasColors.ink else NeoCanvasColors.muted
    StudioTooltip("Colour Picker") {
        Box(
            Modifier.size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) NeoCanvasColors.accent else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { state.activateTool(Tool.Eyedropper) }
                .semantics {
                    contentDescription = "Colour Picker"
                    this.selected = selected
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(30.dp)) {
                val w = size.width
                val h = size.height
                val stroke = 2.2f
                val barrel = Path().apply {
                    moveTo(w * .24f, h * .70f)
                    lineTo(w * .57f, h * .37f)
                    lineTo(w * .68f, h * .48f)
                    lineTo(w * .35f, h * .81f)
                    close()
                }
                drawPath(barrel, tint, style = Stroke(stroke, cap = StrokeCap.Round))
                drawLine(tint, Offset(w * .54f, h * .32f), Offset(w * .73f, h * .51f), stroke, StrokeCap.Round)
                drawRoundRect(
                    tint,
                    Offset(w * .62f, h * .16f),
                    Size(w * .22f, h * .24f),
                    androidx.compose.ui.geometry.CornerRadius(w * .08f, w * .08f),
                    style = Stroke(stroke),
                )
                drawLine(tint, Offset(w * .24f, h * .70f), Offset(w * .18f, h * .78f), stroke, StrokeCap.Round)
                drawCircle(state.color, w * .085f, Offset(w * .17f, h * .88f))
                drawCircle(tint, w * .085f, Offset(w * .17f, h * .88f), style = Stroke(1.3f))
            }
        }
    }
}

@Composable
private fun VerticalRailControl(label: String, value: Float, range: ClosedFloatingPointRange<Float>, format: (Float) -> String, onChange: (Float) -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(NeoCanvasColors.panelRaised)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = NeoCanvasColors.faint, fontSize = 7.sp, letterSpacing = .55.sp, maxLines = 1, softWrap = false)
        Box(
            Modifier.padding(top = 4.dp, bottom = 2.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(NeoCanvasColors.chrome)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
            Text(format(value), color = NeoCanvasColors.paper, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        val update: (Float, Float) -> Unit = { y, height -> onChange(verticalValueFromY(y, height, range)) }
        Canvas(
            Modifier.width(44.dp).height(126.dp).padding(top = 4.dp)
                .semantics { contentDescription = "$label ${format(value)} vertical slider" }
                .pointerInput(range) { detectTapGestures { update(it.y, size.height.toFloat()) } }
                .pointerInput(range) {
                    detectDragGestures(
                        onDragStart = { update(it.y, size.height.toFloat()) },
                        onDrag = { change, _ -> update(change.position.y, size.height.toFloat()); change.consume() },
                    )
                },
        ) {
            val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
            val x = size.width / 2f
            val top = 11.dp.toPx()
            val bottom = size.height - 11.dp.toPx()
            val thumbY = bottom - (bottom - top) * fraction
            drawLine(NeoCanvasColors.track, Offset(x, top), Offset(x, bottom), 8.dp.toPx(), StrokeCap.Round)
            drawLine(NeoCanvasColors.accent, Offset(x, thumbY), Offset(x, bottom), 8.dp.toPx(), StrokeCap.Round)
            drawCircle(Color.Black.copy(alpha = .55f), 13.dp.toPx(), Offset(x, thumbY))
            drawCircle(NeoCanvasColors.paper, 10.5.dp.toPx(), Offset(x, thumbY))
            drawCircle(NeoCanvasColors.accent, 3.dp.toPx(), Offset(x, thumbY))
        }
    }
}

@Composable
fun studioSliderColors() = SliderDefaults.colors(
    thumbColor = NeoCanvasColors.paper,
    activeTrackColor = NeoCanvasColors.accent,
    inactiveTrackColor = NeoCanvasColors.track,
)

@Composable
fun studioTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = NeoCanvasColors.muted,
    errorTextColor = Color.White,
    focusedBorderColor = NeoCanvasColors.accent,
    unfocusedBorderColor = NeoCanvasColors.muted,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = NeoCanvasColors.paper,
    cursorColor = Color.White,
    errorCursorColor = Color.White,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudioTooltip(label: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = { PlainTooltip(containerColor = NeoCanvasColors.panelRaised, contentColor = NeoCanvasColors.paper) { Text(label) } },
        state = rememberTooltipState(),
        content = content,
    )
}

@Composable
private fun StudioButton(glyph: Glyph, label: String, selected: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val tint = when {
        !enabled -> NeoCanvasColors.disabled
        selected -> NeoCanvasColors.ink
        else -> NeoCanvasColors.muted
    }
    val surface = if (selected) NeoCanvasColors.accent else Color.Transparent
    StudioTooltip(label) {
    Box(
        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(surface)
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .semantics { contentDescription = label; this.selected = selected },
        contentAlignment = Alignment.Center,
    ) { StudioGlyph(glyph, tint) }
    }
}

@Composable
private fun DividerTick() = Box(Modifier.width(1.dp).height(24.dp).background(NeoCanvasColors.line).padding(horizontal = 3.dp))

@Composable
private fun BrandMark() = androidx.compose.foundation.Image(
    painter = neoCanvasIcon(), contentDescription = "NeoCanvas logo", modifier = Modifier.size(40.dp),
)

@Composable
internal fun StudioGlyph(glyph: Glyph, color: Color, modifier: Modifier = Modifier.size(28.dp)) = Canvas(modifier) {
    val w = size.width
    val h = size.height
    fun line(a: Offset, b: Offset, width: Float = 1.8f) = drawLine(color, a, b, width, StrokeCap.Round)
    when (glyph) {
        Glyph.Previous -> {
            line(Offset(w * .65f, h * .2f), Offset(w * .35f, h * .5f))
            line(Offset(w * .35f, h * .5f), Offset(w * .65f, h * .8f))
        }
        Glyph.Next -> {
            line(Offset(w * .35f, h * .2f), Offset(w * .65f, h * .5f))
            line(Offset(w * .65f, h * .5f), Offset(w * .35f, h * .8f))
        }
        Glyph.Gallery -> {
            val s = 2.0f
            line(Offset(w * .18f, h * .46f), Offset(w * .50f, h * .20f), s)
            line(Offset(w * .50f, h * .20f), Offset(w * .82f, h * .46f), s)
            line(Offset(w * .26f, h * .42f), Offset(w * .26f, h * .80f), s)
            line(Offset(w * .74f, h * .42f), Offset(w * .74f, h * .80f), s)
            line(Offset(w * .26f, h * .80f), Offset(w * .74f, h * .80f), s)
            drawRoundRect(
                color,
                Offset(w * .43f, h * .56f),
                Size(w * .14f, h * .24f),
                androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(1.8f),
            )
        }
        Glyph.ImportImage -> {
            val s = 1.9f
            drawRoundRect(
                color,
                Offset(w * .14f, h * .20f),
                Size(w * .58f, h * .56f),
                androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(s),
            )
            drawCircle(color, w * .055f, Offset(w * .31f, h * .36f))
            val mountain = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * .20f, h * .68f)
                lineTo(w * .38f, h * .49f)
                lineTo(w * .50f, h * .60f)
                lineTo(w * .60f, h * .48f)
                lineTo(w * .70f, h * .68f)
            }
            drawPath(mountain, color, style = Stroke(s, cap = StrokeCap.Round))
            line(Offset(w * .72f, h * .30f), Offset(w * .90f, h * .30f), 2.2f)
            line(Offset(w * .81f, h * .21f), Offset(w * .81f, h * .39f), 2.2f)
        }
        Glyph.Select -> {
            val s = 2.0f
            line(Offset(w * .16f, h * .34f), Offset(w * .16f, h * .16f), s)
            line(Offset(w * .16f, h * .16f), Offset(w * .34f, h * .16f), s)
            line(Offset(w * .66f, h * .16f), Offset(w * .84f, h * .16f), s)
            line(Offset(w * .84f, h * .16f), Offset(w * .84f, h * .34f), s)
            line(Offset(w * .84f, h * .66f), Offset(w * .84f, h * .84f), s)
            line(Offset(w * .84f, h * .84f), Offset(w * .66f, h * .84f), s)
            line(Offset(w * .34f, h * .84f), Offset(w * .16f, h * .84f), s)
            line(Offset(w * .16f, h * .84f), Offset(w * .16f, h * .66f), s)
        }
        Glyph.ClearSelection -> {
            line(Offset(w * .2f, h * .2f), Offset(w * .8f, h * .8f))
            line(Offset(w * .8f, h * .2f), Offset(w * .2f, h * .8f))
        }
        Glyph.Fill -> {
            val s = 2.2f
            line(Offset(w * .28f, h * .30f), Offset(w * .58f, h * .22f), s)
            line(Offset(w * .58f, h * .22f), Offset(w * .76f, h * .50f), s)
            line(Offset(w * .76f, h * .50f), Offset(w * .46f, h * .66f), s)
            line(Offset(w * .46f, h * .66f), Offset(w * .24f, h * .42f), s)
            line(Offset(w * .24f, h * .42f), Offset(w * .28f, h * .30f), s)
            line(Offset(w * .30f, h * .46f), Offset(w * .70f, h * .46f), 1.7f)
            drawCircle(color, w * .085f, Offset(w * .79f, h * .76f))
        }
        Glyph.Eyedropper -> {
            val s = 2.1f
            val barrel = Path().apply {
                moveTo(w * .22f, h * .68f)
                lineTo(w * .56f, h * .34f)
                lineTo(w * .68f, h * .46f)
                lineTo(w * .34f, h * .80f)
                close()
            }
            drawPath(barrel, color, style = Stroke(s, cap = StrokeCap.Round))
            drawRoundRect(
                color,
                Offset(w * .61f, h * .14f),
                Size(w * .23f, h * .25f),
                androidx.compose.ui.geometry.CornerRadius(w * .08f, w * .08f),
                style = Stroke(s),
            )
            line(Offset(w * .53f, h * .29f), Offset(w * .73f, h * .49f), s)
            line(Offset(w * .22f, h * .68f), Offset(w * .16f, h * .78f), s)
            drawCircle(color, w * .07f, Offset(w * .15f, h * .88f), style = Stroke(1.5f))
        }
        Glyph.Wrench -> {
            val s = 2.2f
            val head = Path().apply {
                moveTo(w * .66f, h * .18f)
                cubicTo(w * .82f, h * .15f, w * .88f, h * .30f, w * .82f, h * .43f)
                lineTo(w * .70f, h * .34f)
                lineTo(w * .58f, h * .46f)
                lineTo(w * .67f, h * .58f)
                cubicTo(w * .54f, h * .64f, w * .39f, h * .58f, w * .42f, h * .42f)
            }
            drawPath(head, color, style = Stroke(s, cap = StrokeCap.Round))
            line(Offset(w * .47f, h * .53f), Offset(w * .22f, h * .78f), 4.0f)
            drawCircle(color, w * .045f, Offset(w * .21f, h * .79f), style = Stroke(1.5f))
        }
        Glyph.Workbench -> {
            drawRoundRect(color, Offset(w * .15f, h * .24f), Size(w * .70f, h * .48f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(2f))
            line(Offset(w * .25f, h * .72f), Offset(w * .20f, h * .86f), 2f)
            line(Offset(w * .75f, h * .72f), Offset(w * .80f, h * .86f), 2f)
            drawRect(color.copy(alpha = .55f), Offset(w * .28f, h * .34f), Size(w * .18f, h * .20f), style = Stroke(1.4f))
            line(Offset(w * .56f, h * .38f), Offset(w * .73f, h * .38f), 1.5f)
            line(Offset(w * .56f, h * .49f), Offset(w * .69f, h * .49f), 1.5f)
        }
        Glyph.Versions -> {
            drawCircle(color, w * .28f, Offset(w * .52f, h * .52f), style = Stroke(2f))
            line(Offset(w * .52f, h * .52f), Offset(w * .52f, h * .33f), 2f)
            line(Offset(w * .52f, h * .52f), Offset(w * .68f, h * .60f), 2f)
            line(Offset(w * .28f, h * .25f), Offset(w * .18f, h * .25f), 1.8f)
            line(Offset(w * .18f, h * .25f), Offset(w * .18f, h * .72f), 1.8f)
            line(Offset(w * .18f, h * .72f), Offset(w * .29f, h * .72f), 1.8f)
        }
        Glyph.Stroke -> {
            val path = Path().apply {
                moveTo(w * .15f, h * .69f)
                cubicTo(w * .32f, h * .20f, w * .52f, h * .86f, w * .84f, h * .35f)
            }
            drawPath(path, color, style = Stroke(3.2f, cap = StrokeCap.Round))
        }
        Glyph.Text -> {
            line(Offset(w * .20f, h * .22f), Offset(w * .80f, h * .22f), 2.2f)
            line(Offset(w * .50f, h * .22f), Offset(w * .50f, h * .82f), 2.2f)
            line(Offset(w * .34f, h * .82f), Offset(w * .66f, h * .82f), 2.2f)
        }
        Glyph.Shape -> {
            drawRoundRect(color, Offset(w * .14f, h * .18f), Size(w * .43f, h * .43f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.9f))
            drawCircle(color, w * .22f, Offset(w * .64f, h * .63f), style = Stroke(1.9f))
        }
        Glyph.Group -> {
            drawRoundRect(color.copy(alpha = .65f), Offset(w * .28f, h * .18f), Size(w * .53f, h * .48f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.8f))
            drawRoundRect(color, Offset(w * .16f, h * .34f), Size(w * .53f, h * .48f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(2f))
        }
        Glyph.New -> { line(Offset(w * .50f, h * .16f), Offset(w * .50f, h * .84f)); line(Offset(w * .16f, h * .50f), Offset(w * .84f, h * .50f)) }
        Glyph.Open -> { drawRect(color, Offset(w * .18f, h * .34f), Size(w * .64f, h * .42f), style = Stroke(1.8f)); line(Offset(w * .20f, h * .34f), Offset(w * .42f, h * .18f)); line(Offset(w * .42f, h * .18f), Offset(w * .62f, h * .34f)) }
        Glyph.Save -> { drawRoundRect(color, Offset(w * .20f, h * .16f), Size(w * .60f, h * .68f), androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.8f)); line(Offset(w * .35f, h * .20f), Offset(w * .35f, h * .44f)); line(Offset(w * .35f, h * .44f), Offset(w * .65f, h * .44f)); drawRect(color, Offset(w * .34f, h * .58f), Size(w * .32f, h * .18f), style = Stroke(1.5f)) }
        Glyph.Export -> { drawRect(color, Offset(w * .20f, h * .57f), Size(w * .60f, h * .22f), style = Stroke(1.8f)); line(Offset(w * .50f, h * .16f), Offset(w * .50f, h * .61f)); line(Offset(w * .50f, h * .16f), Offset(w * .34f, h * .32f)); line(Offset(w * .50f, h * .16f), Offset(w * .66f, h * .32f)) }
        Glyph.Brush -> {
            line(Offset(w * .30f, h * .72f), Offset(w * .76f, h * .26f), 3.2f)
            line(Offset(w * .23f, h * .80f), Offset(w * .39f, h * .64f), 5.0f)
            line(Offset(w * .20f, h * .84f), Offset(w * .34f, h * .80f), 2.2f)
        }
        Glyph.Eraser -> {
            line(Offset(w * .23f, h * .64f), Offset(w * .52f, h * .28f), 3.0f)
            line(Offset(w * .52f, h * .28f), Offset(w * .79f, h * .50f), 3.0f)
            line(Offset(w * .79f, h * .50f), Offset(w * .51f, h * .80f), 3.0f)
            line(Offset(w * .51f, h * .80f), Offset(w * .23f, h * .64f), 3.0f)
            line(Offset(w * .37f, h * .47f), Offset(w * .64f, h * .69f), 2.0f)
        }
        Glyph.Smudge -> {
            val finger = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * .31f, h * .82f)
                cubicTo(w * .18f, h * .67f, w * .22f, h * .53f, w * .34f, h * .56f)
                lineTo(w * .42f, h * .61f)
                lineTo(w * .42f, h * .28f)
                cubicTo(w * .42f, h * .14f, w * .60f, h * .14f, w * .60f, h * .28f)
                lineTo(w * .60f, h * .49f)
                cubicTo(w * .78f, h * .45f, w * .83f, h * .60f, w * .74f, h * .76f)
                cubicTo(w * .67f, h * .87f, w * .43f, h * .89f, w * .31f, h * .82f)
                close()
            }
            drawPath(finger, color, style = Stroke(2.2f, cap = StrokeCap.Round))
            line(Offset(w * .18f, h * .31f), Offset(w * .31f, h * .25f), 1.5f)
            line(Offset(w * .17f, h * .43f), Offset(w * .30f, h * .43f), 1.5f)
        }
        Glyph.Transform -> {
            val s = 2.1f
            line(Offset(w * .18f, h * .50f), Offset(w * .82f, h * .50f), s)
            line(Offset(w * .18f, h * .50f), Offset(w * .30f, h * .39f), s)
            line(Offset(w * .18f, h * .50f), Offset(w * .30f, h * .61f), s)
            line(Offset(w * .82f, h * .50f), Offset(w * .70f, h * .39f), s)
            line(Offset(w * .82f, h * .50f), Offset(w * .70f, h * .61f), s)
            line(Offset(w * .50f, h * .18f), Offset(w * .50f, h * .82f), s)
            line(Offset(w * .50f, h * .18f), Offset(w * .39f, h * .30f), s)
            line(Offset(w * .50f, h * .18f), Offset(w * .61f, h * .30f), s)
            line(Offset(w * .50f, h * .82f), Offset(w * .39f, h * .70f), s)
            line(Offset(w * .50f, h * .82f), Offset(w * .61f, h * .70f), s)
        }
        Glyph.Undo -> {
            val path = Path().apply { moveTo(w * .78f, h * .72f); cubicTo(w * .82f, h * .38f, w * .55f, h * .28f, w * .30f, h * .42f) }
            drawPath(path, color, style = Stroke(2.4f, cap = StrokeCap.Round))
            line(Offset(w * .30f, h * .42f), Offset(w * .47f, h * .22f), 2.4f)
            line(Offset(w * .30f, h * .42f), Offset(w * .50f, h * .54f), 2.4f)
        }
        Glyph.Redo -> {
            val path = Path().apply { moveTo(w * .22f, h * .72f); cubicTo(w * .18f, h * .38f, w * .45f, h * .28f, w * .70f, h * .42f) }
            drawPath(path, color, style = Stroke(2.4f, cap = StrokeCap.Round))
            line(Offset(w * .70f, h * .42f), Offset(w * .53f, h * .22f), 2.4f)
            line(Offset(w * .70f, h * .42f), Offset(w * .50f, h * .54f), 2.4f)
        }
        Glyph.Fit -> {
            val s = 2.0f
            line(Offset(w * .14f, h * .36f), Offset(w * .14f, h * .14f), s)
            line(Offset(w * .14f, h * .14f), Offset(w * .36f, h * .14f), s)
            line(Offset(w * .64f, h * .14f), Offset(w * .86f, h * .14f), s)
            line(Offset(w * .86f, h * .14f), Offset(w * .86f, h * .36f), s)
            line(Offset(w * .86f, h * .64f), Offset(w * .86f, h * .86f), s)
            line(Offset(w * .86f, h * .86f), Offset(w * .64f, h * .86f), s)
            line(Offset(w * .36f, h * .86f), Offset(w * .14f, h * .86f), s)
            line(Offset(w * .14f, h * .86f), Offset(w * .14f, h * .64f), s)
            drawRect(color.copy(alpha = .55f), Offset(w * .31f, h * .31f), Size(w * .38f, h * .38f), style = Stroke(1.4f))
        }
        Glyph.Palette -> { drawCircle(color, w * .35f, Offset(w * .50f, h * .50f), style = Stroke(1.9f)); drawCircle(color, 2f, Offset(w * .39f, h * .44f)); drawCircle(color, 2f, Offset(w * .57f, h * .40f)); drawCircle(color, 2f, Offset(w * .55f, h * .60f)) }
        Glyph.Library -> { drawRoundRect(color, Offset(w * .20f, h * .22f), Size(w * .60f, h * .56f), androidx.compose.ui.geometry.CornerRadius(4f, 4f), style = Stroke(1.8f)); line(Offset(w * .32f, h * .42f), Offset(w * .68f, h * .42f)); line(Offset(w * .32f, h * .58f), Offset(w * .57f, h * .58f)) }
        Glyph.Layers -> {
            val cr = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            drawRoundRect(color.copy(alpha = .45f), Offset(w * .28f, h * .18f), Size(w * .52f, h * .42f), cr, style = Stroke(1.8f))
            drawRoundRect(color.copy(alpha = .72f), Offset(w * .20f, h * .30f), Size(w * .52f, h * .42f), cr, style = Stroke(1.8f))
            drawRoundRect(color, Offset(w * .12f, h * .42f), Size(w * .52f, h * .42f), cr, style = Stroke(2.0f))
        }
        Glyph.Fx -> {
            fun sparkle(cx: Float, cy: Float, radius: Float, stroke: Float) {
                line(Offset(cx - radius, cy), Offset(cx + radius, cy), stroke)
                line(Offset(cx, cy - radius), Offset(cx, cy + radius), stroke)
                line(Offset(cx - radius * .68f, cy - radius * .68f), Offset(cx + radius * .68f, cy + radius * .68f), stroke)
                line(Offset(cx + radius * .68f, cy - radius * .68f), Offset(cx - radius * .68f, cy + radius * .68f), stroke)
            }
            sparkle(w * .43f, h * .45f, w * .22f, 1.9f)
            sparkle(w * .73f, h * .25f, w * .10f, 1.5f)
            sparkle(w * .72f, h * .72f, w * .13f, 1.5f)
        }
        Glyph.Settings -> {
            val s = 2.0f
            val c = Offset(w * .5f, h * .5f)
            drawCircle(color, w * .24f, c, style = Stroke(s))
            drawCircle(color, w * .08f, c, style = Stroke(s))
            line(Offset(w * .5f, h * .12f), Offset(w * .5f, h * .26f), s)
            line(Offset(w * .5f, h * .74f), Offset(w * .5f, h * .88f), s)
            line(Offset(w * .12f, h * .5f), Offset(w * .26f, h * .5f), s)
            line(Offset(w * .74f, h * .5f), Offset(w * .88f, h * .5f), s)
            line(Offset(w * .23f, h * .23f), Offset(w * .33f, h * .33f), s)
            line(Offset(w * .67f, h * .67f), Offset(w * .77f, h * .77f), s)
            line(Offset(w * .77f, h * .23f), Offset(w * .67f, h * .33f), s)
            line(Offset(w * .33f, h * .67f), Offset(w * .23f, h * .77f), s)
        }
        Glyph.Ellipsis -> {
            drawCircle(color, w * .065f, Offset(w * .25f, h * .5f))
            drawCircle(color, w * .065f, Offset(w * .5f, h * .5f))
            drawCircle(color, w * .065f, Offset(w * .75f, h * .5f))
        }
        Glyph.Add -> {
            line(Offset(w * .5f, h * .18f), Offset(w * .5f, h * .82f), 2.2f)
            line(Offset(w * .18f, h * .5f), Offset(w * .82f, h * .5f), 2.2f)
        }
        Glyph.Canvas -> {
            drawRoundRect(color, Offset(w * .16f, h * .18f), Size(w * .68f, h * .64f),
                androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(2f))
            line(Offset(w * .28f, h * .70f), Offset(w * .46f, h * .48f), 1.8f)
            line(Offset(w * .46f, h * .48f), Offset(w * .58f, h * .61f), 1.8f)
            line(Offset(w * .58f, h * .61f), Offset(w * .73f, h * .40f), 1.8f)
        }
        Glyph.Assist -> {
            line(Offset(w * .2f, h * .8f), Offset(w * .8f, h * .2f), 2f)
            drawCircle(color, w * .08f, Offset(w * .28f, h * .28f))
            drawCircle(color, w * .055f, Offset(w * .73f, h * .7f))
            drawCircle(color, w * .035f, Offset(w * .5f, h * .18f))
        }
        Glyph.Tools -> {
            drawCircle(color, w * .18f, Offset(w * .34f, h * .34f), style = Stroke(2f))
            line(Offset(w * .46f, h * .46f), Offset(w * .8f, h * .8f), 3f)
            line(Offset(w * .62f, h * .25f), Offset(w * .78f, h * .41f), 2f)
        }
        Glyph.Import -> {
            drawRoundRect(color, Offset(w * .18f, h * .18f), Size(w * .64f, h * .64f),
                androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.8f))
            line(Offset(w * .5f, h * .2f), Offset(w * .5f, h * .62f), 2f)
            line(Offset(w * .34f, h * .47f), Offset(w * .5f, h * .63f), 2f)
            line(Offset(w * .66f, h * .47f), Offset(w * .5f, h * .63f), 2f)
        }
        Glyph.Share -> {
            drawRoundRect(color, Offset(w * .2f, h * .42f), Size(w * .6f, h * .4f),
                androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.8f))
            line(Offset(w * .5f, h * .64f), Offset(w * .5f, h * .16f), 2f)
            line(Offset(w * .34f, h * .32f), Offset(w * .5f, h * .16f), 2f)
            line(Offset(w * .66f, h * .32f), Offset(w * .5f, h * .16f), 2f)
        }
        Glyph.Delete -> {
            drawRoundRect(color, Offset(w * .3f, h * .28f), Size(w * .4f, h * .56f),
                androidx.compose.ui.geometry.CornerRadius(2f, 2f), style = Stroke(2f))
            line(Offset(w * .22f, h * .28f), Offset(w * .78f, h * .28f), 2f)
            line(Offset(w * .4f, h * .17f), Offset(w * .6f, h * .17f), 2f)
        }
        Glyph.Grid -> {
            for (index in 1..2) {
                val p = index / 3f
                line(Offset(w * p, h * .16f), Offset(w * p, h * .84f), 1.7f)
                line(Offset(w * .16f, h * p), Offset(w * .84f, h * p), 1.7f)
            }
            drawRect(color, Offset(w * .16f, h * .16f), Size(w * .68f, h * .68f), style = Stroke(1.7f))
        }
    }
}
