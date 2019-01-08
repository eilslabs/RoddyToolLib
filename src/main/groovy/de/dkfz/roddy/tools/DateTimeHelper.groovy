package de.dkfz.roddy.tools

import groovy.transform.CompileStatic

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class DateTimeHelper {

    private final DateTimeFormatter DATE_PATTERN

    DateTimeHelper(String dateParserPatternPattern = null, ZoneId timeZoneId = ZoneId.systemDefault()) {
        if (!dateParserPatternPattern)
            DATE_PATTERN = DateTimeFormatter.ISO_DATE_TIME
        else
            this.DATE_PATTERN = DateTimeFormatter
                    .ofPattern(dateParserPatternPattern)
                    .withLocale(Locale.ENGLISH)
                    .withZone(timeZoneId ?: ZoneId.systemDefault())
    }

    DateTimeFormatter getDatePattern() {
        return DATE_PATTERN
    }

    ZonedDateTime parseTime(String str) {
        ZonedDateTime date = ZonedDateTime.parse(str, DATE_PATTERN)
        return date
    }

    static Duration toDuration(LocalDateTime ldt) {
        // There is no plusYears, we just assume 365 days for a year
        Duration.ofSeconds(ldt.second).plusMinutes(ldt.minute).plusHours(ldt.hour).plusDays(ldt.dayOfYear + ldt.year * 365)
    }
    
    static Duration toDuration(int years, int days, int hours, int minutes, int seconds) {
        Duration.ofSeconds(seconds).plusMinutes(minutes).plusHours(hours).plusDays(days + years * 365)
    }

    static Duration timeSpanOf(LocalDateTime lower, LocalDateTime higher) {
        toDuration(higher) - toDuration(lower)
    }

    static boolean isOlderThan(Duration age, int days, int hours, int minutes, int seconds) {
        Duration maxAge = toDuration(0, days, hours, minutes, seconds)
        return isOlderThan(age, maxAge)
    }

    static boolean isOlderThan(Duration timeSpan, Duration maxAge) {
        (maxAge - timeSpan).isNegative()
    }
}
