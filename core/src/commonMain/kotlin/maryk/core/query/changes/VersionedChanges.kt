@file:Suppress("EXPERIMENTAL_API_USAGE")

package maryk.core.query.changes

import maryk.core.models.QueryDataModel
import maryk.core.objects.ObjectValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.definitions.NumberDefinition
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.UInt64

/** Contains a list of [changes] that belongs to a [version] */
data class VersionedChanges(
    val version: ULong,
    val changes: List<IsChange>
) {
    object Properties : ObjectPropertyDefinitions<VersionedChanges>() {
        val version = add(1, "version", NumberDefinition(
            type = UInt64
        ), VersionedChanges::version)

        val changes = add(2, "changes",
            ListDefinition(
                default = emptyList(),
                valueDefinition = MultiTypeDefinition(
                    typeEnum = ChangeType,
                    definitionMap = mapOfChangeDefinitions
                )
            ),
            getter = VersionedChanges::changes,
            toSerializable = { TypedValue(it.changeType, it) },
            fromSerializable = { it.value as IsChange }
        )
    }

    companion object: QueryDataModel<VersionedChanges, Properties>(
        properties = Properties
    ) {
        override fun invoke(map: ObjectValues<VersionedChanges, Properties>) = VersionedChanges(
            version = map(1),
            changes = map(2)
        )
    }
}