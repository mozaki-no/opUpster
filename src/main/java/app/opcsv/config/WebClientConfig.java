package app.opcsv.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Configuration
public class WebClientConfig {

  @Bean
  WebClient webClient(AppProperties props) {

    HttpClient http = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(30));

    if (props.getProxy() != null && props.getProxy().isEnabled()) {
      http = http.proxy(p -> p
          .type(ProxyProvider.Proxy.HTTP)
          .host(props.getProxy().getHost())
          .port(props.getProxy().getPort()));
    }

    // ★ DataBufferLimitException 対策（4MB まで拡張）
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
        .build();

    return WebClient.builder()
        .baseUrl(props.getBaseUrl())                // 末尾は / でも / なしでもOK
        .clientConnector(new ReactorClientHttpConnector(http))
        .exchangeStrategies(strategies)
        // OpenProject API は HAL+JSON を返すので Accept を明示
        .defaultHeader("Accept", "application/hal+json")
        // 認証ヘッダ（apikey <token>）
        .defaultHeaders(h -> h.setBasicAuth("apikey", props.getApiToken()))
        .build();
  }
}
