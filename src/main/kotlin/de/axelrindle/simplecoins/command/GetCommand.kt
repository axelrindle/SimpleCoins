package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.util.UUIDUtils
import de.axelrindle.pocketknife.util.sendMessageF
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Consumer

/**
 * Command for retrieving the amount of coins for a given player.
 */
internal class GetCommand : PocketCommand() {

    override fun getName(): String {
        return "get"
    }

    override fun getDescription(): String {
        return "Returns the amount of coins for yourself or the given player."
    }

    override fun getUsage(): String {
        return "/simplecoins get [player]"
    }

    override fun getPermission(): String {
        return "simplecoins.get"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        if (sender !is Player && args.isEmpty()) {
            sender.sendMessageF("&cThe console does not have any balance!")
            return true
        }

        val targetName = if (args.isNotEmpty()) args[0] else sender.name
        val currency = CoinManager.getCurrentName()
        UUIDUtils.lookup(targetName, Consumer {
            if (it == null){
                sender.sendMessageF("&cNo player found with name '$targetName'")
                return@Consumer
            }
            val got = CoinManager.getCoins(it.toString())
            sender.sendMessageF("${SimpleCoins.prefix} The player &a$targetName &rcurrently has &a$got $currency&r.")
        })

        return true
    }

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage(getUsage())
    }
}