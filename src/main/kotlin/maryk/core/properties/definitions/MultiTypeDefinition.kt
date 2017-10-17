package maryk.core.properties.definitions

import maryk.core.exceptions.DefNotFoundException
import maryk.core.extensions.bytes.computeVarByteSize
import maryk.core.extensions.bytes.initIntByVar
import maryk.core.extensions.bytes.writeVarBytes
import maryk.core.json.JsonGenerator
import maryk.core.json.JsonParser
import maryk.core.json.JsonToken
import maryk.core.properties.exceptions.ParseException
import maryk.core.properties.exceptions.PropertyValidationException
import maryk.core.properties.references.PropertyReference
import maryk.core.properties.types.TypedValue
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType

/**
 * Definition for objects with multiple types
 * @param typeMap definition of all sub types
 */
class MultiTypeDefinition(
        name: String? = null,
        index: Int = -1,
        indexed: Boolean = false,
        searchable: Boolean = true,
        required: Boolean = false,
        final: Boolean = false,
        val typeMap: Map<Int, AbstractSubDefinition<*>>
) : AbstractPropertyDefinition<TypedValue<*>>(
        name, index, indexed, searchable, required, final
) {
    @Throws(PropertyValidationException::class)
    override fun validate(previousValue: TypedValue<*>?, newValue: TypedValue<*>?, parentRefFactory: () -> PropertyReference<*, *>?) {
        super.validate(previousValue, newValue, parentRefFactory)
        if (newValue != null) {
            @Suppress("UNCHECKED_CAST")
            val definition = this.typeMap[newValue.typeIndex] as AbstractSubDefinition<Any>?
                    ?: throw DefNotFoundException("No def found for index ${newValue.typeIndex} for ${this.getRef(parentRefFactory).completeName}")

            definition.validate(
                    previousValue?.value,
                    newValue.value
            ) {
                getRef(parentRefFactory)
            }
        }
    }

    override fun writeJsonValue(generator: JsonGenerator, value: TypedValue<Any>) {
        generator.writeStartArray()
        generator.writeValue(value.typeIndex.toString())
        @Suppress("UNCHECKED_CAST")
        val definition = this.typeMap[value.typeIndex] as AbstractSubDefinition<Any>?
                ?: throw DefNotFoundException("No def found for index ${value.typeIndex} for $name")

        definition.writeJsonValue(generator, value.value)
        generator.writeEndArray()
    }

    override fun parseFromJson(parser: JsonParser): TypedValue<*> {
        if(parser.nextToken() !is JsonToken.ARRAY_VALUE) {
            throw ParseException("Expected an array value at start")
        }

        val index: Int
        try {
            index = parser.lastValue.toInt()
        }catch (e: Throwable) {
            throw ParseException("Invalid multitype index ${parser.lastValue} for $name")
        }
        parser.nextToken()

        val definition: AbstractSubDefinition<*>? = this.typeMap[index]
                ?: throw ParseException("Unknown multitype index ${parser.lastValue} for $name")

        return TypedValue<Any>(
                index,
                definition!!.parseFromJson(parser)
        )
    }

    override fun readTransportBytes(length: Int, reader: () -> Byte): TypedValue<*> {
        val typeIndex = initIntByVar(reader)
        val def = this.typeMap[typeIndex] ?: throw ParseException("Unknown multitype index $typeIndex for $name")
        return TypedValue(
                typeIndex,
                def.readTransportBytes(
                        length - typeIndex.computeVarByteSize(),
                        reader
                )
        )
    }

    override fun writeTransportBytesWithKey(value: TypedValue<*>, reserver: (size: Int) -> Unit, writer: (byte: Byte) -> Unit) {
        ProtoBuf.writeKey(this.index, WireType.LENGTH_DELIMITED, reserver, writer)
        this.writeTransportBytes(value, { givenLength ->
            val newLength = givenLength + value.typeIndex.computeVarByteSize()
            reserver(newLength + newLength.computeVarByteSize())
            newLength.writeVarBytes(writer)
            value.typeIndex.writeVarBytes(writer)
        }, writer)
    }

    override fun writeTransportBytes(value: TypedValue<*>, reserver: (size: Int) -> Unit, writer: (byte: Byte) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val def = this.typeMap[value.typeIndex]!! as AbstractSubDefinition<Any>

        def.writeTransportBytes(value.value, reserver, writer)
    }
}