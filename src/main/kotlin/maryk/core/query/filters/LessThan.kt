package maryk.core.query.filters

import maryk.core.objects.QueryDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.wrapper.IsDataObjectValueProperty
import maryk.core.properties.references.IsPropertyReference

/** Referenced value should be less than and not equal given value
 * @param reference to property to compare
 * @param value the value which is checked against
 * @param T: type of value to be operated on
 */
data class LessThan<T: Any>(
        override val reference: IsPropertyReference<T, IsDataObjectValueProperty<T, IsPropertyContext, *>>,
        override val value: T
) : IsPropertyComparison<T> {
    override val filterType = FilterType.LESS_THAN

    companion object: QueryDataModel<LessThan<*>>(
            properties = object : PropertyDefinitions<LessThan<*>>() {
                init {
                    IsPropertyCheck.addReference(this, LessThan<*>::reference)
                    IsPropertyComparison.addValue(this, LessThan<*>::value)
                }
            }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = LessThan(
                reference = map[0] as IsPropertyReference<Any, IsDataObjectValueProperty<Any, IsPropertyContext, *>>,
                value = map[1] as Any
        )
    }
}