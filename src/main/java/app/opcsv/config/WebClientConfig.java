package app.opcsv.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.ExchangeFilterFunctions; // ★ Basic認証用
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.util.DefaultUriBuilderFactory;

import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

//WebClientConfig.java
@Configuration
public class WebClientConfig {

	@Bean
	WebClient webClient(AppProperties props) {

		HttpClient http = HttpClient.create().responseTimeout(Duration.ofSeconds(30));

		if (props.getProxy() != null && props.getProxy().isEnabled()) {
			http = http.proxy(spec -> spec.type(ProxyProvider.Proxy.HTTP).host(props.getProxy().getHost())
					.port(props.getProxy().getPort()));
		}

		// 受信ボディのメモリ上限を引き上げ（DataBufferLimitException対策）
		ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
				.build();

		return WebClient.builder().clientConnector(new ReactorClientHttpConnector(http)).exchangeStrategies(strategies)
				.baseUrl(props.getBaseUrl()) // 末尾スラッシュは AppProperties 側で整形でもOK
				.defaultHeaders(h -> h.setBasicAuth("apikey", props.getApiToken())).build();
	}

//  private ExchangeFilterFunction logRequest() {
//    return ExchangeFilterFunction.ofRequestProcessor(req -> {
//      String auth = req.headers().getFirst("Authorization");
//      System.out.println("[HTTP] " + req.method() + " " + req.url());
//      System.out.println("[HTTP] Authorization header present? " + (auth != null));
//      return reactor.core.publisher.Mono.just(req);
//    });
//  }
//
//  private ExchangeFilterFunction logResponseAndFailOn401() {
//    return ExchangeFilterFunction.ofResponseProcessor(res -> {
//      System.out.println("[HTTP] <- " + res.statusCode());
//      if (res.statusCode().value() == 401) {
//        return reactor.core.publisher.Mono.error(new RuntimeException(
//            "401 Unauthorized: APIトークン未設定/無効/ドメイン不一致の可能性。" +
//            " username=apikey の Basic 認証で APIキーを使ってください。"));
//      }
//      return reactor.core.publisher.Mono.just(res);
//    });
//  }

}
