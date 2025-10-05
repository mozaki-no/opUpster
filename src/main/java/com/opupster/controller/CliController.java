package com.opupster.controller;

import com.opupster.service.OpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
public class CliController {
    private static final Logger log = LoggerFactory.getLogger(CliController.class);
    private final CommandLine cmd;

    public CliController(OpService service) {
        this.cmd = new CommandLine(new RootCommand(service));
    }

    public void run(String[] args) {
        int exit = cmd.execute(args);
        if (exit != 0) {
            throw new IllegalStateException("CLI finished with non-zero exit code: " + exit);
        }
    }

    @Command(name = "op", mixinStandardHelpOptions = true,
             version = "opUpster 0.2",
             description = "opUpster CLI")
    static class RootCommand implements Runnable {
        private final OpService service;
        RootCommand(OpService service) { this.service = service; }

        @Option(names = {"-v","--version"}, description = "Show version and exit")
        boolean showVersion;

        @Option(names = {"-d","--debug"}, description = "Enable debug logs")
        boolean debug;

        @Override public void run() {
            if (showVersion) {
                System.out.println("opUpster 0.2.0");
                return;
            }
            if (debug) {
                System.setProperty("logging.level.root", "DEBUG");
            }
            service.execute();
        }
    }
}
