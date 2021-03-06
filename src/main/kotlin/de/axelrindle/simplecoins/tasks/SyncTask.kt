package de.axelrindle.simplecoins.tasks

import de.axelrindle.simplecoins.*
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import org.bukkit.Bukkit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import java.util.function.Supplier
import java.util.logging.Logger
import kotlin.math.ceil

internal class SyncTask {

    companion object {
        private const val QUERY_LIMIT = 50
        val VALID_DESTINATIONS = listOf("local", "remote")
    }

    private val logger = Logger.getLogger(javaClass.simpleName)

    var destination: String? = null
        set(value) {
            if (!VALID_DESTINATIONS.contains(value))
                throw IllegalArgumentException("destination must be one of " +
                        VALID_DESTINATIONS.joinToString(", ") + "!")

            field = value
        }

    var isRunning: Boolean = false
        private set

    fun isReady(): Boolean {
        return destination != null
    }

    fun run(callback: BiConsumer<Int, Throwable?>?): CompletableFuture<Int> {
        var future = CompletableFuture.supplyAsync(
                handleAsync(),
                Executor { Bukkit.getScheduler().runTaskAsynchronously(SimpleCoins.get(), it) }
        )
        if (callback != null) {
            future = future.whenCompleteAsync(
                    callback,
                    Executor { Bukkit.getScheduler().runTask(SimpleCoins.get(), it) }
            )
        }
        return future
    }

    private fun handleAsync() = Supplier {
        if (isRunning) throw IllegalStateException("Task already running!")

        isRunning = true
        logger.info("Synchronizing data to $destination...")
        val amount: Int = when (destination) {
            "local" -> syncToLocal()
            "remote" -> syncToRemote()
            else -> -1
        }
        isRunning = false
        logger.info("Synchronized $amount entries.")
        amount
    }

    private fun syncToLocal(): Int {
        val store = CoinManager.dbStore!!
        val count = store.count(CoinUser::class).get().value()
        val iterations = if (count <= QUERY_LIMIT) 1 else ceil(count.toDouble() / QUERY_LIMIT).toInt()
        var offset = 0

        logger.info("$count entries will be written to local file in $iterations iteration(s)...")

        SimpleCoins.get().pocketConfig.edit("database") { config ->
            repeat(iterations) { iteration ->
                logger.info("Iteration ${iteration + 1}...")
                val remoteList = CoinManager.dbStore!!
                        .select(CoinUser::class)
                        .limit(QUERY_LIMIT)
                        .offset(offset)
                        .get()

                remoteList.each { user ->
                    config[user.uuid] = user.amount
                }
                remoteList.close()

                // increase offset by limit after each iteration
                offset += QUERY_LIMIT
            }
        }

        return count
    }

    private fun syncToRemote(): Int {
        val config = SimpleCoins.get().pocketConfig.access("database")!!
        val values = config.getValues(false)
        logger.info("${values.count()} entries will be written to the remote database...")

        // drop table
        SchemaModifier(CoinManager.dataSource!!, Models.DEFAULT)
                .createTables(TableCreationMode.DROP_CREATE)

        values.forEach { (uuid, amount) ->
            CoinManager.dbStore!!
                    .insert(CoinUser::class)
                    .value(CoinUserEntity.UUID, uuid)
                    .value(CoinUserEntity.AMOUNT, amount as Double)
                    .get().close()
        }

        return values.size
    }
}
