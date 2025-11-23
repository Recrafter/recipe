package io.github.recrafter.recipe.extensions.gradle

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.gradle.GradleExtension
import io.github.recrafter.recipe.configurations.CrafterConfiguration
import io.github.recrafter.recipe.configurators.CrafterConfigurator
import io.github.recrafter.recipe.configurators.common.AbstractConfigurator

open class RecipeExtension : GradleExtension() {

    private var configurator: AbstractConfigurator? = null
    private var onConfiguratorReadyCallback: ((AbstractConfigurator) -> Unit)? = null

    fun crafter(configure: CrafterConfiguration.() -> Unit) {
        setConfigurator(CrafterConfigurator(CrafterConfiguration().apply(configure)))
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
