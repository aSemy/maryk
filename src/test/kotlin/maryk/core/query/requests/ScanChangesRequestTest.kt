package maryk.core.query.requests

import maryk.SubMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.properties.types.numeric.toUInt32
import maryk.core.properties.types.toUInt64
import maryk.core.query.DataModelPropertyContext
import maryk.core.query.Order
import maryk.core.query.filters.Exists
import maryk.test.shouldBe
import kotlin.test.Test

class ScanChangesRequestTest {
    private val key1 = SubMarykObject.key.getKey(SubMarykObject("test1"))

    private val scanChangesRequest = ScanChangesRequest(
            SubMarykObject,
            startKey = key1,
            fromVersion = 1234L.toUInt64()
    )

    private val scanChangeMaxRequest = ScanChangesRequest(
            SubMarykObject,
            startKey = key1,
            filter = Exists(SubMarykObject.Properties.value.getRef()),
            order = Order(SubMarykObject.Properties.value.getRef()),
            limit = 100.toUInt32(),
            filterSoftDeleted = true,
            toVersion = 2345L.toUInt64(),
            fromVersion = 1234L.toUInt64()
    )

    private val context = DataModelPropertyContext(mapOf(
            SubMarykObject.name to SubMarykObject
    ))

    @Test
    fun testProtoBufConversion() {
        checkProtoBufConversion(this.scanChangesRequest, ScanChangesRequest, this.context, ::compareRequest)
        checkProtoBufConversion(this.scanChangeMaxRequest, ScanChangesRequest, this.context, ::compareRequest)
    }

    @Test
    fun testJsonConversion() {
        checkJsonConversion(this.scanChangesRequest, ScanChangesRequest, this.context, ::compareRequest)
        checkJsonConversion(this.scanChangeMaxRequest, ScanChangesRequest, this.context, ::compareRequest)
    }

    private fun compareRequest(converted: ScanChangesRequest<*, *>, expected: ScanChangesRequest<*, *>) {
        converted.dataModel shouldBe expected.dataModel
        converted.startKey shouldBe expected.startKey
        converted.filter shouldBe expected.filter
        converted.order shouldBe expected.order
        converted.limit shouldBe expected.limit
        converted.toVersion shouldBe expected.toVersion
        converted.fromVersion shouldBe expected.fromVersion
        converted.filterSoftDeleted shouldBe expected.filterSoftDeleted
    }
}