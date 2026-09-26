# Seamless Canvas Compositor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove visible 256-pixel tile seams from opaque and translucent artwork at fractional zoom and rotation without changing stored raster pixels or exports.

**Architecture:** Keep sparse 256×256 storage and tile image caching. Assemble each raster layer in an offscreen canvas pass using device-pixel-derived edge bleed and `BlendMode.Src`, then apply the layer opacity and blend mode once when restoring that pass.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Canvas/DrawScope, Skia-backed desktop rendering tests, kotlin.test.

**Spec:** `docs/superpowers/specs/2026-09-26-commercial-brush-library-design.md`

## Global Constraints

- Keep the existing tiled document format, history commands and export implementations unchanged.
- Preserve iOS 15, Swift 6, Kotlin 2.4.20 and Compose Multiplatform 1.10 compatibility.
- Never overlap semi-transparent tiles directly onto the final document canvas.
- Clip assembled raster layers to the document bounds.
- Preserve nearest-neighbour tile sampling for pixel-art behavior.

## Review Focus

- Adjacent translucent tiles at 75% zoom must not create either a light gap or dark double-blend line; Task 2 renders and samples this case.
- Negative tile coordinates from previews or transforms must use the same bleed and clipping rules; Task 1 tests their geometry.
- Canvas rotation must not change bleed magnitude or expose corners; Task 2 renders a rotated case.
- Non-Normal layer blend modes and group opacity must be applied once per layer; Task 3 pins both behaviors.
- Sparse layers with missing neighboring tiles must retain transparent regions rather than stretching paint into empty tiles; Task 2 tests a missing neighbor.

---

### Task 1: Define seam-safe tile geometry

**Files:**
- Create: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositor.kt`
- Create: `ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorGeometryTest.kt`

**Interfaces:**
- Consumes: `TileKey`, `TILE_SIZE_PIXELS`, document scale and document dimensions.
- Produces: `internal data class TileCompositeBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)` and `internal fun seamSafeTileBounds(key: TileKey, documentScale: Float, documentWidth: Int, documentHeight: Int): TileCompositeBounds`.

- [ ] **Step 1: Write failing geometry tests**

Add tests named `fractional_scale_adds_less_than_one_document_pixel_of_bleed`, `document_edges_are_clipped`, and `negative_tile_coordinates_remain_ordered`. Assert 75% scale expands internal edges, never expands beyond document bounds, and always returns positive width/height for an intersecting tile.

- [ ] **Step 2: Run the tests and verify RED**

Run: `./gradlew :ui:desktopTest --tests '*TiledLayerCompositorGeometryTest'`

Expected: compilation fails because `seamSafeTileBounds` does not exist.

- [ ] **Step 3: Implement the geometry helper**

Use a bleed derived from one device pixel divided by `documentScale`, capped below one document pixel. Expand only internal tile edges and clamp to `[0, documentWidth] × [0, documentHeight]`.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run: `./gradlew :ui:desktopTest --tests '*TiledLayerCompositorGeometryTest'`

Expected: all geometry tests pass.

- [ ] **Step 5: Commit**

```bash
git add ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositor.kt ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorGeometryTest.kt
git commit -m "fix(canvas): define seam-safe tile geometry"
```

### Task 2: Render tiles through one layer pass

**Files:**
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositor.kt`
- Create: `ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorRenderTest.kt`

**Interfaces:**
- Consumes: `TileCompositeBounds` from Task 1 and `List<RasterTileImage>`.
- Produces: `internal data class RasterTileImage(val key: TileKey, val image: ImageBitmap)` and `internal fun DrawScope.drawSeamlessRasterTiles(tiles: List<RasterTileImage>, documentWidth: Int, documentHeight: Int, documentScale: Float, alpha: Float, blendMode: BlendMode)`.

- [ ] **Step 1: Write the failing rendered regression tests**

