package org.example.document;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.awt.Desktop;

@CommandLine.Command(name = "open", description = "Открыть скан документа в системном просмотрщике")
public class DocumentOpenCommand implements Callable<Integer> {

    private final DocumentService docService;

    public DocumentOpenCommand(DocumentService docService) {
        this.docService = docService;
    }

    @CommandLine.Parameters(index = "0", description = "ID документа")
    private String docId;

    @Override
    public Integer call() {
        try {
            Optional<Path> scan = docService.findScan(docId);
            if (scan.isEmpty()) {
                Console.error("У документа " + docId + " нет скана.");
                return 1;
            }
            Path file = scan.get().toAbsolutePath();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.toFile());
                Console.print("Открыт: " + file);
            } else {
                Console.print("Не удалось открыть автоматически. Файл: " + file);
            }
            return 0;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return 1;
        }
    }
}
