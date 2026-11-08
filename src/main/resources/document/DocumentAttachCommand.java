package org.example.document;

import org.example.documentCLI.cli.Console;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "attach", description = "Прикрепить скан к документу")
public class DocumentAttachCommand implements Callable<Integer> {
    private final DocumentService docService;

    public DocumentAttachCommand(DocumentService docService) {
        this.docService = docService;
    }

    @CommandLine.Parameters(index = "0", description = "ID документа")
    private String docId;

    @CommandLine.Parameters(index = "1", description = "Путь к файлу скана")
    private String path;

    @CommandLine.Option(names = "--overwrite", description = "Заменить существующий скан")
    private boolean overwrite;

    @Override
    public Integer call() {
        try {
            Path saved = docService.attachScan(docId, Path.of(path), overwrite);
            Console.print("Скан прикреплён к документу " + docId + ": " + saved.getFileName());
            return 0;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return 1;
        }
    }
}
