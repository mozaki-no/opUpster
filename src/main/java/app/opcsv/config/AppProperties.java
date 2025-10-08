package app.opcsv.config;

import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
@Getter @Setter
public class AppProperties {
    @NotBlank private String apiToken;
    @NotNull  private Integer projectId;
    @NotBlank private String baseUrl;
    @NotNull  private Integer externalKeyCustomFieldId;
    @NotBlank private String csvPath;
    private boolean dryRun = true;
    @Getter @Setter
    public static class Proxy {
      private boolean enabled;
      private String host;
      private Integer port;
      private String username;
      private String password;
      private String nonProxyHosts;
    }

    // デフォルトインスタンスでNPE回避
    private Proxy proxy = new Proxy();
}
