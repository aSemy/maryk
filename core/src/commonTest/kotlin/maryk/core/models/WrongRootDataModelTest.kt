package maryk.core.models

import maryk.core.properties.RootModel
import maryk.core.properties.definitions.string
import maryk.test.models.TestMarykModel
import kotlin.test.Test
import kotlin.test.assertFailsWith

object WrongModelIndex : RootModel<WrongModelIndex>(
    reservedIndices = listOf(1u),
) {
    val value by string(1u)
}

object WrongModelName : RootModel<WrongModelName>(
    reservedNames = listOf("value"),
) {
    val value by string (1u)
}

internal class WrongRootDataModelTest {
    @Test
    fun checkDataModel() {
        TestMarykModel.Model.check()

        assertFailsWith<IllegalArgumentException> {
            WrongModelIndex.Model.check()
        }

        assertFailsWith<IllegalArgumentException> {
            WrongModelName.Model.check()
        }
    }
}
