package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.command.util.CoinCommand

/**
 * Command for adding an amount of coins to a player's balance.
 */
internal class AddCommand : CoinCommand() {

    override fun getName(): String {
        return "add"
    }

    override fun getDescription(): String {
        return localize("Commands.Add")
    }

    override fun getUsage(): String {
        return "/simplecoins add <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.add"
    }

    override fun manipulateBalance(uuid: String, amount: Double): Double {
        return CoinManager.addCoins(uuid, amount)
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}