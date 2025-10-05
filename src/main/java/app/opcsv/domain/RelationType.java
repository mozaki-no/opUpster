package app.opcsv.domain;

import java.util.Arrays;
import java.util.Optional;

public enum RelationType {
  RELATES("relates"),
  BLOCKS("blocks"),
  BLOCKED_BY("blockedBy"),
  PRECEDES("precedes"),
  FOLLOWS("follows");

  public final String apiName;
  RelationType(String apiName){ this.apiName = apiName; }

  public static Optional<RelationType> from(String s){
    if (s == null) return Optional.empty();
    String k = s.trim().toLowerCase();
    return Arrays.stream(values()).filter(t -> t.apiName.equalsIgnoreCase(k)).findFirst();
  }

  public String apiKey() {
	// TODO 自動生成されたメソッド・スタブ
	return null;
}
}
