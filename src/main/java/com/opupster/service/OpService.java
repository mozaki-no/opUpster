package com.opupster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpService {
    private static final Logger log = LoggerFactory.getLogger(OpService.class);

    public void execute() {
        log.info("start execute");
        // TODO: ここに既存ロジックを段階的に移設
        log.info("done execute");
    }
}
