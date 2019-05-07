package maryk.core.query.responses

import maryk.core.models.IsRootDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.properties.IsPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.enum.TypeEnum
import maryk.core.properties.types.TypedValue
import maryk.core.query.responses.statuses.IsDeleteResponseStatus
import maryk.core.values.SimpleObjectValues

/** Response with [statuses] to a Delete request to [dataModel] */
data class DeleteResponse<DM : IsRootDataModel<*>>(
    override val dataModel: DM,
    val statuses: List<IsDeleteResponseStatus<DM>>
) : IsDataModelResponse<DM> {
    companion object : SimpleQueryDataModel<DeleteResponse<*>>(
        properties = object : ObjectPropertyDefinitions<DeleteResponse<*>>() {
            init {
                IsDataModelResponse.addDataModel(this, DeleteResponse<*>::dataModel)
                IsDataModelResponse.addStatuses(this) { response ->
                    response.statuses.map { TypedValue(it.statusType, it) }
                }
            }
        }
    ) {
        override fun invoke(values: SimpleObjectValues<DeleteResponse<*>>) = DeleteResponse(
            dataModel = values(1u),
            statuses = values<List<TypedValue<TypeEnum<IsDeleteResponseStatus<IsRootDataModel<IsPropertyDefinitions>>>, IsDeleteResponseStatus<IsRootDataModel<IsPropertyDefinitions>>>>?>(2u)?.map { it.value } ?: emptyList()
        )
    }
}
