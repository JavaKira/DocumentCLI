package org.example.documentCLI.cli.fund;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

/**
 * Группирующая команда {@code fund}: объединяет подкоманды для работы с фондами.
 *
 * <p>При запуске без подкоманды печатает краткую справку по доступным
 * подкомандам. Полная справка — {@code fund --help}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "fund",
        description = "Работа с фондами",
        subcommands = {
                FundLs.class,
                FundMk.class,
                // TODO: добавить FundPatchCommand, FundRemCommand
        }
)
public class FundCommand implements Runnable {

    /**
     * Печатает краткую справку по подкомандам {@code fund}.
     */
    @Override
    public void run() {
        Console.print("""
                Команда "fund" — работа с фондами.

                Доступные подкоманды:
                  ls      список всех фондов
                  mk      создать новый фонд

                Справка по подкоманде: fund <подкоманда> --help
                Полная справка:         fund --help
                """);
    }
}