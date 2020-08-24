package de.axelrindle.simplecoins.hooks

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class SimpleCoinsPlaceholderExpansion(
        private val plugin: SimpleCoins
) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "simplecoins"

    override fun getAuthor(): String = plugin.description.authors.joinToString(", ")

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null
        val uuid = player.uniqueId.toString()

        return when (identifier) {
            "balance" -> CoinManager.getCoins(uuid).toString()
            "currency_name" -> CoinManager.getCurrentName()
            else -> null
        }
    }
}