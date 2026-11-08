package org.example.documentCLI.cli.document;

import org.example.documentCLI.application.document.DocumentService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Document;
import org.example.documentCLI.infrastructure.DateParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate;
import java.util.concurrent.Callable;

@Command(name = "mk", description = "Создать документ")
public class DocMk implements Callable<Integer> {
    private final DocumentService docService;

    public DocMk(DocumentService docService) {
        this.docService = docService;
    }

    @Option(names = "--title", required = true, description = "Заголовок")
    private String title;

    @Option(names = "--date", required = true, description = "Дата (dd.MM.yyyy, yyyy-MM-dd, ...)")
    private String dateStr;

    @Option(names = "--note", description = "Примечание")
    private String note;

    @Override
    public Integer call() {
        try {
            LocalDate date = DateParser.parse(dateStr);
            Document d = docService.create(title, date, note);


            if (d.isAttached()) {
                Console.print("Документ создан с №" + d.regNumber()
                        + " в деле " + d.caseId() + ". ID: " + d.id());
            } else {
                Console.print("Документ создан без привязки. ID: " + d.id());
            }
            return 0;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return 1;
        }
    }
}