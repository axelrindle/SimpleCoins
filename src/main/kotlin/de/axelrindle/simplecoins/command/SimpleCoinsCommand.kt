package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.CommandSender

internal class SimpleCoinsCommand : PocketCommand() {

    override fun getName(): String {
        return "simplecoins"
    }

    override val subCommands: ArrayList<PocketCommand> = arrayListOf(
            GetCommand(),
            SetCommand(),
            AddCommand(),
            RemoveCommand(),
            ReloadCommand()
    )

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${SimpleCoins.prefix} §lHelp")
        subCommands.forEach {
            sender.sendMessage("§6§l${it.getUsage()}  -  §3§l${it.getDescription()}")
        }
    }
}