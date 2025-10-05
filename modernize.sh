set -euo pipefail

# 0) guard
[ -f pom.xml ] || { echo "pom.xmlが見つからないので中断"; exit 1; }

# 1) 依存/親追加（sedで安全に挿入）
# - 既存pomのバックアップ
cp -n pom.xml pom.xml.bak || true

# - 親がSpring Bootでなければ parent を追加
if ! grep -q '<artifactId>spring-boot-starter-parent</artifactId>' pom.xml; then
  awk '
    BEGIN{inserted=0}
    /<modelVersion>/{print; next}
    {print}
  ' pom.xml > pom.tmp1

  # groupId/artifactId/version が既にある想定なので、<modelVersion>の直後に parent を差し込む
  awk '
    BEGIN{done=0}
    {
      print $0
      if(!done && $0 ~ /<modelVersion>/){
        print "  <parent>"
        print "    <groupId>org.springframework.boot</groupId>"
        print "    <artifactId>spring-boot-starter-parent</artifactId>"
        print "    <version>3.3.4</version>"
        print "    <relativePath/>"
        print "  </parent>"
        done=1
      }
    }
  ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
else
  cp pom.xml pom.tmp1
fi

# - <properties> に <java.version>17</java.version> を足す（なければ）
if ! grep -q '<java.version>' pom.tmp1; then
  awk '
    BEGIN{done=0}
    {
      print $0
      if(!done && $0 ~ /<properties>/){
        print "    <java.version>17</java.version>"
        done=1
      }
    }
  ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
fi

# - <dependencies> がなければ作る
if ! grep -q '<dependencies>' pom.tmp1; then
  awk '
    BEGIN{done=0}
    {
      print $0
      if(!done && $0 ~ /<\/properties>/){
        print "  <dependencies>"
        print "  </dependencies>"
        done=1
      }
    }
  ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
fi

# - Spring Boot core, picocli, logstash encoder, test 追加（重複チェック）
add_dep() {
  gid="$1"; aid="$2"; ver="$3"; scope="$4"
  if ! grep -q "<artifactId>${aid}</artifactId>" pom.tmp1; then
    awk -v G="$gid" -v A="$aid" -v V="$ver" -v S="$scope" '
      BEGIN{done=0}
      {
        if(!done && $0 ~ /<dependencies>/){
          print $0
          print "    <dependency>"
          print "      <groupId>" G "</groupId>"
          print "      <artifactId>" A "</artifactId>"
          if(V != "") print "      <version>" V "</version>"
          if(S != "") print "      <scope>" S "</scope>"
          print "    </dependency>"
          done=1; next
        }
        print $0
      }
    ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
  fi
}
add_dep "org.springframework.boot" "spring-boot-starter" "" ""
add_dep "info.picocli" "picocli" "4.7.6" ""
add_dep "net.logstash.logback" "logstash-logback-encoder" "7.4" ""
add_dep "org.springframework.boot" "spring-boot-starter-test" "" "test"
add_dep "org.mockito" "mockito-core" "5.13.0" "test"

# - build/plugins に spring-boot-maven-plugin 追加
if ! grep -q 'spring-boot-maven-plugin' pom.tmp1; then
  if ! grep -q '<build>' pom.tmp1; then
    # buildブロックが無ければ作る
    awk '
      {
        print $0
        if($0 ~ /<\/dependencies>/){
          print "  <build>"
          print "    <plugins>"
          print "    </plugins>"
          print "  </build>"
        }
      }
    ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
  fi
  # pluginsが無ければ作る
  if ! grep -q '<plugins>' pom.tmp1; then
    awk '
      BEGIN{done=0}
      {
        print $0
        if(!done && $0 ~ /<build>/){
          print "    <plugins>"
          print "    </plugins>"
          done=1
        }
      }
    ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
  fi

  awk '
    BEGIN{done=0}
    {
      if(!done && $0 ~ /<plugins>/){
        print $0
        print "      <plugin>"
        print "        <groupId>org.springframework.boot</groupId>"
        print "        <artifactId>spring-boot-maven-plugin</artifactId>"
        print "      </plugin>"
        done=1; next
      }
      print $0
    }
  ' pom.tmp1 > pom.tmp2 && mv pom.tmp2 pom.tmp1
fi

mv pom.tmp1 pom.xml

# 2) ソース/リソース作成
mkdir -p src/main/java/com/opupster/controller
mkdir -p src/main/java/com/opupster/service
mkdir -p src/main/resources
mkdir -p src/test/java/com/opupster/service
mkdir -p .github/workflows

# Application
cat > src/main/java/com/opupster/OpUpsterApplication.java <<'JAVA'
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
JAVA

# CLI Controller
cat > src/main/java/com/opupster/controller/CliController.java <<'JAVA'
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
JAVA

# Service
cat > src/main/java/com/opupster/service/OpService.java <<'JAVA'
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
JAVA

# application.yml
cat > src/main/resources/application.yml <<'YML'
spring:
  main:
    web-application-type: none   # CLIモード
  output:
    ansi:
      enabled: DETECT

logging:
  level:
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] %logger - %msg%n"
YML

# logback-spring.xml（JSONレイアウト切替の土台）
cat > src/main/resources/logback-spring.xml <<'XML'
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] %logger - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <timestamp/>
        <pattern>
          <pattern>{"level":"%level","logger":"%logger","thread":"%thread","message":"%message"}</pattern>
        </pattern>
        <arguments/>
        <stackTrace/>
      </providers>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE" />
    <!-- 運用でJSONにしたい場合はCONSOLEを外してJSONを有効化 -->
    <!-- <appender-ref ref="JSON" /> -->
  </root>
</configuration>
XML

# 単体テスト（サンプル）
cat > src/test/java/com/opupster/service/OpServiceTest.java <<'JAVA'
package com.opupster.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OpServiceTest {
    @Test
    void execute_shouldRunWithoutException() {
        var svc = new OpService();
        assertDoesNotThrow(svc::execute);
    }
}
JAVA

# GitHub Actions (CI)
cat > .github/workflows/ci.yml <<'YML'
name: Build and Test
on:
  push:
    branches: [ main, develop, "feature/**", "feat/**" ]
  pull_request:
    branches: [ main, develop ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: maven
      - name: Build & Test
        run: ./mvnw -B -DskipTests=false clean verify
YML

echo "== Done. Try build =="
./mvnw -q -DskipTests=false clean verify
