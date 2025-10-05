package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.OpenProjectClient.CreateWorkPackageReq;
import app.opcsv.openproject.OpenProjectClient.UpdateWorkPackageReq;
import app.opcsv.openproject.OpenProjectQuery;
import app.opcsv.openproject.dto.WorkPackageDto;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UpsertService {
  private final OpenProjectClient client;
  private final OpenProjectQuery  query;
  private final AppProperties     props;

  public UpsertService(OpenProjectClient client, OpenProjectQuery query, AppProperties props) {
    this.client = client; this.query = query; this.props = props;
  }

  public Map<String, Long> upsertAll(List<WorkPackagePlan> plans) {
    Map<String, Long> keyToId = new HashMap<>();
    for (var p : plans) {
	  if (p.external_key() == null || p.external_key().isBlank()) {
		    System.out.println("[SKIP] external_key is blank. subject=" + p.subject());
		    continue;
	  }
      var existingOpt = query.findByexternal_key(props.getProjectId(),
          props.getexternal_keyCustomFieldId(), p.external_key()).blockOptional();

      if (existingOpt.isPresent()) {
        WorkPackageDto wp = (WorkPackageDto) existingOpt.get();
        keyToId.put(p.external_key(), wp.getId());
        if (!props.isDryRun()) {
          var req = new UpdateWorkPackageReq();
          req.lockVersion  = wp.getLockVersion();
          req.subject      = p.subject();
          req.description  = p.description();
          req.startDate    = toIsoDate(p.startDate());
          req.dueDate      = toIsoDate(p.dueDate());
          req.estimatedTime= toIsoDuration(p.estimatedHours());
          client.updateWorkPackage(wp.getId(), req).block();
        }
      } else {
        if (!props.isDryRun()) {
          var req = new CreateWorkPackageReq();
          req.subject       = p.subject();
          req.description   = p.description();
          req.startDate     = toIsoDate(p.startDate());
          req.dueDate       = toIsoDate(p.dueDate());
          req.estimatedTime = toIsoDuration(p.estimatedHours());
          req._links = Map.of("project", Map.of("href", "/api/v3/projects/" + props.getProjectId()));
          req.customField1  = p.external_key(); // external_key をCF1へ
          var created = client.createWorkPackage(req).block();
          if (created != null) keyToId.put(p.external_key(), created.getId());
        }
      }
    }
    return keyToId;
  }

  private static String toIsoDate(java.time.LocalDate d){ return d==null? null : d.toString(); }
  private static String toIsoDuration(java.math.BigDecimal hours){
    if (hours == null) return null;
    int mins = hours.multiply(java.math.BigDecimal.valueOf(60)).intValue();
    int h = mins/60, m = mins%60;
    return m==0 ? "PT"+h+"H" : "PT"+h+"H"+m+"M";
  }
}
