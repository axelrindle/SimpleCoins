package de.axelrindle.simplecoins.command.util

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.PocketLang
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.CommandSender

/**
 * Provides the basic structure and helpers for localized commands.
 */
abstract class LocalizedCommand : PocketCommand() {

    /**
     * Defines the localized name of this command used to localized
     * the command description.
     */
    protected abstract val localizedName: String

    /**
     * Shorthand method for localizing a message.
     *
     * @see PocketLang.localize
     */
    protected fun localize(key: String, vararg args: Any?): String {
        return SimpleCoins.get().pocketLang.localize(key, *args)!!
    }

    final override fun getDescription(): String {
        return localize("Commands.$localizedName")
    }

    final override fun messageNoMatch(input: String): String {
        return localize("Messages.PocketKnife.NoMatch", input)
    }

    final override fun messageNoPermission(): String? {
        return localize("Messages.PocketKnife.NoPermission")
    }

    final override fun messageNoPlayer(sender: CommandSender): String {
        return localize("Messages.PocketKnife.NoPlayer")
    }
}