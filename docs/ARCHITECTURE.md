# opUpster Architecture

## 目的
CSV から OpenProject の Work Package を **安全に作成/更新/関連付け（upsert）** する CLI/Batch。

## 構成
- **CLI**: `app.opcsv.cli.*` … 引数解析・dry-run 制御・起動ガード
- **Service**: `app.opcsv.service.*` … Upsert / Purge / ParentLink
- **Infra**: `app.opcsv.openproject.*`（API） / `app.opcsv.csv.*`（CSV）
- **DTO**: `app.opcsv.openproject.dto.*`
- **DryRun**: `app.opcsv.dryrun.*` … JSONL 出力

```mermaid
flowchart LR
  CLI[UpsertCommand] --> SRV[UpsertService / PurgeService / ParentLinkService]
  SRV --> CSV[CsvReader]
  SRV --> DRY[DryRunWriter (JSONL)]
  SRV --> W[WebClient]
  W -->|GET/POST/PATCH| OP[(OpenProject API)]
```

## データフロー（標準）
1. CLI 起動 → `csv.path` / `dryRun` などの設定を解決
2. `CsvReader.read()` が行データ（List<Map>）を返す
3. `UpsertService` が各行を検索→作成/更新→関連付け
4. `--dryRun=true` のときは API を呼ばず `dry-run-output.jsonl` を出力して終了

## 非機能（現状）
- 冪等性: `external_key` をキーに upsert
- 安全性: dry-run / CSV 存在チェック
- 例外: URI 生成・filters の扱いは API-INTEGRATION を参照
