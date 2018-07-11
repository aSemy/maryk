package maryk.core.query.responses

import maryk.core.models.IsRootDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.objects.SimpleValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.query.changes.DataObjectChange

/** Response with all [changes] since version in request to [dataModel] */
data class ObjectChangesResponse<DO: Any, out DM: IsRootDataModel<*>>(
    override val dataModel: DM,
    val changes: List<DataObjectChange<DO>>
) : IsDataModelResponse<DM> {
    internal companion object: SimpleQueryDataModel<ObjectChangesResponse<*, *>>(
        properties = object : ObjectPropertyDefinitions<ObjectChangesResponse<*, *>>() {
            init {
                IsDataModelResponse.addDataModel(this, ObjectChangesResponse<*, *>::dataModel)
                add(1, "changes", ListDefinition(
                    valueDefinition = EmbeddedObjectDefinition(
                        dataModel = { DataObjectChange }
                    )
                ), ObjectChangesResponse<*, *>::changes)
            }
        }
    ) {
        override fun invoke(map: SimpleValues<ObjectChangesResponse<*, *>>) = ObjectChangesResponse(
            dataModel = map(0),
            changes = map(1)
        )
    }
}
