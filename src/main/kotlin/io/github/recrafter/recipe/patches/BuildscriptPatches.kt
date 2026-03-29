package io.github.recrafter.recipe.patches

import org.gradle.api.initialization.Settings

object BuildscriptPatches {

    /**
     * Applies workarounds for known plugin compatibility issues.
     * Currently, forces Loom-based builds to use a modern Gson version,
     * preventing reflection access errors on Java 17+.
     *
     * See: https://github.com/orgs/FabricMC/discussions/3546#discussioncomment-8345643
     */
    fun patchLoomGsonCompatibility(settings: Settings) {
        settings.gradle.beforeProject {
            buildscript.repositories.apply {
                mavenCentral()
            }
            buildscript.dependencies.apply {
                add("classpath", "com.google.code.gson:gson:2.13.2")
            }
        }
    }
}
