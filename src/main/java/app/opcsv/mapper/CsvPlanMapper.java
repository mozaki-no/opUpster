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
        nz(r.external_key()), nz(r.subject()), nz(r.type()), nz(r.description()),
        parseDate(r.startDate()), parseDate(r.dueDate()),
        nz(r.assignee()), parseHours(r.estimatedHours()),
        nz(r.parent_key()), parseRelations(r.relations())
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
	  if (s == null || s.isBlank()) return List.of();

	  List<RelationSpec> out = new ArrayList<>();

	  // 区切り: カンマ/セミコロン/全角読点
	  for (String raw : s.split("[;,、]")) {
	    final String part = dequote(raw.trim());      // 余計な引用符を除去
	    if (part.isEmpty()) continue;

	    String toKey;
	    String rawType;

	    // 「A-002:blocks」 or 「blocks:A-002」 or 「A-002」
	    String[] pieces = part.split("[:：]", 2);
	    if (pieces.length == 1) {
	      // 種別省略は relates
	      toKey = pieces[0].trim();
	      rawType = "relates";
	    } else {
	      final String left  = pieces[0].trim();
	      final String right = pieces[1].trim();

	      // どっちがtypeかを判定（同義語も normalize して照合）
	      boolean leftIsType  = isTypeToken(left);
	      boolean rightIsType = isTypeToken(right);

	      if (leftIsType && !rightIsType) {
	        // type:key
	        rawType = left;
	        toKey   = right;
	      } else if (!leftIsType && rightIsType) {
	        // key:type
	        toKey   = left;
	        rawType = right;
	      } else if (!leftIsType && !rightIsType) {
	        // どちらもtypeっぽくない：安全側に toKey=左、type=relates
	        toKey   = left;
	        rawType = "relates";
	      } else {
	        // どちらもtypeっぽい（レアケース）：左をkey、右をtypeにする
	        toKey   = left;
	        rawType = right;
	      }
	    }

	    if (toKey.isEmpty()) continue;

	    final String typeKey = normalizeRelation(rawType);
	    var rtOpt = RelationType.from(typeKey);
	    if (rtOpt.isEmpty()) {
	      throw new IllegalArgumentException("Unsupported relation type: " + rawType);
	    }
	    out.add(new RelationSpec(toKey, rtOpt.get()));
	  }
	  return out.stream().distinct().toList();
	}

	private static boolean isTypeToken(String t){
	  if (t == null || t.isBlank()) return false;
	  String k = normalizeRelation(t);
	  return k.equals("relates") || k.equals("blocks") || k.equals("blockedBy")
	      || k.equals("precedes") || k.equals("follows");
	}

	private static String dequote(String s){
	  if (s == null) return null;
	  String x = s.trim();
	  if ((x.startsWith("\"") && x.endsWith("\"")) || (x.startsWith("'") && x.endsWith("'"))) {
	    return x.substring(1, x.length()-1).trim();
	  }
	  return x;
	}

	private static String normalizeRelation(String s){
	  String k = s.toLowerCase();
	  return switch (k) {
	    case "rel", "related", "relation", "=", "＝" -> "relates";
	    case "block", "blocks", "b"                  -> "blocks";
	    case "blockedby", "blocked_by", "blocked-by","bb" -> "blockedBy";
	    case "precedes", "before", "p"               -> "precedes";
	    case "follows", "after", "f"                 -> "follows";
	    default -> k;
	  };
	}

}