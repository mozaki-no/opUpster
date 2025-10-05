package app.opcsv.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;

public class CsvReader {
  public List<CsvRecordDto> read(String path) throws IOException { return read(Path.of(path)); }

  public List<CsvRecordDto> read(Path path) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
         CSVParser p = CSVFormat.DEFAULT.builder()
             .setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(br)) {
      List<CsvRecordDto> out = new ArrayList<>();
      for (CSVRecord r : p) {
        out.add(new CsvRecordDto(
            get(r,"externalKey"), get(r,"subject"), get(r,"type"),
            get(r,"description"), get(r,"startDate"), get(r,"dueDate"),
            get(r,"assignee"), get(r,"estimatedHours"), get(r,"parentKey"),
            get(r,"relations")
        ));
      }
      return out;
    }
  }
  private static String get(CSVRecord r, String name){ return r.isMapped(name)? r.get(name): ""; }
}
