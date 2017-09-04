package maryk.core.properties.definitions

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import maryk.core.properties.exceptions.*
import org.junit.Test
import kotlin.test.assertTrue

internal class SetDefinitionTest {
    val subDef = StringDefinition(
            name = "string",
            regEx = "T.*",
            required = true
    )

    val def = SetDefinition<String>(
            name = "stringSet",
            minSize = 2,
            maxSize = 4,
            required = true,
            valueDefinition = subDef
    )

    val def2 = SetDefinition<String>(
            name = "stringSet",
            valueDefinition = subDef
    )

    @Test
    fun testValidateRequired() {
        def2.validate(newValue = null)

        shouldThrow<PropertyRequiredException> {
            def.validate(newValue = null)
        }
    }

    @Test
    fun testValidateSize() {
        def.validate(newValue = setOf("T", "T2"))
        def.validate(newValue = setOf("T", "T2", "T3"))
        def.validate(newValue = setOf("T", "T2", "T3", "T4"))

        shouldThrow<PropertyTooLittleItemsException> {
            def.validate(newValue = setOf("T"))
        }

        shouldThrow<PropertyTooMuchItemsException> {
            def.validate(newValue = setOf("T", "T2", "T3", "T4", "T5"))
        }
    }

    @Test
    fun testValidateContent() {
        val e = shouldThrow<PropertyValidationUmbrellaException> {
            def.validate(newValue = setOf("T", "WRONG", "WRONG2"))
        }
        e.exceptions.size shouldBe 2

        assertTrue(e.exceptions[0] is PropertyInvalidValueException)
        assertTrue(e.exceptions[1] is PropertyInvalidValueException)
    }
}