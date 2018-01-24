package maryk.core.query.changes

import maryk.SimpleMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.query.DataModelPropertyContext
import kotlin.test.Test

class ObjectSoftDeleteTest {
    private val objectSoftDeleteChange = ObjectSoftDeleteChange(
            isDeleted = true
    )

    @Suppress("UNCHECKED_CAST")
    private val context = DataModelPropertyContext(
            mapOf(
                    SimpleMarykObject.name to SimpleMarykObject
            ),
            dataModel = SimpleMarykObject as RootDataModel<Any, PropertyDefinitions<Any>>
    )

    @Test
    fun testProtoBufConversion() {
        checkProtoBufConversion(this.objectSoftDeleteChange, ObjectSoftDeleteChange, this.context)
    }

    @Test
    fun testJsonConversion() {
        checkJsonConversion(this.objectSoftDeleteChange, ObjectSoftDeleteChange, this.context)
    }
}