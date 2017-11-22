package maryk.core.properties.definitions

import maryk.TestMarykObject
import maryk.core.extensions.toHex
import maryk.core.json.JsonReader
import maryk.core.json.JsonWriter
import maryk.core.properties.ByteCollectorWithLengthCacher
import maryk.core.properties.exceptions.PropertyInvalidValueException
import maryk.core.properties.exceptions.PropertyOutOfRangeException
import maryk.core.properties.exceptions.PropertyTooLittleItemsException
import maryk.core.properties.exceptions.PropertyTooMuchItemsException
import maryk.core.properties.exceptions.PropertyValidationUmbrellaException
import maryk.core.properties.types.numeric.SInt32
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test
import kotlin.test.assertTrue

internal class MapDefinitionTest {
    private val intDef = NumberDefinition(
            type = SInt32,
            required = true,
            maxValue = 1000
    )

    private val stringDef = StringDefinition(
            required = true,
            regEx = "#.*"
    )

    private val subModelDef = SubModelDefinition(
            name = "marykRef",
            dataModel = TestMarykObject,
            required = true
    )

    private val def = MapDefinition(
            name = "intStringMap",
            index = 4,
            minSize = 2,
            maxSize = 4,
            keyDefinition = intDef,
            valueDefinition = stringDef
    )

    private val value = mapOf(
            12 to "#twelve",
            30 to "#thirty",
            100 to "#hundred",
            1000 to "#thousand"
    )

    @Test
    fun testValidateSize() {
        def.validate(newValue = mapOf(
                12 to "#twelve",
                30 to "#thirty"
        ))
        def.validate(newValue = mapOf(
                12 to "#twelve",
                30 to "#thirty",
                100 to "#hundred",
                1000 to "#thousand"
        ))

        shouldThrow<PropertyTooLittleItemsException> {
            def.validate(newValue = mapOf(
                    1 to "#one"
            ))
        }

        shouldThrow<PropertyTooMuchItemsException> {
            def.validate(newValue = mapOf(
                    12 to "#twelve",
                    30 to "#thirty",
                    100 to "#hundred",
                    1000 to "#thousand",
                    0 to "#zero"
            ))
        }
    }

    @Test
    fun testValidateContent() {
        val e = shouldThrow<PropertyValidationUmbrellaException> {
            def.validate(newValue = mapOf(
                    12 to "#twelve",
                    30 to "WRONG",
                    1001 to "#thousandone",
                    3000 to "#threethousand"
            ))
        }
        e.exceptions.size shouldBe 3

        with(e.exceptions[0]) {
            assertTrue(this is PropertyInvalidValueException)
            this.reference!!.completeName shouldBe "intStringMap.@30"
        }

        with(e.exceptions[1]) {
            assertTrue(this is PropertyOutOfRangeException)
            this.reference!!.completeName shouldBe "intStringMap.$1001"
        }

        with(e.exceptions[2]) {
            assertTrue(this is PropertyOutOfRangeException)
            this.reference!!.completeName shouldBe "intStringMap.$3000"
        }
    }

    @Test
    fun testTransportConversion() {
        val bc = ByteCollectorWithLengthCacher()

        bc.reserve(
                def.calculateTransportByteLengthWithKey(value, bc::addToCache)
        )
        def.writeTransportBytesWithKey(value, bc::nextLengthFromCache, bc::write)

        bc.bytes!!.toHex() shouldBe "220c08181207237477656c7665220c083c120723746869727479220e08c80112082368756e64726564220f08d00f12092374686f7573616e64"

        fun readKey() {
            val key = ProtoBuf.readKey(bc::read)
            key.wireType shouldBe WireType.LENGTH_DELIMITED
            key.tag shouldBe 4
        }

        fun readValue(): Pair<Int, String> {
            ProtoBuf.getLength(WireType.LENGTH_DELIMITED, bc::read)
            return def.readMapTransportBytes(bc::read)
        }

        this.value.forEach {
            readKey()
            val mapValue = readValue()
            mapValue.first shouldBe it.key
            mapValue.second shouldBe it.value
        }
    }

    @Test
    fun testJsonConversion() {
        var totalString = ""
        def.writeJsonValue(value, JsonWriter { totalString += it })

        totalString shouldBe "{\"12\":\"#twelve\",\"30\":\"#thirty\",\"100\":\"#hundred\",\"1000\":\"#thousand\"}"

        val iterator = totalString.iterator()
        val reader = JsonReader { iterator.nextChar() }
        reader.nextToken()
        val converted = def.readJson(reader)

        converted shouldBe this.value
    }
}