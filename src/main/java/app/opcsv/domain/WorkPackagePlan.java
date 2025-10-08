package app.opcsv.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class WorkPackagePlan {

  // 既存CSVの列名に合わせてスネークケースを維持
  private final String external_key;      // 必須（空ならSKIP）
  private final String subject;
  private final String type;              // 例: "Epic", "Feature", "Task"...
  private final String description;
  private final LocalDate startDate;      // null許容
  private final LocalDate dueDate;        // null許容
  private final String assignee;          // ユーザー名/IDなど（解決は使用側）
  private final BigDecimal estimatedHours;// null許容
  private final String parent_key;        // 親のexternal_key（null/空OK）

  @Singular("relation")
  private final List<RelationSpec> relations; // null/空OK

  // --- 互換ゲッター（既存呼び出しを壊さないために残す） ---
  public String getexternal_key() { return external_key; }
  public String getparent_key()   { return parent_key; }
}
