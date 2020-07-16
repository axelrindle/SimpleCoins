package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.util.UUIDUtils
import de.axelrindle.pocketknife.util.sendMessageF
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*

/**
 * Provides the base functionality for commands working with coin balances.
 */
internal abstract class CoinCommand : PocketCommand() {

    protected open fun validateArguments(args: Array<out String>): Boolean = true

    /**
     * Retrieves an [OfflinePlayer] by a UUID or player name.
     */
    protected fun getPlayer(id: String): OfflinePlayer? {
        // lookup by uuid if valid one given
        if (UUIDUtils.isValid(id)) {
            return Bukkit.getOfflinePlayer(UUID.fromString(id))
        }

        // try player name otherwise
        return Bukkit.getOfflinePlayer(id)
    }

    protected fun validate(args: Array<out String>, sender: CommandSender): OfflinePlayer? {
        // test amount of arguments
        if (validateArguments(args).not()) {
            sender.sendMessageF("&cInvalid amount of arguments:")
            sendHelp(sender)
            return null
        }

        // check if player can be found by given id
        val name = args[0]
        val player = getPlayer(name)
        if (player == null) {
            sender.sendMessageF("&cNo player found by '$name'!")
            return null
        }

        return player
    }
}