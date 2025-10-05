package app.opcsv.openproject;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import app.opcsv.config.AppProperties;
import app.opcsv.openproject.dto.WorkPackageDto;
import reactor.core.publisher.Mono;

@Component
public class OpenProjectQuery {

  private final WebClient web;
  private final AppProperties props;

  public OpenProjectQuery(WebClient web, AppProperties props) {
    this.web = web;
    this.props = props;
  }
  
	private String normBaseUrl(String baseUrl) {
	 return baseUrl != null && baseUrl.endsWith("/")
	     ? baseUrl.substring(0, baseUrl.length() - 1)
	     : baseUrl;
	}

  /** external_key(=custom field) 完全一致で1件取得（無ければ empty） */
  public Mono<Object> findByexternal_key(int projectId, int customFieldId, String external_key) {
	  if (external_key == null || external_key.isBlank()) {
		    System.out.println("[SKIP] query.findByexternal_key: blank external_key");
		    return Mono.just(Optional.empty());
	  }
	  String filtersJson = """
	    [
	      {"project":{"operator":"=","values":["%d"]}},
	      {"customField%d":{"operator":"=","values":["%s"]}}
	    ]
	  """.formatted(projectId, customFieldId, external_key);

	  String encoded = UriUtils.encode(filtersJson, StandardCharsets.UTF_8);

	  URI base = URI.create(normBaseUrl(props.getBaseUrl()));   // ★ fromUri を使う
	  URI uri = UriComponentsBuilder.fromUri(base)
	      .path("/api/v3/work_packages")
	      .queryParam("filters", encoded)   // 既に encode 済みの値を渡す
	      .queryParam("pageSize", "2")
	      .build(true)                      // ★ 再エンコード＆テンプレ展開しない
	      .toUri();

	  return web.get()
	      .uri(uri)      // ★ 絶対URIを渡yす
	      .retrieve()
	      .bodyToMono(SearchResult.class)
	      .flatMap(res -> (res != null && res._embedded != null
	                     && res._embedded.elements != null
	                     && !res._embedded.elements.isEmpty())
	                     ? Mono.just(res._embedded.elements.get(0))
	                     : Mono.empty());
	}

  /** プロジェクト全件（最小DTO） */
  public Mono<List<WorkPackageDto>> listAllByProject(int projectId) {
    String filtersJson = """
      [
        {"project":{"operator":"=","values":["%d"]}}
      ]
    """.formatted(projectId);

    return fetchPage(filtersJson, 0)
        .expand(res -> {
          int next = res.offset + res.count;
          boolean hasNext = next < res.total;
          return hasNext ? fetchPage(filtersJson, next) : Mono.empty();
        })
        .map(res -> res._embedded == null ? List.<WorkPackageDto>of() : res._embedded.elements)
        .flatMap(reactor.core.publisher.Flux::fromIterable)
        .collectList();
  }

  private Mono<SearchResult> fetchPage(String filtersJson, int offset) {
	  String encoded = UriUtils.encode(filtersJson, StandardCharsets.UTF_8);

	  URI base = URI.create(normBaseUrl(props.getBaseUrl()));   // ★ fromUri を使う
	  URI uri = UriComponentsBuilder.fromUri(base)
	      .path("/api/v3/work_packages")
	      .queryParam("filters", encoded)
	      .queryParam("pageSize", "200")
	      .queryParam("offset", offset)
	      .build(true)
	      .toUri();

	  return web.get().uri(uri).retrieve().bodyToMono(SearchResult.class);
	}

  // ====== 必要最小の受け取り用DTO（検索結果） ======
  @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
  static class SearchResult {
    public int total;
    public int count;
    public int offset;
    @com.fasterxml.jackson.annotation.JsonProperty("_embedded")
    public Embedded _embedded;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    static class Embedded {
      @com.fasterxml.jackson.annotation.JsonProperty("elements")
      public List<WorkPackageDto> elements;
    }
  }
}
