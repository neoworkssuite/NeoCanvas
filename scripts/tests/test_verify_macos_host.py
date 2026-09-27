import tempfile
import unittest
from pathlib import Path

from scripts.verify_macos_host import verify_macos_host


class MacOSHostContractTest(unittest.TestCase):
    def test_complete_host_contract(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "settings.gradle.kts").write_text('include(":macosApp")')
            (root / "macosApp/src/jvmMain/kotlin/com/neoworksuite/neocanvas").mkdir(parents=True)
            (root / "macosApp/src/jvmMain/kotlin/com/neoworksuite/neocanvas/Main.kt").write_text(
                "fun main() = application { Window { NeoCanvasApp() }; MenuBar {} }"
            )
            (root / "macosApp/build.gradle.kts").write_text(
                'implementation(project(":ui")); targetFormats(TargetFormat.Dmg, TargetFormat.Pkg); '
                'bundleID = "com.neoworksuite.neocanvas"; packageBuildVersion = "12"; appStore = true'
            )
            packaging = root / "macosApp/packaging/macos"
            packaging.mkdir(parents=True)
            (packaging / "NeoCanvas.entitlements").write_text(
                "com.apple.security.app-sandbox com.apple.security.files.user-selected.read-write"
            )
            (packaging / "NeoCanvasRuntime.entitlements").write_text("runtime")
            privacy = root / "macosApp/packaging/resources/macos/PrivacyInfo.xcprivacy"
            privacy.parent.mkdir(parents=True)
            privacy.write_text("privacy")
            icon = root / "assets/branding/macos/NeoCanvas.icns"
            icon.parent.mkdir(parents=True)
            icon.write_bytes(b"icns")
            (root / ".github/workflows").mkdir(parents=True)
            (root / ".github/workflows/macos-build.yml").write_text(
                "runs-on: macos-latest\nrun: ./gradlew :macosApp:packageDmg\n"
            )
            self.assertEqual([], verify_macos_host(root))

    def test_missing_host_boundaries_are_reported(self):
        with tempfile.TemporaryDirectory() as directory:
            errors = verify_macos_host(Path(directory))
            self.assertTrue(any("module" in error.lower() for error in errors))
            self.assertTrue(any("workflow" in error.lower() for error in errors))
            self.assertTrue(any("menu" in error.lower() for error in errors))

    def test_missing_app_store_packaging_is_reported(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "settings.gradle.kts").write_text('include(":macosApp")')
            (root / "macosApp/src/jvmMain/kotlin/com/neoworksuite/neocanvas").mkdir(parents=True)
            (root / "macosApp/src/jvmMain/kotlin/com/neoworksuite/neocanvas/Main.kt").write_text(
                "fun main() = application { Window { NeoCanvasApp() }; MenuBar {} }"
            )
            (root / "macosApp/build.gradle.kts").write_text(
                'implementation(project(":ui")); targetFormats(TargetFormat.Dmg); '
                'bundleID = "com.neoworksuite.neocanvas"'
            )
            (root / ".github/workflows").mkdir(parents=True)
            (root / ".github/workflows/macos-build.yml").write_text(
                "runs-on: macos-latest\nrun: ./gradlew :macosApp:packageDmg\n"
            )

            errors = verify_macos_host(root)

            self.assertTrue(any("app store" in error.lower() for error in errors), errors)
            self.assertTrue(any("sandbox" in error.lower() for error in errors), errors)
            self.assertTrue(any("privacy" in error.lower() for error in errors), errors)
            self.assertTrue(any("icon" in error.lower() for error in errors), errors)


if __name__ == "__main__":
    unittest.main()
