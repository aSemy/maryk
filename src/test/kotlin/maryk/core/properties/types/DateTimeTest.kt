package maryk.core.properties.types

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import org.junit.Test

internal class DateTimeTest {
    private fun cleanToSeconds(it: DateTime) = DateTime(it.date, Time(it.hour, it.minute, it.day))

    private val dateTime = DateTime(
            year = 2017,
            month = 8,
            day = 16,
            hour = 11,
            minute = 28,
            second = 22,
            milli = 2344
    )

    val dateTimesWithSecondsToTest = arrayOf(
            cleanToSeconds(DateTime.nowUTC()),
            cleanToSeconds(DateTime.MAX_IN_SECONDS),
            cleanToSeconds(dateTime),
            DateTime.MIN
    )

    val dateTimesWithMillisToTest = arrayOf(
            DateTime.nowUTC(),
            DateTime.MAX_IN_MILLIS,
            DateTime.MIN
    )
    
    @Test
    fun compare() {
        DateTime.MIN.compareTo(DateTime.MAX_IN_SECONDS) shouldBe -1999999998
        DateTime.MAX_IN_MILLIS.compareTo(DateTime.MIN) shouldBe 1999999998
        dateTime.compareTo(dateTime) shouldBe 0
    }

    @Test
    fun epochSecondConversion() {
        dateTimesWithSecondsToTest.forEach {
            DateTime.ofEpochSecond(
                    it.toEpochSecond()
            ) shouldBe it
        }
    }

    @Test
    fun epochMilliConversion() {
        arrayOf(
                DateTime.nowUTC()
        ).forEach {
            DateTime.ofEpochMilli(
                    it.toEpochMilli()
            ) shouldBe it
        }
    }

    @Test
    fun testConversion() {
        dateTimesWithSecondsToTest.forEach {
            DateTime.ofBytes(
                    it.toBytes(TimePrecision.SECONDS)
            ) shouldBe it
        }
    }

    @Test
    fun testOffsetConversion() {
        dateTimesWithSecondsToTest.forEach {
            val bytes = ByteArray(22)
            DateTime.ofBytes(
                    it.toBytes(TimePrecision.SECONDS, bytes, 10),
                    10,
                    7
            ) shouldBe it
        }
    }

    @Test
    fun testMillisConversion() {
        dateTimesWithMillisToTest.forEach {
            DateTime.ofBytes(
                    it.toBytes(TimePrecision.MILLIS)
            ) shouldBe it
        }
    }

    @Test
    fun testMillisOffsetConversion() {
        dateTimesWithMillisToTest.forEach {
            val bytes = ByteArray(22)
            DateTime.ofBytes(
                    it.toBytes(TimePrecision.MILLIS, bytes, 10),
                    10,
                    9
            ) shouldBe it
        }
    }

    @Test
    fun testWrongByteSizeError() {
        val bytes = ByteArray(22)
        shouldThrow<IllegalArgumentException> {
            DateTime.ofBytes(
                    bytes,
                    10,
                    22
            )
        }
    }


    @Test
    fun testStringConversion() {
        dateTimesWithMillisToTest.forEach {
            DateTime.parse(
                    it.toString(true),
                    iso8601 = true
            ) shouldBe it
        }
    }

    @Test
    fun testStringOptimizedConversion() {
        dateTimesWithMillisToTest.forEach {
            DateTime.parse(
                    it.toString(false),
                    iso8601 = false
            ) shouldBe it
        }
    }
}