package maryk.core.query.changes

import maryk.SubMarykObject
import maryk.TestMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.numeric.toUInt64
import maryk.core.query.DataModelPropertyContext
import maryk.test.shouldBe
import kotlin.test.Test

class DataObjectChangeTest {
    private val key1 = TestMarykObject.key(
        byteArrayOf(0, 0, 2, 43, 1, 1, 1, 0, 2)
    )

    private val subModel = TestMarykObject.ref { subModel }

    private val dataObjectChange = key1.change(
        Change(SubMarykObject.ref(subModel) { value }, "new"),
        Delete(SubMarykObject.ref(subModel) { value }),
        Check(SubMarykObject.ref(subModel) { value }, "current"),
        ObjectSoftDeleteChange(true),
        ListChange(TestMarykObject.ref { list }),
        SetChange(TestMarykObject.ref { set }),
        MapChange(TestMarykObject.ref { map }),
        lastVersion = 12345L.toUInt64()
    )

    @Suppress("UNCHECKED_CAST")
    private val context = DataModelPropertyContext(
        mapOf(
            TestMarykObject.name to TestMarykObject
        ),
        dataModel = TestMarykObject as RootDataModel<Any, PropertyDefinitions<Any>>
    )

    @Test
    fun convert_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.dataObjectChange, DataObjectChange, this.context)
    }

    @Test
    fun convert_to_JSON_and_back() {
        checkJsonConversion(this.dataObjectChange, DataObjectChange, this.context)
    }

    @Test
    fun convert_to_YAML_and_back() {
        checkYamlConversion(this.dataObjectChange, DataObjectChange, this.context) shouldBe """
        key: AAACKwEBAQAC
        changes:
        - !Change
          reference: subModel.value
          value: new
        - !Delete
          reference: subModel.value
        - !Check
          reference: subModel.value
          value: current
        - !ObjectDelete
          isDeleted: true
        - !ListChange
          reference: list
        - !SetChange
          reference: set
        - !MapChange
          reference: map
        lastVersion: 0x0000000000003039

        """.trimIndent()
    }
}
