package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.util.UUIDUtils
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.function.Consumer

/**
 * Command for setting the amount of coins of a player to a new value.
 */
internal class SetCommand : PocketCommand() {

    override fun getName(): String {
        return "set"
    }

    override fun getDescription(): String {
        return "Changes the amount of coins to the given amount."
    }

    override fun getUsage(): String {
        return "/simplecoins set <player> <amount>"
    }

    override fun getPermission(): String {
        return "simplecoins.set"
    }

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {

        if (args.size != 2) {
            sender.sendMessage("§cTwo arguments required:")
            sendHelp(sender)
        } else {
            val player = args[0]
            var amount = -1.0
            try {
                amount = args[1].toDouble()
            } catch (e: NumberFormatException) {
                sender.sendMessage("§c'${args[1]}' is not a valid number!")
                return true
            }

            println(amount)

            val currency = CoinManager.getCurrentName()
            UUIDUtils.lookup(player, Consumer {
                if (it == null){
                    sender.sendMessage("§cNo player found with name '$player'")
                    return@Consumer
                }
                val offlinePlayer = Bukkit.getOfflinePlayer(it)
                val new = CoinManager.setCoins(
                        it.toString(),
                        amount
                )
                sender.sendMessage("${SimpleCoins.prefix} §a$player §rnow has §a$new $currency§r.")
                if (offlinePlayer.isOnline)
                    Bukkit.getPlayer(player)!!
                            .sendMessage("${SimpleCoins.prefix} You now have §b$new $currency§r.")
            })
        }

        return true
    }

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage(getUsage())
    }
}