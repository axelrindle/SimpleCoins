import de.axelrindle.simplecoins.CoinManager
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CoinManagerTest : StringSpec({

    val uuid = "2dcd2efe-e0ef-40e9-81c5-345de1b8ff65"

    "getCurrentName() should return the default value (Coins)" {
        CoinManager.getCurrentName() shouldBe "Coins"
    }

    "hasPlayer() should be false when account does not exist" {
        CoinManager.hasPlayer(uuid) shouldBe false
    }

    "getCoins() should return 0 when account does not exist" {
        CoinManager.getCoins(uuid) shouldBe 0.0
    }

    "addPlayer() should return true and then false" {
        CoinManager.addPlayer(uuid) shouldBe true
        CoinManager.addPlayer(uuid) shouldBe false
    }

    "hasPlayer() should be true when the account exists" {
        CoinManager.hasPlayer(uuid) shouldBe true
    }

    "setCoins() should return 10 when the account exists" {
        CoinManager.setCoins(uuid, 10.0) shouldBe 10.0
    }

    "getCoins() should return 10 when the account exists" {
        CoinManager.getCoins(uuid) shouldBe 10.0
    }

    "removeCoins() should return 0 with a value greater than the actual amount" {
        CoinManager.removeCoins(uuid, 1000.0) shouldBe 0.0
    }
})