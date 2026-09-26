# NeoCanvas Commercial Brush Library and Seamless Canvas Design

**Date:** 2026-09-26  
**Branch:** `ipad-dev`  
**Status:** Approved design

## Purpose

Replace NeoCanvas's large, repetitive built-in brush catalogue with a smaller commercial-quality collection whose brushes produce visibly distinct marks. Brush previews must be rendered by the real brush engine, and the faint 256-pixel tile grid visible under broad paint must be removed from the live canvas.

The design keeps the existing Kotlin Multiplatform modules, sparse tiled document model, imported brush-pack format, history model, and platform implementations. It improves the current brush and display paths without replacing the application architecture.

This specification supersedes the exact 180/210 preset-count requirements in the 2026-09-17 brush-library design and the procedural Neo Nature Studio recipes in the 2026-09-23 brush-pack design. It retains their requirements for stable IDs, deterministic rendering, safe data-only packs, local persistence and platform parity.

## Product outcomes

- Ship approximately 48 deliberately authored built-in paint brushes across six collections.
- Give Nature the broadest range, with recognizable leaves, grasses, foliage, conifers, branches, bark, fern and ground-cover marks.
- Remove the 210 generated presets from the visible library, search, favourites and recent lists.
- Render every library preview with the same rasterizer, assets and settings used on the canvas.
- Eliminate visible tile seams at fractional zoom and canvas rotation without changing stored pixels or exported artwork.
- Preserve custom brushes, imported packs, existing documents, history behavior and the six public legacy brush IDs.
- Use only original NeoCanvas shape and grain artwork.

## Current problems

`NeoBrushLibrary` creates ten presets for each of 21 categories from shared templates. The presets vary names and a few scalar settings but ultimately share thirteen procedural `BrushTip` implementations, so many brushes look alike.

`StrokePreview` does not invoke `Rasterizer`. It draws the same synthetic curved line for every brush and changes mostly width, alpha and a few dots. The preview therefore does not represent the selected brush.

`BrushStamp` declares shape, grain, direction, color jitter, pressure scatter, stamp count and taper controls, but the normal editor path does not supply a brush asset resolver. The rasterizer currently resolves only the shape reference and does not apply several declared settings. `NeoNatureStudio` has no shape or grain assets, so its version-two brushes fall back to procedural tips.

The live canvas draws each 256×256 tile as a separate transformed image. Setting `FilterQuality.None` reduced filtering artifacts but did not stop fractional transform coverage gaps. On iPad, broad continuous colour reveals the tile grid as faint squares even though the raster data uses continuous global pixel coordinates.

## Curated catalogue

The default collection targets 48 brushes. The final count may decrease when two candidates cannot meet the visual-distinction gate; it must not be padded with near-duplicates.

| Collection | Target | Scope |
|---|---:|---|
| Essentials | 6 | Hard round, soft airbrush, monoline, block/fill, pixel and clean utility paint |
| Sketching | 6 | Graphite, technical pencil, soft pencil, charcoal, chalk and crayon |
| Inking | 6 | Studio, technical, dry, brush, comic and marker inks |
| Painting | 8 | Round, flat, filbert, dry paint, gouache, oil, watercolour wash and glaze |
| Textures | 6 | Grain, spray, stipple, paper, rock and distressed marks |
| Nature | 16 | Individual leaves, leaf clusters, canopy, wild grass, meadow grass, fern, pine needles, pine bough, hedge, branch, twig, bark, moss and related ground cover |

Each preset is authored individually. Shared helpers may enforce consistent validation and naming, but a template loop must not manufacture cosmetic variants.

The categories appear in the table order, with Essentials selected initially. Search, favourites, recent brushes, custom brushes and imported packs remain available.

## Brush model and runtime

Introduce a new built-in brush asset catalogue owned by the `brushes` module. Each asset has a stable portable ID, SHA-256 reference and decoded coverage representation. Built-in and installed-pack assets resolve through one composite `BrushAssetResolver` supplied to every editor, preview, replay and test-pad rasterization call.

Advance asset-backed built-ins to brush schema version 3 while continuing to decode version 1 and 2 definitions. Version 3 adds an ordered set of shape variants. A deterministic stamp selector chooses a variant from the stroke seed, stamp index and sub-stamp index, producing natural variety without making saved behavior nondeterministic.

The rasterizer will implement the declared behavior used by shipped presets:

- Shape masks sampled with antialiased coverage.
- Grain sampled either in stamp space or stable canvas space.
- Rotation aligned to the local stroke tangent for direction modes.
- Bounded angle jitter and horizontal mirroring for organic shapes.
- Pressure-controlled size, opacity, scatter and stamp count.
- Start and end taper over the whole interpolated stroke.
- Bounded hue, saturation and brightness variation where a preset opts in.

Unsupported controls must not be exposed in Brush Studio until the renderer implements them. Existing custom version 1 and 2 brushes retain their current behavior.

## Original brush assets

Shape masks are monochrome coverage assets with transparent backgrounds. Organic brushes use multiple variants rather than repeatedly rotating one silhouette. The Nature collection includes distinct original silhouettes for broad leaves, fine leaves, oak-like clusters, tropical foliage, grass blades, meadow clumps, fern fronds, pine needles, boughs, branches, twigs, bark fragments and moss.

Grain sources are seamless monochrome textures designed for graphite, chalk, dry paint, canvas, paper, bark, stone and organic breakup. Grain assets include padded or seamless borders so their repetition cannot introduce square edges.

