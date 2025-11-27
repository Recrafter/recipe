package io.github.recrafter.recipe.configurators

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.saveDependencyResolutionRepositories
import io.github.diskria.kotlin.utils.extensions.asDirectoryOrNull
import io.github.diskria.kotlin.utils.extensions.common.buildUrl
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.listDirectories
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.extensions.splitToPairOrNull
import io.github.diskria.kotlin.utils.properties.autoNamedProperty
import io.github.recrafter.bedrock.MinecraftConstants
import io.github.recrafter.bedrock.crafter.CrafterBisectDirection
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.bedrock.extensions.setModRecipe
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.recipes.ModRecipe
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.MinecraftVersion
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.bedrock.versions.isInternalServer
import io.github.recrafter.recipe.configurations.CrafterConfiguration
import io.github.recrafter.recipe.configurators.common.PluginConfigurator
import io.github.recrafter.recipe.extensions.configureMaven
import io.github.recrafter.recipe.extensions.dependencyRepositories
import io.github.recrafter.recipe.extensions.repositories
import io.ktor.http.*
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.internal.extensions.core.extra
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
                    getModDirectories(loader.getName(`kebab-case`))
                        .mapNotNull { modDirectory ->
                            val versionRange = MinecraftVersionRange.parseOrNull(
                                modDirectory.name,
                                RANGE_SEPARATOR
                            ) ?: return@mapNotNull null
                            modDirectory to versionRange
                        }
                        .forEach { (modDirectory, range) ->
                            includeModProject(settings, loader, modDirectory, range.min)
                        }
                }
            }

            is CrafterFlow.Single -> {
                val loader = flow.loader
                val version = flow.version
                val loaderName = loader.getName(`kebab-case`)
                val modDirectory = rootDirectory.resolve(loaderName).resolve(version.asString()).ensureDirectoryExists()
                includeModProject(settings, loader, modDirectory, version)
            }

            is CrafterFlow.Bisect -> {
                val loader = flow.loader
                val direction = flow.direction
                val loaderName = loader.getName(`kebab-case`)
                val pendingDirectories = getModDirectories(loaderName, isBisect = true).mapNotNull { modDirectory ->
                    val targetVersionName = when (direction) {
                        CrafterBisectDirection.FUTURE -> modDirectory.name.substringBefore(RANGE_SEPARATOR)
                        CrafterBisectDirection.PAST -> modDirectory.name.substringAfter(RANGE_SEPARATOR)
                    }
                    val targetVersion = MinecraftVersion.parseOrNull(targetVersionName) ?: return@mapNotNull null
                    modDirectory to targetVersion
                }
                val (modDirectory, targetVersion) = pendingDirectories.singleOrNull()
                    ?: gradleError(
                        "Only one pending bisect directory is allowed per loader and direction, " +
                                "but found: ${pendingDirectories.joinToString { it.first.name }}"
                    )
                gradle.rootProject {
                    val bisectTarget by targetVersion.autoNamedProperty()
                    extra[bisectTarget.name] = bisectTarget.value
                }
                includeModProject(settings, loader, modDirectory, targetVersion)
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

    private fun includeModProject(
        settings: Settings,
        loader: ModLoaderType,
        modDirectory: File,
        version: MinecraftVersion,
    ) = with(settings) {
        val sides = when {
            version.isInternalServer -> listOf(ModSide.CLIENT)
            else -> configuration.requireEnvironment().sides
        }
        sides.forEach { side ->
            modDirectory.resolve(side.getName()).asDirectoryOrNull()?.let { sideDirectory ->
                include(gradleProjectPath(loader.getName(`kebab-case`), modDirectory.name, sideDirectory.name))
            }
        }
    }

    private fun Settings.getModDirectories(loaderName: String, isBisect: Boolean = false): List<File> =
        rootDirectory
            .resolve(loaderName)
            .asDirectoryOrNull()
            ?.listDirectories()
            ?.filter {
                val isBisectDirectory = it.name
                    .splitToPairOrNull(RANGE_SEPARATOR)
                    ?.toList()
                    ?.contains(CrafterFlow.Bisect.name) == true
                if (isBisect) isBisectDirectory
                else !isBisectDirectory
            }
            .orEmpty()

    companion object {
        private const val RANGE_SEPARATOR: String = "--"
    }
}
