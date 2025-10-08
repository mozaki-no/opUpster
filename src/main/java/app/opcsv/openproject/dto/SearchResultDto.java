package app.opcsv.openproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResultDto {
  private int total;
  private int count;
  private int offset;

  @JsonProperty("_embedded")
  private Embedded embedded;

  @Getter @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Embedded {
    @JsonProperty("elements")
    private List<WorkPackageDto> elements;
  }

  /** 便利アクセサ（null安全） */
  public List<WorkPackageDto> embeddedElements() {
    return embedded == null ? null : embedded.getElements();
  }
}
