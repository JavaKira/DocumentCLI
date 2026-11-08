package org.example.documentCLI.cli.cases;

import org.example.documentCLI.application.cases.CaseService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Case;
import org.example.documentCLI.infrastructure.DateParser;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.concurrent.Callable;

/**
 * Подкоманда {@code case mk}: создаёт новое дело в указанном фонде.
 *
 * <p>Обязательные опции: {@code --fund}, {@code --id}, {@code --title}.
 * Дата создания задаётся через {@code --date} (формат см. в
 * {@link DateParser}); если не указана — используется текущая дата.</p>
 *
 * <p>Валидация id, существования фонда и уникальности дела выполняется в
 * {@code CaseService#create}. При успехе команда печатает сообщение и
 * возвращает {@code 0}; при ошибке — {@code 1}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "mk",
        description = "Создать дело"
)
public class CaseMk implements Callable<Integer> {

    /** Код возврата при успехе. */
    private static final int EXIT_OK = 0;

    /** Код возврата при ошибке. */
    private static final int EXIT_ERROR = 1;

    private final CaseService caseService;

    /**
     * @param caseService сервис дел
     */
    public CaseMk(CaseService caseService) {
        this.caseService = caseService;
    }

    @CommandLine.Option(
            names = "--fund",
            required = true,
            description = "ID фонда"
    )
    private Integer fundId;

    @CommandLine.Option(
            names = "--id",
            required = true,
            description = "ID дела (например, 0001-2026 или ВУ)"
    )
    private String id;

    @CommandLine.Option(
            names = "--title",
            required = true,
            description = "Название дела"
    )
    private String title;

    @CommandLine.Option(
            names = "--date",
            description = "Дата создания (по умолчанию — сегодня)"
    )
    private String dateStr;

    /**
     * Выполняет команду.
     *
     * @return {@code 0} при успехе, {@code 1} при ошибке
     */
    @Override
    public Integer call() {
        try {
            LocalDate date = resolveDate();
            Case c = caseService.create(fundId, id, title, date);
            Console.print("Дело " + c.id() + " создано в фонде \"" + fundId + "\".");
            return EXIT_OK;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return EXIT_ERROR;
        }
    }

    /**
     * Определяет дату создания дела: из {@code --date} или текущую.
     *
     * @return дата создания
     * @throws IllegalArgumentException если {@code --date} задан в неверном формате
     */
    private LocalDate resolveDate() {
        return dateStr != null ? DateParser.parse(dateStr) : LocalDate.now();
    }
}