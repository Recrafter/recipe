package io.github.recrafter.recipe.extensions

import io.ktor.http.*
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven
import kotlin.also

@Suppress("UnstableApiUsage")
fun RepositoryHandler.configureMaven(
    name: String,
    url: Url,
    group: String? = null,
    includeSubgroups: Boolean = true,
): MavenArtifactRepository =
    maven(url.toString()) {
        this.name = name
    }.also { repository ->
        if (group == null) {
            return@also
        }
        exclusiveContent {
            forRepository {
                repository
            }
            filter {
                if (includeSubgroups) {
                    includeGroupAndSubgroups(group)
                } else {
                    includeGroup(group)
                }
            }
        }
    }
