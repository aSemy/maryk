package maryk.core.extensions

import io.kotlintest.matchers.shouldBe
import kotlin.test.Test

internal class RandomKtTest {
    @Test
    fun randomBytes() {
        randomBytes(7).size shouldBe 7
    }
}