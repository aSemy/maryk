package maryk.core.properties.enum

import maryk.core.exceptions.DefNotFoundException

/**
 * Implementation for IndexedEnumComparable so they are easier to implement
 */
abstract class IndexedEnumImpl<E: IndexedEnum>(
    final override val index: UInt
) : IndexedEnumComparable<E> {
    override val name = this::class.simpleName ?: throw DefNotFoundException("Missing enum option name")

    override fun compareTo(other: E) = this.index.compareTo(other.index)

    override fun equals(other: Any?) = other is IndexedEnum && index == other.index && name == other.name

    override fun hashCode() = 31 * index.hashCode() + name.hashCode()

    override fun toString() = name
}
