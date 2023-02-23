package maryk.core.query.requests

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.IsRootDataModel
import maryk.core.models.QueryDataModel
import maryk.core.models.ValuesDataModelImpl
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualEmbeddedValuesDefinition
import maryk.core.properties.definitions.list
import maryk.core.query.RequestContext
import maryk.core.query.requests.RequestType.Add
import maryk.core.query.responses.AddResponse
import maryk.core.values.ObjectValues
import maryk.core.values.Values

/** Creates a Request to add multiple [objectToAdd] to a store defined by given DataModel */
fun <DM : IsRootDataModel<P>, P : IsValuesPropertyDefinitions> DM.add(vararg objectToAdd: Values<P>) =
    AddRequest(this, objectToAdd.toList())

/** A Request to add [objects] to [dataModel] */
data class AddRequest<DM : IsRootDataModel<P>, P : IsValuesPropertyDefinitions> internal constructor(
    override val dataModel: DM,
    val objects: List<Values<P>>
) : IsStoreRequest<DM, AddResponse<DM>>, IsTransportableRequest<AddResponse<DM>> {
    override val requestType = Add
    override val responseModel = AddResponse

    object Properties : ObjectPropertyDefinitions<AddRequest<*, *>>() {
        val to by addDataModel(AddRequest<*, *>::dataModel)

        val objects by list(
            index = 2u,
            getter = AddRequest<*, *>::objects,
            valueDefinition = ContextualEmbeddedValuesDefinition<RequestContext>(
                contextualResolver = {
                    @Suppress("UNCHECKED_CAST")
                    it?.dataModel as? ValuesDataModelImpl<RequestContext>? ?: throw ContextNotFoundException()
                }
            )
        )
    }

    companion object : QueryDataModel<AddRequest<*, *>, Properties>(
        properties = Properties
    ) {
        override fun invoke(values: ObjectValues<AddRequest<*, *>, Properties>) =
            AddRequest<IsRootDataModel<IsValuesPropertyDefinitions>, IsValuesPropertyDefinitions>(
                dataModel = values(1u),
                objects = values(2u)
            )
    }
}
