package maryk.core.query

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.objects.QueryDataModel
import maryk.core.properties.definitions.EnumDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.types.IndexedEnum
import maryk.core.properties.types.IndexedEnumDefinition

/** Direction Enumeration */
enum class Direction(override val index: Int) : IndexedEnum<Direction> {
    ASC(0), DESC(1);

    companion object: IndexedEnumDefinition<Direction>("Direction", Direction::values)
}

/** Descending ordering of property */
fun IsPropertyReference<*, *>.descending() = Order(this, Direction.DESC)

/** Ascending ordering of property */
fun IsPropertyReference<*, *>.ascending() = Order(this, Direction.ASC)

/**
 * To define the order of results of property referred to [propertyReference] into [direction]
 */
data class Order internal constructor(
    val propertyReference: IsPropertyReference<*, *>,
    val direction: Direction = Direction.ASC
) {
    internal companion object: QueryDataModel<Order>(
        properties = object : PropertyDefinitions<Order>() {
            init {
                add(0, "propertyReference", ContextualPropertyReferenceDefinition<DataModelPropertyContext>(
                    contextualResolver = {
                        it?.dataModel?.properties ?: throw ContextNotFoundException()
                    }
                ), Order::propertyReference)

                add(1, "direction", EnumDefinition(
                    enum = Direction
                ), Order::direction)
            }
        }
    ) {
        override fun invoke(map: Map<Int, *>) = Order(
            propertyReference = map(0),
            direction = map(1)
        )
    }
}
