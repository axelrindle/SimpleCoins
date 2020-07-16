package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

internal class ReloadCommand : PocketCommand() {

    override fun getName(): String {
        return "reload"
    }

    override fun getDescription(): String {
        return "Reloads the configuration from disk."
    }

    override fun getPermission(): String {
        return "simplecoins.reload"
    }

    override fun getUsage(): String {
        return "/simplecoins reload"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        sender.sendMessage("${SimpleCoins.prefix} §bReloading...")

        try {
            // unload the CoinManager
            CoinManager.close()

            // reload the config files and re-init the CoinManager
            SimpleCoins.get().apply {
                pocketConfig.reloadAll()
                CoinManager.init(pocketConfig)
            }

            sender.sendMessage("${SimpleCoins.prefix} §aDone.")
        } catch (e: Exception) {
            sender.sendMessage("${SimpleCoins.prefix} §cSomething went wrong! Check the console for more information.")
            e.printStackTrace()
        }

        return true
    }

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage(getUsage())
    }

}