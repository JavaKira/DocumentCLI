package org.example.documentCLI.cli.fund;

import org.example.documentCLI.application.fund.FundService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Fund;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Подкоманда {@code fund mk}: создаёт новый фонд.
 *
 * <p>Идентификатор фонда генерируется автоматически сервисом. При успехе
 * печатает id и название созданного фонда и возвращает {@code 0}; при
 * ошибке печатает сообщение и возвращает {@code 1}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "mk",
        description = "Создать фонд"
)
public class FundMk implements Callable<Integer> {

    /** Код возврата при успехе. */
    private static final int EXIT_OK = 0;

    /** Код возврата при ошибке. */
    private static final int EXIT_ERROR = 1;

    @CommandLine.Option(
            names = "--title",
            required = true,
            description = "Название фонда"
    )
    private String title;

    @CommandLine.Option(
            names = "--note",
            description = "Примечание"
    )
    private String note;

    private final FundService service;

    /**
     * @param service сервис фондов
     */
    public FundMk(FundService service) {
        this.service = service;
    }

    /**
     * Выполняет команду.
     *
     * @return {@code 0} при успехе, {@code 1} при ошибке
     */
    @Override
    public Integer call() {
        try {
            Fund fund = service.create(title, note);
            Console.print("Фонд \"" + fund.id() + "\" создан: " + fund.title());
            return EXIT_OK;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return EXIT_ERROR;
        }
    }
}