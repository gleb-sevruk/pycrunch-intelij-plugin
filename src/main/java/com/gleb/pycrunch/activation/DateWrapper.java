package com.gleb.pycrunch.activation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class DateWrapper {
    public static Instant parse_from_iso(String iso_date) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(iso_date);
            LocalDateTime localDate = odt.toLocalDateTime();
            Instant instant = localDate.toInstant(ZoneOffset.UTC);
            return instant;
        } catch (Exception e)
        {
            System.out.println("Failed to parse date " + iso_date);
        }
        return null;

    }

    public static boolean licence_still_valid(Instant expiration_date) {
        return Instant.now().isBefore(expiration_date);
    }
}
