package org.example.documentCLI.cli.cases;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

/**
 * Группирующая команда {@code case}: объединяет подкоманды для работы с делами.
 *
 * <p>При запуске без подкоманды печатает краткую справку по доступным
 * подкомандам. Полная справка — {@code case --help}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "case",
        description = "Работа с делами",
        subcommands = {
                CaseLs.class,
                CaseMk.class,
                // TODO: добавить CaseRemCommand, CasePatchCommand,
                //       CaseInventoryCommand, CaseExtractCommand
        }
)
public class CaseCommand implements Runnable {

    /**
     * Печатает краткую справку по подкомандам {@code case}.
     */
    @Override
    public void run() {
        Console.print("""
                Команда "case" — работа с делами.

                Доступные подкоманды:
                  ls      список всех дел
                  mk      создать новое дело

                Справка по подкоманде: case <подкоманда> --help
                Полная справка:         case --help
                """);
    }
}