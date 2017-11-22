package maryk.core.query.requests

import maryk.SubMarykObject
import maryk.checkJsonConversion
import maryk.checkProtoBufConversion
import maryk.core.query.DataModelPropertyContext
import maryk.test.shouldBe
import kotlin.test.Test

class AddRequestTest {
    private val addRequest = AddRequest(
            SubMarykObject,
            SubMarykObject(value = "haha1"),
            SubMarykObject(value = "haha2")
    )

    @Test
    fun testAddObject() {
        this.addRequest.dataModel shouldBe SubMarykObject
        this.addRequest.objectsToAdd.size shouldBe 2
    }

    private val context = DataModelPropertyContext(mapOf(
            SubMarykObject.name to SubMarykObject
    ))

    @Test
    fun testProtoBufConversion() {
        checkProtoBufConversion(this.addRequest, AddRequest, this.context, ::compareRequest)
    }

    @Test
    fun testJsonConversion() {
        checkJsonConversion(this.addRequest, AddRequest, this.context, ::compareRequest)
    }

    private fun compareRequest(converted: AddRequest<*, *>, original: AddRequest<*, *>) {
        converted.objectsToAdd.contentDeepEquals(original.objectsToAdd) shouldBe true
        converted.dataModel shouldBe original.dataModel
    }
}