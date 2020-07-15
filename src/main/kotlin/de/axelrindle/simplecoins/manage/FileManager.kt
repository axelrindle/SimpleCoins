package de.axelrindle.simplecoins.manage

import de.axelrindle.pocketknife.PocketConfig

internal class FileManager(
        private val pocketConfig: PocketConfig,
        private val configName: String
) : IManager {

    override fun addPlayer(uuid: String): Boolean {
        if (hasPlayer(uuid)) return false

        setCoins(uuid, 0.0)
        return true
    }

    override fun hasPlayer(uuid: String): Boolean {
        return pocketConfig.access(configName)!!.contains(uuid)
    }

    override fun getCoins(uuid: String): Double {
        return pocketConfig.access(configName)!!.getDouble(uuid)
    }

    override fun setCoins(uuid: String, amount: Double): Double {
        pocketConfig.edit(configName) {
            it.set(uuid, amount)
        }
        return amount
    }

    override fun addCoins(uuid: String, amount: Double): Double {
        val current = getCoins(uuid)
        val new = current + amount
        return setCoins(uuid, new)
    }

    override fun removeCoins(uuid: String, amount: Double): Double {
        val current = getCoins(uuid)
        var new = current - amount
        if (new < 0) new = 0.0 // avoid values like -1.0
        return setCoins(uuid, new)
    }

}