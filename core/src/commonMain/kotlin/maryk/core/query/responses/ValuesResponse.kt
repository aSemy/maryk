package maryk.core.query.responses

import maryk.core.aggregations.AggregationsResponse
import maryk.core.models.QueryDataModel
import maryk.core.properties.IsRootModel
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.embedObject
import maryk.core.properties.definitions.wrapper.ObjectListDefinitionWrapper
import maryk.core.query.ValuesWithMetaData
import maryk.core.values.ObjectValues

/** Response with [values] to an objects (Get/Scan) request to [dataModel] */
data class ValuesResponse<DM : IsRootModel>(
    override val dataModel: DM,
    val values: List<ValuesWithMetaData<DM>>,
    val aggregations: AggregationsResponse? = null
) : IsDataResponse<DM> {
    object Properties : ObjectPropertyDefinitions<ValuesResponse<*>>() {
        val dataModel by addDataModel({ it.dataModel })
        val values = ObjectListDefinitionWrapper(
            2u, "values",
            properties = ValuesWithMetaData.Properties,
            definition = ListDefinition(
                valueDefinition = EmbeddedObjectDefinition(
                    dataModel = { ValuesWithMetaData }
                )
            ),
            getter = ValuesResponse<*>::values
        ).also(::addSingle)

        val aggregations by embedObject(
            index = 3u,
            getter = ValuesResponse<*>::aggregations,
            dataModel = { AggregationsResponse },
            alternativeNames = setOf("aggs")
        )
    }

    companion object : QueryDataModel<ValuesResponse<*>, Properties>(
        properties = Properties
    ) {
        override fun invoke(values: ObjectValues<ValuesResponse<*>, Properties>) = ValuesResponse(
            dataModel = values(1u),
            values = values(2u),
            aggregations = values(3u)
        )
    }
}
