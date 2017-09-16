package maryk.core.properties.definitions

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import maryk.TestValueObject
import maryk.core.properties.ByteCollector
import maryk.core.properties.exceptions.PropertyOutOfRangeException
import maryk.core.properties.exceptions.PropertyValidationUmbrellaException
import maryk.core.properties.types.Date
import maryk.core.properties.types.DateTime
import maryk.core.properties.types.Time
import org.junit.Test

internal class ValueModelDefinitionTest {
    val def = ValueModelDefinition(
            name = "test",
            dataModel = TestValueObject
    )

    @Test
    fun hasValues() {
        def.dataModel shouldBe TestValueObject
    }

    val value = TestValueObject(
            int = 4,
            dateTime = DateTime(date = Date.nowUTC(), time = Time.nowUTC().copy(milli = 0)),
            bool = true
    )

    @Test
    fun testConvertBytes() {
        val bytes = value._bytes
        val new = TestValueObject.createFromBytes(bytes, 0)

        new shouldBe value
    }

    @Test
    fun testConvertStreamableBytes() {
        val bc = ByteCollector()
        def.convertToBytes(value, bc::reserve, bc::write)
        val new = def.convertFromBytes(bc.size, bc::read)

        new shouldBe value
    }

    @Test
    fun testConvertString() {
        def.convertFromString(
                def.convertToString(value)
        ) shouldBe value
    }

    @Test
    fun validate() {
        def.validate(newValue = TestValueObject(
                int = 4,
                dateTime = DateTime.nowUTC(),
                bool = true
        ))
        val e = shouldThrow<PropertyValidationUmbrellaException> {
            def.validate(newValue = TestValueObject(
                    int = 1000,
                    dateTime = DateTime.nowUTC(),
                    bool = true
            ))
        }

        e.exceptions.size shouldBe 1

        with (e.exceptions[0]) {
            this is PropertyOutOfRangeException
            this.reference!!.completeName shouldBe "test.int"
        }
    }
}