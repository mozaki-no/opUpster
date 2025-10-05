package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RelationService {
  private final OpenProjectClient client;
  private final AppProperties props;
  public RelationService(OpenProjectClient client, AppProperties props) { this.client = client; this.props = props; }

  public void applyRelations(List<WorkPackagePlan> plans, Map<String, Long> keyToId) {
    for (var p : plans) {
      for (var rel : p.relationSpecs()) {
        var fromId = keyToId.get(p.externalKey());
        var toId   = keyToId.get(rel.toExternalKey());
        if (fromId == null || toId == null) continue;
        if (!props.isDryRun()) client.createRelation(fromId, toId, rel.type().apiName).block();
      }
    }
  }
}
