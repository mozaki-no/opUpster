package app.opcsv.openproject.dto;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateWorkPackageReq {
  @JsonProperty("lockVersion")
  public long lockVersion;

  public String subject;
  public String description;
  public String startDate;                 // yyyy-MM-dd
  public String dueDate;                   // yyyy-MM-dd
  @JsonProperty("estimatedTime")
  public String estimatedTime;             // ISO8601 Duration
  public Map<String,Object> _links;

  @JsonProperty("customField1")
  public String customField1;
}
