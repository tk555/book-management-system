# GET /api/authors/{id} - 著者取得
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "芥川龍之介",
  "dateOfBirth": "1892-03-01"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 作成した著者を取得
GET http://localhost:8080/api/authors/{{author_id}}

HTTP 200
[Asserts]
jsonpath "$.id" == {{author_id}}
jsonpath "$.name" == "芥川龍之介"
jsonpath "$.dateOfBirth" == "1892-03-01"
