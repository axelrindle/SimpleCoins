import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import de.axelrindle.simplecoins.SimpleCoins
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.AutoScan
import io.kotest.core.test.TestCaseOrder

@AutoScan
object TestInit : ProjectListener, AbstractProjectConfig() {

    override val parallelism: Int
        get() = Runtime.getRuntime().availableProcessors()

    override val testCaseOrder: TestCaseOrder = TestCaseOrder.Sequential

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