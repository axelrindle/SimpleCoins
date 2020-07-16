package de.axelrindle.simplecoins

import com.mysql.cj.jdbc.MysqlDataSource
import de.axelrindle.pocketknife.PocketConfig
import de.axelrindle.simplecoins.manage.FileManager
import de.axelrindle.simplecoins.manage.IManager
import de.axelrindle.simplecoins.manage.SQLManager
import io.requery.*
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import java.io.Closeable

@Entity
@Table(name = "SimpleCoins")
interface CoinUser : Persistable {
    @get:Key
    @get:Column(unique = true, collate = "utf8_general_ci")
    var uuid: String

    @get:Column(collate = "utf8_general_ci")
    var amount: Double
}

/**
 * The <b>CoinManager</b> is used to retrieve and modify the coin balances for a given player.
 */
object CoinManager : Closeable {

    internal var pocketConfig: PocketConfig? = null
    private var shouldUseSql = false
    private var manager: IManager? = null

    private var dbConfig: KotlinConfiguration? = null
    internal var dbStore: KotlinEntityDataStore<CoinUser>? = null

    internal fun init(pocketConfig: PocketConfig) {
        CoinManager.pocketConfig = pocketConfig
        shouldUseSql = pocketConfig.access("config")!!.getBoolean("Database.UseSQL")

        if (shouldUseSql)
            establishDatabaseConnection()
        else // use the file database
            manager = FileManager(pocketConfig, "database")
    }

    override fun close() {
        dbStore?.close()

        dbConfig = null
        dbStore = null
        manager = null
    }

    private fun establishDatabaseConnection() {
        // configuration values
        val config = pocketConfig!!.access("config")!!.getConfigurationSection("Database")!!
        val host = config.getString("Host")
        val port = config.getString("Port")
        val dbName = config.getString("DatabaseName")
        val user = config.getString("Username")
        val pass = config.getString("Password")

        // establish connection
        Class.forName("com.mysql.jdbc.Driver")
        val dataSource = MysqlDataSource().apply {
            setUrl("jdbc:mysql://$host:$port/$dbName")
            setUser(user)
            setPassword(pass)
            serverTimezone = "UTC"
        }
        dbConfig = KotlinConfiguration(dataSource = dataSource, model = Models.DEFAULT)
        dbStore = KotlinEntityDataStore(dbConfig!!)

        // create table(s)
        SchemaModifier(dataSource, Models.DEFAULT)
                .createTables(TableCreationMode.CREATE_NOT_EXISTS)

        // instantiate an SQLManager
        manager = SQLManager()
    }


    // # # # # # # # # # # # # # # # # # # #
    // Public Section
    //

    /**
     * @return The configured name of the currency.
     */
    fun getCurrentName(): String {
        return pocketConfig!!.access("config")!!.getString("CoinsName")!!
    }

    /**
     * @see IManager.addPlayer
     */
    fun addPlayer(uuid: String): Boolean {
        return manager!!.addPlayer(uuid)
    }

    /**
     * @see IManager.hasPlayer
     */
    fun hasPlayer(uuid: String): Boolean {
        return manager!!.hasPlayer(uuid)
    }

    /**
     * @see IManager.getCoins
     */
    fun getCoins(uuid: String): Double {
        return manager!!.getCoins(uuid)
    }

    /**
     * @see IManager.setCoins
     */
    fun setCoins(uuid: String, amount: Double): Double {
        return manager!!.setCoins(uuid, amount)
    }

    /**
     * @see IManager.addCoins
     */
    fun addCoins(uuid: String, amount: Double): Double {
        return manager!!.addCoins(uuid, amount)
    }

    /**
     * @see IManager.removeCoins
     */
    fun removeCoins(uuid: String, amount: Double): Double {
        return manager!!.removeCoins(uuid, amount)
    }

}