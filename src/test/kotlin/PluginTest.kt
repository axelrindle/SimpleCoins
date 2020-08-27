import be.seeseemelk.mockbukkit.MockBukkit
import de.axelrindle.simplecoins.SimpleCoins
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PluginTest : StringSpec({

    "trying to instantiate a second SimpleCoins instance should fail" {
        val exception = shouldThrow<IllegalStateException> {
            MockBukkit.load(SimpleCoins::class.java)
        }
        exception.message shouldBe "Plugin has already been initialized!"
    }

})