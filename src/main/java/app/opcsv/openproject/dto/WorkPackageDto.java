package app.opcsv.openproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkPackageDto {
  public long id;
  @JsonProperty("lockVersion") public long lockVersion;
  public String subject;

  // 例: external_key を CF1 に置くなら
  @JsonProperty("customField1") public String externalKey;

  public long getId() { return id; }
  public long getLockVersion() { return lockVersion; }
  public String getSubject() { return subject; }
  public String getExternalKey() { return externalKey; }
}
