package com.opupster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.opupster.controller.CliController;

@SpringBootApplication
public class OpUpsterApplication {
    private static final Logger log = LoggerFactory.getLogger(OpUpsterApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OpUpsterApplication.class, args);
    }

    @Bean
    CommandLineRunner runCli(CliController cli) {
        return args -> {
            long start = System.currentTimeMillis();
            cli.run(args);
            log.info("finished ms={}", System.currentTimeMillis() - start);
        };
    }
}
