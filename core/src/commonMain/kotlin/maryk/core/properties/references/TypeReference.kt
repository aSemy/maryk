package maryk.core.properties.references

import maryk.core.exceptions.DefNotFoundException
import maryk.core.exceptions.UnexpectedValueException
import maryk.core.extensions.bytes.calculateVarByteLength
import maryk.core.extensions.bytes.initIntByVar
import maryk.core.extensions.bytes.writeVarBytes
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.IsEmbeddedObjectDefinition
import maryk.core.properties.definitions.IsSubDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.enum.IndexedEnum
import maryk.core.properties.types.TypedValue
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType
import maryk.core.protobuf.WriteCacheReader
import maryk.core.protobuf.WriteCacheWriter

@Suppress("UNCHECKED_CAST")
/** Reference to a Type [E] on [parentReference]  */
class TypeReference<E: IndexedEnum<E>, in CX: IsPropertyContext>  internal constructor(
    val type: E,
    multiTypeDefinition: MultiTypeDefinition<E, CX>,
    parentReference: CanHaveComplexChildReference<*, *, *, *>?
) : HasEmbeddedPropertyReference<Any>, CanHaveComplexChildReference<Any, IsSubDefinition<Any, CX>, CanHaveComplexChildReference<*, *, *, *>, TypedValue<E, Any>>(
    multiTypeDefinition.definitionMap[type] as IsSubDefinition<Any, CX>, parentReference
) {
    override fun resolveFromAny(value: Any) = (value as? TypedValue<*, *>)?.value ?: throw UnexpectedValueException("Expected typed value to get value by reference")

    override fun getEmbedded(name: String, context: IsPropertyContext?): IsPropertyReference<Any, *, *> {
        return if(this.propertyDefinition is IsEmbeddedObjectDefinition<*, *, *, *, *>) {
            this.propertyDefinition.dataModel.properties[name]?.getRef(this)
                    ?: throw DefNotFoundException("Embedded Definition with $name not found")
        } else throw DefNotFoundException("Type reference can not contain embedded name references ($name)")
    }

    override fun getEmbeddedRef(reader: () -> Byte, context: IsPropertyContext?): AnyPropertyReference {
        if(this.propertyDefinition is IsEmbeddedObjectDefinition<*, *, *, *, *>) {
            val index = initIntByVar(reader)
            return this.propertyDefinition.dataModel.properties[index]?.getRef(this)
                    ?: throw DefNotFoundException("Embedded Definition with $index not found")
        } else throw DefNotFoundException("Type reference can not contain embedded index references (${type.name})")
    }

    override val completeName: String get() = this.parentReference?.let {
        "${it.completeName}.*${type.name}"
    } ?: "*${type.name}"

    override fun calculateTransportByteLength(cacher: WriteCacheWriter): Int {
        val parentLength = parentReference?.calculateTransportByteLength(cacher) ?: 0
        return parentLength + 1 + type.index.calculateVarByteLength()
    }

    override fun writeTransportBytes(cacheGetter: WriteCacheReader, writer: (byte: Byte) -> Unit) {
        this.parentReference?.writeTransportBytes(cacheGetter, writer)
        ProtoBuf.writeKey(0, WireType.VAR_INT, writer)
        type.index.writeVarBytes(writer)
    }

    override fun resolve(values: TypedValue<E, Any>) = values.value

}