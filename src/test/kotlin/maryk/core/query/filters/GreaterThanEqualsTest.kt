package maryk.core.query.filters

import maryk.SubMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.objects.RootDataModel
import maryk.core.query.DataModelPropertyContext
import org.junit.Test

class GreaterThanEqualsTest {
    private val greaterThanEquals = GreaterThanEquals(
            reference = SubMarykObject.Properties.value.getRef(),
            value = "test"
    )

    @Suppress("UNCHECKED_CAST")
    private val context = DataModelPropertyContext(
            mapOf(
                    SubMarykObject.name to SubMarykObject
            ),
            dataModel = SubMarykObject as RootDataModel<Any>
    )

    @Test
    fun testProtoBufConversion() {
        checkProtoBufConversion(this.greaterThanEquals, GreaterThanEquals, this.context)
    }

    @Test
    fun testJsonConversion() {
        checkJsonConversion(this.greaterThanEquals, GreaterThanEquals, this.context)
    }
}