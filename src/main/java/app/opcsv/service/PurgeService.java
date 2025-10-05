package app.opcsv.service;


import org.springframework.stereotype.Service;

import app.opcsv.config.AppProperties;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.OpenProjectQuery;
import reactor.core.publisher.Mono;

@Service
public class PurgeService {
  private final OpenProjectQuery query;
  private final OpenProjectClient client;
  private final AppProperties props;

  public PurgeService(OpenProjectQuery query, OpenProjectClient client, AppProperties props) {
    this.query = query; this.client = client; this.props = props;
  }

  public void purgeAbsentKeys(java.util.Set<String> keysInCsv) {
    var all = query.listAllByProject(props.getProjectId()).block(); // List<WorkPackageDto>（最小）
    if (all == null) return;

    var candidates = all.stream()
      // ここで必要なら対象TypeやStatusで絞る
      // 例: 検索でCFが取れてるなら keysInCsv に無いものだけ
      .filter(wp -> {/* TODO: external_key を参照。無ければ別途取得 or Dto拡張 */ return true; })
      .toList();

    if (props.isDryRun()) {
      System.out.println("[DRY] purge candidates: " + candidates.size());
    } else {
      for (var wp : candidates) {
        client.deleteWorkPackage(wp.getId()).onErrorResume(e -> {
          System.err.println("[WARN] delete failed id=" + wp.getId() + " : " + e.getMessage());
          return Mono.empty();
        }).block();
      }
    }
  }
}
