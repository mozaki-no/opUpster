package app.opcsv.domain;

import java.util.Objects;

public class RelationSpec {

  private final String toKey;     // 相手（external_key）
  private final RelationType type;

  public RelationSpec(String toKey, RelationType type) {
    this.toKey = toKey;
    this.type = type;
  }

  public String getToKey() { return toKey; }
  public RelationType getType() { return type; }

  // 重複排除用（toKey + type が同じなら同一とみなす）
  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelationSpec)) return false;
    RelationSpec that = (RelationSpec) o;
    return Objects.equals(toKey, that.toKey) && type == that.type;
  }
  @Override public int hashCode() { return Objects.hash(toKey, type); }

  @Override public String toString() {
    return "RelationSpec{toKey='" + toKey + "', type=" + type + '}';
  }
}
