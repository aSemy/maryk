package maryk.core.query.changes

import maryk.SubMarykObject
import maryk.TestMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.numeric.toUInt64
import maryk.core.query.DataModelPropertyContext
import kotlin.test.Test

class VersionedChangesTest {
    private val versionedChanges = VersionedChanges(
            219674127L.toUInt64(),
            listOf(
                    PropertyChange(TestMarykObject { subModel ref { value } }, "new"),
                    PropertyDelete(TestMarykObject { subModel ref { value } }),
                    PropertyCheck(TestMarykObject { subModel ref { value } }),
                    ObjectSoftDeleteChange(true),
                    ListPropertyChange(TestMarykObject.ref { list }),
                    SetPropertyChange(TestMarykObject.ref { set }),
                    MapPropertyChange(TestMarykObject.ref { map })
            )
    )

    @Suppress("UNCHECKED_CAST")
    private val context = DataModelPropertyContext(
            dataModels = mapOf(
                SubMarykObject.name to SubMarykObject,
                TestMarykObject.name to TestMarykObject
            ),
            dataModel = TestMarykObject as RootDataModel<Any, PropertyDefinitions<Any>>
    )

    @Test
    fun testProtoBufConversion() {
        checkProtoBufConversion(this.versionedChanges, VersionedChanges, this.context)
    }

    @Test
    fun testJsonConversion() {
        checkJsonConversion(this.versionedChanges, VersionedChanges, this.context)
    }
}