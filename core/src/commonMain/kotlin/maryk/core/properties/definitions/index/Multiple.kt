package maryk.core.properties.definitions.index

import maryk.core.extensions.bytes.calculateVarByteLength
import maryk.core.extensions.bytes.writeVarBytes
import maryk.core.models.SingleValueDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.references.IsIndexablePropertyReference
import maryk.core.properties.types.TypedValue
import maryk.core.query.DefinitionsConversionContext
import maryk.core.values.IsValuesGetter
import maryk.core.values.ObjectValues

/** Class to encode multiple [references] for key or other indexable usages */
data class Multiple(
    val references: List<IsIndexablePropertyReference<out Any>>
) : IsIndexable {
    override val indexKeyPartType = IndexKeyPartType.Multiple

    /** Convenience method to set with each [reference] as separate argument */
    constructor(vararg reference: IsIndexablePropertyReference<*>) : this(listOf(*reference))

    override fun calculateStorageByteLengthForIndex(values: IsValuesGetter, key: ByteArray): Int {
        var totalBytes = 0
        for (reference in references) {
            val value = reference.getValue(values)
            @Suppress("UNCHECKED_CAST")
            val length = (reference as IsIndexablePropertyReference<Any>).calculateStorageByteLength(value)
            totalBytes += length + length.calculateVarByteLength()
        }
        return totalBytes + key.size
    }

    override fun writeStorageBytesForIndex(values: IsValuesGetter, key: ByteArray, writer: (byte: Byte) -> Unit) {
        val sizes = IntArray(this.references.size)
        for ((keyIndex, reference) in this.references.withIndex()) {
            val value = reference.getValue(values)

            @Suppress("UNCHECKED_CAST")
            sizes[keyIndex] = (reference as IsIndexablePropertyReference<Any>).calculateStorageByteLength(value)

            reference.writeStorageBytes(value, writer)
        }

        // Write the sizes in backwards order
        for (sizeIndex in sizes.lastIndex..0) {
            sizes[sizeIndex].writeVarBytes(writer)
        }

        key.forEach(writer) // write key at end
    }

    override fun writeStorageBytes(values: IsValuesGetter, writer: (byte: Byte) -> Unit) {
        for (reference in this.references) {
            val value = reference.getValue(values)

            @Suppress("UNCHECKED_CAST")
            (reference as IsIndexablePropertyReference<Any>).writeStorageBytes(value, writer)
        }
    }

    /**
     * Add all lengths of sub references + their lengths
     */
    override fun calculateReferenceStorageByteLength() =
        this.indexKeyPartType.index.calculateVarByteLength() + references.sumBy {
            it.calculateReferenceStorageByteLength().let { length ->
                length + length.calculateVarByteLength()
            }
        }

    /**
     * Write defining byte + all sub reference bytes preceded by their length to avoid conflicts
     */
    override fun writeReferenceStorageBytes(writer: (Byte) -> Unit) {
        this.indexKeyPartType.index.writeVarBytes(writer)
        for (reference in this.references) {
            reference.calculateReferenceStorageByteLength().writeVarBytes(writer)
            reference.writeReferenceStorageBytes(writer)
        }
    }

    object Properties : ObjectPropertyDefinitions<Multiple>() {
        @Suppress("UNCHECKED_CAST")
        val references = add(
            1u, "references",
            ListDefinition(
                valueDefinition = MultiTypeDefinition(
                    typeEnum = IndexKeyPartType,
                    definitionMap = mapOfSimpleIndexKeyPartDefinitions
                )
            ) as ListDefinition<TypedValue<IndexKeyPartType, IsIndexablePropertyReference<*>>, IsPropertyContext>,
            toSerializable = { value ->
                TypedValue(value.indexKeyPartType, value)
            },
            fromSerializable = { typedValue ->
                typedValue.value
            },
            getter = Multiple::references
        )
    }

    internal object Model :
        SingleValueDataModel<List<TypedValue<IndexKeyPartType, IsIndexablePropertyReference<*>>>, List<IsIndexablePropertyReference<*>>, Multiple, Properties, DefinitionsConversionContext>(
            properties = Properties,
            singlePropertyDefinition = Properties.references
        ) {
        override fun invoke(values: ObjectValues<Multiple, Properties>) = Multiple(
            references = values(1u)
        )
    }
}
