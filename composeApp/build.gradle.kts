import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

configurations.configureEach {
    exclude(group = "androidx.vectordrawable", module = "vectordrawable-animated")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(projects.shared)
            export("com.arkivanov.decompose:decompose:3.0.2")
            export("org.jetbrains.kotlinx:atomicfu:0.23.2")
            linkerOpts.add("-lsqlite3")
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            binaryOptions["memoryModel"] = "experimental"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(compose.uiTooling)
            implementation(libs.places)
            implementation("io.ktor:ktor-client-okhttp:3.1.1")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            api(projects.shared)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("io.ktor:ktor-client-core:3.1.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
        }

        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("io.ktor:ktor-client-js:3.1.1")
        }

        iosMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            api("org.jetbrains.kotlinx:atomicfu:0.23.2")
            implementation(libs.ktor.client.darwin)
            implementation(libs.kotlinx.coroutines.core)
            api(projects.shared)
        }
    }
}

android {
    namespace = "org.walks.gamecopilot"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.walks.gamecopilot"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 5
        versionName = "1.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }

    val keystorePropertiesFile = rootProject.file("local.properties")
    if (keystorePropertiesFile.exists()) {
        val props = Properties().apply { load(keystorePropertiesFile.inputStream()) }
        val ksFile = props.getProperty("KEYSTORE_FILE")
        val ksPwd = props.getProperty("KEYSTORE_PASSWORD")
        val keyAlias = props.getProperty("KEY_ALIAS")
        val keyPwd = props.getProperty("KEY_PASSWORD")
        if (ksFile != null && ksPwd != null && keyAlias != null && keyPwd != null) {
            signingConfigs {
                create("release") {
                    storeFile = rootProject.file(ksFile)
                    storePassword = ksPwd
                    this.keyAlias = keyAlias
                    keyPassword = keyPwd
                }
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    signingConfig = signingConfigs.getByName("release")
                }
            }
        } else {
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
        }
    } else {
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}
