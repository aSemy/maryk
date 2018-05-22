package maryk.core.query.pairs

import maryk.core.objects.SimpleDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.SubModelDefinition
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.query.ValueRange
import maryk.core.query.filters.IsPropertyCheck

/** Defines a pair of a [reference] and [range] of type [T] */
data class ReferenceValueRangePair<T: Any> internal constructor(
    val reference: IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, *, IsPropertyContext, *>>,
    val range: ValueRange<T>
) {
    internal object Properties: PropertyDefinitions<ReferenceValueRangePair<*>>() {
        val reference = IsPropertyCheck.addReference(
            this,
            ReferenceValueRangePair<*>::reference
        )
        val range = add(
            index = 1, name = "range",
            definition = SubModelDefinition(
                dataModel = { ValueRange }
            ),
            getter = ReferenceValueRangePair<*>::range
        )
    }

    internal companion object: SimpleDataModel<ReferenceValueRangePair<*>, Properties>(
        properties = Properties
    ) {
        override fun invoke(map: Map<Int, *>) = ReferenceValueRangePair<Any>(
            reference = map(0),
            range = map(1)
        )
    }
}

/** Convenience infix method to create Reference [range] pairs */
infix fun <T: Any> IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, *, IsPropertyContext, *>>.with(range: ValueRange<T>) =
    ReferenceValueRangePair(this, range)

/** Creates a reference value [range] pair */
infix fun <T: Comparable<T>> IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, *, IsPropertyContext, *>>.with(
    range: ClosedRange<T>
) = ReferenceValueRangePair(this, ValueRange(range.start, range.endInclusive, true, true))
