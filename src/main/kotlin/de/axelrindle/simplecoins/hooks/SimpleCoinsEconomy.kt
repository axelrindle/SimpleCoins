package de.axelrindle.simplecoins.hooks

import de.axelrindle.pocketknife.PocketConfig
import de.axelrindle.simplecoins.CoinManager
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import kotlin.math.abs

@Suppress("OverridingDeprecatedMember")
internal class SimpleCoinsEconomy(
        private val pocketConfig: PocketConfig
) : Economy {

    companion object {
        private const val DEPRECATION_MESSAGE_PLAYER_NAME = "Player names may change. Use UUIDs instead."
    }

    //region General

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getName(): String {
        return "SimpleCoins"
    }

    override fun fractionalDigits(): Int {
        return 2
    }

    override fun currencyNameSingular(): String {
        return pocketConfig.access("config")!!.getString("CoinsName")!!
    }

    override fun currencyNamePlural(): String {
        return pocketConfig.access("config")!!.getString("CoinsName")!!
    }

    override fun format(amount: Double): String {
        return "$amount ${currencyNamePlural()}"
    }
    //endregion

    //region Balances
    override fun getBalance(player: OfflinePlayer): Double {
        return CoinManager.getCoins(player.uniqueId.toString())
    }

    override fun getBalance(player: OfflinePlayer, world: String): Double {
        return getBalance(player)
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("getBalance(player: OfflinePlayer)")
    )
    override fun getBalance(playerName: String): Double {
        return 0.0
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("getBalance(player: OfflinePlayer, world: String)")
    )
    override fun getBalance(playerName: String, world: String): Double {
        return 0.0
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("has(player: OfflinePlayer, amount: Double)")
    )
    override fun has(playerName: String, amount: Double): Boolean {
        return false
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return CoinManager.getCoins(player.uniqueId.toString()) >= amount
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("has(player: OfflinePlayer, world: String, amount: Double)")
    )
    override fun has(playerName: String, world: String, amount: Double): Boolean {
        return false
    }

    override fun has(player: OfflinePlayer, world: String, amount: Double): Boolean {
        return CoinManager.getCoins(player.uniqueId.toString()) >= amount
    }
    //endregion

    //region Accounting
    override fun hasAccount(player: OfflinePlayer): Boolean {
        return CoinManager.hasPlayer(player.uniqueId.toString())
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("hasAccount(player: OfflinePlayer)")
    )
    override fun hasAccount(playerName: String): Boolean {
        return false
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("hasAccount(player: OfflinePlayer, world: String)")
    )
    override fun hasAccount(playerName: String, world: String): Boolean {
        return false
    }

    override fun hasAccount(player: OfflinePlayer, world: String): Boolean {
        return CoinManager.hasPlayer(player.uniqueId.toString())
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("createPlayerAccount(player: OfflinePlayer)")
    )
    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        CoinManager.addPlayer(player.uniqueId.toString())
        return true
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("createPlayerAccount(player: OfflinePlayer, world: String)")
    )
    override fun createPlayerAccount(playerName: String, world: String): Boolean {
        return false
    }

    override fun createPlayerAccount(player: OfflinePlayer, world: String): Boolean {
        CoinManager.addPlayer(player.uniqueId.toString())
        return true
    }
    //endregion

    //region Transactions

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("depositPlayer(player: OfflinePlayer, amount: Double)")
    )
    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        CoinManager.addCoins(player.uniqueId.toString(), abs(amount))
        return EconomyResponse(
                amount,
                getBalance(player),
                EconomyResponse.ResponseType.SUCCESS,
                ""
        )
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("depositPlayer(player: OfflinePlayer, world: String, amount: Double)")
    )
    override fun depositPlayer(playerName: String, world: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun depositPlayer(player: OfflinePlayer, world: String, amount: Double): EconomyResponse {
        CoinManager.addCoins(player.uniqueId.toString(), abs(amount))
        return EconomyResponse(
                amount,
                getBalance(player),
                EconomyResponse.ResponseType.SUCCESS,
                ""
        )
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("withdrawPlayer(player: OfflinePlayer, amount: Double)")
    )
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        CoinManager.removeCoins(player.uniqueId.toString(), abs(amount))
        return EconomyResponse(
                amount,
                getBalance(player),
                EconomyResponse.ResponseType.SUCCESS,
                ""
        )
    }

    @Deprecated(
            DEPRECATION_MESSAGE_PLAYER_NAME,
            ReplaceWith("withdrawPlayer(player: OfflinePlayer, world: String, amount: Double)")
    )
    override fun withdrawPlayer(playerName: String, world: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun withdrawPlayer(player: OfflinePlayer, world: String, amount: Double): EconomyResponse {
        CoinManager.removeCoins(player.uniqueId.toString(), abs(amount))
        return EconomyResponse(
                amount,
                getBalance(player),
                EconomyResponse.ResponseType.SUCCESS,
                ""
        )
    }
    //endregion

    //region Vault Banks

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun getBanks(): MutableList<String> {
        return arrayListOf()
    }

    override fun createBank(playerName: String, world: String): EconomyResponse? {
        return null
    }

    override fun createBank(playerName: String, p1: OfflinePlayer): EconomyResponse? {
        return null
    }

    override fun bankBalance(playerName: String): EconomyResponse? {
        return null
    }

    override fun bankHas(playerName: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun isBankOwner(playerName: String, world: String): EconomyResponse? {
        return null
    }

    override fun isBankOwner(playerName: String, p1: OfflinePlayer): EconomyResponse? {
        return null
    }

    override fun isBankMember(playerName: String, world: String): EconomyResponse? {
        return null
    }

    override fun isBankMember(playerName: String, p1: OfflinePlayer): EconomyResponse? {
        return null
    }

    override fun bankDeposit(playerName: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun bankWithdraw(playerName: String, amount: Double): EconomyResponse? {
        return null
    }

    override fun deleteBank(playerName: String): EconomyResponse? {
        return null
    }

    //endregion
}
