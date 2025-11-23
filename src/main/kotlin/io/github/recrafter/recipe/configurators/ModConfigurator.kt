package io.github.recrafter.recipe.configurators

import io.github.diskria.gradle.utils.extensions.common.buildGradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.kotlin.utils.extensions.asDirectoryOrNull
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.extensions.setModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.isInternalServer
import io.github.recrafter.recipe.configurations.ModConfiguration
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ModConfigurator(val configuration: ModConfiguration) : MavenReposConfigurator() {

    override fun configureProjects(settings: Settings) = with(settings) {
        val userEnvironment = configuration.requireEnvironment()
        ModLoaderType.values().forEach { loader ->
            val loaderName = loader.getName(`kebab-case`)
            rootDirectory.resolve(loaderName).asDirectoryOrNull()?.let { loaderDirectory ->
                val ranges = loaderDirectory.listDirectories().map { it.name to MinecraftVersionRange.parse(it.name) }
                ranges.forEach { (rangeDirectoryName, range) ->
                    loaderDirectory.resolve(rangeDirectoryName).asDirectoryOrNull()?.let { rangeDirectory ->
                        val sides = if (range.min.isInternalServer) listOf(ModSide.CLIENT) else userEnvironment.sides
                        sides.forEach { side ->
                            rangeDirectory.resolve(side.getName()).asDirectoryOrNull()?.let { sideDirectory ->
                                include(buildGradleProjectPath(loaderName, rangeDirectoryName, sideDirectory.name))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun configureRootProject(rootProject: Project) {
        rootProject.setModRecipe(
            ModRecipe(
                environment = configuration.requireEnvironment(),
            )
        )
    }
}
