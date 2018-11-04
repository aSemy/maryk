package maryk.core.properties.types.numeric

import maryk.core.properties.enum.IndexedEnum
import maryk.core.properties.enum.IndexedEnumDefinition

enum class NumberType(
    override val index: Int,
    val descriptor: ()-> NumberDescriptor<*>
) : IndexedEnum<NumberType> {
    SInt8(1, { maryk.core.properties.types.numeric.SInt8 }),
    SInt16(2, { maryk.core.properties.types.numeric.SInt16 }),
    SInt32(3, { maryk.core.properties.types.numeric.SInt32 }),
    SInt64(4, { maryk.core.properties.types.numeric.SInt64 }),
    UInt8(5, { maryk.core.properties.types.numeric.UInt8 }),
    UInt16(6, { maryk.core.properties.types.numeric.UInt16 }),
    UInt32(7, { maryk.core.properties.types.numeric.UInt32 }),
    UInt64(8, { maryk.core.properties.types.numeric.UInt64 }),
    Float32(9, { maryk.core.properties.types.numeric.Float32 }),
    Float64(10, { maryk.core.properties.types.numeric.Float64 });

    companion object: IndexedEnumDefinition<NumberType>("NumberType", NumberType::values)
}