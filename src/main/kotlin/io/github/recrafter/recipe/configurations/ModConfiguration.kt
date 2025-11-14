package io.github.recrafter.recipe.configurations

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.recrafter.bedrock.sides.ModEnvironment

open class ModConfiguration {

    /**
     * The target environment where your mod will run.
     *
     * Must be explicitly selected using one of:
     * [clientOnly], [clientServer], or [dedicatedServerOnly].
     */
    var environment: ModEnvironment? = null

    /**
     * Marks this mod as **client-only**.
     *
     * Use this if your mod runs exclusively on the client side —
     * for example, adds UI elements, HUDs, visual effects, or sound features.
     *
     * Client-only mods work both in **singleplayer** and when **connecting to servers**,
     * as long as they don’t modify server-side logic or data.
     *
     * Such mods do **not** run on dedicated servers.
     *
     * **Example:**
     * When Alex looks at a grass block, it visually appears as a cobblestone block
     * on his screen — but only for him. The block is not actually changed in the world.
     */
    fun clientOnly() {
        environment = ModEnvironment.CLIENT
    }

    /**
     * Marks this mod as **client–server**.
     *
     * Use this for mods that include both client and server logic,
     * or if your mod contains only server logic but should still
     * function in singleplayer worlds.
     *
     * This is the most common and recommended option for general mods.
     *
     * **Example:**
     * When Steve walks over a grass block, it is actually replaced
     * with cobblestone in the world — visible to all players.
     */
    fun clientServer() {
        environment = ModEnvironment.CLIENT_SERVER
    }

    /**
     * Marks this mod as **dedicated-server-only**.
     *
     * Use this only if your mod is designed **exclusively for dedicated servers** —
     * such as server utilities, permissions, or management tools.
     *
     * These mods **will not run in singleplayer**.
     *
     * **Example:**
     * When an admin runs a console command, all grass blocks beneath players
     * are replaced with cobblestone — handled entirely by the server.
     */
    fun dedicatedServerOnly() {
        environment = ModEnvironment.DEDICATED_SERVER
    }

    internal fun requireEnvironment(): ModEnvironment =
        environment ?: gradleError(
            """
            Mod environment not set.
            Please specify it inside recipe.mod { ... } using one of:
              - clientOnly()
              - clientServer()
              - dedicatedServerOnly()
            """.trimIndent()
        )
}
