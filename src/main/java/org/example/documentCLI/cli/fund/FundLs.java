package org.example.documentCLI.cli.fund;

import org.example.documentCLI.application.fund.FundService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Fund;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Подкоманда {@code fund ls}: выводит список всех фондов.
 *
 * <p>Формат строки: {@code <id> <title>}. Если фондов нет — печатает
 * подсказку по созданию. Код возврата всегда {@code 0}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "ls",
        description = "Список фондов"
)
public class FundLs implements Callable<Integer> {

    /** Ширина колонки id при форматировании. */
    private static final int ID_WIDTH = 12;

    /** Ширина колонки title при форматировании. */
    private static final int TITLE_WIDTH = 32;

    private final FundService service;

    /**
     * @param service сервис фондов
     */
    public FundLs(FundService service) {
        this.service = service;
    }

    /**
     * Выполняет команду.
     *
     * @return код возврата ({@code 0} — успех)
     */
    @Override
    public Integer call() {
        List<Fund> funds = service.all();
        if (funds.isEmpty()) {
            Console.print("Фондов пока нет. Создайте: fund mk --title \"<название>\"");
            return 0;
        }

        Console.print("Фонды (" + funds.size() + "):");
        for (Fund f : funds) {
            Console.print(String.format("  %-" + ID_WIDTH + "s %-" + TITLE_WIDTH + "s",
                    f.id(), f.title()));
        }
        return 0;
    }
}