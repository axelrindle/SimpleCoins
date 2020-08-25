package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.command.util.LocalizedCommand
import org.bukkit.command.CommandSender

internal class SimpleCoinsCommand : LocalizedCommand() {

    override val localizedName: String = "Main"

    override fun getName(): String {
        return "simplecoins"
    }

    override val subCommands: ArrayList<PocketCommand> = arrayListOf(
            GetCommand(),
            SetCommand(),
            AddCommand(),
            RemoveCommand(),
            ReloadCommand(),
            SyncCommand()
    )

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${SimpleCoins.prefix} §l" + localize("Words.Help"))
        subCommands.forEach {
            sender.sendMessage("§6§l${it.getUsage()}  -  §3§l${it.getDescription()}")
        }
    }
}