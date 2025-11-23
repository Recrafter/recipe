package io.github.recrafter.recipe.configurators

import io.github.diskria.gradle.utils.extensions.common.buildGradleProjectPath
import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.saveDependencyResolutionRepositories
import io.github.diskria.kotlin.utils.extensions.asDirectoryOrNull
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.splitToPairOrNull
import io.github.diskria.kotlin.utils.extensions.toBooleanOrNull
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.extensions.setModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.isInternalServer
import io.github.recrafter.recipe.configurations.CrafterConfiguration
import io.github.recrafter.recipe.configurators.common.AbstractConfigurator
import io.github.recrafter.recipe.extensions.configureMaven
import io.github.recrafter.recipe.extensions.dependencyRepositories
import io.github.recrafter.recipe.extensions.repositories
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import java.io.File

class CrafterConfigurator(val configuration: CrafterConfiguration) : AbstractConfigurator() {

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
        }
        repositories {
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
        if (configuration.isCraftingCrafters) {
            return@with
        }
        if (System.getProperty("drift")?.toBooleanOrNull() == true) {
            println("[Crafter] Running in drift mode")
            val loaderName = System.getProperty("loader")
            val isFuture = System.getProperty("future").toBoolean()
            val projects = rootDirectory.resolve(loaderName).listDirectories().mapNotNull {
                val (min, max) = it.name.splitToPairOrNull("--") ?: return@mapNotNull null
                if (isFuture && max != "drift" || !isFuture && min != "drift") {
                    return@mapNotNull null
                }
                val versionString = if (isFuture) min else max
                val version = MinecraftVersion.parseOrNull(versionString) ?: return@mapNotNull null
                it to version
            }
            val (projectDirectory, version) = projects.singleOrNull()
                ?: gradleError("Only single directory for drifting possible per loader")
            println("[Crafter] Detected version for drift: ${version.asString()}")
            includeSideProjects(settings, loaderName, projectDirectory, version)
            return@with
        }
        ModLoaderType.values().forEach { loader ->
            val loaderName = loader.getName(`kebab-case`)
            rootDirectory.resolve(loaderName).asDirectoryOrNull()?.let { loaderDirectory ->
                val ranges = loaderDirectory.listDirectories().map { it.name to MinecraftVersionRange.parse(it.name) }
                ranges.forEach { (rangeDirectoryName, range) ->
                    loaderDirectory.resolve(rangeDirectoryName).asDirectoryOrNull()?.let { rangeDirectory ->
                        includeSideProjects(settings, loaderName, rangeDirectory, range.min)
                    }
                }
            }
        }
    }

    override fun configureRootProject(settings: Settings, rootProject: Project) = with(rootProject) {
        if (configuration.isCraftingCrafters) {
            return
        }
        settings.saveDependencyResolutionRepositories(this)
        setModRecipe(
            ModRecipe(
                environment = configuration.requireEnvironment(),
            )
        )
    }

    private fun includeSideProjects(
        settings: Settings,
        loaderName: String,
        versionDirectory: File,
        version: MinecraftVersion,
    ) = with(settings) {
        val userEnvironment = configuration.requireEnvironment()
        val sides = if (version.isInternalServer) listOf(ModSide.CLIENT) else userEnvironment.sides
        sides.forEach { side ->
            versionDirectory.resolve(side.getName()).asDirectoryOrNull()?.let { sideDirectory ->
                include(buildGradleProjectPath(loaderName, versionDirectory.name, sideDirectory.name))
            }
        }
    }
}
