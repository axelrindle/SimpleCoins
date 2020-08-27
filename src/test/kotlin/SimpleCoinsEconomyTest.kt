import be.seeseemelk.mockbukkit.MockBukkit
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import de.axelrindle.simplecoins.hooks.SimpleCoinsEconomy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleCoinsEconomyTest : StringSpec({

    val server = MockBukkit.getMock()
    val plugin = server.pluginManager.getPlugin("SimpleCoins")!! as SimpleCoins
    val playerNoAccount = server.getPlayer(0)
    val player = server.getPlayer(2)

    CoinManager.setCoins(player.uniqueId.toString(), 69.0)

    val economy = SimpleCoinsEconomy(plugin.pocketConfig)

    "the correct currency name should be returned" {
        economy.currencyNameSingular() shouldBe "Coins"
        economy.currencyNamePlural() shouldBe "Coins"
    }

    "balances should be formatted like '69.0 Coins'" {
        economy.format(69.0) shouldBe "69.0 Coins"
    }

    "balances should be the same regardless of the world" {
        economy.getBalance(player) shouldBe 69.0
        economy.getBalance(player, "world") shouldBe 69.0
    }

    "has should return whether the player has a minimum of the given amount, regardless of the world" {
        economy.has(player, 50.0) shouldBe true
        economy.has(player, "world", 50.0) shouldBe true
        economy.has(player, "world", 70.0) shouldBe false
        economy.has(player, 70.0) shouldBe false
        economy.has(player, "world", 70.0) shouldBe false
        economy.has(player, "world", 50.0) shouldBe true
    }

    "accounts should be recognized, regardless of the world" {
        economy.hasAccount(player) shouldBe true
        economy.hasAccount(player, "world") shouldBe true
        economy.hasAccount(playerNoAccount) shouldBe false
        economy.hasAccount(playerNoAccount, "world") shouldBe false
    }

    "depositing should add the correct amount of coins" {
        val response1 = economy.depositPlayer(player, 5.0)
        response1.amount shouldBe 5.0
        response1.balance shouldBe (69.0 + 5.0)

        val response2 = economy.depositPlayer(player, "world", 5.0)
        response2.amount shouldBe 5.0
        response2.balance shouldBe (69.0 + 5.0 + 5.0)
    }

    "withdrawing should remove the correct amount of coins" {
        val response1 = economy.withdrawPlayer(player, 5.0)
        response1.amount shouldBe 5.0
        response1.balance shouldBe (69.0 + 5.0)

        val response2 = economy.withdrawPlayer(player, "world", 5.0)
        response2.amount shouldBe 5.0
        response2.balance shouldBe 69.0
    }

    "withdrawing more than a players balance should set it to 0" {
        val response1 = economy.withdrawPlayer(player, 100.0)
        response1.amount shouldBe 100.0
        response1.balance shouldBe 0

        CoinManager.setCoins(player.uniqueId.toString(), 10.0)

        val response2 = economy.withdrawPlayer(player, 100.0)
        response2.amount shouldBe 100.0
        response2.balance shouldBe 0
    }
})