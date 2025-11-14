package io.github.recrafter.recipe.patches

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.ScriptHandlerScope
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.repositories

object BuildscriptPatches {

    /**
     * Applies workarounds for known plugin compatibility issues.
     * Currently, forces Loom-based builds to use a modern Gson version,
     * preventing reflection access errors on Java 17+.
     *
     * See: https://github.com/orgs/FabricMC/discussions/3546#discussioncomment-8345643
     */
    fun patchLoomGsonCompatibility(settings: Settings) {
        patch(settings) {
            repositories {
                mavenCentral()
            }
            dependencies {
                classpath("com.google.code.gson", "gson", "2.13.2")
            }
        }
    }

    private fun patch(settings: Settings, configure: ScriptHandlerScope.() -> Unit) {
        settings.gradle.beforeProject {
            buildscript {
                configure()
            }
        }
    }
}
