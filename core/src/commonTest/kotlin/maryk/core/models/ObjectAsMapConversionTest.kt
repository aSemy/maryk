package maryk.core.models

import maryk.checkJsonConversion
import maryk.checkProtoBufObjectValuesConversion
import maryk.checkYamlConversion
import maryk.core.extensions.toUnitLambda
import maryk.core.query.RequestContext
import maryk.core.query.filters.Exists
import maryk.core.query.requests.GetRequest
import maryk.test.models.SimpleMarykModel
import maryk.test.shouldBe
import kotlin.test.Test

private val key1 = SimpleMarykModel.key("dR9gVdRcSPw2molM1AiOng")
private val key2 = SimpleMarykModel.key("Vc4WgX/mQHYCSEoLtfLSUQ")

private val context = RequestContext(mapOf(
    SimpleMarykModel.name toUnitLambda { SimpleMarykModel }
))

class ObjectAsMapConversionTest {
    private val getRequestWithInjectable = GetRequest.values(context) {
        mapNonNulls(
            dataModel with SimpleMarykModel,
            keys with listOf(key1, key2),
            select with SimpleMarykModel.graph {
                listOf(value)
            },
            where with Exists(SimpleMarykModel { value::ref }),
            toVersion with 333uL,
            filterSoftDeleted with true
        )
    }

    @Test
    fun convertToYAMLAndBack() {
        checkYamlConversion(
            getRequestWithInjectable,
            GetRequest,
            { context },
            checker = { a, b ->
                a.toDataObject() shouldBe b.toDataObject()
            }
        ) shouldBe """
        from: SimpleMarykModel
        keys: [dR9gVdRcSPw2molM1AiOng, Vc4WgX/mQHYCSEoLtfLSUQ]
        select:
        - value
        where: !Exists value
        toVersion: 333
        filterSoftDeleted: true

        """.trimIndent()
    }

    @Test
    fun convertToJSONAndBack() {
        checkJsonConversion(
            getRequestWithInjectable,
            GetRequest,
            { context },
            checker = { a, b ->
                a.toDataObject() shouldBe b.toDataObject()
            }
        )
    }

    @Test
    fun convertToProtoBufAndBack() {
        checkProtoBufObjectValuesConversion(
            getRequestWithInjectable,
            GetRequest,
            { context },
            checker = { a, b ->
                a.toDataObject() shouldBe b.toDataObject()
            }
        )
    }
}
