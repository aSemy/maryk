package maryk.core.properties.definitions

import maryk.core.properties.exceptions.OutOfRangeException
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

internal class AbstractSimpleDefinitionTest {
    val test: String = "test"

    val def = StringDefinition(
            unique = true,
            minValue = "bbb",
            maxValue = "ddd"
    )

    @Test
    fun hasValues() {
        def.unique shouldBe true
        def.minValue shouldBe "bbb"
        def.maxValue shouldBe "ddd"
    }

    @Test
    fun validateValueSize() {
        def.validateWithRef(newValue = "ccc")
        shouldThrow<OutOfRangeException> {
            def.validateWithRef(newValue = "b")
        }

        shouldThrow<OutOfRangeException> {
            def.validateWithRef(newValue = "z")
        }
    }
}