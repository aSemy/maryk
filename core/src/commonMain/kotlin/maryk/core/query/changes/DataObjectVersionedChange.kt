package maryk.core.query.changes

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.IsRootDataModel
import maryk.core.models.QueryDataModel
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.contextual.ContextualReferenceDefinition
import maryk.core.properties.definitions.list
import maryk.core.properties.definitions.wrapper.contextual
import maryk.core.properties.types.Key
import maryk.core.query.RequestContext
import maryk.core.values.ObjectValues

/**
 * Contains versioned [changes] for a specific DataObject by [key]
 */
data class DataObjectVersionedChange<out DM : IsRootDataModel<*>>(
    val key: Key<DM>,
    val changes: List<VersionedChanges>
) {
    object Properties : ObjectPropertyDefinitions<DataObjectVersionedChange<*>>() {
        val key by contextual(
            index = 1u,
            getter = DataObjectVersionedChange<*>::key,
            definition = ContextualReferenceDefinition<RequestContext>(
                contextualResolver = {
                    it?.dataModel as IsRootDataModel<*>? ?: throw ContextNotFoundException()
                }
            )
        )

        val changes by list(
            index = 2u,
            getter = DataObjectVersionedChange<*>::changes,
            default = emptyList(),
            valueDefinition = EmbeddedObjectDefinition(
                dataModel = { VersionedChanges }
            )
        )
    }

    companion object : QueryDataModel<DataObjectVersionedChange<*>, Properties>(
        properties = Properties
    ) {
        override fun invoke(values: ObjectValues<DataObjectVersionedChange<*>, Properties>) = DataObjectVersionedChange(
            key = values(1u),
            changes = values(2u)
        )
    }
}
