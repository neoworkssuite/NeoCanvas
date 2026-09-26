# NeoCanvas migration report

Final verification date: 2026-09-26

## Canonical repository

- URL: <https://github.com/neoworkssuite/NeoCanvas>
- Default branch: `main`
- Active iPad development branch: `ipad-dev`
- Verified development HEAD: `455f9467762115244c3b6849f9206040b51a484b`
- Product version: `1.0.0` with platform build number `1`

The canonical repository retains the complete public Canvas-Mac lineage and
the audited branches and tags. A clean clone confirmed `main` as the remote
default and resolved every required canonical branch and sampled preservation
tag without rewriting history.

## Platform build and CI status

| Platform | Status | Authoritative evidence | Remaining boundary |
|---|---|---|---|
| Shared | ✅ | [run 36237966317](https://github.com/neoworkssuite/NeoCanvas/actions/runs/36237966317) | None in the tested contract |
| iPad | ✅ | [run 36238763263](https://github.com/neoworkssuite/NeoCanvas/actions/runs/36238763263) | Distribution signing is intentionally outside unsigned CI |
| macOS | 🟡 | [run 36236949475](https://github.com/neoworkssuite/NeoCanvas/actions/runs/36236949475) | Native file actions and signed/notarized distribution remain incomplete |
| Windows | ✅ | [run 36236333249](https://github.com/neoworkssuite/NeoCanvas/actions/runs/36236333249) | Production installer signing remains a release operation |
| Android | ✅ | [run 36237376981](https://github.com/neoworkssuite/NeoCanvas/actions/runs/36237376981) | Production AAB signing remains a release operation |

The detailed feature-by-platform truth is maintained in
[PLATFORM-PARITY.md](PLATFORM-PARITY.md). `ipad-dev` is the reference branch;
the promotion rules are in [PARITY-READY.md](PARITY-READY.md).

## Retained source repositories

| Repository | Retained state | Redirect commit |
|---|---|---|
| `neoworkssuite/Canvas-Mac` | Available, public, unarchived | `2ede1c6` |
| `neoworkssuite/Canvas_android` | Available, public, unarchived | `e3ba4b6` |
| `christianrobertson36/neoworks` | Available, private, unarchived | `02e3ddc` |

The notices redirect only future NeoCanvas work. No original repository,
branch, tag, or useful commit was deleted, archived, force-pushed, or replaced.
Private NeoWorks source was not published into the public canonical repository.

## Preservation references

| Repository | Reference | Preserved commit |
|---|---|---|
| `Canvas-Mac` | `archive/pre-consolidation-ipad` | `bbc11c370c0872effee19d8e676d86117b064673` |
| `Canvas-Mac` | `archive/pre-consolidation-macos` | `2c5d93a07446b47c7bcf87c147d76ff6dfec125d` |
| `Canvas-Mac` | `archive/pre-consolidation-windows` | `65120e86cf8a9f97f6873e597781938f30d31394` |
| `Canvas_android` | `archive/pre-consolidation-android` | `74176a28397205b33907ca9e8cb312bdd418875a` |
| `neoworks` | `archive/pre-consolidation-legacy-windows` | `803fc4ba359042a0843761286982b80e839395ae` |

## Source-to-canonical mapping

- The refreshed iPad source commit `bbc11c...` is an ancestor of canonical
  `ipad-dev`.
- Android source history is retained at canonical
  `imports/canvas-android-main`, whose source head is `74176a...`.
- Public Windows lineages remain reachable through their original canonical
  branch names; the selected integrated host is documented in
  [WINDOWS-MIGRATION.md](WINDOWS-MIGRATION.md).
- Private legacy Windows history stays in its private source repository behind
  `archive/pre-consolidation-legacy-windows` rather than being disclosed in the
  public canonical repository.

## Verification summary and blockers

All 35 local Python contract tests passed. Canonical CI passed shared, iPad,
macOS, Windows, and Android builds at the linked evidence commits. The final
iPad run also produced the reproducible Neo Nature Studio brush pack, iPad
simulator app, unsigned physical-device app, and smoke/visual evidence.

The outstanding product blockers are macOS native file integration and signed,
notarized macOS distribution. App Store, Windows, and Android production
signing credentials are release inputs and are not stored in the repository.
GitHub also reports that `actions/upload-artifact@v4` currently runs through a
Node 24 compatibility path; upgrading that action is maintenance, not a build
failure.

## Main-branch decision and next task

`main` remains at the preserved baseline rather than being advanced
automatically. This is intentional: the accepted baseline policy permits
documented gaps, but macOS document operations are still red in the parity
matrix and deserve an explicit product decision before promotion.

The exact next recommended development task is: implement and test native
macOS open, save, export, and brush/font import actions, then run the full
cross-platform matrix and promote the resulting accepted baseline from
`ipad-dev` to `main`.
