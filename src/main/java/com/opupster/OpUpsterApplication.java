package com.opupster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.opupster.controller.CliController;

@SpringBootApplication(
	    scanBasePackages = {
	        "com.opupster",   // 既存配下
	        "app.opcsv"       // ★ これを追加：CSV処理の@Service/@Componentを拾う
	    }
	)
public class OpUpsterApplication {
    private static final Logger log = LoggerFactory.getLogger(OpUpsterApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OpUpsterApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
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
