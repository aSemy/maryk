package maryk.core.query.requests

import maryk.core.objects.QueryDataModel
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.BooleanDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualReferenceDefinition
import maryk.core.properties.types.Key
import maryk.core.query.DataModelPropertyContext

/**
 * A Request to delete [objectsToDelete] from [dataModel]. If [hardDelete] is false the data will still exist but is
 * not possible to request from server.
 */
data class DeleteRequest<DO: Any, out DM: RootDataModel<DO, *>>(
    override val dataModel: DM,
    val objectsToDelete: List<Key<DO>>,
    val hardDelete: Boolean = false
) : IsObjectRequest<DO, DM> {
    constructor(
        dataModel: DM, vararg objectToDelete: Key<DO>, hardDelete: Boolean
    ) : this(dataModel, objectToDelete.toList(), hardDelete)

    internal companion object: QueryDataModel<DeleteRequest<*, *>>(
        properties = object : PropertyDefinitions<DeleteRequest<*, *>>() {
            init {
                IsObjectRequest.addDataModel(this, DeleteRequest<*, *>::dataModel)

                add(1, "objectsToDelete", ListDefinition(
                    valueDefinition = ContextualReferenceDefinition<DataModelPropertyContext>(
                        contextualResolver = { it!!.dataModel!!.key }
                    )
                ),DeleteRequest<*, *>::objectsToDelete)

                add(2, "hardDelete", BooleanDefinition(), DeleteRequest<*,*>::hardDelete)
            }
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = DeleteRequest(
            dataModel = map[0] as RootDataModel<Any, *>,
            objectsToDelete = map[1] as List<Key<Any>>,
            hardDelete = map[2] as Boolean
        )
    }
}