package app.opcsv.csv;

public record CsvRecordDto(
    String external_key,
    String subject,
    String type,
    String description,
    String startDate,
    String dueDate,
    String assignee,
    String estimatedHours,
    String parent_key,
    String relations
) {}
