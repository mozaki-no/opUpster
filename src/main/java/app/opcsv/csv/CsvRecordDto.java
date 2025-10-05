package app.opcsv.csv;

public record CsvRecordDto(
    String externalKey,
    String subject,
    String type,
    String description,
    String startDate,
    String dueDate,
    String assignee,
    String estimatedHours,
    String parentKey,
    String relations
) {}
