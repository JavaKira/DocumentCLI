package org.example.documentCLI.cli;

/**
 * ANSI-коды для цветового оформления вывода в терминале.
 *
 * <p>Каждый элемент хранит управляющую последовательность, которую можно
 * подставлять перед текстом. Для сброса форматирования используйте
 * {@link #RESET}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public enum Ansi {
    /** Жирный текст. */
    BOLD("\u001B[1m"),

    /** Приглушённый (dim) текст. */
    DIM("\u001B[2m"),

    /** Красный цвет — для ошибок. */
    RED("\u001B[31m"),

    /** Зелёный цвет — для успешных операций. */
    GREEN("\u001B[32m"),

    /** Жёлтый цвет — для предупреждений. */
    YELLOW("\u001B[33m"),

    /** Голубой (cyan) цвет — для акцентов. */
    CYAN("\u001B[36m"),

    /** Сброс форматирования. */
    RESET("\u001B[0m");

    /** Управляющая ANSI-последовательность. */
    private final String code;

    /**
     * @param code ANSI-последовательность
     */
    Ansi(String code) {
        this.code = code;
    }

    /**
     * @return ANSI-последовательность
     */
    public String code() {
        return code;
    }

    /**
     * Оборачивает текст в данную ANSI-последовательность с добавлением
     * {@link #RESET} в конце.
     *
     * @param text исходный текст
     * @return текст, окрашенный в соответствующий стиль
     */
    public String apply(String text) {
        return code + text + RESET.code;
    }
}
