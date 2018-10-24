package maryk.core.properties.types.numeric

import maryk.core.extensions.bytes.calculateVarByteLength
import maryk.core.extensions.bytes.initInt
import maryk.core.extensions.bytes.initIntByVar
import maryk.core.extensions.bytes.writeBytes
import maryk.core.extensions.bytes.writeVarBytes
import maryk.lib.exceptions.ParseException
import kotlin.random.Random

/** Base class for 32 bit/4 byte unsigned integers */
class UInt32 internal constructor(number: Int): UInt<Int>(number) {
    override fun compareTo(other: UInt<Int>) = number.compareTo(other.number)
    override fun toString() = (number.toLong() - Int.MIN_VALUE).toString()
    override fun toInt() = this.number - Int.MIN_VALUE
    override fun toLong() = number.toLong() - Int.MIN_VALUE

    companion object : UnsignedNumberDescriptor<UInt32>(
        size = 4,
        MIN_VALUE = UInt32(Int.MIN_VALUE),
        MAX_VALUE = UInt32(Int.MAX_VALUE),
        type = NumberType.UInt32
    ) {
        override fun fromStorageByteReader(length: Int, reader: () -> Byte) = UInt32(initInt(reader))
        override fun writeStorageBytes(value: UInt32, writer: (byte: Byte) -> Unit) = value.number.writeBytes(writer)
        override fun readTransportBytes(reader: () -> Byte) = UInt32(initIntByVar(reader) + Int.MIN_VALUE)
        override fun calculateTransportByteLength(value: UInt32) = (value.number - Int.MIN_VALUE).calculateVarByteLength()
        override fun writeTransportBytes(value: UInt32, writer: (byte: Byte) -> Unit) {
            val number = value.number.toLong() - Int.MIN_VALUE
            number.writeVarBytes(writer)
        }
        override fun ofString(value: String) = UInt32((value.toLong() + Int.MIN_VALUE).toInt())
        override fun ofDouble(value: Double) = value.toInt().toUInt32()
        override fun ofInt(value: Int) = value.toUInt32()
        override fun ofLong(value: Long) = value.toInt().toUInt32()
        override fun createRandom() = UInt32(Random.nextInt())
        override fun isOfType(value: Any) = value == UInt32
    }
}

fun Int.toUInt32() = if (this >= 0) {
    UInt32(this + Int.MIN_VALUE)
} else { throw ParseException("Negative Int not allowed $this") }

fun Long.toUInt32() = if (this >= 0) {
    UInt32((this + Int.MIN_VALUE).toInt())
} else { throw ParseException("Negative Long not allowed $this") }
