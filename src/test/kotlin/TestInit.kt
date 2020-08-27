import be.seeseemelk.mockbukkit.MockBukkit
import de.axelrindle.simplecoins.SimpleCoins
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.AutoScan
import io.kotest.core.test.TestCaseOrder

@AutoScan
object TestInit : ProjectListener, AbstractProjectConfig() {

    override val parallelism: Int = Runtime.getRuntime().availableProcessors()
    override val testCaseOrder: TestCaseOrder = TestCaseOrder.Sequential

    override suspend fun beforeProject() {
        val server = MockBukkit.mock()
        MockBukkit.load(SimpleCoins::class.java)

        server.addPlayer("lalo5").apply {
            isOp = true
        }
        server.addPlayer()
        server.addPlayer()
    }

    override suspend fun afterProject() {
        // print player chat
        println("------------------ Player Chat ------------------")
        val player = MockBukkit.getMock().getPlayer(0)
        var msg: String? = ""
        while(msg != null) {
            println(msg)
            msg = player.nextMessage()
        }
        println("-------------------------------------------------")

        MockBukkit.unmock()
    }

}