package com.samuel.sniffers.api;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private DateTimeUtils() {
        // Prevent instantiation of DateTimeUtils
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    /**
     * Standard ISO 8601 format (ZonedDateTime)
     */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Custom readable format: YYYY-MM-DD HH:mm:ss
     */
    private static final DateTimeFormatter READABLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get current date-time in UTC (ISO 8601 format)
     */
    public static String getCurrentDateTimeISO() {
        return ZonedDateTime.now(DEFAULT_ZONE).format(ISO_FORMATTER);
    }

    /**
     * Get current date-time in a human-readable format
     */
    public static String getCurrentDateTimeReadable() {
        return LocalDateTime.now(DEFAULT_ZONE).format(READABLE_FORMATTER);
    }

    /**
     * Convert an Instant to ISO 8601 formatted string
     */
    public static String formatInstantToISO(Instant instant) {
        return ISO_FORMATTER.withZone(DEFAULT_ZONE).format(instant);
    }

    /**
     * Convert an Instant to human-readable format
     */
    public static String formatInstantReadable(Instant instant) {
        return READABLE_FORMATTER.withZone(DEFAULT_ZONE).format(instant);
    }

    /**
     * Convert timestamp (milliseconds) to ISO 8601 format
     */
    public static String timestampToISO(long timestamp) {
        return formatInstantToISO(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Convert timestamp (milliseconds) to human-readable format
     */
    public static String timestampToReadable(long timestamp) {
        return formatInstantReadable(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Convert LocalDateTime to timestamp (milliseconds since epoch)
     */
    public static long toTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * Convert timestamp (milliseconds) to LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
