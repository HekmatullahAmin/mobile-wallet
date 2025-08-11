package org.mifospay

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.text.get

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKotlinMultiplatform() {
    // Pick up a flag from env or a Gradle property
    val deviceOnlyIos: Boolean = providers.environmentVariable("KMP_IOS_DEVICE_ONLY")
        .orElse(providers.gradleProperty("kmp.ios.deviceOnly"))
        .orElse("false")
        .map { it.equals("true", ignoreCase = true) || it == "1" }
        .get()

    logger.lifecycle("[KMP] deviceOnlyIos=$deviceOnlyIos for project ${project.path}")
    extensions.configure<KotlinMultiplatformExtension> {
        applyProjectHierarchyTemplate()

        jvm("desktop")
        androidTarget()
        // iOS – conditional targets
        if (deviceOnlyIos) {
            iosArm64()              // ✅ device only (for archives / CI)
        } else {
            iosArm64()              // device
            iosSimulatorArm64()     // Apple Silicon simulator
            iosX64()                // Intel simulator (keep if any devs still on Intel)
        }
//        iosSimulatorArm64()
//        iosX64()
//        iosArm64()
        js(IR) {
            this.nodejs()
            binaries.executable()
        }
        wasmJs() {
            browser()
            nodejs()
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}