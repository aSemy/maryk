package maryk.core.properties.definitions

import maryk.core.objects.DataModel
import maryk.core.properties.exceptions.PropertyAlreadySetException
import maryk.core.properties.exceptions.PropertyRequiredException
import maryk.core.properties.exceptions.PropertyValidationException
import maryk.core.properties.references.PropertyReference

/**
 * Abstract Property Definition to define properties
 * @param <T> Type defined by definition
 */
abstract class AbstractPropertyDefinition<T: Any>  (
        override final val name: String?,
        override final val index: Short,
        val indexed: Boolean,
        val searchable: Boolean,
        val required: Boolean,
        val final: Boolean
) : IsPropertyDefinition<T> {
    override fun getRef(parentRefFactory: () -> PropertyReference<*, *>?) =
            PropertyReference(this, parentRefFactory())

    @Throws(PropertyValidationException::class)
    override fun validate(previousValue: T?, newValue: T?, parentRefFactory: () -> PropertyReference<*, *>?) = when {
        this.final && previousValue != null -> throw PropertyAlreadySetException(this.getRef(parentRefFactory))
        this.required && newValue == null -> throw PropertyRequiredException(this.getRef(parentRefFactory))
        else -> {}
    }

    fun <DM : Any> getValue(dataModel: DataModel<DM>, dataObject: DM): T {
        @Suppress("UNCHECKED_CAST")
        return dataModel.getPropertyGetter(
                this.index
        )?.invoke(dataObject) as T
    }
}