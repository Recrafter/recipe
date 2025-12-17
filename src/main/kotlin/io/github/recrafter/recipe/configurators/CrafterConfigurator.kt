package io.github.recrafter.recipe.configurators

import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.saveDependencyResolutionRepositories
import io.github.diskria.kotlin.utils.extensions.asDirectory
import io.github.diskria.kotlin.utils.extensions.asDirectoryOrNull
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.wrapWithSingleQuote
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.bedrock.extensions.setModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.recipe.configurations.CrafterConfiguration
import io.github.recrafter.recipe.configurators.common.PluginConfigurator
import io.github.recrafter.recipe.extensions.configureMaven
import io.github.recrafter.recipe.extensions.dependencyRepositories
import io.github.recrafter.recipe.extensions.repositories
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import java.io.File

class CrafterConfigurator(val configuration: CrafterConfiguration) : PluginConfigurator() {

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
                name = ModLoaderType.BABRIC.displayName,
                url = buildUrl("maven.glass-launcher.net") {
                    path("babric")
                }
            )
            configureMaven(
                name = ModLoaderType.ORNITHE.displayName,
                url = buildUrl("maven.ornithemc.net") {
                    path("releases")
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
        when (val flow = CrafterFlow.detect()) {
            is CrafterFlow.Normal -> {
                ModLoaderType.values().forEach { loader ->
                    val loaderDirectory = rootDirectory
                        .resolve(loader.getName(`kebab-case`))
                        .asDirectoryOrNull()
                        ?: return@forEach
                    val ranges = loaderDirectory.listDirectories().mapNotNull { modDirectory ->
                        val range = MinecraftVersionRange.parseOrNull(
                            modDirectory.name,
                            MinecraftVersionRange.PROJECT_NAME_SEPARATOR
                        )
                        if (range == null) {
                            val modProjectPath = gradleProjectPath(loaderDirectory.name, modDirectory.name)
                            println(
                                "Skipping mod project ${modProjectPath.wrapWithSingleQuote()}: " +
                                        "invalid Minecraft version range."
                            )
                            return@mapNotNull null
                        }
                        modDirectory to range
                    }
                    val allSupportedVersions = mutableSetOf<MinecraftVersion>()
                    ranges.forEach { (modDirectory, range) ->
                        val supportedVersions = range.expand()
                        val overlappingVersions = supportedVersions.intersect(allSupportedVersions)
                        requireGradle(overlappingVersions.isEmpty()) {
                            val modProjectPath = gradleProjectPath(loaderDirectory.name, modDirectory.name)
                            buildString {
                                appendLine(
                                    "Mod project ${modProjectPath.wrapWithSingleQuote()} targets Minecraft versions " +
                                            "${range.asString()}, which overlap with another mod's range."
                                )
                                appendLine(
                                    "Overlapping versions: ${overlappingVersions.joinToString { it.asString() }}."
                                )
                            }
                        }
                        includeModProject(settings, loader, modDirectory)
                        allSupportedVersions.addAll(supportedVersions)
                    }
                }
            }

            is CrafterFlow.Single -> {
                val loader = flow.loader
                val modProjectName = flow.modProjectName
                val loaderName = loader.getName(`kebab-case`)
                val modDirectory = rootDirectory.resolve(loaderName).resolve(modProjectName).asDirectory()
                includeModProject(settings, loader, modDirectory)
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

    private fun includeModProject(settings: Settings, loader: ModLoaderType, modDirectory: File) = with(settings) {
        val sides = configuration.requireEnvironment().sides
        sides.forEach { side ->
            modDirectory.resolve(side.getName()).asDirectoryOrNull()?.let { sideDirectory ->
                include(gradleProjectPath(loader.getName(`kebab-case`), modDirectory.name, sideDirectory.name))
            }
        }
    }
}
