package de.axelrindle.simplecoins.command

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.command.util.LocalizedCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

internal class ReloadCommand : LocalizedCommand() {

    override fun getName(): String {
        return "reload"
    }

    override fun getDescription(): String {
        return localize("Commands.Reload")
    }

    override fun getPermission(): String {
        return "simplecoins.reload"
    }

    override fun getUsage(): String {
        return "/simplecoins reload"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        sender.sendMessage("${SimpleCoins.prefix} §b${localize("Words.Reloading")}...")

        try {
            // unload the CoinManager
            CoinManager.close()

            // reload the config files and re-init the CoinManager
            SimpleCoins.get().apply {
                pocketConfig.reloadAll()
                CoinManager.init(pocketConfig)
            }

            sender.sendMessage("${SimpleCoins.prefix} §a${localize("Words.Done")}.")
        } catch (e: Exception) {
            sender.sendMessage("${SimpleCoins.prefix} §c${localize("Messages.Error.General")}")
            e.printStackTrace()
        }

        return true
    }

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage(getUsage())
    }

}