package maryk.core.models.definitions

import maryk.core.definitions.MarykPrimitiveDescriptor
import maryk.core.definitions.PrimitiveType.ValueModel
import maryk.core.exceptions.SerializationException
import maryk.core.models.DefinitionModel
import maryk.core.models.IsObjectDataModel
import maryk.core.models.serializers.ObjectDataModelSerializer
import maryk.core.properties.definitions.string
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.values.ObjectValues
import maryk.json.IsJsonLikeWriter

/**
 * ObjectDataModel for objects that can be encoded in fixed length width.
 */
data class ValueDataModelDefinition(
    override val name: String,
) : ObjectDataModelDefinition(name), MarykPrimitiveDescriptor {
    override val primitiveType = ValueModel

    internal object Model : DefinitionModel<ValueDataModelDefinition>() {
        val name by string(1u, ValueDataModelDefinition::name)

        override fun invoke(values: ObjectValues<ValueDataModelDefinition, IsObjectDataModel<ValueDataModelDefinition>>) = ValueDataModelDefinition(
            name = values(name.index),
        )

        override val Serializer = object: ObjectDataModelSerializer<ValueDataModelDefinition, IsObjectDataModel<ValueDataModelDefinition>, ContainsDefinitionsContext, ContainsDefinitionsContext>(this) {
            override fun writeJson(
                values: ObjectValues<ValueDataModelDefinition, IsObjectDataModel<ValueDataModelDefinition>>,
                writer: IsJsonLikeWriter,
                context: ContainsDefinitionsContext?
            ) {
                throw SerializationException("Cannot write definitions from values")
            }
        }
    }
}
