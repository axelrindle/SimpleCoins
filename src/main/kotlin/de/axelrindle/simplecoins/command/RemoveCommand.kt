package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for removing an amount of coins from a player's balance.
 */
internal class RemoveCommand : CoinCommand() {

    override fun getName(): String {
        return "remove"
    }

    override fun getDescription(): String {
        return "Removes the given amount of coins from the account of the given player."
    }

    override fun getUsage(): String {
        return "/simplecoins remove <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.remove"
    }

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        val player = validate(args, sender) ?: return true
        var amount = -1.0
        try {
            amount = args[1].toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c'${args[1]}' is not a valid number!")
            return true
        }

        val currency = CoinManager.getCurrentName()
        val new = CoinManager.removeCoins(player.uniqueId.toString(), amount)
        sender.sendMessage("${SimpleCoins.prefix} §a${player.name} §rnow has §a$new $currency§r.")
        if (player.isOnline) {
            (player as Player).sendMessage("${SimpleCoins.prefix} §cYou now have §b$new $currency§r.")
        }

        return true
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}