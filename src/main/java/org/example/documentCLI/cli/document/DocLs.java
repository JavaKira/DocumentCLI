package org.example.documentCLI.cli.document;

import org.example.documentCLI.application.document.DocumentService;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "ls", description = "Список документов")
public class DocLs implements Callable<Integer> {
    private DocumentService documentService;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
