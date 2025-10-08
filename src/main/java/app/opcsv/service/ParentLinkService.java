package app.opcsv.service;

import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.dto.WorkPackageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentLinkService {

  // ★ DIに任せる。= null は消す
  private final OpenProjectClient client;

  // /api/v3/work_packages/{id} で lockVersion を取る
  private Mono<WorkPackageDto> getWp(long id) {
    return client.getWorkPackage(id);
  }

  // ★ 1件リンク（標準PATCHで parent を設定）
  private Mono<Void> linkParentOnce(long childId, long parentId) {
    return getWp(childId)
        .flatMap(wp ->
            client.setParent(childId, wp.getLockVersion(), parentId)
                  .then() // WorkPackageDto → Void
        )
        .doOnError(e -> logServerError("parent-link", childId, parentId, e))
        .onErrorResume(e -> Mono.empty()); // 全体を落とさない
  }

  public Mono<Void> linkParents(Map<String, Long> byExternalKey, List<WorkPackagePlan> plans) {
    return Flux.fromIterable(plans)
        .filter(p -> p.getparent_key() != null && !p.getparent_key().isBlank())
        .flatMap(p -> {
          Long childId  = byExternalKey.get(p.getexternal_key());
          Long parentId = byExternalKey.get(p.getparent_key());

          if (childId == null || parentId == null) {
            log.warn("[SKIP] parent link: childId={} parentId={} (childKey={} parentKey={})",
                childId, parentId, p.getexternal_key(), p.getparent_key());
            return Mono.empty();
          }
          return linkParentOnce(childId, parentId)
              .doOnSuccess(v -> log.info("[OK ] parent linked: child={} -> parent={}", childId, parentId));
        }, 4) // concurrency
        .then();
  }

  // ---- エラーログ ----
  private void logServerError(String action, long childId, long parentId, Throwable e) {
    if (e instanceof WebClientResponseException w) {
      log.error("[FAIL:{}] child={} parent={} status={} body={}",
          action, childId, parentId, w.getStatusCode(), w.getResponseBodyAsString());
    } else {
      log.error("[FAIL:{}] child={} parent={} err={}", action, childId, parentId, e.toString());
    }
  }
}
