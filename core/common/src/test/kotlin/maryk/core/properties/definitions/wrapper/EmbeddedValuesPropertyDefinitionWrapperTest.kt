package maryk.core.properties.definitions.wrapper

import maryk.EmbeddedMarykModel
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.properties.definitions.EmbeddedValuesDefinition
import maryk.core.query.DataModelContext
import kotlin.test.Test

class EmbeddedValuesPropertyDefinitionWrapperTest {
    private val def = EmbeddedValuesPropertyDefinitionWrapper(
        index = 1,
        name = "wrapper",
        definition = EmbeddedValuesDefinition(
            dataModel = { EmbeddedMarykModel }
        ),
        getter = { _: Any -> null }
    )

    @Test
    fun convert_definition_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.def, IsPropertyDefinitionWrapper.Model, { DataModelContext() }, ::comparePropertyDefinitionWrapper)
    }

    @Test
    fun convert_definition_to_JSON_and_back() {
        checkJsonConversion(this.def, IsPropertyDefinitionWrapper.Model, { DataModelContext() }, ::comparePropertyDefinitionWrapper)
    }
}
