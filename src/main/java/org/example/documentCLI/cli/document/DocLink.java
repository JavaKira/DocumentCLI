package org.example.documentCLI.cli.document;

import org.example.documentCLI.application.cases.CaseService;
import org.example.documentCLI.application.document.DocumentService;
import org.example.documentCLI.cli.Console;
import org.example.documentCLI.domain.Document;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "link", description = "Привязать документ к делу")
public class DocLink implements Callable<Integer> {
    private final DocumentService docService;
    private final CaseService caseService;

    public DocLink(DocumentService docService, CaseService caseService) {
        this.docService = docService;
        this.caseService = caseService;
    }

    @Parameters(index = "0", description = "ID документа")
    private String docId;

    @Option(names = "--to", required = true, description = "ID дела")
    private String caseId;

    @Override
    public Integer call() {
        try {
            Document d = docService.byId(docId);
            if (d.isAttached()) {
                Console.error("Документ уже привязан к делу \"" + d.caseId() + "\".");
                return 1;
            }

            caseService.linkDocument(caseId, docId);
            // Перечитываем, чтобы получить присвоенный regNumber
            Document updated = docService.byId(docId);
            Console.print("Документ " + docId + " привязан к делу "
                    + caseId + ", получил №" + updated.regNumber() + ".");
            return 0;
        } catch (Exception e) {
            Console.error("Ошибка: " + e.getMessage());
            return 1;
        }
    }
}