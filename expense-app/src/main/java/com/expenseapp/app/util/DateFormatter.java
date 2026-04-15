package com.expenseapp.app.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateFormatter {

    public String formatDateTime(Instant instant) {

        if (instant == null) return null;

        ZoneId zone = ZoneId.systemDefault(); // later → user timezone

        ZonedDateTime zdt = instant.atZone(zone);
        ZonedDateTime now = ZonedDateTime.now(zone);

        LocalDate date = zdt.toLocalDate();
        LocalDate today = now.toLocalDate();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        if (date.equals(today)) {
            return "Today, " + zdt.format(timeFormatter);
        }

        if (date.equals(today.minusDays(1))) {
            return "Yesterday, " + zdt.format(timeFormatter);
        }

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("MMM d, h:mm a");

        return zdt.format(dateFormatter);
    }


    public String formatDate(Instant instant) {

        if (instant == null) return null;

        ZoneId zone = ZoneId.systemDefault(); // later → user timezone

        ZonedDateTime zdt = instant.atZone(zone);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM d, yyyy");

        return zdt.format(formatter);
    }
}
