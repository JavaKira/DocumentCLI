package org.example.documentCLI.cli;

import org.jline.reader.LineReader;

import java.util.Objects;
import java.util.Scanner;

import static org.example.documentCLI.cli.Ansi.*;

/**
 * Статическая утилита для вывода текста в терминал и чтения пользовательского ввода.
 *
 * <p>Класс предназначен для использования в CLI-приложении и инициализируется один раз
 * в {@link org.example.documentCLI.cli.DocumentCLI} через {@link #init(LineReader)}. Если
 * {@link LineReader} не был передан (или передан {@code null}), утилита автоматически
 * переключается на стандартные потоки {@link System#out}, {@link System#err} и
 * {@link System#in}. Это позволяет использовать {@code Console} как в интерактивном
 * режиме с JLine, так и в тестах/скриптах без него.</p>
 *
 * <p>Все методы класса статические и потокобезопасны на уровне отдельных вызовов
 * (за исключением момента инициализации {@link #init(LineReader)} — его следует
 * вызывать до начала работы с утилитой).</p>
 *
 * @author Javakira
 * @since 1.0
 */
public final class Console {
    /**
     * Активный {@link LineReader} JLine. Может быть {@code null}, если утилита
     * не инициализирована — в этом случае используются стандартные потоки.
     */
    private static LineReader reader;

    /** Приватный конструктор: класс-утилита не предназначен для инстанцирования. */
    private Console() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Инициализирует утилиту переданным {@link LineReader}.
     *
     * <p>Метод следует вызывать один раз при старте приложения — до первого
     * обращения к {@link #print(String)}, {@link #error(String)} или
     * {@link #prompt(String)}. Повторный вызов перезапишет предыдущий reader.</p>
     *
     * @param lineReader активный {@link LineReader}; если {@code null} — утилита
     *                   переключится на стандартные потоки ввода-вывода
     */
    public static void init(LineReader lineReader) {
        reader = lineReader;
    }

    /**
     * Выводит информационное сообщение в терминал.
     *
     * <p>Если {@link LineReader} инициализирован — сообщение печатается через
     * {@link LineReader#printAbove(String)} (не ломая текущую строку ввода).
     * Иначе — через {@code IO.println}.</p>
     *
     * @param msg текст сообщения; {@code null} трактуется как пустая строка
     */
    public static void print(String msg) {
        String message = msg != null ? msg : "";
        if (reader != null) {
            reader.printAbove(message);
        } else {
            IO.println(message);
        }
    }

    /**
     * Выводит сообщение об ошибке в терминал, выделяя его красным цветом.
     *
     * <p>Если {@link LineReader} инициализирован — сообщение печатается через
     * {@link LineReader#printAbove(String)} с ANSI-обёрткой. Иначе — через
     * {@link System#err} (без ANSI-окраски, чтобы не засорять перенаправленный
     * поток ошибок управляющими последовательностями).</p>
     *
     * @param msg текст сообщения об ошибке; {@code null} трактуется как пустая строка
     */
    public static void error(String msg) {
        String message = msg != null ? msg : "";
        if (reader != null) {
            reader.printAbove(RED.apply(message));
        } else {
            System.err.println(message);
        }
    }

    /**
     * Запрашивает у пользователя строку ввода, отображая приглашение.
     *
     * <p>Если {@link LineReader} инициализирован — используется
     * {@link LineReader#readLine(String)}. Любое исключение чтения (например,
     * {@code EndOfFileException} при Ctrl+D) приводит к возврату пустой строки.
     * Иначе — читается одна строка из {@link System#in} через {@link Scanner}.</p>
     *
     * @param message текст приглашения (может быть пустым)
     * @return введённая строка; пустая строка, если ввод недоступен или прерван
     */
    public static String prompt(String message) {
        String promptText = message != null ? message : "";
        if (reader != null) {
            try {
                return Objects.requireNonNullElse(reader.readLine(promptText), "");
            } catch (Exception e) {
                return "";
            }
        }

        IO.print(promptText);
        try (Scanner sc = new Scanner(System.in)) {
            return sc.hasNextLine() ? sc.nextLine() : "";
        }
    }
}