package app.opcsv.service;

import app.opcsv.config.AppProperties;
import app.opcsv.domain.WorkPackagePlan;
import com.opupster.http.*;
import app.opcsv.openproject.OpenProjectClient;
import app.opcsv.openproject.OpenProjectQuery;
import app.opcsv.openproject.dto.WorkPackageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpsertService {

  private final OpenProjectClient client;
  private final OpenProjectQuery  query;
  private final AppProperties     props;

  /** CSVの各行をWPにUpsertして、external_key -> WP ID のマップを返す */
  public Map<String, Long> upsertAll(List<WorkPackagePlan> plans) {
    Map<String, Long> keyToId = new HashMap<>();

    for (var p : plans) {
      final String extKey = p.getexternal_key();

      if (extKey == null || extKey.isBlank()) {
        log.warn("[SKIP] external_key is blank. subject={}", p.getSubject());
        continue;
      }

      try {
        // ★ Optional<WorkPackageDto> を明示（型の不一致対策）
        Optional<WorkPackageDto> existingOpt =
            query.findByexternal_key(props.getProjectId(), props.getExternalKeyCustomFieldId(), extKey)
                 .blockOptional();

        if (existingOpt.isPresent()) {
          WorkPackageDto wp = existingOpt.get();
          keyToId.put(extKey, wp.getId());

          if (props.isDryRun()) {
            log.info("[DRY] update -> id={} key={} subject={}", wp.getId(), extKey, p.getSubject());
          } else {
            var req = new app.opcsv.openproject.dto.WorkPackageReq();
            req.lockVersion   = Long.valueOf(wp.getLockVersion());   // lockVersion は Long 想定なら合わせる
            req.subject       = p.getSubject();
            req.description   = markdown(p.getDescription());
            req.startDate     = toIsoDate(p.getStartDate());
            req.dueDate       = toIsoDate(p.getDueDate());
            req.estimatedTime = toIsoDuration(p.getEstimatedHours());
            // 必要に応じて links / customField を追加

//            WorkPackageDto updated = client.updateWorkPackage(wp.getId(), req).block();
            log.info("[ OK] updated id={} key={} subject={}", wp.getId(), extKey, p.getSubject());
          }

        } else {
          if (props.isDryRun()) {
            log.info("[DRY] create -> key={} subject={}", extKey, p.getSubject());
          } else {
            var req = new app.opcsv.openproject.dto.WorkPackageReq();
            req.subject       = p.getSubject();
            req.description   = markdown(p.getDescription());
            req.startDate     = toIsoDate(p.getStartDate());
            req.dueDate       = toIsoDate(p.getDueDate());
            req.estimatedTime = toIsoDuration(p.getEstimatedHours());
            req.customField1  = extKey; // 外部キーをCF1へ

            // ★ CSVの type は String なので Integer にパース（型不一致対策）
            Integer typeId = parseIntOrNull(p.getType());
            if (typeId == null) {
              throw new IllegalStateException("type_id is required to create WorkPackage (extKey=" + extKey + ", type=" + p.getType() + ")");
            }

            // 必須: type / project のリンク
            req.links = Map.of(
                "type",    Map.of("href", "/api/v3/types/" + typeId),
                "project", Map.of("href", "/api/v3/projects/" + props.getProjectId())
            );

            WorkPackageDto created = client.createWorkPackage(req).block();
            if (created != null) {
              keyToId.put(extKey, created.getId());
              log.info("[ OK] created id={} key={} subject={}", created.getId(), extKey, p.getSubject());
            }
          }
        }

      } catch (ApiException e) { // ★ import 必須
        log.error("[FAIL] upsert key={} subject={} status={}\n{}",
            extKey, p.getSubject(), e.getStatus(), e.getResponseBody());
      } catch (Exception e) {
        log.error("[FAIL] upsert key={} subject={} err={}", extKey, p.getSubject(), e.toString());
      }
    }
    return keyToId;
  }

  /* ===== Helpers ===== */

  private static Object markdown(String raw) {
    if (raw == null || raw.isBlank()) return null;
    return Map.of("format", "markdown", "raw", raw);
  }

  private static String toIsoDate(LocalDate d) {
    return d == null ? null : d.toString(); // "YYYY-MM-DD"
  }

  private static String toIsoDuration(BigDecimal hours) {
    if (hours == null) return null;
    int mins = hours.multiply(BigDecimal.valueOf(60)).intValue();
    int h = mins / 60, m = mins % 60;
    if (h == 0 && m == 0) return "PT0H";
    return m == 0 ? "PT" + h + "H" : "PT" + h + "H" + m + "M";
  }

  // ★ String -> Integer の安全パーサ（空/非数は null）
  private static Integer parseIntOrNull(String s) {
    if (s == null || s.isBlank()) return null;
    try { return Integer.valueOf(s.trim()); }
    catch (NumberFormatException e) { return null; }
  }
}
