package maryk.core.properties.definitions

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.models.DataModel
import maryk.core.objects.ValueMap
import maryk.core.properties.ByteCollector
import maryk.core.properties.exceptions.ValidationUmbrellaException
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType
import maryk.core.protobuf.WriteCache
import maryk.core.query.DataModelContext
import maryk.lib.extensions.toHex
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

internal class EmbeddedObjectDefinitionTest {
    private data class MarykObject(
        val string: String = "jur"
    ){
        object Properties : PropertyDefinitions<MarykObject>() {
            init {
                add(0, "string", StringDefinition(
                    regEx = "jur"
                ), MarykObject::string)
            }
        }
        companion object: DataModel<MarykObject, Properties>(
            name = "MarykObject",
            properties = Properties
        ) {
            override fun invoke(map: ValueMap<MarykObject, Properties>) = MarykObject(
                map(0)
            )
        }
    }

    private val def = EmbeddedObjectDefinition(
        dataModel = { MarykObject }
    )
    private val defMaxDefined = EmbeddedObjectDefinition(
        indexed = true,
        required = false,
        final = true,
        dataModel = { MarykObject },
        default = MarykObject("default")
    )

    @Test
    fun hasValues() {
        def.dataModel shouldBe MarykObject
    }

    @Test
    fun validate() {
        def.validateWithRef(newValue = MarykObject())
        shouldThrow<ValidationUmbrellaException> {
            def.validateWithRef(newValue = MarykObject("wrong"))
        }
    }

    @Test
    fun convert_values_to_transport_bytes_and_back() {
        val bc = ByteCollector()
        val cache = WriteCache()

        val value = MarykObject()
        val asHex = "2a0502036a7572"

        bc.reserve(
            def.calculateTransportByteLengthWithKey(5, value, cache)
        )
        bc.bytes!!.size shouldBe 7
        def.writeTransportBytesWithKey(5, value, cache, bc::write, null)

        bc.bytes!!.toHex() shouldBe asHex

        val key = ProtoBuf.readKey(bc::read)
        key.wireType shouldBe WireType.LENGTH_DELIMITED
        key.tag shouldBe 5

        def.readTransportBytes(
            ProtoBuf.getLength(WireType.LENGTH_DELIMITED, bc::read),
            bc::read
        ) shouldBe value
    }

    @Test
    fun convert_definition_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.def, EmbeddedObjectDefinition.Model, { DataModelContext() })
        checkProtoBufConversion(this.defMaxDefined, EmbeddedObjectDefinition.Model, { DataModelContext() })
    }

    @Test
    fun convert_definition_to_JSON_and_back() {
        checkJsonConversion(this.def, EmbeddedObjectDefinition.Model, { DataModelContext() })
        checkJsonConversion(this.defMaxDefined, EmbeddedObjectDefinition.Model, { DataModelContext() })
    }

    @Test
    fun convert_definition_to_YAML_and_back() {
        checkYamlConversion(this.def, EmbeddedObjectDefinition.Model, { DataModelContext() })
        checkYamlConversion(this.defMaxDefined, EmbeddedObjectDefinition.Model, { DataModelContext() }) shouldBe """
        indexed: true
        required: false
        final: true
        dataModel: MarykObject
        default:
          string: default

        """.trimIndent()
    }
}