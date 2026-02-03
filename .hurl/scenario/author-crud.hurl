# 著者のCRUD操作シナリオ

# 1. 著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "夏目漱石",
  "dateOfBirth": "1867-02-09"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"
[Asserts]
jsonpath "$.name" == "夏目漱石"
jsonpath "$.dateOfBirth" == "1867-02-09"
jsonpath "$.id" exists
jsonpath "$.createdAt" exists
jsonpath "$.updatedAt" exists


# 2. 作成した著者を取得
GET http://localhost:8080/api/authors/{{author_id}}

HTTP 200
[Asserts]
jsonpath "$.id" == "{{author_id}}"
jsonpath "$.name" == "夏目漱石"
jsonpath "$.dateOfBirth" == "1867-02-09"


# 3. 著者情報を更新
PUT http://localhost:8080/api/authors/{{author_id}}
Content-Type: application/json
{
  "name": "夏目漱石（なつめそうせき）",
  "dateOfBirth": "1867-02-09"
}

HTTP 200
[Asserts]
jsonpath "$.id" == "{{author_id}}"
jsonpath "$.name" == "夏目漱石（なつめそうせき）"
jsonpath "$.dateOfBirth" == "1867-02-09"


# 4. 更新後の著者を取得して確認
GET http://localhost:8080/api/authors/{{author_id}}

HTTP 200
[Asserts]
jsonpath "$.name" == "夏目漱石（なつめそうせき）"


# 5. 存在しない著者を取得（404エラー）
GET http://localhost:8080/api/authors/00000000-0000-0000-0000-000000000000

HTTP 404
