package app.opcsv.openproject;

import app.opcsv.openproject.dto.SearchResultDto;
import app.opcsv.openproject.dto.WorkPackageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenProjectQuery {

  /** WebClientConfig で定義した Bean 名に合わせる（例: @Bean name="webClient"） */
  @Qualifier("webClient")
  private final WebClient web;

  /**
   * external_key(=custom field) 完全一致で1件取得（無ければ empty）
   * @param projectId プロジェクトID
   * @param customFieldId external_key を格納しているカスタムフィールドID（例: 1）
   * @param externalKey 参照キー
   */
  public Mono<WorkPackageDto> findByexternal_key(int projectId, int customFieldId, String externalKey) {
    if (externalKey == null || externalKey.isBlank()) {
      return Mono.empty();
    }

    // OpenProject filters JSON（SpringがURLエンコードしてくれるので生文字でOK）
    String filtersJson = """
      [
        {"project":{"operator":"=","values":["%d"]}},
        {"customField%d":{"operator":"=","values":["%s"]}}
      ]
      """.formatted(projectId, customFieldId, externalKey);

    return web.get()
        .uri(uri -> uri
            .path("/api/v3/work_packages")
            .queryParam("filters", filtersJson)
            .queryParam("pageSize", 1)
            .build())
        .retrieve()
        .bodyToMono(SearchResultDto.class)
        .flatMap(res -> {
          List<WorkPackageDto> els = res.embeddedElements();
          return (els != null && !els.isEmpty()) ? Mono.just(els.get(0)) : Mono.empty();
        });
  }

  /** プロジェクト配下のWPを全部（必要最小DTO） */
  public Mono<List<WorkPackageDto>> listAllByProject(int projectId) {
	  String filtersJson = """
	    [
	      {"project":{"operator":"=","values":["%d"]}}
	    ]
	    """.formatted(projectId);

	  return fetchPage(filtersJson, 0)
	      .expand(res -> {
	        int next = res.getOffset() + res.getCount();
	        boolean hasNext = next < res.getTotal();
	        return hasNext ? fetchPage(filtersJson, next) : Mono.empty();
	      })
	      .map(SearchResultDto::embeddedElements)                 // Flux<List<WorkPackageDto>>
	      .flatMapIterable(list -> list == null ? List.of() : list) // ← ここを修正（Flux<WorkPackageDto>）
	      .collectList();                                         // Mono<List<WorkPackageDto>>
	}

  private Mono<SearchResultDto> fetchPage(String filtersJson, int offset) {
    return web.get()
        .uri(uri -> uri
            .path("/api/v3/work_packages")
            .queryParam("filters", filtersJson)
            .queryParam("pageSize", 200)
            .queryParam("offset", offset)
            .build())
        .retrieve()
        .bodyToMono(SearchResultDto.class);
  }
}
