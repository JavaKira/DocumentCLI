package org.example.documentCLI.cli;

import org.example.documentCLI.cli.cases.CaseCommand;
import org.example.documentCLI.cli.document.DocCommand;
import org.example.documentCLI.cli.fund.FundCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "documentCLI",
        version = "1.0",
        mixinStandardHelpOptions = true,
        description = "Утилита для делопроизводства",
        subcommands = {
                FundCommand.class,
                DocCommand.class,
                CaseCommand.class,
        }
)
public class DocumentCLICommand implements Runnable{
    @Override
    public void run() {

    }
}
