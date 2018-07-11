package maryk.core.query.filters

import maryk.SimpleMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.models.RootObjectDataModel
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.query.DataModelPropertyContext
import maryk.test.shouldBe
import kotlin.test.Test

class NotTest {
    private val not = Not(
        Exists(SimpleMarykObject.ref { value })
    )

    @Suppress("UNCHECKED_CAST")
    private val context = DataModelPropertyContext(
        mapOf(
            SimpleMarykObject.name to { SimpleMarykObject }
        ),
        dataModel = SimpleMarykObject as RootObjectDataModel<Any, ObjectPropertyDefinitions<Any>>
    )

    @Test
    fun convert_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.not, Not, { this.context })
    }

    @Test
    fun convert_to_JSON_and_back() {
        checkJsonConversion(this.not, Not, { this.context })
    }

    @Test
    fun convert_to_YAML_and_back() {
        checkYamlConversion(this.not, Not, { this.context }) shouldBe """
        - !Exists value

        """.trimIndent()
    }
}
