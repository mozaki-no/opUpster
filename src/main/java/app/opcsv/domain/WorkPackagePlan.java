package app.opcsv.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkPackagePlan(
    String external_key,
    String subject,
    String type,
    String description,
    LocalDate startDate,
    LocalDate dueDate,
    String assignee,
    BigDecimal estimatedHours,
    String parentexternal_key,
    List<RelationSpec> relationSpecs
) {}
