package maryk.core.properties.definitions.key

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.objects.DefinitionDataModel
import maryk.core.properties.definitions.IsSubDefinition
import maryk.core.properties.definitions.SubModelDefinition
import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.types.IndexedEnum
import maryk.core.properties.types.IndexedEnumDefinition
import maryk.core.query.DataModelContext
import maryk.json.TokenType
import maryk.json.ValueType

/** Indexed type of property definitions */
sealed class KeyPartType(
    override val name: String,
    override val index: Int
): IndexedEnum<KeyPartType>, TokenType {
    override fun compareTo(other: KeyPartType) =
        this.index.compareTo(other.index)

    object UUID: KeyPartType("UUID", 0), ValueType.IsNullValueType
    object Reference: KeyPartType("Ref", 1), ValueType<String>
    object TypeId: KeyPartType("TypeId", 2), ValueType<String>
    object Reversed: KeyPartType("Reversed", 3), ValueType<String>

    companion object: IndexedEnumDefinition<KeyPartType>(
        "KeyPartType", { keyPartValues }
    )
}

val keyPartValues = arrayOf<KeyPartType>(KeyPartType.UUID, KeyPartType.Reference, KeyPartType.TypeId, KeyPartType.Reversed)

internal val mapOfKeyPartDefinitions = mapOf<KeyPartType, IsSubDefinition<*, DataModelContext>>(
    KeyPartType.UUID to SubModelDefinition(dataModel = { UUIDKey.Model }),
    KeyPartType.Reference to ContextualPropertyReferenceDefinition(
        contextualResolver = {
            it?.propertyDefinitions ?: throw ContextNotFoundException()
        }
    ),
    KeyPartType.TypeId to SubModelDefinition(dataModel = {
        @Suppress("UNCHECKED_CAST")
        TypeId.Model as DefinitionDataModel<TypeId<IndexedEnum<Any>>>
    }),
    KeyPartType.Reversed to SubModelDefinition(dataModel = { Reversed.Model })
)
