import be.seeseemelk.mockbukkit.MockBukkit
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.hooks.SimpleCoinsPlaceholderExpansion
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleCoinsPlaceholderExpansionTest : StringSpec({

    val server = MockBukkit.getMock()
    val plugin = server.pluginManager.getPlugin("SimpleCoins")!! as SimpleCoins
    val player = server.getPlayer(1)

    val placeholderExpansion = SimpleCoinsPlaceholderExpansion(plugin)
    CoinManager.setCoins(player.uniqueId.toString(), 69.0)

    "requesting the balance identifier should return 69.0" {
        placeholderExpansion.onRequest(player, "balance") shouldBe "69.0"
    }

    "requesting the currency name identifier should return Coins" {
        placeholderExpansion.onRequest(player, "currency_name") shouldBe "Coins"
    }

    "requesting with invalid parameters should return null" {
        placeholderExpansion.onRequest(player, "i_do_not_exist") shouldBe null
        placeholderExpansion.onRequest(null, "balance") shouldBe null
    }

})