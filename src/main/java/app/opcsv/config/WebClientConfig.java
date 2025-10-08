package app.opcsv.config;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import com.opupster.http.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Configuration
public class WebClientConfig {

  private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

  @Bean
  WebClient openProjectWebClient(AppProperties props) {

    // ベースのHTTPクライアント
    HttpClient http = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(30));

    // プロキシ対応（必要なときだけ）
    if (props.getProxy() != null && props.getProxy().isEnabled()) {
      http = http.proxy(p -> p
          .type(ProxyProvider.Proxy.HTTP)
          .host(props.getProxy().getHost())
          .port(props.getProxy().getPort()));
    }

    // DataBufferLimitException 対策（4MBまで拡張）
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
        .build();

    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(http))
        .exchangeStrategies(strategies)
        // OpenProjectはHAL+JSON
        .defaultHeader("Accept", "application/hal+json")
        // 認証（OpenProjectは Basic 認証: ユーザ=apikey, PW=token）
        .defaultHeaders(h -> h.setBasicAuth("apikey", props.getApiToken()))
        // ↓↓↓ 共通フィルタ（順序も重要）
        .filter(correlationIdFilter())
        .filter(requestLoggingFilter())   // リクエスト安全ログ
        .filter(errorMappingFilter())     // 2xx以外は本文を読んでApiException
        .filter(responseLoggingFilter())  // レスポンスログ（成功時）
        .build();
  }

  /** 各リクエストに相関IDを付与（ログ突合用） */
  private ExchangeFilterFunction correlationIdFilter() {
    return (req, next) -> {
      String cid = UUID.randomUUID().toString();
      var mutated = WebClient
          .RequestHeadersSpec.class.isAssignableFrom(req.getClass())
          ? req
          : req; // 型安全のためthisまま。CIDはヘッダに入れる
      var newReq = org.springframework.web.reactive.function.client.ClientRequest
          .from(req)
          .header("X-Correlation-Id", cid)
          .build();
      return next.exchange(newReq).contextWrite(ctx -> ctx.put("cid", cid));
    };
  }

  /** リクエストログ（Authorizationは伏せる） */
  private ExchangeFilterFunction requestLoggingFilter() {
    return (req, next) -> {
      String cid = req.headers().getFirst("X-Correlation-Id");
      URI uri = req.url();
      log.debug("[{}] >> {} {}", cid, req.method(), uri);
      req.headers().forEach((k, v) -> {
        if ("Authorization".equalsIgnoreCase(k)) {
          log.trace("[{}] >> {}: <redacted>", cid, k);
        } else {
          log.trace("[{}] >> {}: {}", cid, k, v);
        }
      });
      return next.exchange(req);
    };
  }

  /** 2xx以外を本文つき ApiException に変換（method/urlも保持） */
  private ExchangeFilterFunction errorMappingFilter() {
    return (req, next) ->
        next.exchange(req)
            .flatMap(res -> {
              if (res.statusCode().is2xxSuccessful()) {
                return Mono.just(res);
              }
              return res.bodyToMono(String.class).defaultIfEmpty("")
                  .flatMap(body -> {
                    String cid = req.headers().getFirst("X-Correlation-Id");
                    log.error("[{}] HTTP {} {} -> {}\n{}",
                        cid, req.method(), req.url(), res.statusCode(), body);
                    return Mono.error(new ApiException(
                        res.statusCode(),
                        req.method().name(),
                        req.url().toString(),
                        res.headers().asHttpHeaders(),
                        body));
                  });
            });
  }

  /** 成功時のレスポンスログ（簡潔） */
  private ExchangeFilterFunction responseLoggingFilter() {
    return (req, next) ->
        next.exchange(req)
            .doOnNext(res -> {
              String cid = req.headers().getFirst("X-Correlation-Id");
              log.debug("[{}] << {} {} -> {}", cid, req.method(), req.url(), res.statusCode());
            });
  }
}
