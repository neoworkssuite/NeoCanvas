import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val macAppStore = providers.gradleProperty("macAppStore")
    .map(String::toBoolean)
    .orElse(false)
val macDistribution = providers.gradleProperty("macDistribution")
    .orElse(macAppStore.map { if (it) "app-store" else "direct" })
val macAppStoreProfile = providers.gradleProperty("macAppStoreProfile")
val macSigningIdentity = providers.gradleProperty("macSigningIdentity")
    .orElse("christian robertson (5ZD52867Y7)")
val macSigningKeychain = providers.gradleProperty("macSigningKeychain")
    .orElse("${System.getProperty("user.home")}/Library/Keychains/login.keychain-db")

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":core"))
            implementation(project(":brushes"))
            implementation(project(":renderer"))
            implementation(project(":ui"))
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}

compose.desktop {
    application {
        mainClass = "com.neoworksuite.neocanvas.MainKt"
        jvmArgs += "-Dneocanvas.distribution=${macDistribution.get()}"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg)
            packageName = "NeoCanvas"
            packageVersion = "1.0.0"
            vendor = "NeoWorksSuite"
            description = "Professional raster drawing and illustration for Mac."
            copyright = "Copyright © 2026 NeoWorksSuite"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("packaging/resources"))
            macOS {
                bundleID = "com.neoworksuite.neocanvas"
                packageBuildVersion = "12"
                pkgPackageBuildVersion = "12"
                appCategory = "public.app-category.graphics-design"
                minimumSystemVersion = "13.0"
                iconFile.set(project.file("../assets/branding/macos/NeoCanvas.icns"))
                if (macAppStore.get()) {
                    appStore = true
                    entitlementsFile.set(project.file("packaging/macos/NeoCanvas.entitlements"))
                    runtimeEntitlementsFile.set(project.file("packaging/macos/NeoCanvasRuntime.entitlements"))
                    provisioningProfile.set(file(macAppStoreProfile.get()))
                    runtimeProvisioningProfile.set(file(macAppStoreProfile.get()))
                    signing {
                        sign.set(true)
                        identity.set(macSigningIdentity)
                        keychain.set(macSigningKeychain)
                    }
                }
            }
        }
    }
}
