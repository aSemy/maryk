package maryk

import maryk.core.models.ObjectDataModel
import maryk.core.objects.ObjectValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.StringDefinition

data class EmbeddedMarykObject(
    val value: String,
    val model: EmbeddedMarykObject? = null,
    val marykModel: TestMarykObject? = null
){
    object Properties : ObjectPropertyDefinitions<EmbeddedMarykObject>() {
        val value = add(
            index = 0, name = "value",
            definition = StringDefinition(),
            getter = EmbeddedMarykObject::value
        )
        val model = add(
            index = 1, name = "model",
            definition = EmbeddedObjectDefinition(
                required = false,
                dataModel = { EmbeddedMarykObject }
            ),
            getter = EmbeddedMarykObject::model
        )
        val marykModel = add(
            index = 2, name = "marykModel",
            definition = EmbeddedObjectDefinition(
                required = false,
                dataModel = { TestMarykObject }
            ),
            getter = EmbeddedMarykObject::marykModel
        )
    }
    companion object: ObjectDataModel<EmbeddedMarykObject, Properties>(
        name = "EmbeddedMarykObject",
        properties = Properties
    ) {
        override fun invoke(map: ObjectValues<EmbeddedMarykObject, Properties>) =
            EmbeddedMarykObject(
                value = map(0),
                model = map(1),
                marykModel = map(2)
            )

        override fun equals(other: Any?): Boolean {
            if (other !is ObjectDataModel<*, *>) return false

            @Suppress("UNCHECKED_CAST")
            val otherModel = other as ObjectDataModel<Any, ObjectPropertyDefinitions<Any>>

            if (this.name != otherModel.name) return false
            if (this.properties.size != otherModel.properties.size) return false

            return true
        }
    }
}
