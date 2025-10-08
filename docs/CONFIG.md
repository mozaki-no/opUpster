# Config

| Key | 意味 | 例 | 必須 |
|-----|------|----|------|
| openproject.base-url | API ベース URL | https://op.example.com | ✅ |
| openproject.token    | PAT（Bearer） | 環境 or properties | ✅ |
| csv.path             | 入力 CSV パス | .\up.csv / C:\temp\up.csv | ✅ |
| pageSize             | 1ページ件数 | 200 | 任意 |

## 指定方法（例）
- `application.properties` / 環境変数 / CLI 引数
- `java -jar ... --csv.path=.\up.csv --dryRun=true`
