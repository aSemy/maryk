package maryk.core.query.responses.statuses

import maryk.core.models.SimpleQueryDataModel
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.definitions.NumberDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.Key
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.UInt64
import maryk.core.query.changes.ChangeType
import maryk.core.query.changes.IsChange
import maryk.core.query.changes.mapOfChangeDefinitions

/** Successful add of given object with [key], [version] and added [changes] */
data class AddSuccess<DO: Any>(
    val key: Key<DO>,
    val version: UInt64,
    val changes: List<IsChange>
) : IsAddResponseStatus<DO> {
    override val statusType = StatusType.ADD_SUCCESS

    internal companion object: SimpleQueryDataModel<AddSuccess<*>>(
        properties = object : PropertyDefinitions<AddSuccess<*>>(){
            init {
                IsResponseStatus.addKey(this, AddSuccess<*>::key)
                add(1,"version", NumberDefinition(type = UInt64), AddSuccess<*>::version)
                add(2,"changes",
                    ListDefinition(
                        default = emptyList(),
                        valueDefinition = MultiTypeDefinition(
                            typeEnum = ChangeType,
                            definitionMap = mapOfChangeDefinitions
                        )
                    ),
                    getter = maryk.core.query.responses.statuses.AddSuccess<*>::changes,
                    toSerializable = { TypedValue(it.changeType, it) },
                    fromSerializable = { it.value as IsChange }
                )
            }
        }
    ) {
        override fun invoke(map: Map<Int, *>) = AddSuccess(
            key = map(0),
            version = map(1),
            changes = map(2)
        )
    }
}
