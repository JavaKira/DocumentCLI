package org.example.documentCLI.infrastructure;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DateParser {

    private static final List<DateTimeFormatter> FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    private DateParser() {}

    public static LocalDate parse(String s) {
        for (DateTimeFormatter f : FORMATS) {
            try {
                return LocalDate.parse(s, f);
            } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException(
                "Не удалось распознать дату: \"" + s + "\". Поддерживаются: dd.MM.yyyy, yyyy-MM-dd, dd/MM/yyyy");
    }
}