package maryk.core.query.changes

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.IsRootDataModel
import maryk.core.models.QueryDataModel
import maryk.core.objects.ObjectValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.contextual.ContextualReferenceDefinition
import maryk.core.properties.types.Key
import maryk.core.query.RequestContext

/**
 * Contains versioned [changes] for a specific DataObject by [key]
 */
data class DataObjectVersionedChange<out DM: IsRootDataModel<*>>(
    val key: Key<DM>,
    val changes: List<VersionedChanges>
) {
    object Properties : ObjectPropertyDefinitions<DataObjectVersionedChange<*>>() {
        val key = add(1, "key", ContextualReferenceDefinition<RequestContext>(
            contextualResolver = {
                it?.dataModel as IsRootDataModel<*>? ?: throw ContextNotFoundException()
            }
        ), DataObjectVersionedChange<*>::key)

        val changes = add(2, "changes", ListDefinition(
            default = emptyList(),
            valueDefinition = EmbeddedObjectDefinition(
                dataModel = { VersionedChanges }
            )
        ), DataObjectVersionedChange<*>::changes)
    }

    companion object: QueryDataModel<DataObjectVersionedChange<*>, Properties>(
        properties = Properties
    ) {
        override fun invoke(map: ObjectValues<DataObjectVersionedChange<*>, Properties>) = DataObjectVersionedChange(
            key = map(1),
            changes = map(2)
        )
    }
}