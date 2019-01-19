package maryk.core.query.pairs

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.IsValuesDataModel
import maryk.core.models.SimpleObjectDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.IsChangeableValueDefinition
import maryk.core.properties.definitions.IsEmbeddedValuesDefinition
import maryk.core.properties.definitions.IsListDefinition
import maryk.core.properties.definitions.IsMapDefinition
import maryk.core.properties.definitions.IsMultiTypeDefinition
import maryk.core.properties.definitions.IsSetDefinition
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.definitions.contextual.ContextualValueDefinition
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.enum.IndexedEnum
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.types.TypedValue
import maryk.core.query.DefinedByReference
import maryk.core.query.RequestContext
import maryk.core.values.ObjectValues
import maryk.core.values.Values

/** Compares given [value] of type [T] against referenced value [reference] */
data class ReferenceValuePair<T: Any> internal constructor(
    override val reference: IsPropertyReference<T, IsChangeableValueDefinition<T, IsPropertyContext>, *>,
    val value: T
) : DefinedByReference<T> {

    override fun toString() = "$reference: $value"

    object Properties: ObjectPropertyDefinitions<ReferenceValuePair<*>>() {
        val reference = DefinedByReference.addReference(
            this,
            ReferenceValuePair<*>::reference
        )
        val value = add(
            2, "value",
            ContextualValueDefinition(
                contextualResolver = { context: RequestContext? ->
                    context?.reference?.let {
                        @Suppress("UNCHECKED_CAST")
                        it.comparablePropertyDefinition as IsValueDefinition<Any, IsPropertyContext>
                    } ?: throw ContextNotFoundException()
                }
            ),
            ReferenceValuePair<*>::value
        )
    }

    companion object: SimpleObjectDataModel<ReferenceValuePair<*>, Properties>(
        properties = Properties
    ) {
        override fun invoke(values: ObjectValues<ReferenceValuePair<*>, Properties>) = ReferenceValuePair(
            reference = values(1),
            value = values(2)
        )
    }
}

/** Convenience infix method to create Reference [value] pairs */
infix fun <T: Any> IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, *, IsPropertyContext, *>, *>.with(value: T) =
    ReferenceValuePair(this, value)

/** Convenience infix method to create Reference [value] pairs with a list */
@Suppress("UNCHECKED_CAST")
infix fun <T: Any, D: IsListDefinition<T, *>> IsPropertyReference<List<T>, D, *>.with(list: List<T>): ReferenceValuePair<List<T>> =
    ReferenceValuePair(this as IsPropertyReference<List<T>, IsChangeableValueDefinition<List<T>, IsPropertyContext>, *>, list)

/** Convenience infix method to create Reference [value] pairs with a set */
@Suppress("UNCHECKED_CAST")
infix fun <T: Any, D: IsSetDefinition<T, *>> IsPropertyReference<Set<T>, D, *>.with(set: Set<T>): ReferenceValuePair<Set<T>> =
    ReferenceValuePair(this as IsPropertyReference<Set<T>, IsChangeableValueDefinition<Set<T>, IsPropertyContext>, *>, set)

/** Convenience infix method to create Reference [value] pairs with a map */
@Suppress("UNCHECKED_CAST")
infix fun <K: Any, V: Any, D: IsMapDefinition<K, V, *>> IsPropertyReference<Map<K, V>, D, *>.with(map: Map<K, V>): ReferenceValuePair<Map<K, V>> =
    ReferenceValuePair(this as IsPropertyReference<Map<K, V>, IsChangeableValueDefinition<Map<K, V>, IsPropertyContext>, *>, map)

/** Convenience infix method to create Reference [value] pairs with a typed value */
@Suppress("UNCHECKED_CAST")
infix fun <E: IndexedEnum<E>, D: IsMultiTypeDefinition<E, *>> IsPropertyReference<TypedValue<E, *>, D, *>.with(map: TypedValue<E, *>): ReferenceValuePair<TypedValue<E, *>> =
    ReferenceValuePair(this as IsPropertyReference<TypedValue<E, *>, IsChangeableValueDefinition<TypedValue<E, *>, IsPropertyContext>, *>, map)

/** Convenience infix method to create Reference [values] pairs with a set */
@Suppress("UNCHECKED_CAST")
infix fun <DM: IsValuesDataModel<P>, P: PropertyDefinitions, D: IsEmbeddedValuesDefinition<DM, P, *>> IsPropertyReference<Values<DM, P>, D, *>.with(values: Values<DM, P>): ReferenceValuePair<Values<DM, P>> =
    ReferenceValuePair(this as IsPropertyReference<Values<DM, P>, IsChangeableValueDefinition<Values<DM, P>, IsPropertyContext>, *>, values)
