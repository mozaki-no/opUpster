package app.opcsv.cli;

import app.opcsv.config.AppProperties;
import app.opcsv.csv.CsvReader;
//import app.opcsv.mapper.CsvPlanMapper;
import app.opcsv.domain.WorkPackagePlan;
import app.opcsv.service.ParentLinkService;
import app.opcsv.service.PurgeService;
import app.opcsv.service.RelationService;
import app.opcsv.service.UpsertService;

import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer; 

@Component
public class UpsertCommand {

  private final AppProperties props;
  private final ApplicationArguments appArgs;
  private final CsvReader csvReader;
  private final UpsertService upsertService;
  private final ParentLinkService parentLinkService;
  private final RelationService relationService;
  private final PurgeService purgeService;

  public UpsertCommand(
      AppProperties props,
      ApplicationArguments appArgs,
      CsvReader csvReader,
      UpsertService upsertService,
      ParentLinkService parentLinkService,
      RelationService relationService,
      PurgeService purgeService) {
    this.props = props;
    this.appArgs = appArgs;
    this.csvReader = csvReader;
    this.upsertService = upsertService;
    this.parentLinkService = parentLinkService;
    this.relationService = relationService;
    this.purgeService = purgeService;
  }

  public void execute(String... args) throws Exception {
    // === ここでCLI引数があれば properties を上書きする ===
    overrideIfPresent("project-id", v -> props.setProjectId(Integer.parseInt(v)));
    overrideIfPresent("csv-path", props::setCsvPath);
    overrideIfPresent("base-url", props::setBaseUrl);
    overrideIfPresent("external-key-custom-field-id", v -> props.setExternalKeyCustomFieldId(Integer.parseInt(v)));
    overrideIfPresent("dry-run", v -> props.setDryRun(Boolean.parseBoolean(v)));
    // proxy系（必要なら）
    overrideIfPresent("proxy.enabled", v -> props.getProxy().setEnabled(Boolean.parseBoolean(v)));
    overrideIfPresent("proxy.host", v -> props.getProxy().setHost(v));
    overrideIfPresent("proxy.port", v -> props.getProxy().setPort(Integer.parseInt(v)));
    overrideIfPresent("proxy.username", v -> props.getProxy().setUsername(v));
    overrideIfPresent("proxy.password", v -> props.getProxy().setPassword(v));
    overrideIfPresent("proxy.non-proxy-hosts", v -> props.getProxy().setNonProxyHosts(v));
    
    if (props.getCsvPath() == null || props.getCsvPath().isBlank()) {
      System.err.println("csvPath が未指定です。--app.csv-path=/path/to/file.csv を指定してください。");
      return;
    }
    System.out.printf("Start upsert (projectId=%d, dryRun=%s)%n", props.getProjectId(), props.isDryRun());

    var rows  = csvReader.read(props.getCsvPath());
    var plans = rows.stream().map(app.opcsv.mapper.CsvPlanMapper::toPlan).toList();


    Map<String, Long> keyToId = upsertService.upsertAll(plans);
    parentLinkService.linkParents(keyToId, plans);
    relationService.applyRelations(plans, keyToId,props.isDryRun());

    Set<String> csvKeys = plans.stream().map(WorkPackagePlan::getexternal_key).collect(Collectors.toSet());
    purgeService.purgeAbsentKeys(csvKeys);

    System.out.println("Done.");
  }

  private void overrideIfPresent(String opt, Consumer<String> setter) {
    if (appArgs.containsOption(opt)) {
      var list = appArgs.getOptionValues(opt);
      if (list != null && !list.isEmpty()) {
        setter.accept(list.get(0));
      }
    }
  }
}
