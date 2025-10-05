package app.opcsv.openproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkPackageDto {
  public long id;
  @JsonProperty("lockVersion") public Long lockVersion;
  public String subject;

  // external_key を CF1 に置いている前提
  @JsonProperty("customField1") public String external_key;

  public long getId() { return id; }
  public Long getLockVersion() { return lockVersion; } 
  public String getSubject() { return subject; }
  public String getexternal_key() { return external_key; }
}
