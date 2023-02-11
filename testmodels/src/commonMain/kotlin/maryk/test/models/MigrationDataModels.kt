@file:Suppress("unused")

package maryk.test.models

import maryk.core.properties.RootModel
import maryk.core.properties.definitions.number
import maryk.core.properties.definitions.string
import maryk.core.properties.types.Version
import maryk.core.properties.types.numeric.SInt32

object ModelV1 : RootModel<ModelV1>(
    name = "Model",
    version = Version(1),
    indices = {
        listOf(
            ModelV1.value.ref()
        )
    },
) {
    val value by string(index = 1u, default = "haha", regEx = "ha.*")
}

object ModelV1_1WrongKey : RootModel<ModelV1_1WrongKey>(
    name = "Model",
    version = Version(1),
    keyDefinition = {
        ModelV1_1WrongKey.newNumber.ref()
    },
) {
    val value by string(index = 1u, default = "haha", regEx = "ha.*")
    val newNumber by number(index = 2u, type = SInt32, required = true, final = true)
}

object ModelV1_1 : RootModel<ModelV1_1>(
    name = "Model",
    version = Version(1, 1),
) {
    val value by string(index = 1u, default = "haha", regEx = "ha.*")
    val newNumber by number(index = 2u, type = SInt32, required = false)
}

object ModelV2 : RootModel<ModelV2>(
    name = "Model",
    version = Version(2),
    indices = { listOf(
        ModelV2.value.ref()
    ) },
) {
    val value by string(index = 1u, default = "haha", regEx = "ha.*")
    val newNumber by number(index = 2u, type = SInt32, required = true)
}

object ModelV2ExtraIndex : RootModel<ModelV2ExtraIndex>(
    name = "Model",
    version = Version(2),
    indices = {
        listOf(
            ModelV2ExtraIndex.value.ref(),
            ModelV2ExtraIndex.newNumber.ref()
        )
    },
) {
    val value by string(index = 1u, default = "haha", regEx = "ha.*")
    val newNumber by number(index = 2u, type = SInt32, required = true)
}

object ModelWrongValueType : RootModel<ModelWrongValueType>(
    name = "Model",
    version = Version(2),
) {
    val value by number(index = 1u,  type = SInt32)
}

object ModelMissingProperty : RootModel<ModelMissingProperty>(
    name = "Model",
    version = Version(1, 2),
) {
    val newNumber by number(index = 2u, type = SInt32, required = true)
}

object ModelV2ReservedNamesAndIndices : RootModel<ModelV2ReservedNamesAndIndices>(
    name = "Model",
    version = Version(1, 2),
    reservedNames = listOf("value"),
    reservedIndices = listOf(1u),
) {
    val newNumber by number(index = 2u, type = SInt32, required = false)
}
