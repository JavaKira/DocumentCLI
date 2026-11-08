package org.example.documentCLI.cli.document;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

/**
 * Группирующая команда {@code doc}: объединяет подкоманды для работы
 * с документами.
 *
 * <p>При запуске без подкоманды печатает краткую справку по доступным
 * подкомандам. Полная справка — {@code doc --help}.</p>
 *
 * @author Javakira
 * @since 1.0
 */
@CommandLine.Command(
        name = "doc",
        description = "Работа с документами",
        subcommands = {
                DocLs.class,
                DocMk.class,
                DocLink.class,
                DocOpen.class,
                DocAttach.class,
                // TODO: добавить DocRemCommand, DocPatchCommand,
                //       DocInventoryCommand, DocExtractCommand
        }
)
public class DocCommand implements Runnable {

    /**
     * Печатает краткую справку по подкомандам {@code doc}.
     */
    @Override
    public void run() {
        Console.print("""
                Команда "doc" — работа с документами.

                Доступные подкоманды:
                  ls      список всех документов
                  mk      создать новый документ
                  link    привязать документ к делу
                  open    открыть документ / показать содержимое
                  attach  прикрепить скан (файл) к документу

                Справка по подкоманде: doc <подкоманда> --help
                Полная справка:         doc --help
                """);
    }
}