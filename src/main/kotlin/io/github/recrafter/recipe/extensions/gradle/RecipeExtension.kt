package io.github.recrafter.recipe.extensions.gradle

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.gradle.GradleExtension
import io.github.recrafter.recipe.configurations.ModConfiguration
import io.github.recrafter.recipe.configurators.ModConfigurator
import io.github.recrafter.recipe.configurators.common.AbstractConfigurator

open class RecipeExtension : GradleExtension() {

    private var configurator: AbstractConfigurator? = null
    private var onConfiguratorReadyCallback: ((AbstractConfigurator) -> Unit)? = null

    fun mod(configure: ModConfiguration.() -> Unit) {
        setConfigurator(ModConfigurator(ModConfiguration().apply(configure)))
    }

    fun onConfiguratorReady(callback: (AbstractConfigurator) -> Unit) {
        onConfiguratorReadyCallback = callback
    }

    fun ensureConfigured() {
        configurator ?: notConfiguredError()
    }

    protected fun setConfigurator(configurator: AbstractConfigurator) {
        if (this.configurator != null) {
            alreadyConfiguredError()
        }
        this.configurator = configurator
        onConfiguratorReadyCallback?.invoke(configurator)
    }

    private fun notConfiguredError(): Nothing =
        gradleError("Recipe not configured")

    private fun alreadyConfiguredError(): Nothing =
        gradleError("Recipe already configured")
}
