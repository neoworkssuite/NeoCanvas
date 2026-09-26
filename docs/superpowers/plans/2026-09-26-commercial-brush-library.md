# Commercial Brush Library Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace 210 repetitive generated presets with a curated, asset-backed launch library whose actual rendered previews and natural-media marks meet the approved TestFlight quality bar.

**Architecture:** Extend the compatible data-only brush model to version 3, connect a composite asset resolver to every rasterization path, and author six explicit collections totaling up to 48 distinct brushes. Generate previews with the shared rasterizer, cache them by settings and asset hashes, and keep legacy IDs resolvable through upgraded presets.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Kotlin/Native, deterministic RGBA tile rasterizer, Python asset-preparation script, grayscale PNG source masks, kotlin.test.

**Spec:** `docs/superpowers/specs/2026-09-26-commercial-brush-library-design.md`

## Global Constraints

- Preserve the repository architecture, sparse tile format, custom brushes, installed packs and all platform targets.
- Ship no copied Procreate brushes, settings or source-library artwork; all masks and grains are original NeoCanvas assets.
- Keep `neo.pencil`, `neo.ink`, `neo.soft-round`, `neo.dry-paint`, `neo.flat-marker` and `neo.eraser` resolvable.
- Decode version 1 and 2 brush files exactly as before.
- Never silently substitute a procedural tip when an asset-backed brush references a missing or invalid asset.
- Preview pixels must come from the same rasterizer and resolver used on the artwork canvas.
- The catalogue target is 48, but quality review may remove a near-duplicate rather than pad the count.

## Review Focus

- A version-3 brush with a missing shape variant must fail validation instead of rendering a misleading fallback; Tasks 1 and 3 test this.
- Direction-aligned stamps must follow left-to-right, right-to-left and curved paths deterministically; Task 2 tests all three.
- Canvas-space grain must remain stationary across adjacent stamps and tile boundaries; Task 2 tests this.
- Restoring favourites and recents containing removed generated IDs must safely discard them while retaining legacy IDs; Task 7 tests this.
- Preview cache invalidation must include brush settings and asset hashes so a replaced pack cannot display stale marks; Task 6 tests this.

---

### Task 1: Add the version-3 brush contract

**Files:**
- Modify: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BrushDefinition.kt`
- Modify: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BrushStamp.kt`
- Modify: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/NeoBrushCodec.kt`
- Modify: `brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/NeoBrushCodecTest.kt`
- Modify: `brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/BrushStampTest.kt`

**Interfaces:**
- Consumes: existing V1/V2 fields and `BrushAssetRef`.
- Produces: `BrushDefinition.description: String`, `BrushStamp.shapeVariants: List<BrushAssetRef>`, `BrushStamp.resolvedShapes: List<BrushAssetRef>`, and `NEOCANVAS_BRUSH=3` codec support.

- [ ] **Step 1: Write failing V3 round-trip and validation tests**

Test a two-shape brush with a description and grain; assert exact round trip. Test duplicate/empty variants, incomplete hashes, more than eight variants and a V3 asset reference missing from its declared set. Retain byte-for-byte behavioral tests for V1/V2 decoding defaults.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :brushes:desktopTest --tests '*NeoBrushCodecTest' --tests '*BrushStampTest'`

Expected: V3 tests fail because the fields and header do not exist.

- [ ] **Step 3: Implement the minimal compatible model and codec**

Use an ordered, escaped V3 field for shape variants; keep the V2 `shape` field readable and expose it through `resolvedShapes` when no variant list exists. Default descriptions to an empty string for V1/V2.

- [ ] **Step 4: Run the brushes tests and verify GREEN**

Run: `./gradlew :brushes:allTests`

Expected: all brush codec/model tests pass.

- [ ] **Step 5: Commit**

```bash
git add brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BrushDefinition.kt brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BrushStamp.kt brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/NeoBrushCodec.kt brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes
git commit -m "feat(brushes): add version 3 asset variants"
```

### Task 2: Implement truthful stamp dynamics

