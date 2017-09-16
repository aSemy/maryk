package maryk.core.properties.types.numeric

import maryk.core.extensions.bytes.initByte
import maryk.core.extensions.bytes.toBytes
import maryk.core.extensions.bytes.writeBytes
import maryk.core.extensions.random

/** Base class for 8 bit/1 byte unsigned integers */
class UInt8 internal constructor(number: Byte): UInt<Byte>(number) {
    override fun compareTo(other: UInt<Byte>) = number.compareTo(other.number)
    override fun toString() = (number.toShort() - Byte.MIN_VALUE).toString()
    override fun toBytes(bytes: ByteArray?, offset: Int) = number.toBytes(bytes ?: ByteArray(size), offset)
    override fun writeBytes(writer: (Byte) -> Unit) = number.writeBytes(writer)
    companion object : UnsignedNumberDescriptor<UInt8>(
            size = 1,
            MIN_VALUE = UInt8(Byte.MIN_VALUE),
            MAX_VALUE = UInt8(Byte.MAX_VALUE)
    ) {
        override fun fromByteReader(length: Int, reader: () -> Byte): UInt8 = UInt8(initByte(reader))
        override fun writeBytes(value: UInt8, writer: (byte: Byte) -> Unit) = value.writeBytes(writer)
        override fun toBytes(value: UInt8, bytes: ByteArray?, offset: Int) = value.toBytes(bytes, offset)
        override fun ofBytes(bytes: ByteArray, offset: Int, length: Int) = UInt8(initByte(bytes, offset))
        override fun ofString(value: String) = UInt8((value.toShort() + Byte.MIN_VALUE).toByte())
        override fun createRandom() = UInt8(Byte.random())
    }
}

fun Byte.toUInt8() = if (this > 0) {
    UInt8((this + Byte.MIN_VALUE).toByte())
} else { throw NumberFormatException("Negative Byte not allowed $this") }

fun Int.toUInt8() = if (this > 0) {
    UInt8((this + Byte.MIN_VALUE).toByte())
} else { throw NumberFormatException("Negative Int not allowed $this") }