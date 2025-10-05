package app.opcsv.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public class WorkPackagePlan {

  private final String external_key;   // 必須（空ならSKIP）
  private final String subject;
  private final String type;          // OpenProjectのタイプ名（例: "Epic", "Feature", "Task"...）
  private final String description;
  private final LocalDate startDate;  // null許容
  private final LocalDate dueDate;    // null許容
  private final String assignee;      // ユーザー名/IDなど。使用側で解決する想定。null/空OK
  private final BigDecimal estimatedHours; // null許容
  private final String parent_key;     // 親もexternal_keyで参照。null/空OK
  private final List<RelationSpec> relations; // null/空OK

  public WorkPackagePlan(
      String external_key,
      String subject,
      String type,
      String description,
      LocalDate startDate,
      LocalDate dueDate,
      String assignee,
      BigDecimal estimatedHours,
      String parent_key,
      List<RelationSpec> relations
  ) {
    this.external_key = external_key;
    this.subject = subject;
    this.type = type;
    this.description = description;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.assignee = assignee;
    this.estimatedHours = estimatedHours;
    this.parent_key = parent_key;
    this.relations = relations;
  }

  public String getexternal_key() { return external_key; }
  public String getSubject() { return subject; }
  public String getType() { return type; }
  public String getDescription() { return description; }
  public LocalDate getStartDate() { return startDate; }
  public LocalDate getDueDate() { return dueDate; }
  public String getAssignee() { return assignee; }
  public BigDecimal getEstimatedHours() { return estimatedHours; }
  public String getparent_key() { return parent_key; }
  public List<RelationSpec> getRelations() { return relations; }
}

