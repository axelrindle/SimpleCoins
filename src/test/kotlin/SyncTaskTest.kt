import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.tasks.SyncTask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.whenReady
import java.util.*

@DoNotParallelize
class SyncTaskTest : StringSpec({

    val task = SyncTask()

    "task shouldn't be ready right after creation" {
        task.isReady() shouldBe false
        task.isRunning shouldBe false
    }

    "changing the destination should fail for invalid destinations" {
        val exception = shouldThrow<IllegalArgumentException> {
            task.destination = "unknown"
            null
        }
        exception.message shouldBe "destination must be one of local, remote!"
    }

    "valid destinations should be accepted" {
        shouldNotThrow<java.lang.IllegalArgumentException> {
            task.destination = "remote"
            null
        }
    }

    // generate test data
    val uuid1 = UUID.randomUUID().toString()
    val uuid2 = UUID.randomUUID().toString()
    SimpleCoins.get().pocketConfig.edit("database") { config ->
        config.set(uuid1, 150.0)
        config.set(uuid2, 510.0)
    }

    "syncing two entries from local should result in two entries in the remote database" {
        shouldNotThrow<Exception> {
            task.run(null).whenReady {
                it shouldBeGreaterThan 0
                CoinManager.getCoins(uuid1) shouldBe 150.0
                CoinManager.getCoins(uuid2) shouldBe 510.0
            }
        }
    }

    "changes in the database should be synced to the local file" {
        shouldNotThrow<Exception> {
            task.destination = "local"
            task.run(null).whenReady {
                it shouldBeGreaterThan 0

                val config = SimpleCoins.get().pocketConfig.access("database")!!
                config.get(uuid1) shouldBe 150.0
                config.get(uuid2) shouldBe 510.0
            }
        }
    }

})