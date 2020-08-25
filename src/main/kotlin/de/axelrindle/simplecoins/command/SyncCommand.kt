package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.util.sendMessageF
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.tasks.SyncTask
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal class SyncCommand : PocketCommand() {

    companion object {
        private const val CONFIRM_TIMEOUT = 60 // seconds

        private val validDestinations = listOf("local", "remote")
        private val validConfirmations = listOf("cancel", "confirm")

        private var task: SyncTask? = null
        private var confirmTimer: Int = CONFIRM_TIMEOUT
        private var confirmCountdown: Int? = null
        private var confirmTimeout: Int? = null
    }

    override fun getName(): String {
        return "sync"
    }

    override fun getDescription(): String {
        return "Synchronize the database from/to the local file to/from the MySQL database."
    }

    override fun getPermission(): String {
        return "simplecoins.sync"
    }

    override fun getUsage(): String {
        return "/simplecoins sync <destination>"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        if (task == null) task = SyncTask { result ->
            if (result) {
                sender.sendMessageF("${SimpleCoins.prefix} &aThe synchronization has finished.")
            } else {
                sender.sendMessageF("${SimpleCoins.prefix} &cSomething went wrong! " +
                        "Check to console for more information.")
            }
            Bukkit.getScheduler().runTask(SimpleCoins.get(), Runnable { task = null })
        }

        // SQL connection must be established
        if (CoinManager.dbStore == null) {
            sender.sendMessageF("${SimpleCoins.prefix} &cNo SQL connection is established! Make sure the config " +
                    "entry &aDatabase.UseSQL &cis set to &atrue&r.")
            return true
        }

        // task must not be running
        if (task!!.isRunning) {
            sender.sendMessageF("${SimpleCoins.prefix} &cThe task is already running!")
            return true
        }

        // require one argument
        if (args.isEmpty()) {
            return sendDestinationHelp(sender)
        }

        // validate argument
        val destination = args[0]
        if (task!!.isReady()) {
            if (!validConfirmations.contains(destination)) {
                sender.sendMessageF("${SimpleCoins.prefix} Use \"&acancel&r\" or \"&aconfirm&r\" as the argument!")
                return true
            } else if (destination == "cancel") {
                task!!.destination = null
                cancel()
                sender.sendMessageF("${SimpleCoins.prefix} Synchronization has been cancelled.")
                return true
            }
        } else if (!validDestinations.contains(destination)) {
            return sendDestinationHelp(sender)
        }

        when {
            // let the user confirm
            task!!.isReady().not() -> {
                sender.sendMessageF("${SimpleCoins.prefix} Data will be synchronized to ${destination}.")
                sender.sendMessageF("${SimpleCoins.prefix} Please note that existing data will be overwritten.")
                sender.sendMessageF("${SimpleCoins.prefix} Disclaimer: You should backup your existing database, as " +
                        "this is a destructive operation! I do not take responsibility for lost data!")
                if (destination == "remote") {
                    sender.sendMessageF("${SimpleCoins.prefix} A notice on remote sync: An existing table will be" +
                            "dropped, so ALL data will be lost!")
                }
                sender.sendMessage("")
                sender.sendMessageF("${SimpleCoins.prefix} Type \"&a/simplecoins sync confirm&r\" within the next " +
                        "minute to start synchronizing.")

                task!!.destination = destination
                scheduleConfirmation(sender)
            }

            // synchronize
            else -> {
                cancel()
                task!!.start()
            }
        }

        return true
    }

    private fun scheduleConfirmation(sender: CommandSender) {
        confirmTimer = CONFIRM_TIMEOUT

        Bukkit.getScheduler().apply {
            // count down to timeout
            confirmCountdown = scheduleSyncRepeatingTask(
                    SimpleCoins.get(),
                    {
                        if (confirmTimer % 10 == 0 || confirmTimer <= 10) {
                            if (sender is Player) {
                                val sound =
                                        if (confirmTimer > 3) Sound.BLOCK_NOTE_BLOCK_SNARE
                                        else Sound.BLOCK_NOTE_BLOCK_PLING
                                sender.playSound(sender.location, sound, 1f, 1f)
                            }
                            sender.sendMessageF("${SimpleCoins.prefix} &cCountdown timeout in &6$confirmTimer &cseconds...")
                        }
                        confirmTimer--
                    },
                    0, 20L
            )

            // timeout handler
            confirmTimeout = scheduleSyncDelayedTask(
                    SimpleCoins.get(),
                    {
                        cancel()
                        task!!.destination = null
                        sender.sendMessageF("${SimpleCoins.prefix} &cConfirm has timed out.")
                    },
                    20L * CONFIRM_TIMEOUT
            )
        }
    }

    private fun cancel() {
        Bukkit.getScheduler().apply {
            if (confirmCountdown != null) {
                cancelTask(confirmCountdown!!)
                confirmCountdown = null
            }
            if (confirmTimeout != null) {
                cancelTask(confirmTimeout!!)
                confirmTimeout = null
            }
        }
    }

    private fun sendDestinationHelp(sender: CommandSender): Boolean {
        sender.sendMessageF("&cProvide a destination: \"local\" or \"remote\"!")
        return true
    }
}