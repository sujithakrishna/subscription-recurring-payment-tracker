package utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtility {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtility() {
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static String getCurrentDateString() {
        return LocalDate.now().format(FORMATTER);
    }

    public static String formatDate(LocalDate date) {

        if (date == null) {
            return "-";
        }

        return date.format(FORMATTER);
    }

    public static LocalDate parseDate(String date) {

        try {

            return LocalDate.parse(
                    date,
                    FORMATTER
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid date. Use YYYY-MM-DD."
            );
        }
    }

    public static LocalDate calculateNextBillingDate(
            LocalDate startDate,
            String billingCycle) {

        switch (billingCycle) {

            case "WEEKLY":
                return startDate.plusWeeks(1);

            case "MONTHLY":
                return startDate.plusMonths(1);

            case "QUARTERLY":
                return startDate.plusMonths(3);

            case "HALF_YEARLY":
                return startDate.plusMonths(6);

            case "YEARLY":
                return startDate.plusYears(1);

            default:
                throw new IllegalArgumentException(
                        "Invalid billing cycle."
                );
        }
    }

    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
}