**Files:**
- Modify: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/Rasterizer.kt`
- Modify: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/StampMaskSampler.kt`
- Create: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BrushGrainSampler.kt`
- Create: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BrushDynamicsV3Test.kt`
- Modify: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/StampMaskSamplerTest.kt`

**Interfaces:**
- Consumes: `BrushStamp.resolvedShapes`, existing `BrushAssetResolver`, stroke points and pressure.
- Produces: internal `RasterStampSample(point: RasterPoint, tangentRadians: Float, progress: Float)`, deterministic variant selection, grain sampling, direction modes, taper and pressure dynamics.

- [ ] **Step 1: Write failing dynamics tests**

Add tests for deterministic variant choice, forward/reverse/curved direction alignment, stamp-space grain, canvas-space grain across a tile edge, pressure scatter/count bounds, start/end taper and bounded color jitter.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :renderer:desktopTest --tests '*BrushDynamicsV3Test' --tests '*StampMaskSamplerTest'`

Expected: assertions fail because the declared dynamics are not applied.

- [ ] **Step 3: Add tangent and progress to interpolation**

Change internal stroke interpolation to return `RasterStampSample` while preserving the public `Rasterizer.stroke` signature and endpoint budget.

- [ ] **Step 4: Implement variant, grain, direction, taper and pressure behavior**

Select all variation from stable stroke coordinates, stamp index and sub-stamp index. Resolve every referenced asset before mutating tiles and reject missing assets.

- [ ] **Step 5: Run renderer tests and verify GREEN**

Run: `./gradlew :renderer:allTests`

Expected: all renderer tests pass, including existing stamp-work budgets.

- [ ] **Step 6: Commit**

```bash
git add renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer
git commit -m "feat(renderer): implement asset-backed brush dynamics"
```

### Task 3: Build the original asset pipeline and resolver

**Files:**
- Create: `assets/brushes/source/shapes/*.png`
- Create: `assets/brushes/source/grains/*.png`
- Create: `assets/brushes/manifest.json`
- Create: `scripts/prepare-brush-assets.py`
- Create generated file: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BuiltInBrushAssetRefs.kt`
- Create: `brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/BuiltInBrushAssetRefsTest.kt`
- Create: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BrushAssetCodec.kt`
- Create: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssets.kt`
- Create generated file: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssetData.kt`
- Create: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BrushAssetCodecTest.kt`
- Create: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssetsTest.kt`
- Create: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/CompositeBrushAssetResolver.kt`
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryState.kt`
- Create: `ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/CompositeBrushAssetResolverTest.kt`

**Interfaces:**
- Consumes: original grayscale source PNGs and installed-pack `.neomask` coverage bytes.
- Produces: `BuiltInBrushAssetRefs` in `brushes`, `BrushAssetCodec.decode(bytes): BrushAsset` and `BuiltInBrushAssets.resolver` in `renderer`, `BrushLibraryState.assetResolver` in `ui`, and deterministic generated coverage data keyed by `BrushAssetRef`.

- [ ] **Step 1: Write failing manifest and resolver tests**

