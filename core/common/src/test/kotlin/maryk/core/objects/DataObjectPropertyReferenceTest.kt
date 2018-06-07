package maryk.core.objects

import maryk.TestMarykObject
import maryk.lib.time.Date
import maryk.lib.time.Time
import maryk.test.shouldBe
import kotlin.test.Test

internal class DataObjectPropertyReferenceTest {
    @Test
    fun testReference() {
        TestMarykObject.ref { string }.completeName shouldBe "string"
        TestMarykObject.ref { bool }.completeName shouldBe "bool"

        TestMarykObject { embeddedObject ref { value } }.completeName shouldBe "embeddedObject.value"

        TestMarykObject { embeddedObject ref { embedded } }.completeName shouldBe "embeddedObject.embedded"
        TestMarykObject { embeddedObject { embedded { embedded ref { value } } } }.completeName shouldBe "embeddedObject.embedded.embedded.value"
        TestMarykObject { embeddedObject { embedded { embedded { embedded ref { value } } } } }.completeName shouldBe "embeddedObject.embedded.embedded.embedded.value"

        TestMarykObject { embeddedObject { marykModel { list at 5 } } }.completeName shouldBe "embeddedObject.marykModel.list.@5"
        TestMarykObject { embeddedObject { marykModel { set at Date(2017, 12, 5) } } }.completeName shouldBe "embeddedObject.marykModel.set.\$2017-12-05"

        TestMarykObject { embeddedObject { marykModel { map key Time(12, 23) } } }.completeName shouldBe """embeddedObject.marykModel.map.$12:23"""
        TestMarykObject { embeddedObject { marykModel { map at Time(12, 23) } } }.completeName shouldBe "embeddedObject.marykModel.map.@12:23"
    }
}
