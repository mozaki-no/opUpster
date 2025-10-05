package app.opcsv.service;

//import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.dto.WorkPackageDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                   // ★ ロガー
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;         // ★ import
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;


import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentLinkService {

  private final OpenProjectClient client = null;
  private static final Logger log = LoggerFactory.getLogger(ParentLinkService.class);

  // /api/v3/work_packages/{id} で lockVersion を取る (OpenProjectClient に実装、後述)
  private Mono<WorkPackageDto> getWp(long id){ return client.getWorkPackage(id); }

  // ★ 1件リンク（/form → /commit）
  private Mono<Void> linkParentOnce(long childId, long parentId) {
    return getWp(childId)
        .flatMap(wp -> {
          var body = new ParentPatch();
          body.lockVersion = wp.getLockVersion();
          body.links.parent = new ParentPatch.Href("/api/v3/work_packages/" + parentId);

          // /form
          return client.webClient().post()
              .uri("/api/v3/work_packages/{id}/form", childId)
              .contentType(MediaType.valueOf("application/hal+json"))
              .bodyValue(body)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}) // ★型明示
              .flatMap(form -> {
                @SuppressWarnings("unchecked")
                var links = (Map<String, Object>) form.get("_links");
                @SuppressWarnings("unchecked")
                var commit = (Map<String, Object>) links.get("commit");
                var href = (String) commit.get("href");

                return client.webClient().post()
                    .uri(href)
                    .contentType(MediaType.valueOf("application/hal+json"))
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
              });
        })
        .doOnError(e -> logServerError("parent-link", childId, parentId, e))
        .onErrorResume(e -> Mono.empty()); // ★ 全体を落とさない
  }

  public Mono<Void> linkParents(Map<String, Long> byExternalKey, List<WorkPackagePlan> plans){
    return Flux.fromIterable(plans)
        .filter(p -> p.getparent_key() != null && !p.getparent_key().isBlank()) // ★ WorkPackagePlan は parentKey()
        .flatMap(p -> {
          Long childId  = byExternalKey.get(p.getexternal_key());
          Long parentId = byExternalKey.get(p.getparent_key()); // ★ parentExternalKey() ではなく parentKey()

          if (childId == null || parentId == null) {
            log.warn("[SKIP] parent link: childId={} parentId={} (childKey={} parentKey={})",
                childId, parentId, p.getexternal_key(), p.getparent_key());
            return Mono.empty();
          }
          return linkParentOnce(childId.longValue(), parentId.longValue()) // ★ long に
              .doOnSuccess(v -> log.info("[OK ] parent linked: child={} -> parent={}", childId, parentId));
        }, 4) // ★ concurrency 指定の flatMap
        .then();
  }

  // ---- 内部 DTO ----
  static final class ParentPatch {
    public long lockVersion;
    @JsonProperty("_links") public Links links = new Links();
    static final class Links { public Href parent; }
    static final class Href { public String href; Href(String href){ this.href = href; } }
  }

  // ---- エラーログ ----
  private void logServerError(String action, long childId, long parentId, Throwable e){
    if (e instanceof WebClientResponseException w) {
      log.error("[FAIL:{}] child={} parent={} status={} body={}",
          action, childId, parentId, w.getStatusCode(), w.getResponseBodyAsString());
    } else {
      log.error("[FAIL:{}] child={} parent={} err={}", action, childId, parentId, e.toString());
    }
  }
}