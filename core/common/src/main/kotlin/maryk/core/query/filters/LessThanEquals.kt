package maryk.core.query.filters

import maryk.core.objects.QueryDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference

/** Referenced value [reference] should be less than or equal given [value] of type [T] */
data class LessThanEquals<T: Any>(
    override val reference: IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, IsPropertyContext, *>>,
    override val value: T
) : IsPropertyComparison<T> {
    override val filterType = FilterType.LESS_THAN_EQUALS

    internal companion object: QueryDataModel<LessThanEquals<*>>(
        properties = object : PropertyDefinitions<LessThanEquals<*>>() {
            init {
                IsPropertyCheck.addReference(this, LessThanEquals<*>::reference)
                IsPropertyComparison.addValue(this, LessThanEquals<*>::value)
            }
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = LessThanEquals(
            reference = map[0] as IsPropertyReference<Any, IsValuePropertyDefinitionWrapper<Any, IsPropertyContext, *>>,
            value = map[1] as Any
        )
    }
}