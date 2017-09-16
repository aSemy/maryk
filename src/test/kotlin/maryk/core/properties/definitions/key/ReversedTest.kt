package maryk.core.properties.definitions.key

import io.kotlintest.matchers.shouldBe
import maryk.core.objects.Def
import maryk.core.objects.RootDataModel
import maryk.core.objects.definitions
import maryk.core.properties.ByteCollector
import maryk.core.properties.definitions.BooleanDefinition
import maryk.core.properties.definitions.DateTimeDefinition
import maryk.core.properties.types.DateTime
import org.junit.Test

internal class ReversedTest {
    private data class MarykObject(
            val boolean: Boolean,
            val dateTime: DateTime
    ){
        object Properties {
            val boolean = BooleanDefinition(
                    index = 0,
                    name = "boolean",
                    required = true,
                    final = true
            )
            val dateTime = DateTimeDefinition(
                    name = "dateTime",
                    index = 1,
                    required = true,
                    final = true
            )
        }
        companion object: RootDataModel<MarykObject>(
            keyDefinitions = definitions(
                    Reversed(Properties.boolean),
                    Reversed(Properties.dateTime)
            ),
            definitions = listOf(
                    Def(Properties.boolean, MarykObject::boolean),
                    Def(Properties.dateTime, MarykObject::dateTime)
            )
        )
    }

    @Test
    fun testKey(){
        val dt = DateTime(year = 2017, month = 9, day = 3, hour = 12, minute = 43, second = 40)

        val obj = MarykObject(
                boolean = true,
                dateTime = dt
        )

        val key = MarykObject.key.getKey(obj)

        key.toHex() shouldBe "fe017fffffa6540703"

        @Suppress("UNCHECKED_CAST")
        with(MarykObject.key.keyDefinitions[1] as Reversed<DateTime>){
            this.convertFromBytes(key.bytes, 2) shouldBe dt

            val bc = ByteCollector()
            this.convertToBytes(dt, bc::reserve, bc::write)
            this.convertFromBytes(bc.size, bc::read) shouldBe dt
        }
    }
}