package app.opcsv.domain;


public enum RelationType {
  RELATES("relates"),
  BLOCKS("blocks"),
  BLOCKED_BY("blockedBy"),
  PRECEDES("precedes"),
  FOLLOWS("follows");

  private final String apiKey;
  RelationType(String apiKey){ this.apiKey = apiKey; }
  public String apiKey(){ return apiKey; }

  public String typeHref(){
    return "/api/v3/relations/types/" + apiKey;
  }

  // ★追加：CsvPlanMapper から呼ばれているユーティリティ
  public static java.util.Optional<RelationType> from(String key){
    if (key == null) return java.util.Optional.empty();
    String k = key.trim().toLowerCase();
    switch (k){
      case "rel", "related", "relation", "=", "＝", "relates": return java.util.Optional.of(RELATES);
      case "block", "blocks", "b":                              return java.util.Optional.of(BLOCKS);
      case "blockedby", "blocked_by", "blocked-by", "bb": return java.util.Optional.of(BLOCKED_BY);
      case "precedes", "before", "p":                           return java.util.Optional.of(PRECEDES);
      case "follows", "after", "f":                             return java.util.Optional.of(FOLLOWS);
      default:
        // 公式キーそのままのとき
        for (var t: values()) if (t.apiKey.equals(k)) return java.util.Optional.of(t);
        return java.util.Optional.empty();
    }
  }
}
