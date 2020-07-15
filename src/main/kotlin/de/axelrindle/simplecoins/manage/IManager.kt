package de.axelrindle.simplecoins.manage

/**
 * Interface for classes managing coin balances.
 */
internal interface IManager {

    /**
     * Creates a new database entry for the given player.
     *
     * @param uuid The player to add.
     * @return Whether the account was newly created.
     *         If the account was already created, `false` is returned.
     */
    fun addPlayer(uuid: String): Boolean

    /**
     * Checks whether the given player has an account.
     *
     * @param uuid The UUID of the player to check.
     * @return Whether the given player has an account.
     */
    fun hasPlayer(uuid: String): Boolean

    /**
     * Retrieves the amount of coins for the given player.
     *
     * @param uuid The UUID of the player to check.
     * @return The amount of coins.
     */
    fun getCoins(uuid: String): Double

    /**
     * Changes the amount of coins to the given amount.
     *
     * @param uuid The UUID of the player to modify.
     * @param amount The new amount of coins.
     *
     * @return The new amount of coins.
     */
    fun setCoins(uuid: String, amount: Double): Double

    /**
     * Adds the given amount of coins to the given players balance.
     *
     * @param uuid The UUID of the player to add coins to.
     * @param amount The amount of coins to add.
     *
     * @return The new amount of coins.
     */
    fun addCoins(uuid: String, amount: Double): Double

    /**
     * Removes the given amount of coins from the given players balance.
     * If the amount is bigger than the current balance, the balance is simply set to zero.
     *
     * @param uuid The UUID of the player to remove coins from.
     * @param amount The amount of coins to remove.
     *
     * @return The new amount of coins.
     */
    fun removeCoins(uuid: String, amount: Double): Double

}