package maryk.core.aggregations

import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition

/** Defines an aggregation with a type so it can be transported */
interface IsAggregationRequest {
    val aggregationType: AggregationType

    companion object {
        fun <DM: Any> addAggregationsDefinition(definitions: ObjectPropertyDefinitions<*>, getter: (DM) -> Aggregations?) {
            @Suppress("UNCHECKED_CAST")
            definitions.add(
                2u,
                "aggregations",
                EmbeddedObjectDefinition(dataModel = { Aggregations }),
                getter as (Any) -> Aggregations?,
                alternativeNames = setOf("aggs")
            )
        }
    }
}
