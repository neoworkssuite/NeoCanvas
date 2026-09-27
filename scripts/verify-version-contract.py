#!/usr/bin/env python3
"""Validate NeoCanvas product-version and documentation consistency."""

from __future__ import annotations

import argparse
import re
from pathlib import Path


SEMVER_RE = re.compile(
    r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)"
    r"(?:[-+][0-9A-Za-z.-]+)?$"
)


def verify_version_contract(root: Path) -> list[str]:
    errors: list[str] = []
    version_path = root / "VERSION"
    versioning_path = root / "docs" / "VERSIONING.md"
    readme_path = root / "README.md"
    build_sources = {
        "Apple": (
            root / "iosApp" / "Configuration" / "Config.xcconfig",
            re.compile(r"CURRENT_PROJECT_VERSION\s*=\s*(\d+)"),
        ),
        "Android": (
            root / "androidApp" / "build.gradle.kts",
            re.compile(r"versionCode\s*=\s*(\d+)"),
        ),
        "Windows": (
            root / "windowsApp" / "src" / "jvmMain" / "kotlin" / "com" / "neoworksuite" / "neocanvas" / "platform" / "WindowsLaunchContract.kt",
            re.compile(r"buildNumber\s*=\s*(\d+)"),
        ),
    }
    for path in (version_path, versioning_path, readme_path, *(source[0] for source in build_sources.values())):
        if not path.is_file():
            errors.append(f"missing required version file: {path.relative_to(root)}")
    if errors:
        return errors

    version = version_path.read_text(encoding="utf-8").strip()
    if not SEMVER_RE.fullmatch(version):
        errors.append(f"VERSION must contain valid SemVer, got {version!r}")

    versioning = versioning_path.read_text(encoding="utf-8")
    if f"Product version: `{version}`" not in versioning:
        errors.append("documented product version does not match VERSION")
    for platform, (source_path, source_pattern) in build_sources.items():
        match = re.search(rf"{platform} build:\s*`([^`]+)`", versioning)
        if not match or not match.group(1).isdigit() or int(match.group(1)) < 1:
            errors.append(f"{platform} build must be a positive numeric value")
            continue
        source_match = source_pattern.search(source_path.read_text(encoding="utf-8"))
        if not source_match:
            errors.append(f"{platform} native build value is missing")
        elif int(match.group(1)) != int(source_match.group(1)):
            errors.append(
                f"{platform} build does not match native metadata: "
                f"documented {match.group(1)}, native {source_match.group(1)}"
            )

    readme = readme_path.read_text(encoding="utf-8")
    required_readme = (
        "iPad",
        "macOS",
        "Windows",
        "Android",
        "PARITY-READY",
        "docs/MIGRATION-AUDIT.md",
        "docs/PLATFORM-PARITY.md",
        "docs/VERSIONING.md",
    )
    missing = [value for value in required_readme if value not in readme]
    if missing:
        errors.append(f"README is missing platform/policy links: {', '.join(missing)}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("root", nargs="?", type=Path, default=Path(__file__).parents[1])
    args = parser.parse_args()
    errors = verify_version_contract(args.root.resolve())
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    print("OK: NeoCanvas version contract")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
