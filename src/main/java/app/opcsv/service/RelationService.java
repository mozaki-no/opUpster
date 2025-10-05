package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.openproject.OpenProjectClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RelationService {
  private final OpenProjectClient client;
  public RelationService(OpenProjectClient client, AppProperties props) { this.client = client;}

  public Mono<Void> applyRelations(List<WorkPackagePlan> plans, Map<String, Long> keyToId, boolean dryRun) {
	  Set<String> sent = new HashSet<>();

	  List<Mono<Void>> calls = new ArrayList<>();
	  for (var p : plans) {
	    if (p.getRelations() == null) continue;

	    for (var rel : p.getRelations()) {
	      Long fromId = keyToId.get(p.getexternal_key());
	      Long toId   = keyToId.get(rel.getToKey());
	      if (fromId == null || toId == null) continue;

	      String typeKey = rel.getType().apiKey(); // 例: RelationType が "relates" 等を返す想定
	      String dedup = fromId + "->" + toId + ":" + typeKey;
	      if (!sent.add(dedup)) continue;

	      if (dryRun) {
	        System.out.printf("[DRY] relate %d -> %d (%s)%n", fromId, toId, typeKey);
	      } else {
	        calls.add(client.createRelation(fromId, toId, typeKey)
	            .doOnSuccess(v -> System.out.printf("[OK ] related %d -> %d (%s)%n", fromId, toId, typeKey)));
	      }
	    }
	  }
	  return Flux.concat(calls).then();
	}

  
}
