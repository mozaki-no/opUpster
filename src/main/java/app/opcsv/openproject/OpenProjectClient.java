package app.opcsv.openproject;

import java.net.URI;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;
import app.opcsv.config.AppProperties;
import app.opcsv.openproject.dto.WorkPackageDto;

@Component
public class OpenProjectClient {

  private final WebClient web;
  private final AppProperties props;

  public OpenProjectClient(WebClient web, AppProperties props) {
    this.web = web;
    this.props = props;
  }

  private URI build(String path) {
    var base = java.net.URI.create(
        props.getBaseUrl().endsWith("/") ? props.getBaseUrl().substring(0, props.getBaseUrl().length()-1) : props.getBaseUrl()
    );
    return UriComponentsBuilder.fromUri(base).path(path).build(true).toUri();
  }

  /* ---------- Get ---------- */
  public Mono<WorkPackageDto> getWorkPackage(long id) {
    URI uri = build("/api/v3/work_packages/" + id);
    return web.get().uri(uri)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }
  
  /* ---------- Create ---------- */
  public Mono<WorkPackageDto> createWorkPackage(CreateWorkPackageReq req) {
    URI uri = build("/api/v3/work_packages");
    return web.post().uri(uri)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- Update (lockVersion必須) ---------- */
  public Mono<WorkPackageDto> updateWorkPackage(long id, UpdateWorkPackageReq req) {
    URI uri = build("/api/v3/work_packages/" + id);
    return web.patch().uri(uri)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- 親子リンク（子にparentをPATCH） ---------- */
  public Mono<WorkPackageDto> setParent(long childId, long lockVersion, long parentId) {
    URI uri = build("/api/v3/work_packages/" + childId);
    var body = Map.of(
      "lockVersion", lockVersion,
      "_links", Map.of(
        "parent", Map.of("href", "/api/v3/work_packages/" + parentId)
      )
    );
    return web.patch().uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(WorkPackageDto.class);
  }

  /* ---------- リレーション作成 ---------- */
  public Mono<Void> createRelation(long fromId, long toId, String type /* relates, blocks, precedes 等 */) {
    URI uri = build("/api/v3/relations");
    var body = Map.of(
      "_links", Map.of(
        "from", Map.of("href", "/api/v3/work_packages/" + fromId),
        "to",   Map.of("href", "/api/v3/work_packages/" + toId),
        "type", Map.of("href", "/api/v3/relations/types/" + type) // 例: "relates","blocks","precedes"
      )
    );
    return web.post().uri(uri)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Void.class);
  }

  /* ---------- 削除 ---------- */
  public Mono<Void> deleteWorkPackage(long id) {
    URI uri = build("/api/v3/work_packages/" + id);
    return web.delete().uri(uri)
        .retrieve()
        .bodyToMono(Void.class);
  }

  /* ===== リクエストDTO（最小） ===== */

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CreateWorkPackageReq {
    public String subject;
    public String description;
    @JsonProperty("lockVersion") public Long lockVersion; // create時は不要
    public Map<String,Object> _links;
    // custom fields は JSON 直で: "customField<id>": value を付けたい場合は Update側に寄せてもOK
	public String startDate;
	public String dueDate;
	public String estimatedTime;
	public String customField1;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class UpdateWorkPackageReq {
    @JsonProperty("lockVersion") public long lockVersion;
    public String subject;
    public String description;
    public Map<String,Object> _links;
    // customField はここに普通のプロパティとして載せる: "customField1": "A-001" など
    @JsonProperty("customField1") public String customField1; // 例: cf1 を使うなら
    // 他のCFは必要に応じて追加（動的にやるなら Map<String,Object> でOK）
	public String startDate;
	public String dueDate;
	public String estimatedTime;
  }
}