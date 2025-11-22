package io.github.recrafter.recipe.configurators.common

import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.recrafter.recipe.extensions.configureMaven
import io.github.recrafter.recipe.extensions.pluginRepositories
import io.github.recrafter.recipe.extensions.repositories
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.internal.Actions.with

open class AbstractConfigurator {

    fun configure(settings: Settings) {
        applyCommonConfiguration(settings)
        configureRepositories(settings)
        configureProjects(settings)
    }

    open fun configureRootProject(rootProject: Project) {

    }

    protected open fun configureRepositories(settings: Settings) {

    }

    protected open fun configureProjects(settings: Settings) {

    }

    private fun applyCommonConfiguration(settings: Settings) = with(settings) {
        repositories {
            configureMaven(
                "MavenCentralMirror",
                buildUrl("repo1.maven.org") {
                    path("maven2")
                }
            )
            mavenCentral()
        }
        pluginRepositories {
            gradlePluginPortal()
        }
    }
}