Assert every manifest hash/dimension, non-empty coverage, transparent border, unique ID, missing-hash rejection, bounded `.neomask` decoding, malformed-data rejection, built-in resolution and installed-pack precedence without ID/hash collision.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :brushes:desktopTest --tests '*BuiltInBrushAssetRefsTest' :renderer:desktopTest --tests '*BrushAssetCodecTest' --tests '*BuiltInBrushAssetsTest' :ui:desktopTest --tests '*CompositeBrushAssetResolverTest'`

Expected: compilation fails because the catalog and resolver do not exist.

- [ ] **Step 3: Author source masks and grains**

Create original high-contrast grayscale assets for the approved shape and grain families. Keep organic masks clear of rectangular borders and provide multiple variants for Nature silhouettes.

- [ ] **Step 4: Implement deterministic preparation**

`prepare-brush-assets.py` validates the manifest, generates stable `BrushAssetRef` constants in the `brushes` module, and generates compact run-length coverage arrays in the `renderer` module. It also defines the deterministic bounded `.neomask` format used by version-3 pack assets. Running it twice must produce identical bytes; `--verify` fails if either generated file is stale.

- [ ] **Step 5: Implement built-in and composite resolvers**

Decode bounded `.neomask` bytes with `BrushAssetCodec`, expand built-in coverage lazily into `BrushAsset`, verify decoded dimensions and SHA-256, and expose one resolver through `BrushLibraryState` for built-ins and installed packs. Keep V1/V2 definition decoding unchanged.

- [ ] **Step 6: Run asset verification and tests**

Run: `python3 scripts/prepare-brush-assets.py --verify && ./gradlew :brushes:allTests :renderer:allTests :ui:desktopTest --tests '*CompositeBrushAssetResolverTest'`

Expected: generated data is current and all tests pass.

- [ ] **Step 7: Commit**

```bash
git add assets/brushes scripts/prepare-brush-assets.py brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/BuiltInBrushAssetRefs.kt brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/BuiltInBrushAssetRefsTest.kt renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BrushAssetCodec.kt renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssets.kt renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssetData.kt renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BrushAssetCodecTest.kt renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BuiltInBrushAssetsTest.kt ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/CompositeBrushAssetResolver.kt ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryState.kt ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/CompositeBrushAssetResolverTest.kt
git commit -m "feat(brushes): add original brush asset pipeline"
```

### Task 4: Author the curated core collections

**Files:**
- Replace: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/NeoBrushLibrary.kt`
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/EssentialBrushes.kt`
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/SketchingBrushes.kt`
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/InkingBrushes.kt`
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/PaintingBrushes.kt`
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/TextureBrushes.kt`
- Modify: `brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/BrushCatalogTest.kt`
- Create: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/CommercialBrushQualityTest.kt`

**Interfaces:**
- Consumes: version-3 definitions and built-in asset references.
- Produces: 32 explicit non-Nature presets across Essentials 6, Sketching 6, Inking 6, Painting 8 and Textures 6; the six required legacy IDs map to upgraded definitions.

- [ ] **Step 1: Write failing catalogue and output tests**

Assert exact category order/counts, unique IDs, non-empty descriptions, valid referenced assets, legacy ID resolution, deterministic output and no exact duplicate preview/output hashes.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :brushes:desktopTest --tests '*BrushCatalogTest' :renderer:desktopTest --tests '*CommercialBrushQualityTest'`

Expected: tests fail against the generated 210-preset catalogue.

- [ ] **Step 3: Replace template generation with explicit presets**

Give every preset an authored description, shape/grain selection and dynamics appropriate to its named medium. Remove the 21-category template generator.

- [ ] **Step 4: Run focused tests and inspect generated samples**

Run: `./gradlew :brushes:desktopTest --tests '*BrushCatalogTest' :renderer:desktopTest --tests '*CommercialBrushQualityTest'`

Expected: all 32 core brushes pass deterministic and uniqueness gates.

- [ ] **Step 5: Commit**

```bash
git add brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/BrushCatalogTest.kt renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/CommercialBrushQualityTest.kt
git commit -m "feat(brushes): curate commercial core collection"
```

### Task 5: Author the detailed Nature collection

**Files:**
- Create: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/NatureBrushes.kt`
- Remove: `brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes/NeoNatureStudio.kt`
- Replace: `brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes/NeoNatureStudioPackTest.kt` with `NatureBrushesTest.kt`
- Modify: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/NeoNatureStudioQualityTest.kt` and rename class/file to `NatureBrushQualityTest.kt`

**Interfaces:**
- Consumes: authored leaf, grass, fern, pine, branch, bark and moss assets.
- Produces: 16 explicit Nature presets and a total built-in catalogue of 48 unless a quality gate removes a candidate.

- [ ] **Step 1: Write failing silhouette and behavior tests**

Assert leaf taper, separated grass blades, fern lateral leaflets, direction-aligned branches, borderless bark/grain tiling, deterministic scatter and visibly different coverage hashes for all Nature presets.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :brushes:desktopTest --tests '*NatureBrushesTest' :renderer:desktopTest --tests '*NatureBrushQualityTest'`

Expected: failures show procedural recipes cannot meet the silhouette gates.

- [ ] **Step 3: Implement 16 authored Nature presets**

Use multiple shape variants where organic repetition would be visible. Tune scale, density, scatter, tangent alignment and grain for recognizable use at the default size.

