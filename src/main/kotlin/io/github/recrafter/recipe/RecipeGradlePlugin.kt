package io.github.recrafter.recipe

import io.github.diskria.gradle.utils.extensions.registerExtension
import io.github.diskria.gradle.utils.extensions.saveDependencyResolutionRepositories
import io.github.recrafter.recipe.extensions.gradle.RecipeExtension
import io.github.recrafter.recipe.patches.BuildscriptPatches
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class RecipeGradlePlugin : Plugin<Settings> {

    override fun apply(settings: Settings) = with(settings) {
        BuildscriptPatches.patchLoomGsonCompatibility(settings)

        val extension = registerExtension<RecipeExtension>()
        extension.onConfiguratorReady { configurator ->
            configurator.configure(this)
            gradle.rootProject {
                saveDependencyResolutionRepositories(this)
                configurator.configureRootProject(this)
            }
        }
        gradle.settingsEvaluated {
            extension.ensureConfigured()
        }
    }
}
