package maryk.core.query.responses.updates

import maryk.core.query.ValuesWithMetaData
import maryk.core.query.changes.Change
import maryk.core.query.pairs.with
import maryk.core.query.responses.updates.RemovalReason.HardDelete
import maryk.test.models.SimpleMarykModel
import maryk.test.models.SimpleMarykModel.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

internal class processUpdateResponseTest {
    val key1 = SimpleMarykModel.key("dR9gVdRcSPw2molM1AiOng")
    val key2 = SimpleMarykModel.key("Vc4WgX/mQHYCSEoLtfLSUQ")

    val initialItems = listOf<ValuesWithMetaData<SimpleMarykModel, Properties>>(
        ValuesWithMetaData(
            key = key1,
            firstVersion = 1234uL,
            lastVersion = 2345uL,
            values = SimpleMarykModel(value = "v1"),
            isDeleted = false
        ),
        ValuesWithMetaData(
            key = key2,
            firstVersion = 12345uL,
            lastVersion = 23456uL,
            values = SimpleMarykModel(value = "v2"),
            isDeleted = false
        )
    )

    @Test
    fun testAddition() {
        val addition = AdditionUpdate(
            key = SimpleMarykModel.key("0ruQCs38S2QaByYof+IJgA"),
            firstVersion = 3456uL,
            version = 4567uL,
            insertionIndex = 1,
            values = SimpleMarykModel(value = "v3"),
            isDeleted = false
        )

        val newItems = processUpdateResponse(addition, initialItems)

        assertEquals(3, newItems.size)

        newItems[1].apply {
            assertEquals(addition.key, key)
            assertEquals(addition.values, values)
            assertEquals(addition.firstVersion, firstVersion)
            assertEquals(addition.version, lastVersion)
            assertEquals(false, isDeleted)
        }
    }

    @Test
    fun testChangeInPlace() {
        val newValue = "new value"

        val change = ChangeUpdate(
            key = key2,
            version = 4567uL,
            index = 1,
            changes = listOf(
                Change(
                    SimpleMarykModel { value::ref } with newValue
                )
            )
        )

        val newItems = processUpdateResponse(change, initialItems)

        assertEquals(2, newItems.size)

        newItems[1].apply {
            assertEquals(key2, key)
            assertEquals(newValue, values { value })
        }
    }

    @Test
    fun testChangeWithMove() {
        val newValue = "new value"

        val change = ChangeUpdate(
            key = key2,
            version = 4567uL,
            index = 0,
            changes = listOf(
                Change(
                    SimpleMarykModel { value::ref } with newValue
                )
            )
        )

        val newItems = processUpdateResponse(change, initialItems)

        assertEquals(2, newItems.size)

        newItems[0].apply {
            assertEquals(key2, key)
            assertEquals(newValue, values { value })
        }
    }

    @Test
    fun testRemoval() {
        val removal = RemovalUpdate(
            key = key1,
            version = 4567uL,
            reason = HardDelete
        )

        val newItems = processUpdateResponse(removal, initialItems)

        assertEquals(1, newItems.size)

        assertEquals(key2, newItems[0].key)
    }
}
