package org.example.document;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "scan", description = "Показать информацию о скане документа")
public class DocumentScansCommand implements Callable<Integer> {
    private final DocumentService docService;

    public DocumentScansCommand(DocumentService docService) {
        this.docService = docService;
    }

    @CommandLine.Parameters(index = "0", description = "ID документа")
    private String docId;

    @Override
    public Integer call() {
        try {
            if (docService.findById(docId).isEmpty()) {
                Console.error("Документ \"" + docId + "\" не найден.");
                return 1;
            }
            Optional<Path> scan = docService.findScan(docId);
            if (scan.isEmpty()) {
                Console.print("У документа " + docId + " нет скана.");
                return 0;
            }
            Path p = scan.get();
            long size = Files.size(p);
            Console.print("Скан документа " + docId + ":");
            Console.print("  файл: " + p);
            Console.print("  размер: " + humanSize(size));
            return 0;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return 1;
        }
    }

    private String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
