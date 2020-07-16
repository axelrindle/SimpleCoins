package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.CommandSender

internal class SimpleCoinsCommand : LocalizedCommand() {

    override fun getName(): String {
        return "simplecoins"
    }

    override fun getDescription(): String {
        return SimpleCoins.get().pocketLang.localize("Commands.Main")!!
    }

    override val subCommands: ArrayList<PocketCommand> = arrayListOf(
            GetCommand(),
            SetCommand(),
            AddCommand(),
            RemoveCommand(),
            ReloadCommand()
    )

    override fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${SimpleCoins.prefix} §l" + localize("Words.Help"))
        subCommands.forEach {
            sender.sendMessage("§6§l${it.getUsage()}  -  §3§l${it.getDescription()}")
        }
    }
}