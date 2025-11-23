package io.github.recrafter.recipe.configurations

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.recrafter.bedrock.sides.ModEnvironment

open class CrafterConfiguration {

    internal var isCraftingCrafters: Boolean = false

    /**
     * The target environment where your mod will run.
     *
     * Must be explicitly selected using one of:
     * [clientAndServer], [clientOnly], [serverOnly], or [dedicatedServerOnly].
     */
    internal var environment: ModEnvironment? = null

    /**
     * Marks this mod as **client–server**.
     *
     * Use this if your mod includes logic for both sides —
     * the client handles visuals or input, while the server performs
     * actual world or gameplay changes.
     * Both parts work together to form a complete feature.
     *
     * **Example:**
     * The player presses **Enter** while standing on a cobblestone block.
     * The server replaces the block with grass,
     * and the client displays a small burst of heart particles above it.
     */
    fun clientAndServer() {
        environment = ModEnvironment.CLIENT_SERVER
    }

    /**
     * Marks this mod as **client-only**.
     *
     * Use this if your mod runs exclusively on the client side —
     * for example, adds UI elements, HUDs, visual effects, or sound features.
     * Works both in singleplayer and while connected to multiplayer servers,
     * but does not affect gameplay or world logic.
     *
     * **Example:**
     * The player looks at a grass block, and only on their screen
     * it visually appears as cobblestone.
     * The actual block in the world does not change.
     */
    fun clientOnly() {
        environment = ModEnvironment.CLIENT_ONLY
    }

    /**
     * Marks this mod as **server-only**.
     *
     * Use this if your mod logic runs entirely on the server —
     * it affects gameplay or world rules, but has no visual elements.
     * Works both in singleplayer and on dedicated servers.
     *
     * **Example:**
     * When a player steps onto a grass block,
     * the server instantly turns it into cobblestone for all players.
     */
    fun serverOnly() {
        environment = ModEnvironment.SERVER_ONLY
    }

    /**
     * Marks this mod as **dedicated-server-only**.
     *
     * Use this only for administrative or infrastructure mods
     * designed to run exclusively on dedicated servers —
     * such as permissions, chat management, or automation tools.
     * These mods will not load in singleplayer.
     *
     * **Example:**
     * An administrator executes a console command,
     * and the server replaces all grass blocks under every player
     * with cobblestone.
     */
    fun dedicatedServerOnly() {
        environment = ModEnvironment.DEDICATED_SERVER_ONLY
    }

    fun craftingCrafters() {
        isCraftingCrafters = true
    }

    internal fun requireEnvironment(): ModEnvironment =
        environment ?: gradleError(
            """
            Mod environment not set.
            Please specify it inside recipe.mod { ... } using one of:
              - clientAndServer()
              - clientOnly()
              - serverOnly()
              - dedicatedServerOnly()
            """.trimIndent()
        )
}
