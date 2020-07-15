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
 * Command for adding an amount of coins to a player's balance.
 */
internal class AddCommand : PocketCommand() {

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

            val currency = CoinManager.getCurrentName()
            UUIDUtils.lookup(player, Consumer {
                if (it == null){
                    sender.sendMessage("§cNo player found with name '$player'")
                    return@Consumer
                }
                val offlinePlayer = Bukkit.getOfflinePlayer(it)
                val new = CoinManager.addCoins(
                        it.toString(),
                        amount
                )
                sender.sendMessage("${SimpleCoins.prefix} §a$player §rnow has §a$new $currency§r.")
                if (offlinePlayer.isOnline)
                    Bukkit.getPlayer(player)!!
                            .sendMessage("${SimpleCoins.prefix} §aYou now have §b$new $currency§r.")
            })
        }

        return true
    }

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage("/simplecoins add <player> <amount>")
    }
}