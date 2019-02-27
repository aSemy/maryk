package maryk.core.properties.references

import maryk.core.properties.definitions.IsBytesEncodable
import maryk.core.properties.definitions.index.IsIndexable
import maryk.core.values.IsValuesGetter

interface IsIndexablePropertyReference<T : Any> : IsIndexable, IsBytesEncodable<T> {
    /**
     * Get the value from [values]
     * to be used in a fixed bytes encodable
     */
    fun getValue(values: IsValuesGetter): T

    /**
     * Check if value getter is defined for property referred by [propertyReference]
     * Useful to resolve filters into key filters which are fixed bytes.
     */
    fun isForPropertyReference(propertyReference: AnyPropertyReference): Boolean

    override fun calculateStorageByteLength(values: IsValuesGetter): Int {
        val value = this.getValue(values)
        return this.calculateStorageByteLength(value)
    }

    override fun writeStorageBytes(values: IsValuesGetter, writer: (byte: Byte) -> Unit) {
        val value = this.getValue(values)
        this.writeStorageBytes(value, writer)
    }
}