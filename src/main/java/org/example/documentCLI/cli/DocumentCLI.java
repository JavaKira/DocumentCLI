package org.example.documentCLI.cli;

import org.example.documentCLI.application.cases.CaseRepository;
import org.example.documentCLI.application.cases.CaseService;
import org.example.documentCLI.application.cases.CaseServiceImpl;
import org.example.documentCLI.application.fund.FundRepository;
import org.example.documentCLI.application.fund.FundService;
import org.example.documentCLI.application.fund.FundServiceImpl;
import org.example.documentCLI.infrastructure.FileCaseRepository;
import org.example.documentCLI.application.document.DocumentRepository;
import org.example.documentCLI.application.document.DocumentService;
import org.example.documentCLI.application.document.DocumentServiceImpl;
import org.example.documentCLI.infrastructure.FileDocumentRepository;
import org.example.documentCLI.infrastructure.FileFundRepository;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.example.documentCLI.cli.Ansi.*;
import static org.example.documentCLI.cli.Ansi.CYAN;
import static org.example.documentCLI.cli.Ansi.DIM;
import static org.example.documentCLI.cli.Ansi.GREEN;
import static org.example.documentCLI.cli.Ansi.YELLOW;

/**
 * Точка входа интерактивного CLI-приложения DocumentCLI.
 *
 * <p>Класс собирает зависимости (репозитории → сервисы), конфигурирует
 * {@link CommandLine} Picocli с кастомной фабрикой внедрения сервисов в команды,
 * поднимает JLine-терминал с автодополнением по Tab и запускает цикл чтения
 * команд до выхода ({@code exit}, {@code quit}, Ctrl+C или Ctrl+D).</p>
 *
 * <p>Все данные хранятся в каталоге {@link #DEFAULT_ROOT_PATH} относительно
 * рабочей директории приложения.</p>
 *
 * @author Javakira
 * @since 1.0
 */
public class DocumentCLI {
    /**
     * Каталог хранения данных по умолчанию.
     */
    private static final Path DEFAULT_ROOT_PATH = new File("./DocumentCLIData/").toPath();

    /**
     * Приглашение командной строки.
     */
    private static final String PROMPT = "DocumentCLI> ";

    /**
     * Команда выхода №1.
     */
    private static final String CMD_EXIT = "exit";

    /**
     * Команда выхода №2.
     */
    private static final String CMD_QUIT = "quit";

    /**
     * Приватный конструктор: класс не предназначен для инстанцирования.
     */
    private DocumentCLI() {
        throw new UnsupportedOperationException("Entry-point class");
    }

    /**
     * Точка входа приложения.
     *
     * <p>Последовательность действий:</p>
     * <ol>
     *   <li>Собирает {@link CommandLine} со всеми командами и сервисами.</li>
     *   <li>Поднимает системный {@link Terminal} и {@link LineReader} JLine
     *       с автодополнением Picocli.</li>
     *   <li>Инициализирует {@link Console} полученным reader'ом.</li>
     *   <li>Печатает приветственный баннер.</li>
     *   <li>Крутит цикл чтения команд до явного выхода.</li>
     * </ol>
     *
     * @throws IOException если не удалось инициализировать терминал или репозитории
     */
    static void main() throws IOException {
        CommandLine cmd = buildCommandLine();

        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(new DefaultParser())
                .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                .build();

        Console.init(reader);
        printWelcome();

        runRepl(cmd, reader);

        terminal.close();
    }

    /**
     * Цикл чтения-выполнения-печати (REPL).
     *
     * <p>Читает строку из терминала, разбивает её на аргументы тем же
     * {@link DefaultParser}, что и JLine, и передаёт в Picocli. Пустые строки
     * игнорируются. Выход — по {@code exit}/{@code quit} (без учёта регистра),
     * Ctrl+C ({@link UserInterruptException}) или Ctrl+D ({@link EndOfFileException}).</p>
     *
     * @param cmd    сконфигурированный {@link CommandLine}
     * @param reader активный {@link LineReader}
     */
    private static void runRepl(CommandLine cmd, LineReader reader) {
        DefaultParser parser = new DefaultParser();
        while (true) {
            String line;
            try {
                line = reader.readLine(PROMPT);
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }

            if (line == null) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (CMD_EXIT.equalsIgnoreCase(trimmed) || CMD_QUIT.equalsIgnoreCase(trimmed)) {
                break;
            }

            String[] argv = parser.parse(line, 0).words().toArray(new String[0]);
            cmd.execute(argv);
        }
    }

    /**
     * Собирает граф зависимостей и конфигурирует {@link CommandLine}.
     *
     * <p>Создаёт репозитории файлового хранения, поверх них — сервисы,
     * и регистрирует кастомную {@link CommandLine.IFactory}, которая умеет
     * внедрять нужный набор сервисов в конструкторы команд Picocli.</p>
     *
     * @return готовый к использованию {@link CommandLine}
     * @throws IOException если не удалось инициализировать репозитории
     */
    private static CommandLine buildCommandLine() throws IOException {
        FundRepository fundRepository = new FileFundRepository(DocumentCLI.DEFAULT_ROOT_PATH);
        CaseRepository caseRepository = new FileCaseRepository(DocumentCLI.DEFAULT_ROOT_PATH);
        DocumentRepository documentRepository = new FileDocumentRepository(DocumentCLI.DEFAULT_ROOT_PATH);

        FundService fundService = new FundServiceImpl(fundRepository);
        CaseService caseService = new CaseServiceImpl(caseRepository, documentRepository, fundRepository);
        DocumentService documentService = new DocumentServiceImpl(documentRepository);

        return new CommandLine(
                new DocumentCLICommand(),
                new ServiceInjectingFactory(caseService, fundService, documentService)
        );
    }

