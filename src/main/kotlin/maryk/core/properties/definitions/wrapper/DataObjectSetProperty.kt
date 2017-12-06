package maryk.core.properties.definitions.wrapper

import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsCollectionDefinition
import maryk.core.properties.definitions.SetDefinition
import maryk.core.properties.references.CanHaveComplexChildReference
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.SetReference

data class DataObjectSetProperty<T: Any, CX: IsPropertyContext, in DM: Any>(
        override val index: Int,
        override val name: String,
        override val property: SetDefinition<T, CX>,
        override val getter: (DM) -> Set<T>?
) :
        IsCollectionDefinition<T, Set<T>, CX> by property,
        IsDataObjectProperty<Set<T>, CX, DM>
{
    override fun getRef(parentRefFactory: () -> IsPropertyReference<*, *>?) =
            SetReference(this, parentRefFactory() as CanHaveComplexChildReference<*, *, *>?)

    /** Get a reference to a specific set item
     * @param key to get reference for
     * @param parentRefFactory (optional) factory to create parent ref
     */
    fun getItemRef(value: T, parentRefFactory: () -> IsPropertyReference<*, *>? = { null })
            = this.property.getItemRef(value, this.getRef(parentRefFactory))
}