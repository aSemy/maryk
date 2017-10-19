package maryk.core.properties.definitions

import maryk.core.json.JsonGenerator
import maryk.core.json.JsonParser
import maryk.core.json.JsonToken
import maryk.core.properties.exceptions.ParseException
import maryk.core.properties.exceptions.PropertyTooLittleItemsException
import maryk.core.properties.exceptions.PropertyTooMuchItemsException
import maryk.core.properties.exceptions.PropertyValidationException
import maryk.core.properties.exceptions.createPropertyValidationUmbrellaException
import maryk.core.properties.references.CanHaveComplexChildReference
import maryk.core.properties.references.CanHaveSimpleChildReference
import maryk.core.properties.references.MapKeyReference
import maryk.core.properties.references.MapValueReference
import maryk.core.properties.references.PropertyReference
import maryk.core.protobuf.ProtoBuf
import maryk.core.protobuf.WireType

class MapDefinition<K: Any, V: Any>(
        name: String? = null,
        index: Int = -1,
        indexed: Boolean = false,
        searchable: Boolean = true,
        required: Boolean = false,
        final: Boolean = false,
        override val minSize: Int? = null,
        override val maxSize: Int? = null,
        val keyDefinition: AbstractValueDefinition<K>,
        val valueDefinition: AbstractSubDefinition<V>
) : AbstractPropertyDefinition<Map<K, V>>(
        name, index, indexed, searchable, required, final
), HasSizeDefinition {
    init {
        assert(keyDefinition.required, { "Definition for key should be required on map: $name" })
        assert(valueDefinition.required, { "Definition for value should be required on map: $name" })
    }

    override fun getRef(parentRefFactory: () -> PropertyReference<*, *>?): PropertyReference<Map<K, V>, AbstractPropertyDefinition<Map<K, V>>> =
        when (valueDefinition) {
            is SubModelDefinition<*, *> -> CanHaveSimpleChildReference(
                    this,
                    parentRefFactory()?.let {
                        it as CanHaveComplexChildReference<*, *>
                    },
                    dataModel = valueDefinition.dataModel
            )
            else -> { super.getRef(parentRefFactory)}
        }

    override fun validate(previousValue: Map<K,V>?, newValue: Map<K,V>?, parentRefFactory: () -> PropertyReference<*, *>?) {
        super.validate(previousValue, newValue, parentRefFactory)

        if (newValue != null) {
            val mapSize = newValue.size
            if (isSizeToSmall(mapSize)) {
                throw PropertyTooLittleItemsException(this.getRef(parentRefFactory), mapSize, this.minSize!!)
            }
            if (isSizeToBig(mapSize)) {
                throw PropertyTooMuchItemsException(this.getRef(parentRefFactory), mapSize, this.maxSize!!)
            }

            createPropertyValidationUmbrellaException(parentRefFactory) { addException ->
                @Suppress("UNCHECKED_CAST")
                newValue.forEach { key, value ->
                    try {
                        this.keyDefinition.validate(null, key) {
                            MapKeyReference(key, this.getRef(parentRefFactory) as PropertyReference<Map<K, V>, MapDefinition<K, V>>)
                        }
                    } catch (e: PropertyValidationException) {
                        addException(e)
                    }
                    try {
                        this.valueDefinition.validate(null, value) {
                            MapValueReference(key, this.getRef(parentRefFactory) as PropertyReference<Map<K, V>, MapDefinition<K, V>>)
                        }
                    } catch (e: PropertyValidationException) {
                        addException(e)
                    }
                }
            }
        }
    }

    override fun writeJsonValue(generator: JsonGenerator, value: Map<K, V>) {
        generator.writeStartObject()
        value.forEach { k, v ->
            generator.writeFieldName(
                    keyDefinition.convertToString(k)
            )
            valueDefinition.writeJsonValue(generator, v)
        }
        generator.writeEndObject()
    }

    override fun parseFromJson(parser: JsonParser): Map<K, V> {
        if (parser.currentToken !is JsonToken.START_OBJECT) {
            throw ParseException("JSON value for $name should be an Object")
        }
        val map: MutableMap<K, V> = mutableMapOf()

        while (parser.nextToken() !is JsonToken.END_OBJECT) {
            val key = keyDefinition.convertFromString(parser.lastValue)
            parser.nextToken()

            map.put(
                    key,
                    valueDefinition.parseFromJson(parser)
            )
        }
        return map
    }

    override fun writeTransportBytesWithKey(value: Map<K, V>, reserver: (size: Int) -> Unit, writer: (byte: Byte) -> Unit) {
        value.forEach { key, item ->
            ProtoBuf.writeKey(this.index, WireType.START_GROUP, reserver, writer)
            keyDefinition.writeTransportBytesWithKey(1, key, reserver, writer)
            valueDefinition.writeTransportBytesWithKey(2, item, reserver, writer)
            ProtoBuf.writeKey(this.index, WireType.END_GROUP, reserver, writer)
        }
    }

    fun readMapTransportBytes(length: Int, reader: () -> Byte): Pair<K, V> {
        val keyOfMapKey = ProtoBuf.readKey(reader)
        val key = keyDefinition.readTransportBytes(
                ProtoBuf.getLength(keyOfMapKey.wireType, reader),
                reader
        )

        val keyOfMapValue = ProtoBuf.readKey(reader)
        val value = valueDefinition.readTransportBytes(
                ProtoBuf.getLength(keyOfMapValue.wireType, reader),
                reader
        )

        val endKey = ProtoBuf.readKey(reader)
        if (endKey.wireType != WireType.END_GROUP) {
            throw ParseException("Invalid protobuf map construction")
        }

        return Pair(key, value)
    }
}