    /**
     * Печатает приветственный баннер и подсказки по базовым командам.
     *
     * <p>Для окраски текста используется {@link Ansi#apply(String)}, который
     * сам добавляет сброс ({@link Ansi#RESET}) в конце.</p>
     */
    public static void printWelcome() {
        String banner =
                CYAN.apply("  ╔══════════════════════════════════╗") + "\n" +
                        CYAN.apply("  ║") + BOLD.apply("         DocumentCLI  v1.0        ") + CYAN.apply("║") + "\n" +
                        CYAN.apply("  ║") + "   Утилита для делопроизводства   " + CYAN.apply("║") + "\n" +
                        CYAN.apply("  ║") + "   ИП Выходцев В. А. (Javakira)   " + CYAN.apply("║") + "\n" +
                        CYAN.apply("  ╚══════════════════════════════════╝") + "\n" +
                        "\n" +
                        DIM.apply("  Введите ") + GREEN.apply("help") + DIM.apply(" для списка команд.") + "\n" +
                        DIM.apply("  Введите ") + YELLOW.apply("exit") + DIM.apply(" или ") + YELLOW.apply("quit") + DIM.apply(" для выхода.") + "\n" +
                        "\n";

        Console.print(banner);
    }

    /**
     * Фабрика Picocli, внедряющая сервисы в команды по сигнатуре конструктора.
     *
     * <p>Picocli при создании команды вызывает {@link #create(Class)}. Фабрика
     * последовательно пробует известные наборы зависимостей и выбирает первый
     * подходящий конструктор. Порядок важен: сначала более специфичные наборы
     * (3 аргумента), затем менее специфичные (1 аргумент), в конце — конструктор
     * по умолчанию.</p>
     *
     * <p>Если в проекте появится DI-контейнер (Spring, Guice, Dagger), этот
     * класс можно заменить на соответствующую интеграцию.</p>
     */
    private record ServiceInjectingFactory(
            CaseService caseService,
            FundService fundService,
            DocumentService documentService
    ) implements CommandLine.IFactory {

        /**
         * @param caseService     сервис дел
         * @param fundService     сервис фондов
         * @param documentService сервис документов
         */
        private ServiceInjectingFactory {
        }

        /**
         * Пытается создать экземпляр команды, подобрав подходящий конструктор.
         *
         * @param cls класс команды
         * @param <K> тип команды
         * @return созданный экземпляр
         * @throws Exception если ни один из известных конструкторов не подошёл
         */
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            K instance = tryCreate(cls, CaseService.class, FundService.class, DocumentService.class,
                    caseService, fundService, documentService);
            if (instance != null) return instance;

            instance = tryCreate(cls, CaseService.class, FundService.class,
                    caseService, fundService);
            if (instance != null) return instance;

            instance = tryCreate(cls, DocumentService.class, CaseService.class,
                    documentService, caseService);
            if (instance != null) return instance;

            instance = tryCreate(cls, FundService.class, fundService);
            if (instance != null) return instance;

            instance = tryCreate(cls, CaseService.class, caseService);
            if (instance != null) return instance;

            instance = tryCreate(cls, DocumentService.class, documentService);
            if (instance != null) return instance;

            return cls.getDeclaredConstructor().newInstance();
        }

        /**
         * Пробует создать экземпляр через конструктор с заданными типами.
         *
         * @param cls      класс команды
         * @param argTypes типы параметров конструктора
         * @param args     значения параметров
         * @param <K>      тип команды
         * @return экземпляр или {@code null}, если подходящего конструктора нет
         * @throws Exception если конструктор найден, но выбросил исключение
         */
        private static <K> K tryCreate(Class<K> cls, Class<?>[] argTypes, Object[] args) throws Exception {
            try {
                return cls.getDeclaredConstructor(argTypes).newInstance(args);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }

        /**
         * Удобная обёртка для одного аргумента.
         */
        private static <K, A> K tryCreate(Class<K> cls, Class<A> aType, A a) throws Exception {
            return tryCreate(cls, new Class<?>[]{aType}, new Object[]{a});
        }

        /**
         * Удобная обёртка для двух аргументов.
         */
        private static <K, A, B> K tryCreate(Class<K> cls,
                                             Class<A> aType, Class<B> bType,
                                             A a, B b) throws Exception {
            return tryCreate(cls, new Class<?>[]{aType, bType}, new Object[]{a, b});
        }

        /**
         * Удобная обёртка для трёх аргументов.
         */
        private static <K, A, B, C> K tryCreate(Class<K> cls,
                                                Class<A> aType, Class<B> bType, Class<C> cType,
                                                A a, B b, C c) throws Exception {
            return tryCreate(cls, new Class<?>[]{aType, bType, cType}, new Object[]{a, b, c});
        }
    }
}
