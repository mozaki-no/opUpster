package app.opcsv.openproject.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 使っていないフィールドは無視
public class WorkPackageDto {

  /** /api/v3/work_packages/{id} の {id} */
  private Long id;

  /** 楽観ロック用。更新(PATCH)時に必須 */
  @JsonProperty("lockVersion")
  private Integer lockVersion;

  private String subject;

  /** "YYYY-MM-DD" */
  private String startDate;

  /** "YYYY-MM-DD" */
  private String dueDate;

  /** ISO8601 duration: "PT8H", "PT1H30M" など */
  @JsonProperty("estimatedTime")
  private String estimatedTime;

  /** 説明（markdown/raw/html をOpenProjectが返す場合あり） */
  private Description description;

  /** 作成/更新日時（参照用） */
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  /** カスタムフィールド例：external_key を CF1 に入れている場合 */
  @JsonProperty("customField1")
  private String customField1;

  /** HALリンク */
  @JsonProperty("_links")
  private Links links;

  /** 必要になったら後で引くための置き場（今は使わない） */
  @JsonProperty("_embedded")
  private Map<String, Object> embedded;

  /* ----------------- nested DTOs ----------------- */

  @Getter @Setter @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Description {
    private String format;  // "markdown" など
    private String raw;
    private String html;    // サーバ側でレンダされたHTMLが来ることもある
  }

  @Getter @Setter @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Links {
    private Href self;
    private Href type;
    private Href project;
    private Href parent;
    private Href assignee;
    private Href status;
    // ほかにも多数あるが、使うまで定義不要
  }

  @Getter @Setter @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Href {
    private String href;   // 例: "/api/v3/work_packages/123"
    private String title;  // 例: "Task"
  }

  /* ----------------- helpers ----------------- */

  @JsonIgnore
  private static final Pattern ID_PATTERN = Pattern.compile(".*/(\\d+)$");

  @JsonIgnore
  public Long getTypeId()      { return extractId(links != null ? links.getType()    : null); }

  @JsonIgnore
  public Long getProjectId()   { return extractId(links != null ? links.getProject() : null); }

  @JsonIgnore
  public Long getParentId()    { return extractId(links != null ? links.getParent()  : null); }

  @JsonIgnore
  public Long getAssigneeId()  { return extractId(links != null ? links.getAssignee(): null); }

  @JsonIgnore
  public Long getStatusId()    { return extractId(links != null ? links.getStatus()  : null); }

  @JsonIgnore
  private static Long extractId(Href h) {
    if (h == null || h.getHref() == null) return null;
    Matcher m = ID_PATTERN.matcher(h.getHref());
    return m.find() ? Long.valueOf(m.group(1)) : null;
  }
}