Render adjacent 256×256 solid tiles into an `ImageBitmap` through `CanvasDrawScope`. Add `translucent_tiles_have_no_fractional_zoom_boundary`, `rotated_tiles_have_no_boundary`, and `missing_neighbor_remains_transparent`. Sample pixels on both sides of each boundary and assert channel/alpha differences are at most one for matching tiles.

- [ ] **Step 2: Run the rendered tests and verify RED**

Run: `./gradlew :ui:desktopTest --tests '*TiledLayerCompositorRenderTest'`

Expected: compilation fails because `drawSeamlessRasterTiles` does not exist.

- [ ] **Step 3: Implement the offscreen layer pass**

Use `drawContext.canvas.saveLayer` over the document bounds with the requested final alpha and blend mode. Draw each expanded tile inside that pass with full alpha, `BlendMode.Src`, `FilterQuality.None`, and the Task 1 bounds; restore once after all tiles. Keep empty regions transparent.

- [ ] **Step 4: Run focused geometry and rendered tests**

Run: `./gradlew :ui:desktopTest --tests '*TiledLayerCompositor*'`

Expected: all compositor tests pass.

- [ ] **Step 5: Commit**

```bash
git add ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositor.kt ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorRenderTest.kt
git commit -m "fix(canvas): composite raster tiles without seams"
```

### Task 3: Integrate the compositor into CanvasWorkspace

**Files:**
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/CanvasWorkspace.kt:1453-1501`
- Modify: `ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/CanvasTileSamplingTest.kt`
- Test: `ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorRenderTest.kt`

**Interfaces:**
- Consumes: `drawSeamlessRasterTiles` from Task 2 and the existing mask/clipping pixel preparation path.
- Produces: one compositor call per visible raster layer; text and shape layer rendering remains unchanged.

- [ ] **Step 1: Extend failing integration tests**

Add tests proving layer alpha and a non-Normal blend mode are applied once, and update `CanvasTileSamplingTest` to assert the seamless compositor uses `FilterQuality.None` rather than testing the obsolete standalone helper.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew :ui:desktopTest --tests '*CanvasTileSamplingTest' --tests '*TiledLayerCompositorRenderTest'`

Expected: the new integration assertions fail against per-tile alpha/blending.

- [ ] **Step 3: Replace per-tile final-canvas drawing**

In `drawStoredTiles`, retain mask and clipping preparation, collect `RasterTileImage` values for the current raster layer, then call `drawSeamlessRasterTiles` with the current document scale, effective opacity and layer blend mode.

- [ ] **Step 4: Run UI and shared tests**

Run: `./gradlew :ui:desktopTest :ui:allTests :renderer:allTests`

Expected: all tests pass with no seam regression.

- [ ] **Step 5: Commit**

```bash
git add ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/CanvasWorkspace.kt ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/CanvasTileSamplingTest.kt ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/TiledLayerCompositorRenderTest.kt
git commit -m "fix(canvas): use seamless raster layer compositor"
```

### Task 4: Validate all supported builds

**Files:**
- Modify only if a genuine build issue is found in compositor-owned code.

**Interfaces:**
- Consumes: completed compositor integration.
- Produces: verified cross-platform change ready for the brush-library plan.

- [ ] **Step 1: Run the shared validation suite**

Run: `./gradlew :core:allTests :brushes:allTests :renderer:allTests :ui:allTests`

Expected: BUILD SUCCESSFUL with zero failed tests.

- [ ] **Step 2: Run release contracts**

Run: `python3 scripts/verify-version-contract.py && python3 scripts/verify-release-readiness.py && python3 scripts/verify-platform-parity.py`

Expected: every script exits zero.

- [ ] **Step 3: Generate and compile the iPad project**

Run: `./scripts/prepare-ios.sh`

Then run the established unsigned iPad Simulator build command from the release report.

Expected: XcodeGen succeeds and `xcodebuild` reports `BUILD SUCCEEDED`.

- [ ] **Step 4: Commit any narrowly required build correction**

If no correction is required, do not create an empty commit.
