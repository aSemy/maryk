package maryk.core.query.changes

import maryk.core.objects.QueryDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualValueDefinition
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.query.DataModelPropertyContext

/**
 * Value change for a property
 * @param reference to property affected by the change
 * @param newValue the value in which property is/was changed
 * @param valueToCompare (optional) if set the current value is checked against this value.
 * Operation will only complete if they both are equal
 * @param T: type of value to be operated on
 */
data class PropertyChange<T: Any>(
    override val reference: IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, IsPropertyContext, *>>,
    val newValue: T,
    override val valueToCompare: T? = null
) : IsPropertyOperation<T> {
    override val changeType = ChangeType.PROP_CHANGE

    internal companion object: QueryDataModel<PropertyChange<*>>(
        properties = object : PropertyDefinitions<PropertyChange<*>>() {
            init {
                IsPropertyOperation.addReference(this, PropertyChange<*>::reference)
                IsPropertyOperation.addValueToCompare(this, PropertyChange<*>::valueToCompare)

                add(2, "newValue", ContextualValueDefinition(
                    contextualResolver = { context: DataModelPropertyContext? ->
                        @Suppress("UNCHECKED_CAST")
                        context!!.reference!!.propertyDefinition.definition as IsValueDefinition<Any, IsPropertyContext>
                    }
                ), PropertyChange<*>::newValue)
            }
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = PropertyChange(
            reference = map[0] as IsPropertyReference<Any, IsValuePropertyDefinitionWrapper<Any, IsPropertyContext, Any>>,
            valueToCompare = map[1],
            newValue = map[2] as Any
        )
    }
}