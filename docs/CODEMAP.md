# Code Map

## パッケージ
- `app.opcsv.cli` … CLI 起動、引数解析、dry-run ガード
- `app.opcsv.csv` … CSV 読み込み（存在チェック、UTF-8）
- `app.opcsv.service` … Upsert / Purge / ParentLink のユースケース
- `app.opcsv.openproject` … OpenProject API 連携（検索・ページング・更新）
- `app.opcsv.openproject.dto` … API DTO
- `app.opcsv.dryrun` … `DryRunWriter`（JSON Lines 出力）

## 主要クラスの I/O
- `UpsertCommand` … in: CLI 引数 / out: 実行ログ & JSONL(dry-run)
- `CsvReader` …… in: `csv.path` / out: `List<Map<String,String>>`
- `OpenProjectQuery` … in: filters JSON / offset / out: `SearchResultDto`
- `UpsertService` … in: CSV 1行 / out: WorkPackage 作成/更新/関連