- [ ] **Step 4: Run focused tests and inspect raster fixtures**

Run the focused tests and inspect deterministic raster fixtures produced by the test helper. Task 8 performs the final human review after the preview renderer is complete.

Expected: every preset passes structure and distinctness checks.

- [ ] **Step 5: Commit**

```bash
git add brushes/src/commonMain/kotlin/com/neoworksuite/neocanvas/brushes brushes/src/commonTest/kotlin/com/neoworksuite/neocanvas/brushes renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer
git commit -m "feat(brushes): add detailed nature collection"
```

### Task 6: Render and cache honest previews

**Files:**
- Create: `renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BrushPreviewRenderer.kt`
- Create: `renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BrushPreviewRendererTest.kt`
- Create: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPreviewCache.kt`
- Create: `ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/BrushPreviewCacheTest.kt`
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPanel.kt:265-300,506-536`

**Interfaces:**
- Consumes: `BrushDefinition`, `BrushAssetResolver`, fixed preview dimensions and foreground `RasterColor`.
- Produces: `data class BrushPreview(val width: Int, val height: Int, val rgba: ByteArray)`, `BrushPreviewRenderer.render(...)`, and an LRU `BrushPreviewCache.image(...)` keyed by settings and referenced asset hashes.

- [ ] **Step 1: Write failing renderer and cache tests**

Assert preview repeatability, pressure-ramp width/opacity change, visible final dab, actual Nature silhouettes, cache reuse, settings invalidation, asset-hash invalidation and bounded eviction.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :renderer:desktopTest --tests '*BrushPreviewRendererTest' :ui:desktopTest --tests '*BrushPreviewCacheTest'`

Expected: preview renderer/cache symbols are missing.

- [ ] **Step 3: Implement real raster previews**

Render the standard curve and final dab through `Rasterizer.stroke`, assemble the resulting tiles into the requested RGBA dimensions, and pass the same resolver used by the editor.

- [ ] **Step 4: Replace synthetic `StrokePreview`**

Use cached actual preview pixels in the list and selected-brush area. Remove the generic `drawLine` approximation entirely.

- [ ] **Step 5: Run preview and UI tests**

Run: `./gradlew :renderer:allTests :ui:desktopTest --tests '*BrushPreview*'`

Expected: previews are deterministic, asset-sensitive and cached.

- [ ] **Step 6: Commit**

```bash
git add renderer/src/commonMain/kotlin/com/neoworksuite/neocanvas/renderer/BrushPreviewRenderer.kt renderer/src/commonTest/kotlin/com/neoworksuite/neocanvas/renderer/BrushPreviewRendererTest.kt ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPreviewCache.kt ui/src/desktopTest/kotlin/com/neoworksuite/neocanvas/ui/BrushPreviewCacheTest.kt ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPanel.kt
git commit -m "feat(brushes): show real rendered brush previews"
```

### Task 7: Polish the launch library and migration behavior

**Files:**
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPanel.kt`
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryState.kt`
- Modify: `ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryStateTest.kt`
- Create: `ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryLaunchPresentationTest.kt`

**Interfaces:**
- Consumes: curated catalogue, descriptions and preview cache.
- Produces: six-category launch presentation, larger preview rows, selected preview, and `brushLaunchMessage(brushCount: Int): String`.

- [ ] **Step 1: Write failing presentation and migration tests**

Assert six category names/order, removed preset IDs absent from All/Search, legacy IDs retained, stale favourite/recent IDs discarded, custom/imported collections preserved, and the exact dynamic footer format `<count> launch brushes · More original brushes will arrive in future updates.`

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew :ui:desktopTest --tests '*BrushLibraryStateTest' --tests '*BrushLibraryLaunchPresentationTest'`

Expected: old category/count and UI behavior fails.

- [ ] **Step 3: Implement the polished library rows and footer**

Use wider high-contrast previews, name, authored description and favourite affordance. Show a larger selected-brush preview without blocking brush selection or drawing.

- [ ] **Step 4: Run all UI tests**

Run: `./gradlew :ui:allTests`

Expected: all tests pass and no removed built-in survives through search, favourites or recents.

- [ ] **Step 5: Commit**

