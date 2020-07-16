package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for setting the amount of coins of a player to a new value.
 */
internal class SetCommand : CoinCommand() {

    override fun getName(): String {
        return "set"
    }

    override fun getDescription(): String {
        return localize("Commands.Set")
    }

    override fun getUsage(): String {
        return "/simplecoins set <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.set"
    }

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        val player = validate(args, sender) ?: return true
        var amount = -1.0
        try {
            amount = args[1].toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c${localize("Messages.Error.InvalidNumber", args[1])}")
            return true
        }

        val currency = CoinManager.getCurrentName()
        val new = CoinManager.setCoins(player.uniqueId.toString(), amount)
        sender.sendMessage("${SimpleCoins.prefix} " +
                localize("Messages.Coins.NewBalance.Sender", player.name, new.toString(), currency))
        if (player.isOnline) {
            (player as Player).sendMessage("${SimpleCoins.prefix} " +
                    localize("Messages.Coins.NewBalance.Receiver", new.toString(), currency))
        }

        return true
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}