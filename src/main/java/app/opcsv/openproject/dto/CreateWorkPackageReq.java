package app.opcsv.openproject.dto;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateWorkPackageReq {
  public String subject;
  public String description;
  public String startDate;                 // yyyy-MM-dd
  public String dueDate;                   // yyyy-MM-dd
  @JsonProperty("estimatedTime")
  public String estimatedTime;             // ISO8601 Duration (例: "PT7H30M")
  public Map<String,Object> _links;

  // external_key を CF1 に保存するなら
  @JsonProperty("customField1")
  public String customField1;
}
