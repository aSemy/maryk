package maryk.core.query.responses

import maryk.EmbeddedMarykModel
import maryk.TestMarykModel
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
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
import maryk.core.query.changes.change
import maryk.core.query.pairs.with
import maryk.test.shouldBe
import kotlin.test.Test

class VersionedChangesResponseTest {
    private val key = TestMarykModel.key("AAACKwEBAQAC")

    private val subModel = TestMarykModel.ref { embeddedValues }

    private val objectVersionedChangesResponse = VersionedChangesResponse(
        TestMarykModel,
        listOf(
            DataObjectVersionedChange(
                key = key,
                changes = listOf(
                    VersionedChanges(
                        219674127L.toUInt64(),
                        listOf(
                            ObjectSoftDeleteChange(true),
                            ListChange(TestMarykModel.ref { list }.change()),
                            SetChange(TestMarykModel.ref { set }.change()),
                            MapChange(TestMarykModel.ref { map }.change())
                        )
                    ),
                    VersionedChanges(
                        319674127L.toUInt64(),
                        listOf(
                            Change(EmbeddedMarykModel.ref(subModel) { value } with "new"),
                            Delete(EmbeddedMarykModel.ref(subModel) { value }),
                            Check(EmbeddedMarykModel.ref(subModel) { value } with "current")
                        )
                    )
                )
            )
        )
    )

    private val context = DataModelPropertyContext(
        dataModels = mapOf(
            EmbeddedMarykModel.name to { EmbeddedMarykModel },
            TestMarykModel.name to { TestMarykModel }
        ),
        dataModel = TestMarykModel
    )

    @Test
    fun convert_to_ProtoBuf_and_back() {
        checkProtoBufConversion(this.objectVersionedChangesResponse, VersionedChangesResponse, { this.context })
    }

    @Test
    fun convert_to_JSON_and_back() {
        checkJsonConversion(this.objectVersionedChangesResponse, VersionedChangesResponse, { this.context })
    }

    @Test
    fun convert_to_YAML_and_back() {
        checkYamlConversion(this.objectVersionedChangesResponse, VersionedChangesResponse, { this.context }) shouldBe """
        dataModel: TestMarykModel
        changes:
        - key: AAACKwEBAQAC
          changes:
          - version: 219674127
            changes:
            - !ObjectDelete
              isDeleted: true
            - !ListChange
              list:
            - !SetChange
              set:
            - !MapChange
              map:
          - version: 319674127
            changes:
            - !Change
              embeddedValues.value: new
            - !Delete embeddedValues.value
            - !Check
              embeddedValues.value: current

        """.trimIndent()
    }
}