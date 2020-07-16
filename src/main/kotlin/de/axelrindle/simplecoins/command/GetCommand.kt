package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.util.sendMessageF
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.command.util.CoinCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for retrieving the amount of coins for a given player.
 */
internal class GetCommand : CoinCommand() {

    override val localizedName: String = "Get"

    override fun getName(): String {
        return "get"
    }

    override fun getUsage(): String {
        return "/simplecoins get [player]"
    }

    override fun getPermission(): String {
        return "simplecoins.get"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        if (sender !is Player && args.isEmpty()) {
            sender.sendMessageF("&c" + localize("Messages.Error.ConsoleNoBalance"))
            return true
        }

        val targetName = if (args.isNotEmpty()) args[0] else sender.name
        val player = validate(args, sender) ?: return true

        val currency = CoinManager.getCurrentName()
        val got = CoinManager.getCoins(player.uniqueId.toString())
        sender.sendMessageF("${SimpleCoins.prefix} " +
                localize("Messages.Coins.Current", targetName, got.toString(), currency))

        return true
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size in 0..1
    }
}