# macOS parity

## Host

NeoCanvas now has a dedicated Compose Desktop macOS host in `macosApp`. It
packages a signed-ready `.app` inside a DMG, supplies the macOS application
lifecycle and menu bar, and renders the same shared `core`, `brushes`,
`renderer`, and `ui` modules used by the reference iPad implementation.

The preserved `apple-platform` head
`2c5d93a07446b47c7bcf87c147d76ff6dfec125d` is already an ancestor of the iPad
lineage. It contained iPad/UIKit work, not a macOS host, so it remains lineage
evidence rather than code imported over newer shared behavior.

## Status

| Capability | Status | Evidence |
|---|---|---|
| Shared canvas and editor UI | ✅ | Canonical run `36236949475` passed shared tests at `2c3b1d9` |
| App lifecycle and menu | ✅ | Canonical run `36236949475` compiled the dedicated host at `2c3b1d9` |
| DMG package | ✅ | Canonical run `36236949475` packaged and uploaded the DMG |
| Native open/save/export panels | ✅ | `MacEditorFileActions` provides local Gallery storage, native file dialogs, recovery, versions, imports and professional exports |
| Apple Pencil/touch input | N/A | macOS uses desktop pointer/tablet input rather than iPad touch APIs |
| Mac App Store signing/notarisation | 🔴 | Requires distribution certificates and store configuration |

The macOS host now uses a dedicated local file-action bridge. It stores its
library under `~/Library/Application Support/NeoCanvas`, uses native AWT-backed
macOS file dialogs, and supports NeoCanvas documents, recovery, versions,
images, PSD, brushes, fonts, PNG, JPEG, PDF, TIFF and PSD export. Store signing
and notarisation remain separate release work.
