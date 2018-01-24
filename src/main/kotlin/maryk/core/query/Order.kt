package maryk.core.query

import maryk.core.objects.QueryDataModel
import maryk.core.properties.definitions.EnumDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.types.IndexedEnum

/** Direction Enumeration */
enum class Direction(override val index: Int) : IndexedEnum<Direction> {
    ASC(0), DESC(1)
}

/**
 * To define the order of results of property referred to [propertyReference] into [direction]
 */
data class Order(
    val propertyReference: IsPropertyReference<*, *>,
    val direction: Direction = Direction.ASC
) {
    internal companion object: QueryDataModel<Order>(
        properties = object : PropertyDefinitions<Order>() {
            init {
                add(0, "propertyReference", ContextualPropertyReferenceDefinition<DataModelPropertyContext>(
                    contextualResolver = { it!!.dataModel!!.properties }
                ), Order::propertyReference)

                add(1, "direction", EnumDefinition(
                    values = Direction.values()
                ), Order::direction)
            }
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = Order(
            propertyReference = map[0] as IsPropertyReference<*, *>,
            direction = map[1] as Direction
        )
    }
}