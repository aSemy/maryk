package maryk.core.properties.types.numeric

/** Base class for unsigned integers */
abstract class UInt<T: Number> internal constructor(internal val number: T): Comparable<UInt<T>> {
    override fun hashCode() = number.hashCode()
    override fun equals(other: Any?) =
        when (other) {
            is Double -> other % 1 == 0.0 && this.toLong() == other.toLong()
            is Float -> other % 1 == 0.0F && this.toLong() == other.toLong()
            is Int, is Short, is Byte, is Long -> this.toLong() == other
            is UInt8 -> this.toLong() == other.toLong()
            is UInt16 -> this.toLong() == other.toLong()
            is UInt32 -> this.toLong() == other.toLong()
            is UInt64 -> this.toLong() == other.toLong()
            else -> false
        }

    abstract fun toInt(): Int
    abstract fun toLong(): Long
}