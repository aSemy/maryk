package maryk

import maryk.core.models.RootDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.NumberDefinition
import maryk.core.properties.types.numeric.Float32
import maryk.core.properties.types.numeric.Float64
import maryk.core.properties.types.numeric.SInt16
import maryk.core.properties.types.numeric.SInt32
import maryk.core.properties.types.numeric.SInt64
import maryk.core.properties.types.numeric.SInt8
import maryk.core.properties.types.numeric.UInt16
import maryk.core.properties.types.numeric.UInt32
import maryk.core.properties.types.numeric.UInt64
import maryk.core.properties.types.numeric.UInt8
import maryk.core.properties.types.numeric.toUInt16
import maryk.core.properties.types.numeric.toUInt32
import maryk.core.properties.types.numeric.toUInt64
import maryk.core.properties.types.numeric.toUInt8

object NumericMarykModel: RootDataModel<NumericMarykModel, NumericMarykModel.Properties>(
    name = "NumericMarykModel",
    properties = Properties
) {
    object Properties: PropertyDefinitions() {
        val sInt8 = add(
            index = 1, name = "sInt8",
            definition = NumberDefinition(
                type = SInt8,
                default = 4.toByte()
            )
        )
        val sInt16 = add(
            index = 2, name = "sInt16",
            definition = NumberDefinition(
                type = SInt16,
                default = 42.toShort()
            )
        )
        val sInt32 = add(
            index = 3, name = "sInt32",
            definition = NumberDefinition(
                type = SInt32,
                default = 42
            )
        )
        val sInt64 = add(
            index = 4, name = "sInt64",
            definition = NumberDefinition(
                type = SInt64,
                default = 4123123344572L
            )
        )
        val uInt8 = add(
            index = 5, name = "uInt8",
            definition = NumberDefinition(
                type = UInt8,
                default = 4.toUInt8()
            )
        )
        val uInt16 = add(
            index = 6, name = "uInt16",
            definition = NumberDefinition(
                type = UInt16,
                default = 42.toUInt16()
            )
        )
        val uInt32 = add(
            index = 7, name = "uInt32",
            definition = NumberDefinition(
                type = UInt32,
                default = 42.toUInt32()
            )
        )
        val uInt64 = add(
            index = 8, name = "uInt64",
            definition = NumberDefinition(
                type = UInt64,
                default = 4123123344572L.toUInt64()
            )
        )
        val float32 = add(
            index = 9, name = "float32",
            definition = NumberDefinition(
                type = Float32,
                default = 42.345F
            )
        )
        val float64 = add(
            index = 10, name = "float64",
            definition = NumberDefinition(
                type = Float64,
                default = 2345762.3123
            )
        )
    }

    operator fun invoke(
        sInt8: Byte = 4.toByte(),
        sInt16: Short = 42.toShort(),
        sInt32: Int = 42,
        sInt64: Long = 4123123344572L,
        uInt8: UInt8 = 4.toUInt8(),
        uInt16: UInt16 = 42.toUInt16(),
        uInt32: UInt32 = 42.toUInt32(),
        uInt64: UInt64 = 4123123344572L.toUInt64(),
        float32: Float = 42.345F.toFloat(),
        float64: Double = 2345762.3123
    ) = map {
        mapNonNulls(
            this.sInt8 with sInt8,
            this.sInt16 with sInt16,
            this.sInt32 with sInt32,
            this.sInt64 with sInt64,
            this.uInt8 with uInt8,
            this.uInt16 with uInt16,
            this.uInt32 with uInt32,
            this.uInt64 with uInt64,
            this.float32 with float32,
            this.float64 with float64
        )
    }
}