package app.opcsv.openproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/** POST(PUT) / PATCH 共通のリクエストDTO。null は出力しない */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkPackageReq {
  public String subject;

  /** 推奨形式: {"format":"markdown","raw":"..."}（文字列でも可） */
  public Object description;

  /** 更新(PATCH)時に必須 */
  @JsonProperty("lockVersion")
  public long lockVersion;

  /** "YYYY-MM-DD" */
  public String startDate;
  /** "YYYY-MM-DD" */
  public String dueDate;

  /** ISO8601 duration e.g. "PT8H", "PT1H30M" */
  @JsonProperty("estimatedTime")
  public String estimatedTime;

  /** カスタムフィールド例: external_key を CF1 に入れる場合 */
  @JsonProperty("customField1")
  public String customField1;

  /** HAL _links */
  @JsonProperty("_links")
  public Map<String, Object> links;

  // ---------- helper ----------
  public WorkPackageReq putLink(String key, String href) {
    if (links == null) links = new HashMap<>();
    links.put(key, Map.of("href", href));
    return this;
  }
  public WorkPackageReq type(int typeId) { return putLink("type", "/api/v3/types/" + typeId); }
  public WorkPackageReq project(int projectId) { return putLink("project", "/api/v3/projects/" + projectId); }
  public WorkPackageReq assignee(long userId) { return putLink("assignee", "/api/v3/users/" + userId); }
  public WorkPackageReq parent(long wpId) { return putLink("parent", "/api/v3/work_packages/" + wpId); }

  public static Map<String, String> md(String raw) {
    if (raw == null || raw.isBlank()) return null;
    return Map.of("format","markdown","raw", raw);
  }
}
