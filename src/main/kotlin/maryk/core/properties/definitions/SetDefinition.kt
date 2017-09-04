package maryk.core.properties.definitions

import maryk.core.properties.references.PropertyReference
import maryk.core.properties.references.SetItemReference

class SetDefinition<T: Any>(
        name: String? = null,
        index: Short = -1,
        indexed: Boolean = true,
        searchable: Boolean = true,
        required: Boolean = false,
        final: Boolean = false,
        minSize: Int? = null,
        maxSize: Int? = null,
        valueDefinition: AbstractValueDefinition<T>
) : AbstractCollectionDefinition<T, Set<T>>(
        name, index, indexed, searchable, required, final, minSize, maxSize, valueDefinition
), HasSizeDefinition {
    override fun getSize(newValue: Set<T>) = newValue.size

    override fun validateCollectionForExceptions(parentRefFactory: () -> PropertyReference<*, *>?, newValue: Set<T>, validator: (item: T, parentRefFactory: () -> PropertyReference<*, *>?) -> Any) {
        newValue.forEach {
            validator(it) {
                @Suppress("UNCHECKED_CAST")
                SetItemReference(
                        it,
                        getRef(parentRefFactory) as PropertyReference<Set<T>, SetDefinition<T>>
                )
            }
        }
    }
}