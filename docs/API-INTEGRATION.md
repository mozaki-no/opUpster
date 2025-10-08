# OpenProject API Integration

## ベース
- Base URL: `openproject.base-url`
- 認証: Personal Access Token（`Authorization: Bearer <token>`）
- HTTP クライアント: Spring WebClient

## 代表的エンドポイント
- `GET /api/v3/work_packages?filters=<json>&pageSize=&offset=`
- `POST /api/v3/work_packages`
- `PATCH /api/v3/work_packages/{id}`（`lockVersion` 必須）

## filters JSON（正しい例）
```json
[
  {"project":{"operator":"=","values":["<PROJECT_ID>"]}},
  {"customField<CF_ID>":{"operator":"=","values":["<EXTERNAL_KEY>"]}}
]
```

## URI/エンコードの扱い（現状の指針）
- 文字列テンプレ `{project}` をクエリ値に含めない（展開エラーの原因）
- 方法A: `queryParam("filters", 生JSON)` + `.build()`（WebClient に 1 回だけ任せる）
- 方法B: 1 回だけ自前で URL エンコード → 完成済み `URI` を `.uri(URI)` に渡す

## 例外の例
- `Not enough variable values to expand 'project'` … クエリ値に `{}` が残っている
- `400 Bad Request`（filters invalid） … JSON キーにダブルクォートが無い、または二重エンコード（`%255B`）
