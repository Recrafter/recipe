package io.github.recrafter.recipe.extensions

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings

fun Settings.pluginRepositories(block: RepositoryHandler.() -> Unit) {
    pluginManagement {
        repositories {
            block()
        }
    }
}

@Suppress("UnstableApiUsage")
fun Settings.dependencyRepositories(block: RepositoryHandler.() -> Unit) {
    dependencyResolutionManagement {
        repositories {
            block()
        }
    }
}

fun Settings.repositories(block: RepositoryHandler.() -> Unit) {
    pluginRepositories(block)
    dependencyRepositories(block)
}
