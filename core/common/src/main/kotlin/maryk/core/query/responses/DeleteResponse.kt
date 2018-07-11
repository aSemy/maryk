package maryk.core.query.responses

import maryk.core.models.IsRootDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.objects.SimpleObjectValues
import maryk.core.properties.IsPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.types.TypedValue
import maryk.core.query.responses.statuses.IsDeleteResponseStatus
import maryk.core.query.responses.statuses.StatusType

/** Response with [statuses] to a Delete request to [dataModel] */
data class DeleteResponse<DM: IsRootDataModel<*>>(
    override val dataModel: DM,
    val statuses: List<IsDeleteResponseStatus<DM>>
) : IsDataModelResponse<DM> {
    internal companion object: SimpleQueryDataModel<DeleteResponse<*>>(
        properties = object : ObjectPropertyDefinitions<DeleteResponse<*>>() {
            init {
                IsDataModelResponse.addDataModel(this, DeleteResponse<*>::dataModel)
                IsDataModelResponse.addStatuses(this) { response ->
                    response.statuses.map { TypedValue(it.statusType, it) }
                }
            }
        }
    ) {
        override fun invoke(map: SimpleObjectValues<DeleteResponse<*>>) = DeleteResponse(
            dataModel = map(0),
            statuses = map<List<TypedValue<StatusType, IsDeleteResponseStatus<IsRootDataModel<IsPropertyDefinitions>>>>?>(1)?.map { it.value } ?: emptyList()
        )
    }
}
