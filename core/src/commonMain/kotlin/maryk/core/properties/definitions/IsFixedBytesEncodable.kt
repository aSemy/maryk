package maryk.core.properties.definitions

import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.types.numeric.UInt32

/** Interface to define something can be en/decoded to fixed byte array */
interface IsFixedBytesEncodable<T: Any> {
    /** The byte size */
    val byteSize: Int

    /** Read stored value from [reader] until [length] */
    fun readStorageBytes(length: Int, reader: () -> Byte): T

    /** Calculates the byte size of the storage bytes for [value] */
    fun calculateStorageByteLength(value: T) = byteSize

    /** Write a [value] to bytes with [writer] */
    fun writeStorageBytes(value: T, writer: (byte: Byte) -> Unit)

    companion object {
        @Suppress("EXPERIMENTAL_API_USAGE")
        internal fun <DO:Any> addByteSize(index: Int, definitions: ObjectPropertyDefinitions<DO>, getter: (DO) -> Int) {
            definitions.add(index, "byteSize",
                NumberDefinition(type = UInt32),
                getter,
                toSerializable = { value, _ -> value?.toUInt() },
                fromSerializable = { it?.toInt() }
            )
        }
    }
}
