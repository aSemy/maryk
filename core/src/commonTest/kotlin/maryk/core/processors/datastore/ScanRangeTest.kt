package maryk.core.processors.datastore

import maryk.core.models.key
import maryk.core.query.filters.Equals
import maryk.core.query.pairs.with
import maryk.lib.extensions.compare.compareTo
import maryk.lib.extensions.toHex
import maryk.lib.time.DateTime
import maryk.test.models.Log
import maryk.test.models.Severity.ERROR
import maryk.test.shouldBe
import kotlin.test.Test

class ScanRangeTest {
    private val scanRange = ScanRange(
        start = byteArrayOf(1, 2, 3, 4, 5),
        end = byteArrayOf(9, 8, 7, 6, 5),
        uniques = listOf(
            UniqueToMatch(byteArrayOf(1, 2), "unique"),
            UniqueToMatch(byteArrayOf(1, 2), "unique")
        ),
        partialMatches = listOf(
            PartialToMatch(1, byteArrayOf(2, 4)),
            PartialToMatch(3, byteArrayOf(5, 6))
        )
    )

    @Test
    fun keyOutOfRange() {
        scanRange.keyOutOfRange(byteArrayOf(3, 4, 5, 6, 7)) shouldBe false
        scanRange.keyOutOfRange(byteArrayOf(9, 9, 8, 7, 6)) shouldBe true
    }

    @Test
    fun keyMatches() {
        scanRange.keyMatches(byteArrayOf(3, 2, 4, 5, 6)) shouldBe true
        scanRange.keyMatches(byteArrayOf(3, 4, 4, 5, 6)) shouldBe false
        scanRange.keyMatches(byteArrayOf(3, 2, 4, 6, 6)) shouldBe false
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun convertSimpleFilterToRange() {
        val match = Log.key(Log("message", ERROR, DateTime(2018, 12, 8, 12, 33, 23)))

        // Dont be confused that the time is reversed. Later is referring to it is later in table, not in time.
        val earlier = Log.key(Log("message", ERROR, DateTime(2019, 12, 8, 12, 33, 23)))
        val later = Log.key(Log("message", ERROR, DateTime(2017, 12, 8, 12, 33, 23)))

        val filter = Equals(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, ByteArray(Log.keySize))

        (scanRange.start < match.bytes) shouldBe true
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        (scanRange.start < earlier.bytes) shouldBe false
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        (scanRange.start < later.bytes) shouldBe true
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }
}