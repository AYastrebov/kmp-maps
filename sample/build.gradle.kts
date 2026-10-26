import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetBrains.compose)
    alias(libs.plugins.jetBrains.kotlin.multiplatform)
    alias(libs.plugins.jetBrains.kotlin.plugin.compose)
    alias(libs.plugins.buildkonfig)
}

val googleMapsApiKey = gradleLocalProperties(rootDir, providers).getProperty("MAPS_API_KEY") ?: ""

kotlin {
    jvmToolchain(17)
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Sample"
            isStatic = true
            binaryOption("bundleId", "com.swmansion.kmpmaps.sample")
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidX.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.jetBrains.androidX.lifecycle.runtimeCompose)
            implementation(libs.jetBrains.androidX.lifecycle.viewmodelCompose)
            implementation(compose.materialIconsExtended)
            implementation(project(":kmp-maps:core"))
            implementation(project(":kmp-maps:google-maps"))
        }
        commonTest.dependencies { implementation(libs.jetBrains.kotlin.test) }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jetBrains.kotlinX.coroutinesSwing)
            implementation(libs.kcef)
        }
    }
}

android {
    namespace = "com.swmansion.kmpmaps.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.swmansion.kmpmaps.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["MAPS_API_KEY"] = googleMapsApiKey
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { buildConfig = true }
}

compose.desktop {
    application {
        mainClass = "com.swmansion.kmpmaps.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.swmansion.kmpmaps.sample.MainKt"
            packageVersion = "1.0.0"
        }

        jvmArgs(
            "--add-opens",
            "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
        )
    }
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--add-opens",
        "java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens",
        "java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens",
        "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
    )
}

dependencies { debugImplementation(compose.uiTooling) }

buildkonfig {
    packageName = "com.swmansion.kmpmaps.sample"

    defaultConfigs { buildConfigField(STRING, "MAPS_API_KEY", googleMapsApiKey) }
}

tasks.named("compileKotlinMetadata") { dependsOn("generateBuildKonfig") }
