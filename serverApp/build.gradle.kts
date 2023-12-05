import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "1.9.21"
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.server.core.jvm)
            implementation(libs.ktor.server.websockets.jvm)
            implementation(libs.ktor.server.netty.jvm)
            implementation(libs.koin.annotations)
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.argraur.gasstation.ApplicationKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.argraur.gasstation"
            packageVersion = "1.0.0"
        }
    }

    dependencies {
        ksp(libs.koin.ksp.compiler)
    }
}
