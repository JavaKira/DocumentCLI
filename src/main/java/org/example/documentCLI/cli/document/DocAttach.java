package org.example.documentCLI.cli.document;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "attach", description = "Прикрепить скан к документу")
public class DocAttach implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
