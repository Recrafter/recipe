package io.github.recrafter.recipe.configurators

import io.github.diskria.gradle.utils.extensions.common.buildGradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.extensions.setModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.recipe.configurations.ModConfiguration
import io.github.recrafter.recipe.configurators.common.AbstractConfigurator
import io.github.recrafter.recipe.extensions.configureMaven
import io.github.recrafter.recipe.extensions.dependencyRepositories
import io.github.recrafter.recipe.extensions.repositories
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ModConfigurator(val configuration: ModConfiguration) : AbstractConfigurator() {

    override fun configureRepositories(settings: Settings) = with(settings) {
        dependencyRepositories {
            configureMaven(
                name = MinecraftConstants.FULL_GAME_NAME,
                url = buildUrl("libraries.minecraft.net")
            )
            configureMaven(
                name = "SpongePowered",
                url = buildUrl("repo.spongepowered.org") {
                    path("repository", "maven-public")
                }
            )
            configureMaven(
                name = "Modrinth",
                url = buildUrl("api.modrinth.com") {
                    path("maven")
                },
                group = "maven.modrinth",
                includeSubgroups = false
            )
        }
        repositories {
            configureMaven(
                name = "Parchment",
                url = buildUrl("maven.parchmentmc.org")
            )
            configureMaven(
                name = ModLoaderType.FABRIC.displayName,
                url = buildUrl("maven.fabricmc.net")
            )
            configureMaven(
                name = ModLoaderType.QUILT.displayName,
                url = buildUrl("maven.quiltmc.org") {
                    path("repository", "release")
                }
            )
            configureMaven(
                name = ModLoaderType.LEGACY_FABRIC.displayName,
                url = buildUrl("maven.legacyfabric.net")
            )
            configureMaven(
                name = ModLoaderType.ORNITHE.displayName,
                url = buildUrl("maven.ornithemc.net") {
                    path("releases")
                }
            )
            configureMaven(
                name = ModLoaderType.BABRIC.displayName,
                url = buildUrl("maven.glass-launcher.net") {
                    path("babric")
                }
            )
            configureMaven(
                name = ModLoaderType.FORGE.displayName,
                url = buildUrl("maven.minecraftforge.net")
            )
            configureMaven(
                name = ModLoaderType.NEOFORGE.displayName,
                url = buildUrl("maven.neoforged.net") {
                    path("releases")
                }
            )
        }
    }

    override fun configureProjects(settings: Settings) = with(settings) {
        ModLoaderType.values().forEach { loader ->
            val loaderDirectoryName = loader.getName(`kebab-case`)
            val loaderDirectory = rootDirectory.resolve(loaderDirectoryName)
            val minecraftVersions = loaderDirectory.listDirectories().map { MinecraftVersion.parse(it.name) }
            minecraftVersions.forEach { minecraftVersion ->
                val versionProjectDirectoryName = minecraftVersion.asString()
                val versionProjectDirectory = loaderDirectory.resolve(versionProjectDirectoryName)
                val versionProjectPath = buildGradleProjectPath(loaderDirectoryName, versionProjectDirectoryName)
                include(versionProjectPath)

                configuration.requireEnvironment().sides.forEach { side ->
                    val sideProjectDirectoryName = side.getName()
                    val sideProjectDirectory = versionProjectDirectory.resolve(sideProjectDirectoryName)
                    if (sideProjectDirectory.exists()) {
                        val sideProjectPath = buildGradleProjectPath(versionProjectPath, sideProjectDirectoryName)
                        include(sideProjectPath)
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