```bash
git add ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushPanel.kt ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/BrushLibraryState.kt ui/src/commonTest/kotlin/com/neoworksuite/neocanvas/ui
git commit -m "feat(brushes): polish TestFlight brush library"
```

### Task 8: Perform the commercial quality review

**Files:**
- Modify only catalogue definitions/assets that fail review.
- Create: `scripts/render-brush-review-sheet.py` or the repository-native equivalent that consumes deterministic preview artifacts.
- Create output evidence outside the repository or in an ignored build directory.

**Interfaces:**
- Consumes: all built-in definitions, assets and real previews.
- Produces: labelled review sheets grouped by collection and machine-readable similarity results.

- [ ] **Step 1: Add the duplicate/similarity gate**

Compare normalized alpha previews; reject exact matches and flag high similarity for human review. Include default-size and large-size samples so thickness alone cannot satisfy distinctness.

- [ ] **Step 2: Render review sheets**

Generate one sheet per collection with names, descriptions, preview strokes and final dabs.

- [ ] **Step 3: Inspect every brush and remove or retune failures**

Reject square-edged organic masks, unreadable previews, obvious repeated silhouettes, harsh tiling and names unsupported by their marks. Update the dynamic launch count if a brush is removed.

- [ ] **Step 4: Re-run quality tests**

Run: `python3 scripts/prepare-brush-assets.py --verify && ./gradlew :brushes:allTests :renderer:allTests :ui:allTests`

Expected: all automated gates pass and review sheets show distinct marks.

- [ ] **Step 5: Commit approved tuning**

```bash
git add assets/brushes brushes renderer ui scripts/render-brush-review-sheet.py
git commit -m "refactor(brushes): complete commercial quality pass"
```

### Task 9: Prepare TestFlight build 2

**Files:**
- Modify: `ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/ReleaseInfo.kt`
- Modify: `iosApp/NeoCanvas/Info.plist`
- Modify: `docs/VERSIONING.md` if the Apple build contract is recorded there.
- Modify only other synchronized build-number files required by the existing version contract.

**Interfaces:**
- Consumes: completed compositor and commercial brush plans.
- Produces: NeoCanvas version `1.0.0`, build `2`, regenerated Xcode project, signed archive and physical-iPad acceptance evidence.

- [ ] **Step 1: Update the synchronized Apple build number to 2**

Keep the marketing version at `1.0.0`. Run the version contract immediately after editing.

- [ ] **Step 2: Run complete shared and contract validation**

Run: `./gradlew :core:allTests :brushes:allTests :renderer:allTests :ui:allTests`

Run all existing Python release-contract and platform-parity verification commands recorded in the release report.

Expected: every command exits zero.

- [ ] **Step 3: Generate and build the iPad project**

Run: `./scripts/prepare-ios.sh`, followed by the established unsigned simulator build and signed generic iOS archive commands with automatic provisioning.

Expected: Xcode reports `BUILD SUCCEEDED` and `ARCHIVE SUCCEEDED`.

- [ ] **Step 4: Validate the archive with Apple**

Use the existing validation-only export options with version `1.0.0`, build `2`, and `manageAppVersionAndBuildNumber=false`.

Expected: `Validated NeoCanvas` and `EXPORT SUCCEEDED`; no upload event.

- [ ] **Step 5: Perform physical iPad acceptance**

Install the prepared IPA through the already working Sideloadly route. Verify Gallery, canvas creation, all six brush categories, honest previews, representative pencil/ink/paint/Nature strokes, broad translucent colour without square seams, save and reopen.

- [ ] **Step 6: Record evidence and stop before upload**

Update `/Users/christian/Documents/Codex/2026-09-26/NeoCanvas-release-preparation.md` with commit, version/build, tests, archive/IPA paths and any blocker. Request explicit approval before the final App Store Connect upload.

- [ ] **Step 7: Commit release metadata**

```bash
git add ui/src/commonMain/kotlin/com/neoworksuite/neocanvas/ui/ReleaseInfo.kt iosApp/NeoCanvas/Info.plist docs/VERSIONING.md
git commit -m "chore(release): prepare NeoCanvas 1.0.0 build 2"
```
