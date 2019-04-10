package maryk.core.properties.graph

import maryk.core.properties.enum.IndexedEnumComparable
import maryk.core.properties.enum.IndexedEnumDefinition
import maryk.core.properties.enum.IsCoreEnum

/** Indexed type of property reference graph elements */
enum class PropRefGraphType(
    override val index: UInt
) : IndexedEnumComparable<PropRefGraphType>, IsCoreEnum {
    PropRef(1u),
    Graph(2u);

    companion object : IndexedEnumDefinition<PropRefGraphType>(
        "PropRefGraphType", PropRefGraphType::values
    )
}
