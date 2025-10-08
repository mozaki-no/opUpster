package com.opupster.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opupster.service.OpService;

import app.opcsv.cli.UpsertCommand;
import app.opcsv.service.ParentLinkService;

@Component
public class CliController {
    private final UpsertCommand upsertCommand;
    private static final Logger log = LoggerFactory.getLogger(ParentLinkService.class);
    
    public CliController(OpService service, UpsertCommand upsertCommand) {
		this.upsertCommand = upsertCommand;
    }

    public void run(String[] args) throws Exception {
        log.info("CLI start");
        upsertCommand.execute();
        log.info("CLI end");
    }
}
