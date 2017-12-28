package maryk.core.objects

import maryk.core.bytes.Base64
import maryk.core.exceptions.DefNotFoundException
import maryk.core.extensions.bytes.initByteArray
import maryk.core.properties.definitions.IsFixedBytesEncodable
import maryk.core.properties.definitions.IsFixedBytesProperty
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.definitions.IsValueDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.key.Reversed
import maryk.core.properties.definitions.key.UUIDKey
import maryk.core.properties.definitions.wrapper.FixedBytesPropertyDefinitionWrapper
import maryk.core.properties.exceptions.ParseException
import maryk.core.properties.types.Key

fun definitions(vararg keys: IsFixedBytesProperty<*>) = arrayOf(*keys)

/** DataModel defining data objects of type [DO] which is on root level so it can be stored and thus can have a [key].
 * The key is defined by passing an ordered array of key definitions.
 * If no key is defined the data model will get a UUID.
 *
 * The dataModel can be referenced by the [name] and the properties are defined by a [properties]
 */
abstract class RootDataModel<DO: Any, P: PropertyDefinitions<DO>>(
        name: String,
        keyDefinitions: Array<IsFixedBytesProperty<out Any>> = arrayOf(UUIDKey),
        properties: P
) : DataModel<DO, P>(name, properties){
    val key = KeyDefinition(*keyDefinitions)

    /** Defines the structure of the Key by passing [keyDefinitions] */
    inner class KeyDefinition(vararg val keyDefinitions: IsFixedBytesProperty<out Any>) {
        val size: Int

        init {
            var totalBytes = keyDefinitions.size - 1 // Start with adding size of separators

            keyDefinitions.forEach {
                when {
                    it is FixedBytesPropertyDefinitionWrapper<*, *, *, *>
                            && it.definition is IsValueDefinition<*, *>-> {
                        checkDefinition(it.name, it.definition as IsValueDefinition<*, *>)
                    }
                    it is Reversed<*>
                            && it.definition is FixedBytesPropertyDefinitionWrapper<*, *, *, *>
                            && it.definition.definition is IsValueDefinition<*, *> -> {
                        checkDefinition(it.definition.name, it.definition.definition)
                    }
                }
                totalBytes += it.byteSize
            }
            this.size = totalBytes
        }

        private fun checkDefinition(name: String, it: IsPropertyDefinition<*>) {
            require(it.required, { "Definition of $name should be required" })
            require(it.final, { "Definition of $name should be final" })
        }

        /** Get Key by [bytes] array */
        fun get(bytes: ByteArray): Key<DO> {
            if (bytes.size != this.size) {
               throw ParseException("Invalid byte length for key")
            }
            return Key(bytes)
        }

        /** Get Key by [base64] bytes as string representation */
        fun get(base64: String): Key<DO> = this.get(Base64.decode(base64))

        /** Get Key by byte [reader] */
        fun get(reader: () -> Byte): Key<DO> = Key(
                initByteArray(size, reader)
        )

        /** Get Key based on [dataObject] */
        fun getKey(dataObject: DO): Key<DO> {
            val bytes = ByteArray(this.size)
            var index = 0
            keyDefinitions.forEach {
                val value = it.getValue(this@RootDataModel, dataObject)

                @Suppress("UNCHECKED_CAST")
                (it as IsFixedBytesEncodable<Any>).writeStorageBytes(value, {
                    bytes[index++] = it
                })

                // Add separator
                if (index < this.size) {
                    bytes[index++] = 1
                }
            }
            return Key(bytes)
        }
    }

    /** Get PropertyReference by [referenceName] */
    fun getPropertyReferenceByName(referenceName: String) = try {
        this.properties.getPropertyReferenceByName(referenceName)
    } catch (e: DefNotFoundException) {
        throw DefNotFoundException("Model ${this.name}: ${e.message}")
    }

    /** Get PropertyReference by bytes by reading the [reader] until [length] is reached. */
    fun getPropertyReferenceByBytes(length: Int, reader: () -> Byte) = try {
        this.properties.getPropertyReferenceByBytes(length, reader)
    } catch (e: DefNotFoundException) {
        throw DefNotFoundException("Model ${this.name}: ${e.message}")
    }
}
