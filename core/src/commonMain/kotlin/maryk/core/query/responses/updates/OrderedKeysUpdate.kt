package maryk.core.query.responses.updates

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.IsRootDataModel
import maryk.core.models.IsRootValuesDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.InternalMultiTypeDefinition
import maryk.core.properties.definitions.ReferenceDefinition
import maryk.core.properties.definitions.contextual.ContextualReferenceDefinition
import maryk.core.properties.definitions.list
import maryk.core.properties.definitions.number
import maryk.core.properties.types.Key
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.SInt32
import maryk.core.properties.types.numeric.UInt64
import maryk.core.query.RequestContext
import maryk.core.query.changes.ChangeType
import maryk.core.query.changes.IsChange
import maryk.core.query.changes.mapOfChangeDefinitions
import maryk.core.query.responses.statuses.addKey
import maryk.core.query.responses.updates.UpdateResponseType.Change
import maryk.core.query.responses.updates.UpdateResponseType.OrderedKeys
import maryk.core.values.SimpleObjectValues

/**
 * Update response for describing the initial order and visibility of the Values
 * when requesting changes from a Get or Scan.
 * This is sent for the initial change and describes the order of [keys] at [version]
 *
 * This way the listener is always sure of the current state on which orders are changed.
 */
data class OrderedKeysUpdate<DM: IsRootValuesDataModel<P>, P: PropertyDefinitions>(
    val keys: List<Key<DM>>,
    override val version: ULong
) : IsUpdateResponse<DM, P> {
    override val type = OrderedKeys

    @Suppress("unused")
    object Properties : ObjectPropertyDefinitions<OrderedKeysUpdate<*, *>>() {
        val keys by list(
            index = 1u,
            getter = OrderedKeysUpdate<*, *>::keys,
            valueDefinition = ContextualReferenceDefinition<RequestContext>(
                contextualResolver = {
                    it?.dataModel as IsRootDataModel<*>? ?: throw ContextNotFoundException()
                }
            )
        )
        val version by number(2u, getter = OrderedKeysUpdate<*, *>::version, type = UInt64)
    }

    internal companion object : SimpleQueryDataModel<OrderedKeysUpdate<*, *>>(
        properties = Properties
    ) {
        override fun invoke(values: SimpleObjectValues<OrderedKeysUpdate<*, *>>) = OrderedKeysUpdate<IsRootValuesDataModel<PropertyDefinitions>, PropertyDefinitions>(
            keys = values(1u),
            version = values(2u)
        )
    }
}
