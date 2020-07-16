package de.axelrindle.simplecoins

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.PocketConfig
import de.axelrindle.simplecoins.command.SimpleCoinsCommand
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException

/**
 * Main plugin class.
 */
class SimpleCoins : JavaPlugin() {

    companion object {
        const val prefix = "§6SimpleCoins §r>"

        private var instance: SimpleCoins? = null

        fun get(): SimpleCoins {
            return instance!!
        }
    }

    internal val pocketConfig: PocketConfig = PocketConfig(this)

    override fun onEnable() {
        if (instance != null)
            throw IllegalStateException("Plugin has already been initialized!")
        instance = this

        // config init
        logger.info("Loading configuration...")
        try {
            pocketConfig.register("config", getResource("config.yml")!!)
            pocketConfig.register("database", getResource("database.yml")!!)
        } catch (e: IOException) {
            e.printStackTrace()
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // database init
        logger.info("Loading CoinManager...")
        CoinManager.init(pocketConfig)

        // vault connection
        connectToVault()

        // register command
        PocketCommand.register(this, SimpleCoinsCommand())

        logger.info("SimpleCoins v${description.version} initialized.")
    }

    override fun onDisable() {
        CoinManager.close()
    }

    /**
     * Registers the [SimpleCoinsEconomy] class as an [Economy] service manager.
     */
    private fun connectToVault() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            server.servicesManager.register(
                    Economy::class.java,
                    SimpleCoinsEconomy(),
                    this,
                    ServicePriority.Normal
            )
            logger.info("Connected to Vault.")
        }
    }
}