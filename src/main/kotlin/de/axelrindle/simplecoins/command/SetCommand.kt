package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.command.util.CoinCommand

/**
 * Command for setting the amount of coins of a player to a new value.
 */
internal class SetCommand : CoinCommand() {

    override val localizedName: String = "Set"

    override fun getName(): String {
        return "set"
    }

    override fun getUsage(): String {
        return "/simplecoins set <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.set"
    }

    override fun manipulateBalance(uuid: String, amount: Double): Double {
        return CoinManager.setCoins(uuid, amount)
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size == 2
    }
}