import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.Models
import de.axelrindle.simplecoins.SimpleCoins
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.stringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import java.util.*

@DoNotParallelize
@Suppress("unused")
class CoinManagerTest : StringSpec({

    val runner = fun (uuid: String, context: String) = stringSpec {
        "[$context] getCurrentName() should return the default value (Coins)" {
            CoinManager.getCurrentName() shouldBe "Coins"
        }

        "[$context] hasPlayer() should be false when account does not exist" {
            CoinManager.hasPlayer(uuid) shouldBe false
        }

        "[$context] getCoins() should return 0 when account does not exist" {
            CoinManager.getCoins(uuid) shouldBe 0.0
        }

        "[$context] addPlayer() should return true and then false" {
            CoinManager.addPlayer(uuid) shouldBe true
            CoinManager.addPlayer(uuid) shouldBe false
        }

        "[$context] hasPlayer() should be true when the account exists" {
            CoinManager.hasPlayer(uuid) shouldBe true
        }

        "[$context] setCoins() should return 10 when the account exists" {
            CoinManager.setCoins(uuid, 10.0) shouldBe 10.0
        }

        "[$context] getCoins() should return 10 when the account exists" {
            CoinManager.getCoins(uuid) shouldBe 10.0
        }

        "[$context] removeCoins() should return 0 with a value greater than the actual amount" {
            CoinManager.removeCoins(uuid, 1000.0) shouldBe 0.0
        }

        "[$context] addCoins() should return 10 and then 20 when the account exists" {
            CoinManager.addCoins(uuid, 10.0) shouldBe 10.0
            CoinManager.addCoins(uuid, 10.0) shouldBe 20.0
        }
    }

    include(runner(UUID.randomUUID().toString(), "local"))

    include(stringSpec {
        "connecting to MySQL should succeed" {
            // connect to the MySQL test database and run tests on that
            CoinManager.close()
            SimpleCoins.get().apply {
                pocketConfig.edit("config") { config ->
                    config.set("Database.UseSQL", true)
                    config.set("Database.Host", System.getenv("MYSQL_TEST_HOST"))
                    config.set("Database.Port", System.getenv("MYSQL_TEST_PORT").toInt())
                    config.set("Database.DatabaseName", System.getenv("MYSQL_TEST_DB"))
                    config.set("Database.Username", System.getenv("MYSQL_TEST_USER"))
                    config.set("Database.Password", System.getenv("MYSQL_TEST_PASSWORD"))
                }
                CoinManager.init(pocketConfig)

                CoinManager.dataSource shouldNotBe null
                CoinManager.dbStore shouldNotBe null

                // create empty table(s)
                SchemaModifier(CoinManager.dataSource!!, Models.DEFAULT)
                        .createTables(TableCreationMode.DROP_CREATE)
            }
        }
    })

    include(runner(UUID.randomUUID().toString(), "mysql"))
})