Editable source masks live in the repository as grayscale PNG files. A deterministic preparation step converts them into the renderer's bounded coverage format and verifies dimensions, hashes, non-empty coverage and declared references. The prepared coverage resources are packaged for every supported target, avoiding platform-dependent image decoding during a stroke. Generated binary pack artifacts are derived outputs rather than the only source.

No Procreate brush, source-library image, setting dump or proprietary artwork is imported. Procreate is used only as a product reference for the established shape-plus-grain model and truthful stroke previews.

## Real brush previews

Replace the synthetic `StrokePreview` drawing with `BrushPreviewRenderer`, a shared deterministic renderer backed by `Rasterizer` and the composite asset resolver.

Every preset preview uses a fixed-size transparent raster surface and a standard sample path with a light-to-firm pressure ramp. The path includes a short curve and a final dab so both continuous stroke character and stamp silhouette are visible. Nature brushes may use a category-specific path length, but never a fake illustration disconnected from the brush engine.

Preview cache keys include brush ID, brush version, serialized settings hash, referenced asset hashes, preview dimensions and theme foreground color. Cached previews are reused while scrolling. Editing a setting or replacing a pack invalidates only affected entries.

Each row receives a wider, higher-contrast preview, brush name and concise behavior description. Selecting a brush displays a larger real preview. Brush Studio's test pad continues to offer interactive testing and uses the same resolver.

For the internal TestFlight milestone, the library footer states: `48 launch brushes · More original brushes will arrive in future updates.` The count must be derived from the shipped catalogue if quality review removes a candidate. This informational message must not block drawing or imply that the included collection is unfinished.

## Seamless tiled canvas display

Keep sparse 256×256 storage. Fix the artifact only in live compositing.

For each visible logical layer, create one offscreen compositing pass. Tiles are drawn into that pass with a small device-pixel-derived bleed and source replacement so adjacent transformed quads cover fractional gaps without applying pigment twice. After tile assembly, apply the layer opacity, clipping, mask and blend mode once when compositing the offscreen result into the document view.

The bleed is calculated from the current document-to-device transform and is capped below one document pixel. Tile images remain nearest-sampled for pixel-art correctness. The document bounds clip the assembled layer, preventing edge growth. Because source replacement is used within the layer pass, translucent pixels at tile boundaries are replaced rather than double-blended.

PNG, TIFF, PDF and PSD export paths continue reading stored tile bytes and are unchanged.

If the target's canvas backend cannot provide the required offscreen source-replacement semantics consistently, the fallback is a viewport-sized reusable layer surface rather than expanding destination rectangles directly onto the final canvas. Direct final-canvas overlap is prohibited because it darkens translucent paint.

## Compatibility and migration

Existing document pixels need no migration. Recent editable strokes already retain complete `BrushDefinition` values during their editable lifetime and continue replaying with those definitions.

The following public IDs remain resolvable and point to improved replacements:

- `neo.pencil`
- `neo.ink`
- `neo.soft-round`
- `neo.dry-paint`
- `neo.flat-marker`
- `neo.eraser`

All other generated presets disappear from the built-in catalogue. Restored favourites and recents already discard unresolved IDs. Custom brushes and installed packs remain in their existing categories and persistence format.

Pack decoding remains backward compatible. A version-3 codec extension must reject missing assets, hash mismatches, unsupported dimensions and invalid variant lists with the existing validation error model.

## Quality gates

Each shipped brush must pass deterministic output tests using fixed paths, pressures, colors and seeds. Tests verify non-empty output, repeatability, tile-boundary continuity and expected use of referenced assets.

Pairwise preview comparison rejects exact duplicates and flags candidates above a conservative similarity threshold for human review. Automated similarity is a review gate rather than a claim that two artistic brushes are equivalent.

Nature-specific tests verify recognizable structural properties: leaf masks have tapered silhouettes, grass masks contain separated blades, fern masks contain repeated lateral leaflets, branch masks remain direction-aligned, and bark/grain assets tile without a hard rectangular border.

Canvas compositor tests render opaque and translucent paint across horizontal and vertical tile boundaries at 50%, 75%, 100%, 125% and 200% scale, including rotated views. Boundary pixels must match neighboring interior coverage within a small rendering tolerance. A physical iPad visual check covers panning, zooming and broad low-opacity colour.

Performance checks bound stamp work, preview generation time and cache size. Opening and scrolling the library must not synchronously regenerate unchanged previews. Large textured brushes retain the existing stamp-work guardrails.

The completion suite includes all shared tests, release contracts, simulator compilation, signed ARM64 archive validation and physical iPad smoke testing. The VM's lack of Metal remains an environment constraint, so runtime acceptance uses the physical iPad build.

## Delivery sequence

1. Add failing tile-boundary and real-preview tests.
2. Implement seamless per-layer tile compositing and verify it independently.
3. Add version-3 shape variants, grain behavior, direction and pressure dynamics with codec compatibility tests.
4. Add the original built-in asset catalogue and composite resolver.
5. Author the curated catalogue and remove generated presets from the visible library.
6. Replace synthetic previews with cached real rendered previews and polish the library rows.
7. Run duplicate, silhouette, performance and compatibility gates; remove any brush that does not meet them.
8. Regenerate the Xcode project, run the complete validation suite, produce a new signed build number and repeat physical iPad acceptance before TestFlight upload.

## Release boundary

This redesign changes visible brush behavior and therefore requires a new build number after implementation. The already validated version 1.0.0 build 1 remains an evidence artifact and must not be uploaded as representing this redesign. Final App Store Connect upload still requires explicit approval.
