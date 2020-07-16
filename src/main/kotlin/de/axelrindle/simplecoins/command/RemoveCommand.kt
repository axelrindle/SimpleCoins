package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.command.util.CoinCommand

/**
 * Command for removing an amount of coins from a player's balance.
 */
internal class RemoveCommand : CoinCommand() {

    override fun getName(): String {
        return "remove"
    }

    override fun getDescription(): String {
        return localize("Commands.Remove")
    }

    override fun getUsage(): String {
        return "/simplecoins remove <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.remove"
    }

    override fun manipulateBalance(uuid: String, amount: Double): Double {
        return CoinManager.removeCoins(uuid, amount)
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}