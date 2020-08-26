import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.AutoScan

@AutoScan
object TestInit : ProjectListener {

    private lateinit var server: ServerMock
    private lateinit var plugin: SimpleCoins

    override suspend fun beforeProject() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(SimpleCoins::class.java)
    }

    override suspend fun afterProject() {
        MockBukkit.unmock()
    }

}