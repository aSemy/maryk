package maryk.core.properties.references

import maryk.core.exceptions.UnexpectedValueException
import maryk.core.extensions.bytes.calculateVarIntWithExtraInfoByteSize
import maryk.core.extensions.bytes.writeVarIntWithExtraInfo
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsFixedBytesEncodable
import maryk.core.properties.definitions.IsSetDefinition
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.references.ReferenceType.SET
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType
import maryk.core.protobuf.WriteCacheReader
import maryk.core.protobuf.WriteCacheWriter

/**
 * Reference to a Set Item by [value] of [T] and context [CX] on set referred to [parentReference] and
 * defined by [setDefinition]
 */
class SetItemReference<T : Any, CX : IsPropertyContext> internal constructor(
    val value: T,
    val setDefinition: IsSetDefinition<T, CX>,
    parentReference: SetReference<T, CX>?
) : CanHaveSimpleChildReference<T, IsValueDefinition<T, CX>, SetReference<T, CX>, Set<T>>(
        setDefinition.valueDefinition, parentReference
    ),
    IsPropertyReferenceWithIndirectStorageParent<T, IsValueDefinition<T, CX>, SetReference<T, CX>, Set<T>> {
    override val completeName: String
        get() = this.parentReference?.let {
            "${it.completeName}.$$value"
        } ?: "$$value"

    override fun resolveFromAny(value: Any) =
        if (value is Set<*> && value.contains(this.value)) {
            this.value
        } else {
            throw UnexpectedValueException("Expected List to get value by reference")
        }

    override fun calculateTransportByteLength(cacher: WriteCacheWriter): Int {
        val parentLength = this.parentReference?.calculateTransportByteLength(cacher) ?: 0
        val valueLength = setDefinition.valueDefinition.calculateTransportByteLength(value, cacher)
        return parentLength + 1 + valueLength
    }

    override fun writeTransportBytes(cacheGetter: WriteCacheReader, writer: (byte: Byte) -> Unit) {
        this.parentReference?.writeTransportBytes(cacheGetter, writer)
        ProtoBuf.writeKey(0, WireType.VAR_INT, writer)
        setDefinition.valueDefinition.writeTransportBytes(value, cacheGetter, writer)
    }

    override fun calculateSelfStorageByteLength(): Int {
        // calculate length of index of setDefinition
        return (this.parentReference?.propertyDefinition?.index?.calculateVarIntWithExtraInfoByteSize() ?: 0) +
            // add bytes for set value
            @Suppress("UNCHECKED_CAST")
            (setDefinition.valueDefinition as IsFixedBytesEncodable<T>).calculateStorageByteLength(value)
    }

    override fun writeSelfStorageBytes(writer: (byte: Byte) -> Unit) {
        // Write set index with a SetValue type
        this.parentReference?.propertyDefinition?.index?.writeVarIntWithExtraInfo(SET.value, writer)
        // Write value bytes
        @Suppress("UNCHECKED_CAST")
        (setDefinition.valueDefinition as IsFixedBytesEncodable<T>).writeStorageBytes(value, writer)
    }

    override fun resolve(values: Set<T>): T? {
        return value
    }
}
