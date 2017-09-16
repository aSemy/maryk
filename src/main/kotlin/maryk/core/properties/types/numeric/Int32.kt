package maryk.core.properties.types.numeric

import maryk.core.extensions.bytes.initInt
import maryk.core.extensions.bytes.toBytes
import maryk.core.extensions.bytes.writeBytes
import maryk.core.extensions.random

object Int32 : NumberDescriptor<Int>(
        size = 4
) {
    override fun fromByteReader(length: Int, reader: () -> Byte): Int = initInt(reader)
    override fun writeBytes(value: Int, writer: (byte: Byte) -> Unit) = value.writeBytes(writer)
    override fun toBytes(value: Int, bytes: ByteArray?, offset: Int) = value.toBytes(bytes, offset)
    override fun ofBytes(bytes: ByteArray, offset: Int, length: Int) = initInt(bytes, offset)
    override fun ofString(value: String) = value.toInt()
    override fun createRandom() = Int.random()
}