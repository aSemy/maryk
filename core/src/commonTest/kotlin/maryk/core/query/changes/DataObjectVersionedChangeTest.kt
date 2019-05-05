package maryk.core.query.changes

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.extensions.toUnitLambda
import maryk.core.query.RequestContext
import maryk.core.query.pairs.with
import maryk.test.models.EmbeddedMarykModel
import maryk.test.models.TestMarykModel
import maryk.test.shouldBe
import kotlin.test.Test

class DataObjectVersionedChangeTest {
    private val key1 = TestMarykModel.key(
        byteArrayOf(0, 0, 2, 43, 1, 0, 2)
    )

    private val subModel = TestMarykModel { embeddedValues::ref }

    private val dataObjectVersionedChanges = DataObjectVersionedChange(
        key = key1,
        changes = listOf(
            VersionedChanges(
                219674127uL,
                listOf(
                    ObjectSoftDeleteChange(true),
                    ListChange(TestMarykModel { list::ref }.change()),
                    SetChange(TestMarykModel { set::ref }.change())
                )
            ),
            VersionedChanges(
                319674127uL,
                listOf(
                    Change(EmbeddedMarykModel(subModel) { value::ref } with "new"),
                    Delete(EmbeddedMarykModel(subModel) { value::ref }),
                    Check(EmbeddedMarykModel(subModel) { value::ref } with "current")
                )
            )
        )
    )

    private val context = RequestContext(
        dataModels = mapOf(
            EmbeddedMarykModel.name toUnitLambda { EmbeddedMarykModel },
            TestMarykModel.name toUnitLambda { TestMarykModel }
        ),
        dataModel = TestMarykModel
    )

    @Test
    fun convertToProtoBufAndBack() {
        checkProtoBufConversion(this.dataObjectVersionedChanges, DataObjectVersionedChange, { this.context })
    }

    @Test
    fun convertToJSONAndBack() {
        checkJsonConversion(this.dataObjectVersionedChanges, DataObjectVersionedChange, { this.context })
    }

    @Test
    fun convertToYAMLAndBack() {
        checkYamlConversion(this.dataObjectVersionedChanges, DataObjectVersionedChange, { this.context }) shouldBe """
        key: AAACKwEAAg
        changes:
        - version: 219674127
          changes:
          - !ObjectDelete
            isDeleted: true
          - !ListChange
            list:
          - !SetChange
            set:
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
