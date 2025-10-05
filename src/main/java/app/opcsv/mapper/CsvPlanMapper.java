package app.opcsv.mapper;

import app.opcsv.csv.CsvRecordDto;
import app.opcsv.domain.RelationSpec;
import app.opcsv.domain.RelationType;
import app.opcsv.domain.WorkPackagePlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvPlanMapper {

  private static final DateTimeFormatter[] DF = new DateTimeFormatter[] {
      DateTimeFormatter.ISO_LOCAL_DATE,
      DateTimeFormatter.ofPattern("yyyy/M/d"),
      DateTimeFormatter.ofPattern("M/d/yyyy")
  };

  public static WorkPackagePlan toPlan(CsvRecordDto r) {
    return new WorkPackagePlan(
        nz(r.externalKey()), nz(r.subject()), nz(r.type()), nz(r.description()),
        parseDate(r.startDate()), parseDate(r.dueDate()),
        nz(r.assignee()), parseHours(r.estimatedHours()),
        nz(r.parentKey()), parseRelations(r.relations())
    );
  }

  private static String nz(String s){ return s==null? "" : s.trim(); }
  private static LocalDate parseDate(String s){
    if (s==null || s.isBlank()) return null;
    for (var f: DF) try { return LocalDate.parse(s.trim(), f); } catch (Exception ignore) {}
    throw new IllegalArgumentException("Invalid date: "+s);
  }
  private static BigDecimal parseHours(String s){
    if (s==null || s.isBlank()) return null;
    var t = s.toLowerCase().replace("hours","").replace("hour","").replace("h","").trim();
    return new BigDecimal(t);
  }
  private static List<RelationSpec> parseRelations(String s){
    if (s==null || s.isBlank()) return List.of();
    List<RelationSpec> out = new ArrayList<>();
    for (String token: s.split(";")){
      var part = token.trim(); if (part.isEmpty()) continue;
      int i = part.lastIndexOf(':');
      if (i<=0 || i==part.length()-1) throw new IllegalArgumentException("Invalid relation token: "+part);
      var toKey = part.substring(0,i).trim();
      var type  = part.substring(i+1).trim();
      var rt = RelationType.from(type)
          .orElseThrow(() -> new IllegalArgumentException("Unsupported relation type: "+type));
      out.add(new RelationSpec(toKey, rt));           // ★ 第二引数は RelationType
    }
    return out.stream().distinct().toList();
  }
}
