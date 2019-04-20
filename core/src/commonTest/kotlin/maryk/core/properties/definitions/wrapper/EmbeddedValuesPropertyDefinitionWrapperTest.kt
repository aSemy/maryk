package maryk.core.properties.definitions.wrapper

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.properties.definitions.EmbeddedValuesDefinition
import maryk.core.query.DefinitionsContext
import maryk.test.models.EmbeddedMarykModel
import kotlin.test.Test

class EmbeddedValuesPropertyDefinitionWrapperTest {
    private val def = EmbeddedValuesPropertyDefinitionWrapper(
        index = 1u,
        name = "wrapper",
        definition = EmbeddedValuesDefinition(
            dataModel = { EmbeddedMarykModel }
        ),
        getter = { null }
    )

    @Test
    fun convertDefinitionToProtoBufAndBack() {
        checkProtoBufConversion(
            this.def,
            IsPropertyDefinitionWrapper.Model,
            { DefinitionsContext() },
            ::comparePropertyDefinitionWrapper
        )
    }

    @Test
    fun convertDefinitionToJSONAndBack() {
        checkJsonConversion(
            this.def,
            IsPropertyDefinitionWrapper.Model,
            { DefinitionsContext() },
            ::comparePropertyDefinitionWrapper
        )
    }
}
