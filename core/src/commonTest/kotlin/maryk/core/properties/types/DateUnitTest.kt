package maryk.core.properties.types

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import maryk.core.aggregations.bucket.DateUnit.Centuries
import maryk.core.aggregations.bucket.DateUnit.Days
import maryk.core.aggregations.bucket.DateUnit.Decades
import maryk.core.aggregations.bucket.DateUnit.Hours
import maryk.core.aggregations.bucket.DateUnit.Millennia
import maryk.core.aggregations.bucket.DateUnit.Millis
import maryk.core.aggregations.bucket.DateUnit.Minutes
import maryk.core.aggregations.bucket.DateUnit.Months
import maryk.core.aggregations.bucket.DateUnit.Quarters
import maryk.core.aggregations.bucket.DateUnit.Seconds
import maryk.core.aggregations.bucket.DateUnit.Years
import maryk.core.aggregations.bucket.roundToDateUnit
import maryk.lib.time.Time
import kotlin.test.Test
import kotlin.test.expect

class DateUnitTest {
    @Test
    fun roundDateTime() {
        val dateTime = LocalDateTime(2019, 6, 8, 13, 45, 23, 999000000)

        expect(dateTime) {
            dateTime.roundToDateUnit(Millis)
        }

        expect(LocalDateTime(2019, 6, 8, 13, 45, 23)) {
            dateTime.roundToDateUnit(Seconds)
        }

        expect(LocalDateTime(2019, 6, 8, 13, 45)) {
            dateTime.roundToDateUnit(Minutes)
        }

        expect(LocalDateTime(2019, 6, 8, 13, 0)) {
            dateTime.roundToDateUnit(Hours)
        }

        expect(LocalDateTime(2019, 6, 8, 0, 0)) {
            dateTime.roundToDateUnit(Days)
        }

        expect(LocalDateTime(2019, 6, 1, 0, 0)) {
            dateTime.roundToDateUnit(Months)
        }

        expect(LocalDateTime(2019, 4, 1, 0, 0)) {
            dateTime.roundToDateUnit(Quarters)
        }

        expect(LocalDateTime(2019, 1, 1, 0, 0)) {
            dateTime.roundToDateUnit(Years)
        }

        expect(LocalDateTime(2010, 1, 1, 0, 0)) {
            dateTime.roundToDateUnit(Decades)
        }

        expect(LocalDateTime(1900, 1, 1, 0, 0)) {
            LocalDateTime(1912, 1, 1, 0, 0).roundToDateUnit(Centuries)
        }

        expect(LocalDateTime(1000, 1, 1, 0, 0)) {
            LocalDateTime(1912, 1, 1, 0, 0).roundToDateUnit(Millennia)
        }
    }

    @Test
    fun roundDate() {
        val date = LocalDate(2019, 6, 8)

        expect(date) {
            date.roundToDateUnit(Millis)
        }

        expect(LocalDate(2019, 6, 8)) {
            date.roundToDateUnit(Seconds)
        }

        expect(LocalDate(2019, 6, 8)) {
            date.roundToDateUnit(Minutes)
        }

        expect(LocalDate(2019, 6, 8)) {
            date.roundToDateUnit(Hours)
        }

        expect(LocalDate(2019, 6, 8)) {
            date.roundToDateUnit(Days)
        }

        expect(LocalDate(2019, 6, 1)) {
            date.roundToDateUnit(Months)
        }

        expect(LocalDate(2019, 4, 1)) {
            date.roundToDateUnit(Quarters)
        }

        expect(LocalDate(2019, 1, 1)) {
            date.roundToDateUnit(Years)
        }

        expect(LocalDate(2010, 1, 1)) {
            date.roundToDateUnit(Decades)
        }

        expect(LocalDate(1900, 1, 1)) {
            LocalDate(1912, 1, 1).roundToDateUnit(Centuries)
        }

        expect(LocalDate(1000, 1, 1)) {
            LocalDate(1912, 1, 1).roundToDateUnit(Millennia)
        }
    }

    @Test
    fun roundTime() {
        val time = Time(13, 45, 23, 999)

        expect(time) {
            time.roundToDateUnit(Millis)
        }

        expect(Time(13, 45, 23)) {
            time.roundToDateUnit(Seconds)
        }

        expect(Time(13, 45)) {
            time.roundToDateUnit(Minutes)
        }

        expect(Time(13, 0)) {
            time.roundToDateUnit(Hours)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Days)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Months)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Quarters)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Years)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Decades)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Centuries)
        }

        expect(Time.MIDNIGHT) {
            time.roundToDateUnit(Millennia)
        }
    }
}
