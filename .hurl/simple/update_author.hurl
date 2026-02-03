# PUT /api/authors/{id} - 著者更新
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "太宰治",
  "dateOfBirth": "1909-06-19"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 著者情報を更新
PUT http://localhost:8080/api/authors/{{author_id}}
Content-Type: application/json
{
  "name": "太宰治（更新後）",
  "dateOfBirth": "1909-06-19"
}

HTTP 200
[Asserts]
jsonpath "$.id" == {{author_id}}
jsonpath "$.name" == "太宰治（更新後）"
