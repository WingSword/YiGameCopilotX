import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    androidLibrary {
        namespace = "org.walks.gamecopilot.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.jetbrains.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.napier)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
        }

        androidMain.dependencies {
            implementation(libs.androidx.room.ktx)
            implementation(libs.io.ktor.ktor.client.android11)
            implementation(libs.ktor.client.cio)
            implementation("io.ktor:ktor-server-core:3.3.3")
            implementation("io.ktor:ktor-server-netty:3.3.3")
            implementation("io.ktor:ktor-server-websockets:3.3.3")
            implementation(libs.places)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.3.3")
        }
    }
}


