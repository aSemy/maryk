package maryk.core.properties.definitions

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import maryk.core.extensions.toHex
import maryk.core.objects.DataModel
import maryk.core.objects.Def
import maryk.core.properties.GrowableByteCollector
import maryk.core.properties.exceptions.PropertyValidationUmbrellaException
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType
import org.junit.Test

internal class SubModelDefinitionTest {
    private data class MarykObject(
            val string: String = "jur"
    ){
        object Properties {
            val string = StringDefinition(
                    name = "string",
                    index = 0,
                    regEx = "jur"
            )
        }
        companion object: DataModel<MarykObject>(
                construct = { MarykObject(it[0] as String)},
                definitions = listOf(
                Def(Properties.string, MarykObject::string)
        ))
    }

    private val def = SubModelDefinition(
            name = "test",
            index = 1,
            dataModel = MarykObject
    )

    @Test
    fun hasValues() {
        def.dataModel shouldBe MarykObject
    }

    @Test
    fun testReference() {
        def.getRef().dataModel shouldBe MarykObject
    }

    @Test
    fun validate() {
        def.validate(newValue = MarykObject())
        shouldThrow<PropertyValidationUmbrellaException> {
            def.validate(newValue = MarykObject("wrong"))
        }
    }

    @Test
    fun testTransportConversion() {
        val bc = GrowableByteCollector()

        val value = MarykObject()
        val asHex = "0b02036a75720c"

        def.writeTransportBytesWithKey(value, bc::reserve, bc::write)

        bc.bytes.toHex() shouldBe asHex

        val key = ProtoBuf.readKey(bc::read)
        key.wireType shouldBe WireType.START_GROUP
        key.tag shouldBe 1

        def.readTransportBytes(
                ProtoBuf.getLength(WireType.START_GROUP, bc::read),
                bc::read
        ) shouldBe value
    }
}