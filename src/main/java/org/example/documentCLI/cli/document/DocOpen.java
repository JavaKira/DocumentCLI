package org.example.documentCLI.cli.document;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "open", description = "Открыть скан документа в системном просмотре")
public class DocOpen implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
