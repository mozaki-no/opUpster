package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.OpenProjectQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParentLinkService {
  private final OpenProjectClient client;
  private final OpenProjectQuery  query;
  private final AppProperties     props;

  public ParentLinkService(OpenProjectClient client, OpenProjectQuery query, AppProperties props) {
    this.client = client; this.query = query; this.props = props;
  }

  public void linkParents(List<WorkPackagePlan> plans, Map<String, Long> keyToId) {
    for (var p : plans) {
      String parentKey = p.parentExternalKey();
      if (parentKey == null || parentKey.isBlank()) continue;

      Long childId  = keyToId.get(p.externalKey());
      Long parentId = keyToId.get(parentKey);
      if (childId == null || parentId == null) continue;

      var child = query.findByExternalKey(props.getProjectId(),
          props.getExternalKeyCustomFieldId(), p.externalKey()).block();
      if (child == null) continue;
      if (!props.isDryRun()) {
        client.setParent(childId, child.getLockVersion(), parentId).block();
      }
    }
  }
}
