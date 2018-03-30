package maryk.core.properties.definitions

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.properties.ByteCollector
import maryk.core.properties.exceptions.InvalidSizeException
import maryk.core.properties.exceptions.ParseException
import maryk.core.properties.types.Bytes
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

internal class FlexBytesDefinitionTest {
    private val flexBytesToTest = arrayOf(
        Bytes(ByteArray(5, { 0x00.toByte() } )),
        Bytes(ByteArray(7, { 0xFF.toByte() } )),
        Bytes(ByteArray(9, { if (it % 2 == 1) 0x88.toByte() else 0xFF.toByte() } ))
    )

    val def = FlexBytesDefinition(
        minSize = 4,
        maxSize = 10
    )

    val defMaxDefined = FlexBytesDefinition(
        indexed = true,
        required = false,
        final = true,
        searchable = false,
        unique = true,
        minValue = Bytes.ofHex("0000000000"),
        maxValue = Bytes.ofHex("AAAAAAAAAA"),
        minSize = 4,
        maxSize = 10
    )

    @Test
    fun validate_values() {
        // Should both succeed without errors
        def.validateWithRef(newValue = Bytes(ByteArray(4, { 0x00.toByte() } )))
        def.validateWithRef(newValue = Bytes(ByteArray(5, { 0x00.toByte() } )))
        def.validateWithRef(newValue = Bytes(ByteArray(10, { 0x00.toByte() } )))

        shouldThrow<InvalidSizeException> {
            def.validateWithRef(newValue = Bytes(ByteArray(1, { 0x00.toByte() } )))
        }
        shouldThrow<InvalidSizeException> {
            def.validateWithRef(newValue = Bytes(ByteArray(20, { 0x00.toByte() } )))
        }
    }

    @Test
    fun convert_values_to_storage_bytes_and_back() {
        val bc = ByteCollector()
        for (it in flexBytesToTest) {
            bc.reserve(
                def.calculateStorageByteLength(it)
            )
            def.writeStorageBytes(it, bc::write)
            def.readStorageBytes(bc.size, bc::read) shouldBe it
            bc.reset()
        }
    }

    @Test
    fun convert_values_to_transport_bytes_and_back() {
        val bc = ByteCollector()
        for (it in flexBytesToTest) {
            checkProtoBufConversion(bc, it, this.def)
        }
    }

    @Test
    fun convert_values_to_String_and_back() {
        for (it in flexBytesToTest) {
            val b = def.asString(it)
            def.fromString(b) shouldBe it
        }
    }

    @Test
    fun invalid_String_value_should_throw_exception() {
        shouldThrow<ParseException> {
            def.fromString("wrong§")
        }
    }

    @Test
    fun convert_definition_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.def, FlexBytesDefinition.Model)
        checkProtoBufConversion(this.defMaxDefined, FlexBytesDefinition.Model)
    }

    @Test
    fun convert_definition_to_JSON_and_back() {
        checkJsonConversion(this.def, FlexBytesDefinition.Model)
        checkJsonConversion(this.defMaxDefined, FlexBytesDefinition.Model)
    }
}