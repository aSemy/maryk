package maryk.core.objects

import maryk.Option
import maryk.SubMarykObject
import maryk.TestMarykObject
import maryk.core.properties.ByteCollectorWithLengthCacher
import maryk.core.properties.types.Bytes
import maryk.core.properties.types.DateTime
import maryk.core.properties.types.Time
import maryk.core.properties.types.numeric.toUInt32
import maryk.test.shouldBe
import kotlin.test.Test

internal class RootDataModelTest {
    @Test
    fun testKey() {
            TestMarykObject.key.getKey(
                    TestMarykObject(
                            string = "name",
                            int = 5123123,
                            uint = 555.toUInt32(),
                            double = 6.33,
                            bool = true,
                            enum = Option.V2,
                            dateTime = DateTime.nowUTC()
                    )
            ) shouldBe Bytes(
                    byteArrayOf(0, 0, 2, 43, 1, 1, 1, 0, 2)
            )
    }

    private val subModelRef = SubMarykObject.Properties.value.getRef(TestMarykObject.Properties.subModel.getRef())
    private val mapRef = TestMarykObject.Properties.map.getRef()
    private val mapKeyRef = TestMarykObject.Properties.map.getKeyRef(Time(12, 33, 44))

    @Test
    fun testPropertyReferenceByName() {
        TestMarykObject.getPropertyReferenceByName(mapRef.completeName!!) shouldBe mapRef
        TestMarykObject.getPropertyReferenceByName(subModelRef.completeName!!) shouldBe subModelRef
    }

    @Test
    fun testPropertyReferenceByWriter() {
        val bc = ByteCollectorWithLengthCacher()

        arrayOf(subModelRef, mapRef, mapKeyRef).forEach {
            bc.reserve(
                    it.calculateTransportByteLength(bc::addToCache)
            )
            it.writeTransportBytes(bc::nextLengthFromCache, bc::write)

            TestMarykObject.getPropertyReferenceByBytes(bc.size, bc::read) shouldBe it

            bc.reset()
        }
    }
}