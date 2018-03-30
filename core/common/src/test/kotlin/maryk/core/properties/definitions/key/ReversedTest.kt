package maryk.core.properties.definitions.key

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.objects.RootDataModel
import maryk.core.objects.definitions
import maryk.core.properties.ByteCollector
import maryk.core.properties.definitions.BooleanDefinition
import maryk.core.properties.definitions.DateTimeDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.DateTime
import maryk.core.query.DataModelContext
import maryk.test.shouldBe
import kotlin.test.Test

internal class ReversedTest {
    private data class MarykObject(
        val boolean: Boolean,
        val dateTime: DateTime
    ){
        object Properties : PropertyDefinitions<MarykObject>() {
            val boolean = add(0, "bool", BooleanDefinition(
                final = true
            ), MarykObject::boolean)
            val dateTime = add(1, "dateTime", DateTimeDefinition(
                final = true
            ), MarykObject::dateTime)
        }
        companion object: RootDataModel<MarykObject, Properties>(
            name = "MarykObject",
            keyDefinitions = definitions(
                Reversed(Properties.boolean),
                Reversed(Properties.dateTime)
            ),
            properties = Properties
        ) {
            override fun invoke(map: Map<Int, *>) = MarykObject(
                map[0] as Boolean,
                map[1] as DateTime
            )
        }
    }

    @Test
    fun testKey(){
        val dt = DateTime(year = 2017, month = 9, day = 3, hour = 12, minute = 43, second = 40)

        val obj = MarykObject(
            boolean = true,
            dateTime = dt
        )

        val key = MarykObject.key.getKey(obj)

        @Suppress("UNCHECKED_CAST")
        with(MarykObject.key.keyDefinitions[1] as Reversed<DateTime>) {
            val bc = ByteCollector()
            bc.reserve(8)
            this.writeStorageBytes(dt, bc::write)
            this.readStorageBytes(bc.size, bc::read) shouldBe dt
        }

        key.toHex() shouldBe "fe017fffffa6540703"
    }

    private val context = DataModelContext(
        propertyDefinitions = MarykObject.Properties
    )

    @Test
    fun convert_definition_to_ProtoBuf_and_back() {
        checkProtoBufConversion(
            value = Reversed(MarykObject.Properties.boolean.getRef()),
            dataModel = Reversed.Model,
            context = context
        )
    }

    @Test
    fun convert_definition_to_JSON_and_back() {
        checkJsonConversion(
            value = Reversed(MarykObject.Properties.boolean.getRef()),
            dataModel = Reversed.Model,
            context = context
        )
    }
}