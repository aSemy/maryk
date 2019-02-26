package maryk.core.processors.datastore

import maryk.core.models.key
import maryk.core.query.filters.And
import maryk.core.query.filters.Equals
import maryk.core.query.filters.GreaterThan
import maryk.core.query.filters.GreaterThanEquals
import maryk.core.query.filters.LessThan
import maryk.core.query.filters.LessThanEquals
import maryk.core.query.filters.Range
import maryk.core.query.filters.ValueIn
import maryk.core.query.pairs.with
import maryk.lib.extensions.toHex
import maryk.lib.time.DateTime
import maryk.test.models.Log
import maryk.test.models.Severity.DEBUG
import maryk.test.models.Severity.ERROR
import maryk.test.models.Severity.INFO
import maryk.test.shouldBe
import kotlin.test.Test

class ScanRangeTest {
    private val scanRange = ScanRange(
        start = byteArrayOf(1, 2, 3, 4, 5),
        startInclusive = true,
        end = byteArrayOf(9, 8, 7, 6, 5),
        endInclusive = true,
        uniques = listOf(),
        partialMatches = listOf(
            KeyPartialToMatch(1, byteArrayOf(2, 4)),
            KeyPartialToMatch(3, byteArrayOf(5, 6))
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

    private val match = Log.key(Log("message", ERROR, DateTime(2018, 12, 8, 12, 33, 23)))
    // Dont be confused that the time is reversed. Later is referring to it is later in table, not in time.
    private val earlier = Log.key(Log("message", DEBUG, DateTime(2019, 12, 8, 12, 33, 23)))
    private val later = Log.key(Log("message", INFO, DateTime(2017, 12, 8, 12, 33, 23)))

    @Test
    fun convertSimpleEqualFilterToScanRange() {
        val filter = Equals(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f445ec7fff010000"
        scanRange.end?.toHex() shouldBe "7fffffa3f445ec7fff01ffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertGreaterThanFilterToScanRange() {
        val filter = GreaterThan(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f445ec7fff020000"
        scanRange.end?.toHex() shouldBe "ffffffffffffffffffffffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe true // Because should skip
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe false
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertGreaterThanEqualsFilterToScanRange() {
        val filter = GreaterThanEquals(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f445ec7fff010000"
        scanRange.end?.toHex() shouldBe "ffffffffffffffffffffffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe false
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertLessThanFilterToScanRange() {
        val filter = LessThan(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "000000000000000000000000"
        scanRange.end?.toHex() shouldBe "7fffffa3f445ec7fff00ffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe true // because should not be included
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe false
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertLessThanEqualsFilterToScanRange() {
        val filter = LessThanEquals(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "000000000000000000000000"
        scanRange.end?.toHex() shouldBe "7fffffa3f445ec7fff01ffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe false
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertRangeFilterToScanRange() {
        val filter = Range(
            Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 1, 1)..DateTime(2018, 12, 8, 12, 33, 55, 2)
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f445cc7ffd010000"
        scanRange.end?.toHex() shouldBe "7fffffa3f446027ffe01ffff"

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertValueInFilterToScanRange() {
        val filter = ValueIn(
            Log.ref { timestamp } with setOf(
                DateTime(2018, 12, 8, 12, 1, 1, 1),
                DateTime(2018, 12, 8, 12, 2, 2, 2),
                DateTime(2018, 12, 8, 12, 3, 3, 3)
            )
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f44d087ffc010000"
        scanRange.end?.toHex() shouldBe "7fffffa3f44d827ffe01ffff"

        val match = Log.key(Log("message", ERROR, DateTime(2018, 12, 8, 12, 2, 2, 2)))

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe false

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe false
    }

    @Test
    fun convertAndFilterToScanRange() {
        val filter = And(
            Equals(
                Log.ref { timestamp } with DateTime(2018, 12, 8, 12, 33, 23)
            ),
            LessThan(
                Log.ref { severity } with ERROR
            )
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "7fffffa3f445ec7fff010000"
        scanRange.startInclusive shouldBe true
        scanRange.end?.toHex() shouldBe "7fffffa3f445ec7fff010003"
        scanRange.endInclusive shouldBe false

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe true
        scanRange.keyMatches(match.bytes) shouldBe true

        scanRange.keyBeforeStart(earlier.bytes) shouldBe true
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe true
        scanRange.keyMatches(later.bytes) shouldBe true
    }

    @Test
    fun convertNoKeyPartsToScanRange() {
        val filter = LessThan(
            Log.ref { severity } with ERROR
        )

        val scanRange = Log.createScanRange(filter, null)

        scanRange.start.toHex() shouldBe "000000000000000000000000"
        scanRange.startInclusive shouldBe true
        scanRange.end?.toHex() shouldBe "ffffffffffffffffffffffff"
        scanRange.endInclusive shouldBe true

        scanRange.keyBeforeStart(match.bytes) shouldBe false
        scanRange.keyOutOfRange(match.bytes) shouldBe false
        scanRange.keyMatches(match.bytes) shouldBe false

        scanRange.keyBeforeStart(earlier.bytes) shouldBe false
        scanRange.keyOutOfRange(earlier.bytes) shouldBe false
        scanRange.keyMatches(earlier.bytes) shouldBe true

        scanRange.keyBeforeStart(later.bytes) shouldBe false
        scanRange.keyOutOfRange(later.bytes) shouldBe false
        scanRange.keyMatches(later.bytes) shouldBe true
    }
}
