package app.opcsv.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;

@Component
public class CsvReader {
  public List<CsvRecordDto> read(String path) throws IOException { return read(Path.of(path)); }

  public List<CsvRecordDto> read(Path path) throws IOException {
	  try (InputStream in = Files.newInputStream(path);
			     BOMInputStream bomIn = BOMInputStream.builder()
			         .setInputStream(in)       // 必須
			         .setInclude(false)        // BOM は読み飛ばす（文字として返さない）
			         .get();
			     InputStreamReader reader = new InputStreamReader(bomIn, StandardCharsets.UTF_8);
         CSVParser p = CSVFormat.DEFAULT.builder()
             .setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(reader)) {
      List<CsvRecordDto> out = new ArrayList<>();
      for (CSVRecord r : p) {
    	  var dto = new CsvRecordDto(
    	      get(r,"external_key","external_key"), get(r,"subject"), get(r,"type"),
    	      get(r,"description"), get(r,"startDate"), get(r,"dueDate"),
    	      get(r,"assignee"), get(r,"estimatedHours"), get(r,"parent_key","parent_key"),
    	      get(r,"relations")
    	  );
    	  if (dto.external_key() == null || dto.external_key().isBlank()) {
    	    System.out.printf("[SKIP] blank external_key. subject=%s%n", dto.subject());
    	    continue;
    	  }
    	  out.add(dto);
    	}
      return out;
    }
  }
  private static String get(CSVRecord r, String... names) {
	  for (String n : names) {
	    if (r.isMapped(n)) return r.get(n);
	    // 先頭BOM付きヘッダーにも対応
	    String bomN = "\uFEFF" + n;
	    if (r.isMapped(bomN)) return r.get(bomN);
	  }
	  return "";
	}

}
