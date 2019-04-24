package maryk.core.properties.definitions

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.properties.WriteCacheFailer
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType.VAR_INT
import maryk.lib.exceptions.ParseException
import maryk.test.ByteCollector
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

internal class BooleanDefinitionTest {
    val def = BooleanDefinition()
    val defMaxDefined = BooleanDefinition(
        required = false,
        final = true,
        default = true
    )

    @Test
    fun convertValuesToStorageBytesAndBack() {
        val bc = ByteCollector()
        for (it in booleanArrayOf(true, false)) {
            bc.reserve(
                def.calculateStorageByteLength(it)
            )
            def.writeStorageBytes(it, bc::write)
            def.readStorageBytes(bc.size, bc::read) shouldBe it
            bc.reset()
        }
    }

    @Test
    fun convertValuesToTransportBytesAndBack() {
        val bc = ByteCollector()
        val cacheFailer = WriteCacheFailer()

        for (it in booleanArrayOf(true, false)) {
            bc.reserve(
                def.calculateTransportByteLengthWithKey(23u, it, cacheFailer, null)
            )
            def.writeTransportBytesWithKey(23u, it, cacheFailer, bc::write, null)
            val key = ProtoBuf.readKey(bc::read)
            key.tag shouldBe 23u
            key.wireType shouldBe VAR_INT
            def.readTransportBytes(
                ProtoBuf.getLength(VAR_INT, bc::read),
                bc::read
            ) shouldBe it
            bc.reset()
        }
    }

    @Test
    fun convertValuesToStringAndBack() {
        for (it in booleanArrayOf(true, false)) {
            val b = def.asString(it)
            def.fromString(b) shouldBe it
        }
    }

    @Test
    fun invalidStringValueShouldThrowException() {
        shouldThrow<ParseException> {
            def.fromString("wrong")
        }
    }

    @Test
    fun convertDefinitionToProtoBufAndBack() {
        checkProtoBufConversion(this.def, BooleanDefinition.Model)
        checkProtoBufConversion(this.defMaxDefined, BooleanDefinition.Model)
    }

    @Test
    fun convertDefinitionToJSONAndBack() {
        checkJsonConversion(this.def, BooleanDefinition.Model)
        checkJsonConversion(this.defMaxDefined, BooleanDefinition.Model)
    }

    @Test
    fun convertDefinitionToYAMLAndBack() {
        checkYamlConversion(this.def, BooleanDefinition.Model)
        checkYamlConversion(this.defMaxDefined, BooleanDefinition.Model) shouldBe """
        required: false
        final: true
        default: true

        """.trimIndent()
    }
}
