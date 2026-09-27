from pathlib import Path


def verify_macos_host(root: Path) -> list[str]:
    errors: list[str] = []
    settings = (root / "settings.gradle.kts")
    build = root / "macosApp/build.gradle.kts"
    main = root / "macosApp/src/jvmMain/kotlin/com/neoworksuite/neocanvas/Main.kt"
    workflow = root / ".github/workflows/macos-build.yml"
    entitlements = root / "macosApp/packaging/macos/NeoCanvas.entitlements"
    runtime_entitlements = root / "macosApp/packaging/macos/NeoCanvasRuntime.entitlements"
    privacy_manifest = root / "macosApp/packaging/resources/macos/PrivacyInfo.xcprivacy"
    icon = root / "assets/branding/macos/NeoCanvas.icns"

    if not settings.is_file() or '":macosApp"' not in settings.read_text(encoding="utf-8"):
        errors.append("macOS module is not registered")
    build_text = build.read_text(encoding="utf-8") if build.is_file() else ""
    if 'project(":ui")' not in build_text:
        errors.append("macOS module is not linked to shared UI")
    if "TargetFormat.Dmg" not in build_text or "bundleID" not in build_text:
        errors.append("macOS bundle metadata or DMG packaging is missing")
    if "TargetFormat.Pkg" not in build_text or "appStore = true" not in build_text:
        errors.append("macOS App Store PKG packaging is missing")
    if "packageBuildVersion" not in build_text:
        errors.append("macOS App Store build number is missing")
    if not entitlements.is_file() or not runtime_entitlements.is_file():
        errors.append("macOS App Sandbox entitlements are missing")
    else:
        entitlement_text = entitlements.read_text(encoding="utf-8")
        if "com.apple.security.app-sandbox" not in entitlement_text:
            errors.append("macOS App Sandbox entitlement is missing")
        if "com.apple.security.files.user-selected.read-write" not in entitlement_text:
            errors.append("macOS user-selected file entitlement is missing")
    if not privacy_manifest.is_file():
        errors.append("macOS privacy manifest is missing")
    if not icon.is_file():
        errors.append("macOS branded icon is missing")
    main_text = main.read_text(encoding="utf-8") if main.is_file() else ""
    if "MenuBar" not in main_text:
        errors.append("macOS host menu is missing")
    if "NeoCanvasApp" not in main_text:
        errors.append("macOS host does not launch shared UI")
    workflow_text = workflow.read_text(encoding="utf-8") if workflow.is_file() else ""
    if "macos-latest" not in workflow_text or ":macosApp:packageDmg" not in workflow_text:
        errors.append("macOS workflow does not package the native DMG")
    return errors


if __name__ == "__main__":
    import sys

    findings = verify_macos_host(Path.cwd())
    for finding in findings:
        print(f"ERROR: {finding}")
    raise SystemExit(1 if findings else 0)
