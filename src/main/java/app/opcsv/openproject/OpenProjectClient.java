package app.opcsv.openproject;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import app.opcsv.openproject.dto.WorkPackageReq;
import app.opcsv.openproject.dto.WorkPackageDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OpenProjectClient {

  /** WebClientConfig#webClient(AppProperties) の Bean を注入 */
  @Qualifier("webClient")
  private final WebClient web;

  /* ---------- Get ---------- */
  public Mono<WorkPackageDto> getWorkPackage(long id) {
    return web.get()
        .uri("/api/v3/work_packages/{id}", id)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- Create ---------- */
  public Mono<WorkPackageDto> createWorkPackage(WorkPackageReq req) {
    // 作成時は status を送らない／type, project は payload 側の _links で指定
    return web.post()
        .uri("/api/v3/work_packages")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- Update (lockVersion必須) ---------- */
  public Mono<WorkPackageDto> updateWorkPackage(long id, WorkPackageReq req) {
    return web.patch()
        .uri("/api/v3/work_packages/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- 親子リンク（子にparentをPATCH） ---------- */
  public Mono<WorkPackageDto> setParent(long childId, long lockVersion, long parentId) {
    var body = Map.of(
        "lockVersion", lockVersion,
        "_links", Map.of(
            "parent", Map.of("href", "/api/v3/work_packages/" + parentId)
        )
    );
    return web.patch()
        .uri("/api/v3/work_packages/{id}", childId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- リレーション作成 ---------- */
  public Mono<Void> createRelation(long fromId, long toId, String typeKey) {
    // typeKey: "relates", "blocks", "blockedBy", "precedes", "follows"
    var body = Map.of(
        "_links", Map.of(
            "from", Map.of("href", "/api/v3/work_packages/" + fromId),
            "to",   Map.of("href", "/api/v3/work_packages/" + toId),
            "type", Map.of("href", "/api/v3/relations/types/" + typeKey)
        )
    );

    return web.post()
        .uri("/api/v3/relations")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)  // 本文を読んでおく（エラー時の原因把握）
        .then();
  }

  /* ---------- 削除 ---------- */
  public Mono<Void> deleteWorkPackage(long id) {
    return web.delete()
        .uri("/api/v3/work_packages/{id}", id)
        .retrieve()
        .bodyToMono(Void.class);
  }

//  /* ===== リクエストDTO（最小） ===== */
//
//  @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
//  public static class WorkPackageReq {
//    public String subject;
//    // OpenProjectの推奨形式は { "format": "markdown", "raw": "..." }
//    public Object  description;          // 文字列でも可だが、必要なら型を合わせる
//    @com.fasterxml.jackson.annotation.JsonProperty("lockVersion")
//    public Long lockVersion;             // create時は不要
//    @com.fasterxml.jackson.annotation.JsonProperty("_links")
//    public Map<String, Object> links;    // type/project/assignee/parent をここに
//
//    public String startDate;             // "YYYY-MM-DD"
//    public String dueDate;               // "YYYY-MM-DD"
//    @com.fasterxml.jackson.annotation.JsonProperty("estimatedTime")
//    public String estimatedTime;         // ISO8601 (例: "PT8H")
//    @com.fasterxml.jackson.annotation.JsonProperty("customField1")
//    public String customField1;          // external_key をCF1に入れる例
//  }
//
//  @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
//  public static class UpdateWorkPackageReq {
//    @com.fasterxml.jackson.annotation.JsonProperty("lockVersion")
//    public long lockVersion;
//
//    public String subject;
//    public Object  description;
//    @com.fasterxml.jackson.annotation.JsonProperty("_links")
//    public Map<String, Object> links;
//
//    @com.fasterxml.jackson.annotation.JsonProperty("customField1")
//    public String customField1;
//
//    public String startDate;
//    public String dueDate;
//    @com.fasterxml.jackson.annotation.JsonProperty("estimatedTime")
//    public String estimatedTime;
//  }
}
