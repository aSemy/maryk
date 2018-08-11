package maryk.core.properties.types

import maryk.EmbeddedMarykModel
import maryk.TestMarykModel
import maryk.core.models.asValues
import maryk.core.properties.exceptions.InjectException
import maryk.core.query.DataModelContext
import maryk.core.query.filters.Equals
import maryk.core.query.pairs.with
import maryk.core.query.requests.GetRequest
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

class InjectTest {
    val context = DataModelContext(
        dataModels = mutableMapOf(
            EmbeddedMarykModel.name to { EmbeddedMarykModel }
        )
    )

    private val valuesToCollect = EmbeddedMarykModel(
        value ="a test value",
        model = EmbeddedMarykModel(
            "embedded value"
        )
    )

    init {
        context.collectResult("testCollection", valuesToCollect)
    }

    private val inject = Inject(
        "testCollection",
        EmbeddedMarykModel,
        EmbeddedMarykModel.ref { value }
    )

    val injectDeep = Inject(
        "testCollection",
        EmbeddedMarykModel,
        EmbeddedMarykModel { model.ref { value } }
    )

    @Test
    fun testResolve() {
        inject.resolve(context) shouldBe "a test value"
        injectDeep.resolve(context) shouldBe "embedded value"
    }

    @Test
    fun testInjectInValues() {
        val values = TestMarykModel.map(context) {
            mapNonNulls(
                string with Inject(
                    "testCollection2",
                    EmbeddedMarykModel,
                    EmbeddedMarykModel { model.ref { value } }
                )
            )
        }

        shouldThrow<InjectException> {
            values { string }
        } shouldBe InjectException("testCollection2")

        context.collectResult("testCollection2", valuesToCollect)

        values { string } shouldBe "embedded value"
    }

    @Test
    fun testInjectInObject() {
        val getRequest = GetRequest.map(context) {
            mapNonNulls(
                filter with Inject(
                    "filter",
                    EmbeddedMarykModel,
                    EmbeddedMarykModel { model.ref { value } }
                )
            )
        }

        shouldThrow<InjectException> {
            getRequest { filter }
        } shouldBe InjectException("filter")

        val equals = Equals(
            EmbeddedMarykModel.ref { value } with "hoi"
        )

        context.collectResult("filter", Equals.asValues(equals))

        getRequest { filter } shouldBe equals
    }
}