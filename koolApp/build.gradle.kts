// KMP : Desktop (JVM) + JS (browser), rendered by kool engine.

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

val appGroup = providers.gradleProperty("app.group").get()
val appRootName = providers.gradleProperty("app.rootName").get()
val appDisplayName = providers.gradleProperty("app.displayName").get()
val appVersion = providers.gradleProperty("app.version").get()
val isMacOs = System.getProperty("os.name").orEmpty().contains("mac", ignoreCase = true)

group = appGroup
version = appVersion

kotlin {
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("$appGroup.MainKt")
                applicationDefaultJvmArgs = buildList {
                    add("--add-opens=java.base/java.lang=ALL-UNNAMED")
                    add("--enable-native-access=ALL-UNNAMED")
                    if (isMacOs) {
                        // GLFW must run on the macOS main thread.
                        add("-XstartOnFirstThread")
                        add("-Dapple.awt.application.name=$appDisplayName")
                        add("-Xdock:name=$appDisplayName")
                    }
                }
            }
        }
    }
    jvmToolchain(25)

    js {
        outputModuleName.set(appRootName)
        browser {
            commonWebpackConfig {
                // Fixed bundle name so index.html need not change when ./configure.sh proj_name renames the project.
                outputFileName = "koolApp.js"
                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kool.core)
        }
    }
}
