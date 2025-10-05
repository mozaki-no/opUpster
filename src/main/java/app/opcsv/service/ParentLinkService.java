package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.dto.WorkPackageDto;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParentLinkService {
  private final OpenProjectClient client;
  private final AppProperties props;

  public ParentLinkService(OpenProjectClient client, AppProperties props) {
    this.client = client;
    this.props = props;
  }

  public void linkParents(List<WorkPackagePlan> plans, Map<String, Long> keyToId) {
    for (var p : plans) {
      var parent_key = p.parentexternal_key();
      if (parent_key == null || parent_key.isBlank()) continue;

      Long childId  = keyToId.get(p.external_key());
      Long parentId = keyToId.get(parent_key);
      if (childId == null || parentId == null) continue;

      if (props.isDryRun()) {
        System.out.printf("[DRY] parent link: %s(%d) -> %s(%d)%n",
            p.external_key(), childId, parent_key, parentId);
        continue;
      }

      // lockVersion を取る
      WorkPackageDto child = client.getWorkPackage(childId).block();
      if (child == null || child.getLockVersion() == null) {
        System.out.printf("[WARN] cannot fetch child or lockVersion. id=%d%n", childId);
        continue;
      }

      client.setParent(childId, child.getLockVersion(), parentId).block();
      System.out.printf("[OK ] parent linked: child=%d -> parent=%d (lv=%d)%n",
          childId, parentId, child.getLockVersion());
    }
  }
}
