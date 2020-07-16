package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for adding an amount of coins to a player's balance.
 */
internal class AddCommand : CoinCommand() {

    override fun getName(): String {
        return "add"
    }

    override fun getDescription(): String {
        return "Adds the given amount of coins to the account of the given player."
    }

    override fun getUsage(): String {
        return "/simplecoins add <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.add"
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
        val new = CoinManager.addCoins(player.uniqueId.toString(), amount)
        sender.sendMessage("${SimpleCoins.prefix} §a${player.name} §rnow has §a$new $currency§r.")
        if (player.isOnline) {
            (player as Player).sendMessage("${SimpleCoins.prefix} §aYou now have §b$new $currency§r.")
        }

        return true
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}