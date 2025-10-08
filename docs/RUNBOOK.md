# Runbook

## 前提
- Java 17 / Maven
- OpenProject PAT を用意（環境変数 or `application.properties`）

## ビルド＆実行（Windows PowerShell）
```powershell
# ビルド
.\mvnw.cmd -DskipTests package

# ドライラン（APIを呼ばず JSONL 出力）
java -jar target\op-csv-upserter-0.0.1-SNAPSHOT.jar --csv.path=.\up.csv --dryRun=true

# 本実行
java -jar target\op-csv-upserter-0.0.1-SNAPSHOT.jar --csv.path=.\up.csv
```

## 生成物
- `dry-run-output.jsonl` … 1 行 1 JSON（外部キー / アクション / 行データ）

## よくあるエラー
- CSV が見つからない → `--csv.path` を指定 / パス確認
- expand 'project' → filters の JSON とエンコードを見直す（API-INTEGRATION 参照）
