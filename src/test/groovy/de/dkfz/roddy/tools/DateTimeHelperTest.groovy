package de.dkfz.roddy.tools

import groovy.transform.CompileStatic
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class DateTimeHelperTest extends Specification {

    @CompileStatic
    static LocalDateTime localDateTime(List<Integer> numbers) {
        LocalDateTime.of(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4])
    }

    def "Construct object"(parserPattern, locale, zoneId, expectedZone) {
        expect:
        (new DateTimeHelper(parserPattern, locale, zoneId)).datePattern.zone == expectedZone

        where: // I do not know how to test this properly. We can test for the zoneId, but not for the pattern
        parserPattern        | locale         | zoneId                 | expectedZone
        null                 | Locale.ENGLISH | null                   | null
        null                 | Locale.ENGLISH | ZoneId.of("Asia/Aden") | null
        "MMM ppd HH:mm yyyy" | Locale.ENGLISH | null                   | ZoneId.systemDefault()
        "MMM ppd HH:mm yyyy" | Locale.ENGLISH | ZoneId.systemDefault() | ZoneId.systemDefault()
        "MMM ppd HH:mm yyyy" | Locale.ENGLISH | ZoneId.of("Asia/Aden") | ZoneId.of("Asia/Aden")
    }

    def "ParseTime"(parserPattern, locale, timeString, expectedResult) {
        expect:
        new DateTimeHelper(parserPattern, locale).parseToZonedDateTime(timeString) == expectedResult

        where:
        parserPattern        | locale         | timeString                                 | expectedResult
        null                 | Locale.ENGLISH | "2011-12-03T10:15:30+01:00[Europe/Berlin]" | ZonedDateTime.of(2011, 12, 3, 10, 15, 30, 0, ZoneId.of("Europe/Berlin"))
        "MMM ppd HH:mm yyyy" | Locale.ENGLISH | "Jan  7 10:00 2000"                        | ZonedDateTime.of(2000, 1, 7, 10, 0, 0, 0, ZoneId.systemDefault())
        "MMM ppd HH:mm yyyy" | Locale.ENGLISH | "Dec 28 19:56 2000"                        | ZonedDateTime.of(2000, 12, 28, 19, 56, 0, 0, ZoneId.systemDefault())
        "MMM ppd HH:mm yyyy" | Locale.GERMAN  | "Dez 28 19:57 2000"                        | ZonedDateTime.of(2000, 12, 28, 19, 57, 0, 0, ZoneId.systemDefault())
    }

    def "ParseTimeWithException"() {
        when:
        new DateTimeHelper(null).parseToZonedDateTime("abcinvalid")

        then:
        thrown(DateTimeParseException)
    }

    def 'Test differenceBetween'(List<Integer> a, List<Integer> b, expected) {
        given:
        def dta = localDateTime(a)
        def dtb = localDateTime(b)
        def timeSpan = DateTimeHelper.differenceBetween(dta, dtb)

        expect:
        timeSpan == expected

        where:
        a                     | b                     | expected
        [2000, 2, 10, 12, 0]  | [2000, 2, 11, 12, 0]  | Duration.ofDays(1)
        [2000, 1, 5, 5, 0]    | [2000, 4, 10, 12, 10] | Duration.ofDays(26 + 29 + 31 + 10).plusHours(7).plusMinutes(10)
        [2000, 4, 10, 12, 10] | [2000, 1, 5, 5, 0]    | Duration.ofDays(26 + 29 + 31 + 10).plusHours(7).plusMinutes(10)
        [2001, 1, 1, 0, 0]    | [2000, 1, 1, 0, 0]    | Duration.ofDays(366) // Leap year
        [2100, 1, 1, 0, 0]    | [2000, 1, 1, 0, 0]    | Duration.ofDays(25 * 366 + 75 * 365)
        [2000, 1, 1, 0, 0]    | [2100, 1, 1, 0, 0]    | Duration.ofDays(25 * 366 + 75 * 365)
    }

    def 'Test durationExceeds'(duration, maxDuration, result) {
        expect:
        DateTimeHelper.durationExceeds(duration, maxDuration) == result

        where:
        duration           | maxDuration                     | result
        Duration.ofDays(2) | Duration.ofDays(2).plusHours(1) | false
        Duration.ofDays(2) | Duration.ofDays(1).plusHours(1) | true

    }
}
