@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package maryk.core.query.requests

import maryk.SimpleMarykModel
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.extensions.toUnitLambda
import maryk.core.properties.graph.RootPropRefGraph
import maryk.core.query.RequestContext
import maryk.core.query.descending
import maryk.core.query.filters.Exists
import maryk.test.shouldBe
import kotlin.test.Test

private val key1 = SimpleMarykModel.key("WWurg6ysTsozoMei/SurOw")
private val key2 = SimpleMarykModel.key("awfbjYrVQ+cdXblfQKV10A")

internal val getVersionedChangesRequest = SimpleMarykModel.getVersionedChanges(
    key1,
    key2,
    fromVersion = 1234uL
)

internal val getVersionedChangesMaxRequest = SimpleMarykModel.run {
    getVersionedChanges(
        key1,
        key2,
        filter = Exists(ref { value }),
        order = ref { value }.descending(),
        fromVersion = 1234uL,
        toVersion = 12345uL,
        maxVersions = 5u,
        filterSoftDeleted = true,
        select = SimpleMarykModel.props {
            RootPropRefGraph<SimpleMarykModel>(
                value
            )
        }
    )
}

class GetVersionedChangesRequestTest {
    private val context = RequestContext(mapOf(
        SimpleMarykModel.name toUnitLambda { SimpleMarykModel }
    ))

    @Test
    fun convertToProtoBufAndBack() {
        checkProtoBufConversion(getVersionedChangesRequest, GetVersionedChangesRequest, { this.context })
        checkProtoBufConversion(getVersionedChangesMaxRequest, GetVersionedChangesRequest, { this.context })
    }

    @Test
    fun convertToJSONAndBack() {
        checkJsonConversion(getVersionedChangesRequest, GetVersionedChangesRequest, { this.context })
        checkJsonConversion(getVersionedChangesMaxRequest, GetVersionedChangesRequest, { this.context })
    }

    @Test
    fun convertToYAMLAndBack() {
        checkYamlConversion(getVersionedChangesRequest, GetVersionedChangesRequest, { this.context }) shouldBe """
        dataModel: SimpleMarykModel
        keys: [WWurg6ysTsozoMei/SurOw, awfbjYrVQ+cdXblfQKV10A]
        filterSoftDeleted: true
        fromVersion: 1234
        maxVersions: 1000

        """.trimIndent()

        checkYamlConversion(getVersionedChangesMaxRequest, GetVersionedChangesRequest, { this.context }) shouldBe """
        dataModel: SimpleMarykModel
        keys: [WWurg6ysTsozoMei/SurOw, awfbjYrVQ+cdXblfQKV10A]
        select:
        - value
        filter: !Exists value
        order: !Desc value
        toVersion: 12345
        filterSoftDeleted: true
        fromVersion: 1234
        maxVersions: 5

        """.trimIndent()
    }
}
