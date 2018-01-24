package maryk.core.properties.definitions.wrapper

import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsCollectionDefinition
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.definitions.SetDefinition
import maryk.core.properties.references.CanHaveComplexChildReference
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.SetItemReference
import maryk.core.properties.references.SetReference

/**
 * Wrapper for a set definition to contain the context on how it relates to DataObject
 * @param index: of definition to encode into ProtoBuf
 * @param name: of definition to display in human readable format
 * @param definition: to be wrapped for DataObject
 * @param getter: to get property value on a DataObject
 *
 * @param T: value type of property for list
 * @param CX: Context type for property
 * @param DO: Type of DataObject which contains this property
 */
data class SetPropertyDefinitionWrapper<T: Any, CX: IsPropertyContext, in DO: Any>(
    override val index: Int,
    override val name: String,
    override val definition: SetDefinition<T, CX>,
    override val getter: (DO) -> Set<T>?
) :
    IsCollectionDefinition<T, Set<T>, CX, IsValueDefinition<T, CX>> by definition,
    IsPropertyDefinitionWrapper<Set<T>, CX, DO>
{
    override fun getRef(parentRef: IsPropertyReference<*, *>?) =
        SetReference(this, parentRef as CanHaveComplexChildReference<*, *, *>?)

    /** Get a reference to a specific set item by [value] with optional [parentRef] */
    fun getItemRef(value: T, parentRef: IsPropertyReference<*, *>? = null) =
        this.definition.getItemRef(value, this.getRef(parentRef))

    /** For quick notation to get a set [item] reference */
    infix fun at(item: T): (IsPropertyReference<out Any, IsPropertyDefinition<*>>?) -> SetItemReference<T, *> {
        return { this.getItemRef(item, it) }
    }
}