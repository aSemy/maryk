package maryk.core.properties.definitions.key

import maryk.core.extensions.toHex
import maryk.core.objects.Def
import maryk.core.objects.RootDataModel
import maryk.core.objects.definitions
import maryk.core.properties.ByteCollector
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.AbstractSubDefinition
import maryk.core.properties.definitions.BooleanDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.definitions.StringDefinition
import maryk.core.properties.types.TypedValue
import maryk.test.shouldBe
import kotlin.test.Test

internal class TypeIdTest {
    private data class MarykObject(
            val multi: TypedValue<*>
    ){
        object Properties {
            val multi = MultiTypeDefinition(
                    name = "multi",
                    index = 0,
                    getDefinition = mapOf<Int, AbstractSubDefinition<*, IsPropertyContext>>(
                            0 to StringDefinition(),
                            1 to BooleanDefinition()
                    )::get
            )
        }
        companion object: RootDataModel<MarykObject>(
                name = "MarykObject",
                keyDefinitions = definitions(
                        TypeId(Properties.multi)
                ),
                definitions = listOf(
                    Def(Properties.multi, MarykObject::multi)
                )
        ) {
            override fun invoke(map: Map<Int, *>) = MarykObject(
                    map[0] as TypedValue<*>
            )
        }
    }

    @Test
    fun testKey(){
        val obj = MarykObject(
                multi = TypedValue(1, true)
        )

        val key = MarykObject.key.getKey(obj)
        key.bytes.toHex() shouldBe "0001"

        val keyDef = MarykObject.key.keyDefinitions[0]

        (keyDef is TypeId) shouldBe true
        val specificDef = keyDef as TypeId
        specificDef.multiTypeDefinition shouldBe MarykObject.Properties.multi

        specificDef.getValue(MarykObject, obj) shouldBe 1

        val bc = ByteCollector()
        bc.reserve(2)
        specificDef.writeStorageBytes(1, bc::write)
        specificDef.readStorageBytes(bc.size, bc::read) shouldBe 1
    }
}