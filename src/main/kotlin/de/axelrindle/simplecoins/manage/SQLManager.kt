package de.axelrindle.simplecoins.manage

import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.CoinUser
import de.axelrindle.simplecoins.CoinUserEntity

internal class SQLManager : IManager {

    override fun addPlayer(uuid: String): Boolean {
        if (hasPlayer(uuid)) return false

        CoinManager.dbStore!!.data
                .insert(CoinUser::class.java)
                .value(CoinUserEntity.AMOUNT, 0.0)
                .value(CoinUserEntity.UUID, uuid)
                .get()
                .close()
        return true
    }

    override fun hasPlayer(uuid: String): Boolean {
        return CoinManager.dbStore!!.data
                .select(CoinUser::class.java)
                .where(CoinUserEntity.UUID.eq(uuid))
                .get()
                .firstOrNull() != null
    }

    @Suppress("USELESS_ELVIS")
    override fun getCoins(uuid: String): Double {
        if (!hasPlayer(uuid)) addPlayer(uuid)

        return CoinManager.dbStore!!.data
                .select(CoinUser::class.java)
                .where(CoinUserEntity.UUID.eq(uuid))
                .get()
                .firstOrNull()
                .amount ?: 0.0
    }

    override fun setCoins(uuid: String, amount: Double): Double {
        if (amount < 0)
            throw IllegalArgumentException("The amount must be greater than zero!")

        if (!hasPlayer(uuid)) addPlayer(uuid)

        CoinManager.dbStore!!.data
                .update(CoinUser::class.java)
                .set(CoinUserEntity.AMOUNT, amount)
                .where(CoinUserEntity.UUID.eq(uuid))
                .get().value()
        return getCoins(uuid)
    }

    override fun addCoins(uuid: String, amount: Double): Double {
        val current = getCoins(uuid)
        return setCoins(uuid, current + amount)
    }

    @Suppress("LocalVariableName")
    override fun removeCoins(uuid: String, amount: Double): Double {
        val current = getCoins(uuid)
        var _amount = current - amount
        if (_amount < 0)
            _amount = 0.0

        if (!hasPlayer(uuid)) addPlayer(uuid)

        return setCoins(uuid, _amount)
    }

}