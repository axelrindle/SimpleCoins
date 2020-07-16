package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.util.UUIDUtils
import de.axelrindle.pocketknife.util.sendMessageF
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Provides the base functionality for commands working with coin balances.
 */
internal abstract class CoinCommand : LocalizedCommand() {

    /**
     * Manipulates a player's balance.
     *
     * @param uuid The uuid string that identifies the player.
     * @param amount The amount to use for manipulating. Always a positive value.
     * @return The new balance.
     */
    protected open fun manipulateBalance(uuid: String, amount: Double): Double = 0.0

    /**
     * Validates the command arguments.
     *
     * @param args The string array holding all arguments supplied with the command.
     * @return Whether the arguments are valid.
     */
    protected open fun validateArguments(args: Array<out String>): Boolean = true

    /**
     * Retrieves an [OfflinePlayer] by a UUID or player name.
     *
     * @param id Either the name or uuid of a player.
     * @return An [OfflinePlayer] instance.
     */
    protected fun getPlayer(id: String): OfflinePlayer? {
        // lookup by uuid if valid one given
        if (UUIDUtils.isValid(id)) {
            return Bukkit.getOfflinePlayer(UUID.fromString(id))
        }

        // try player name otherwise
        return Bukkit.getOfflinePlayer(id)
    }

    /**
     * Runs several validations on the command arguments.
     *
     * @param args The string array holding all arguments supplied with the command.
     * @param sender The [CommandSender].
     * @return An [OfflinePlayer] on success, `null` otherwise.
     */
    protected fun validate(args: Array<out String>, sender: CommandSender): OfflinePlayer? {
        // test amount of arguments
        if (validateArguments(args).not()) {
            sender.sendMessageF("&c" + localize("Messages.Error.InvalidAmountArguments"))
            sendHelp(sender)
            return null
        }

        // check if player can be found by given id
        val name = args[0]
        val player = getPlayer(name)
        if (player == null) {
            sender.sendMessageF("&c" + localize("Messages.Error.UnknownPlayer", name))
            return null
        }

        return player
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        // run validations and retrieve a player instance
        val player = validate(args, sender) ?: return true

        // parse the amount argument
        val amount: Double
        try {
            amount = args[1].toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c" + localize("Messages.Error.InvalidNumber", args[1]))
            return true
        }

        // manipulate the balance and send status updates to the sender and, if he's online,
        // the affected player
        val currency = CoinManager.getCurrentName()
        val new = manipulateBalance(player.uniqueId.toString(), amount)
        sender.sendMessage("${SimpleCoins.prefix} " +
                localize("Messages.Coins.NewBalance.Sender", player.name, new.toString(), currency))
        if (player.isOnline) {
            (player as Player).sendMessage("${SimpleCoins.prefix} " +
                    localize("Messages.Coins.NewBalance.Receiver", new.toString(), currency))
        }

        return true
    }
}