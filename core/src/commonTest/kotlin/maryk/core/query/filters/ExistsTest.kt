package maryk.core.query.filters

import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.checkYamlConversion
import maryk.core.extensions.toUnitLambda
import maryk.core.query.RequestContext
import maryk.test.models.TestMarykModel
import maryk.test.shouldBe
import kotlin.test.Test

class ExistsTest {
    private val exists = Exists(
        TestMarykModel { string::ref }
    )
    private val existsMultiple = Exists(
        TestMarykModel { string::ref },
        TestMarykModel { int::ref },
        TestMarykModel { dateTime::ref }
    )

    private val context = RequestContext(
        mapOf(
            TestMarykModel.name toUnitLambda { TestMarykModel }
        ),
        dataModel = TestMarykModel
    )

    @Test
    fun convertToProtoBufAndBack() {
        checkProtoBufConversion(this.exists, Exists, { this.context })
    }

    @Test
    fun convertToJSONAndBack() {
        checkJsonConversion(this.exists, Exists, { this.context })
    }

    @Test
    fun convertToYAMLAndBack() {
        checkYamlConversion(this.exists, Exists, { this.context }) shouldBe """
        string
        """.trimIndent()

        checkYamlConversion(this.existsMultiple, Exists, { this.context }) shouldBe """
        - string
        - int
        - dateTime

        """.trimIndent()
    }
}
