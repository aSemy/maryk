package maryk.core.properties.references

import maryk.core.exceptions.DefNotFoundException
import maryk.core.exceptions.UnexpectedValueException
import maryk.core.extensions.bytes.calculateVarByteLength
import maryk.core.extensions.bytes.calculateVarIntWithExtraInfoByteSize
import maryk.core.extensions.bytes.writeBytes
import maryk.core.extensions.bytes.writeVarBytes
import maryk.core.extensions.bytes.writeVarIntWithExtraInfo
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsChangeableValueDefinition
import maryk.core.properties.definitions.IsEmbeddedDefinition
import maryk.core.properties.definitions.IsEmbeddedObjectDefinition
import maryk.core.properties.definitions.IsListDefinition
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.references.ReferenceType.LIST
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType.VAR_INT
import maryk.core.protobuf.WriteCacheReader
import maryk.core.protobuf.WriteCacheWriter
import maryk.core.query.pairs.ReferenceValuePair

/** Reference to a List Item on [parentReference] with [T] by [index] */
class ListItemReference<T : Any, CX : IsPropertyContext> internal constructor(
    val index: UInt,
    val listDefinition: IsListDefinition<T, CX>,
    parentReference: ListReference<T, CX>?
) : HasEmbeddedPropertyReference<T>,
    IsPropertyReferenceWithIndirectStorageParent<T, IsValueDefinition<T, CX>, ListReference<T, CX>, List<T>>,
    CanHaveComplexChildReference<T, IsValueDefinition<T, CX>, ListReference<T, CX>, List<T>>(
        listDefinition.valueDefinition, parentReference
    ) {
    /** Convenience infix method to create Reference [value] pairs */
    @Suppress("UNCHECKED_CAST")
    infix fun <T : Any> with(value: T) =
        ReferenceValuePair(this as IsPropertyReference<T, IsChangeableValueDefinition<T, IsPropertyContext>, *>, value)

    override fun getEmbedded(name: String, context: IsPropertyContext?) =
        when (this.propertyDefinition) {
            is IsEmbeddedDefinition<*, *> ->
                this.propertyDefinition.resolveReferenceByName(name, this)
            is MultiTypeDefinition<*, *> -> {
                this.propertyDefinition.resolveReferenceByName(name, this)
            }
            else -> throw DefNotFoundException("ListItem can not contain embedded name references ($name)")
        }

    override fun getEmbeddedRef(reader: () -> Byte, context: IsPropertyContext?): IsPropertyReference<Any, *, *> {
        return when (this.propertyDefinition) {
            is IsEmbeddedDefinition<*, *> -> {
                this.propertyDefinition.resolveReference(reader, this)
            }
            is MultiTypeDefinition<*, *> -> {
                this.propertyDefinition.resolveReference(reader, this)
            }
            else -> throw DefNotFoundException("ListItem can not contain embedded index references ($index)")
        }
    }

    override fun getEmbeddedStorageRef(
        reader: () -> Byte,
        context: IsPropertyContext?,
        referenceType: CompleteReferenceType,
        isDoneReading: () -> Boolean
    ): AnyPropertyReference {
        return when (this.propertyDefinition) {
            is IsEmbeddedObjectDefinition<*, *, *, *, *> -> {
                this.propertyDefinition.resolveReferenceFromStorage(reader, this, context, isDoneReading)
            }
            is MultiTypeDefinition<*, *> -> {
                this.propertyDefinition.resolveReferenceFromStorage(reader, this)
            }
            else -> throw DefNotFoundException("ListItem can not contain embedded index references ($index)")
        }
    }

    override val completeName: String
        get() = this.parentReference?.let {
            "${it.completeName}.@$index"
        } ?: "@$index"

    override fun calculateTransportByteLength(cacher: WriteCacheWriter): Int {
        val parentLength = parentReference?.calculateTransportByteLength(cacher) ?: 0
        return parentLength + 1 + index.calculateVarByteLength()
    }

    override fun writeTransportBytes(cacheGetter: WriteCacheReader, writer: (byte: Byte) -> Unit) {
        this.parentReference?.writeTransportBytes(cacheGetter, writer)
        ProtoBuf.writeKey(0u, VAR_INT, writer)
        index.writeVarBytes(writer)
    }

    override fun calculateSelfStorageByteLength(): Int {
        // calculate length of index of setDefinition
        return (this.parentReference?.propertyDefinition?.index?.calculateVarIntWithExtraInfoByteSize() ?: 0) +
            // add bytes for list index
            Int.SIZE_BYTES
    }

    override fun writeSelfStorageBytes(writer: (byte: Byte) -> Unit) {
        // Write set index with a SetValue type
        this.parentReference?.propertyDefinition?.index?.writeVarIntWithExtraInfo(LIST.value, writer)
        // Write index bytes
        index.toUInt().writeBytes(writer)
    }

    override fun resolve(values: List<T>): T? = values[index.toInt()]

    override fun resolveFromAny(value: Any) = (value as? List<*>)?.get(this.index.toInt())
        ?: throw UnexpectedValueException("Expected List to get value by reference")
}
