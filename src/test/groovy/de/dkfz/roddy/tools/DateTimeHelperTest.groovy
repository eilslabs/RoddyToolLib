package de.dkfz.roddy.tools

import groovy.transform.CompileStatic
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

import static de.dkfz.roddy.tools.DateTimeHelper.toDuration

class DateTimeHelperTest extends Specification {

    @CompileStatic
    static LocalDateTime localDateTime(List<Integer> numbers) {
        LocalDateTime.of(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4])
    }

    def "Construct object"(parserPattern, zoneId, expectedZone) {
        expect:
        (new DateTimeHelper(parserPattern, zoneId)).datePattern.zone == expectedZone

        where: // I do not know how to test this properly. We can test for the zoneId, but not for the pattern
        parserPattern        | zoneId                 | expectedZone
        null                 | null                   | null
        null                 | ZoneId.of("Asia/Aden") | null
        "MMM ppd HH:mm yyyy" | null                   | ZoneId.systemDefault()
        "MMM ppd HH:mm yyyy" | ZoneId.systemDefault() | ZoneId.systemDefault()
        "MMM ppd HH:mm yyyy" | ZoneId.of("Asia/Aden") | ZoneId.of("Asia/Aden")
    }

    def "ParseTime"(parserPattern, timeString, expectedResult) {
        expect:
        new DateTimeHelper(parserPattern).parseTime(timeString) == expectedResult

        where:
        parserPattern        | timeString                                 | expectedResult
        null                 | "2011-12-03T10:15:30+01:00[Europe/Berlin]" | ZonedDateTime.of(2011, 12, 3, 10, 15, 30, 0, ZoneId.systemDefault())
        "MMM ppd HH:mm yyyy" | "Jan  7 10:00 2000"                        | ZonedDateTime.of(2000, 1, 7, 10, 0, 0, 0, ZoneId.systemDefault())
    }

    def "ParseTimeWithException"() {
        when:
        new DateTimeHelper(null).parseTime("abcinvalid")

        then:
        thrown(DateTimeParseException)
    }

    def "To Duration from DateTime"(timeArray, expectedDays) {
        given:
        def ldt = localDateTime(timeArray)
        def duration = toDuration(ldt)

        expect:
        duration.toDays() == expectedDays

        where:
        timeArray            | expectedDays
        [2000, 1, 1, 0, 0]   | 2000 * 365 + 1
        [2000, 2, 10, 12, 0] | 2000 * 365 + 31 + 10
    }

    def "To Duration from values"(years, days, hours, minutes, seconds, expectedDays, expectedMinutes) {
        given:
        def duration = toDuration(years, days, hours, minutes, seconds)

        expect:
        duration.toDays() == expectedDays
        duration.toMinutes() == expectedMinutes

        where:
        years | days | hours | minutes | seconds | expectedDays   | expectedMinutes
        2000  | 1    | 1     | 0       | 0       | 2000 * 365 + 1 | expectedDays * 24 * 60 + 60
        2000  | 2    | 10    | 12      | 0       | 2000 * 365 + 2 | expectedDays * 24 * 60 + 600 + 12
    }

    def "TimeSpanCalc"(lower, higher, expected) {
        given:
        def ldt = localDateTime(lower)
        def ldh = localDateTime(higher)
        def timeSpan = DateTimeHelper.timeSpanOf(ldt, ldh)

        expect:
        timeSpan == expected

        where:
        lower                | higher                | expected
        [2000, 2, 10, 12, 0] | [2000, 2, 11, 12, 0]  | toDuration(0, 1, 0, 0, 0)
        [2000, 1, 5, 5, 0]   | [2000, 4, 10, 12, 10] | toDuration(0, 26 + 29 + 31 + 10, 7, 10, 0)
    }

    def "IsOlderThan"(age, maxAge, result) {
        expect:
        DateTimeHelper.isOlderThan(age, maxAge) == result

        where:
        age                       | maxAge                    | result
        toDuration(0, 2, 0, 0, 0) | toDuration(0, 2, 1, 0, 0) | false
        toDuration(0, 2, 0, 0, 0) | toDuration(0, 1, 1, 0, 0) | true

    }
}
