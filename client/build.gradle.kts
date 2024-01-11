@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version "1.9.21"
}

group = "dev.argraur.gasstation"
version = "1.0"

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64()
        hostOs == "Mac OS X" && !isArm64 -> macosX64()
        hostOs == "Linux" && isArm64 -> linuxArm64()
        hostOs == "Linux" && !isArm64 -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            if (isMingwX64) {
                executable {
                    entryPoint = "main_win"
                }
            } else {
                executable {
                    entryPoint = "main_unix"
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
            }
        }
        if (isMingwX64) {
            val mingwMain by getting {
                dependencies {
                    implementation(libs.ktor.client.winhttp)
                }
            }
        } else {
            val unixMain by creating {
                dependsOn(nativeMain)
                dependencies {
                    implementation(libs.ktor.client.cio)
                }
            }
            val linuxMain by getting {
                dependsOn(unixMain)
            }
            //val macosMain by getting {
            //    dependsOn(unixMain)
            //}
        }
    }
}