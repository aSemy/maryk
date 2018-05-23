package maryk.core.query.responses

import maryk.SubMarykObject
import maryk.TestMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.numeric.toUInt64
import maryk.core.query.DataModelPropertyContext
import maryk.core.query.changes.Change
import maryk.core.query.changes.Check
import maryk.core.query.changes.DataObjectVersionedChange
import maryk.core.query.changes.Delete
import maryk.core.query.changes.ListChange
import maryk.core.query.changes.MapChange
import maryk.core.query.changes.ObjectSoftDeleteChange
import maryk.core.query.changes.SetChange
import maryk.core.query.changes.VersionedChanges
import maryk.test.shouldBe
import kotlin.test.Test

class ObjectVersionedChangesResponseTest {
    private val key = TestMarykObject.key("AAACKwEBAQAC")

    private val subModel = TestMarykObject.ref { subModel }

    private val objectVersionedChangesResponse = ObjectVersionedChangesResponse(
        TestMarykObject,
        listOf(
            DataObjectVersionedChange(
                key = key,
                changes = listOf(
                    VersionedChanges(
                        219674127L.toUInt64(),
                        listOf(
                            ObjectSoftDeleteChange(true),
                            ListChange(TestMarykObject.ref { list }),
                            SetChange(TestMarykObject.ref { set }),
                            MapChange(TestMarykObject.ref { map })
                        )
                    ),
                    VersionedChanges(
                        319674127L.toUInt64(),
                        listOf(
                            Change(SubMarykObject.ref(subModel) { value }, "new"),
                            Delete(SubMarykObject.ref(subModel) { value }),
                            Check(SubMarykObject.ref(subModel) { value }, "current")
                        )
                    )
                )
            )
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
    fun convert_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.objectVersionedChangesResponse, ObjectVersionedChangesResponse, this.context)
    }

    @Test
    fun convert_to_JSON_and_back() {
        checkJsonConversion(this.objectVersionedChangesResponse, ObjectVersionedChangesResponse, this.context)
    }

    @Test
    fun convert_to_YAML_and_back() {
        checkYamlConversion(this.objectVersionedChangesResponse, ObjectVersionedChangesResponse, this.context) shouldBe """
        dataModel: TestMarykObject
        changes:
        - key: AAACKwEBAQAC
          changes:
          - version: 0x000000000d17f60f
            changes:
            - !ObjectDelete
              isDeleted: true
            - !ListChange
              reference: list
            - !SetChange
              reference: set
            - !MapChange
              reference: map
          - version: 0x00000000130dd70f
            changes:
            - !Change
              reference: subModel.value
              value: new
            - !Delete
              reference: subModel.value
            - !Check
              reference: subModel.value
              value: current

        """.trimIndent()
    }
}
