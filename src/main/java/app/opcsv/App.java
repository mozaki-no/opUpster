package app.opcsv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan // ← application.yml の app.* をバインド
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
  
  // 起動時に AppProperties をダンプ（長さだけ）
  @Bean
  org.springframework.boot.ApplicationRunner showProps(app.opcsv.config.AppProperties props) {
    return args -> {
      System.out.println("[BOOT2] apiToken length = " + (props.getApiToken() == null ? 0 : props.getApiToken().length()));
    };
  }
}
