# 本のCRUD操作シナリオ

# 1. 著者を作成（本に紐付けるため）
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "村上春樹",
  "dateOfBirth": "1949-01-12"
}

HTTP 201
[Captures]
author1_id: jsonpath "$.id"


# 2. もう一人著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "東野圭吾",
  "dateOfBirth": "1958-02-04"
}

HTTP 201
[Captures]
author2_id: jsonpath "$.id"


# 3. 本を作成（著者1人）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "ノルウェイの森",
  "price": 1500,
  "publicationStatus": "Published",
  "authorIds": ["{{author1_id}}"]
}

HTTP 201
[Captures]
book1_id: jsonpath "$.id"
[Asserts]
jsonpath "$.title" == "ノルウェイの森"
jsonpath "$.price" == 1500
jsonpath "$.publicationStatus" == "Published"
jsonpath "$.authors" count == 1
jsonpath "$.authors[0].id" == "{{author1_id}}"


# 4. 本を作成（著者2人）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "共著の本",
  "price": 2000,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author1_id}}", "{{author2_id}}"]
}

HTTP 201
[Captures]
book2_id: jsonpath "$.id"
[Asserts]
jsonpath "$.authors" count == 2


# 5. 作成した本を取得
GET http://localhost:8080/api/books/{{book1_id}}

HTTP 200
[Asserts]
jsonpath "$.id" == "{{book1_id}}"
jsonpath "$.title" == "ノルウェイの森"


# 6. 本の情報を更新
PUT http://localhost:8080/api/books/{{book1_id}}
Content-Type: application/json
{
  "title": "ノルウェイの森（上）",
  "price": 1800,
  "publicationStatus": "Published",
  "authorIds": ["{{author1_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.title" == "ノルウェイの森（上）"
jsonpath "$.price" == 1800


# 7. 更新後の本を取得して確認
GET http://localhost:8080/api/books/{{book1_id}}

HTTP 200
[Asserts]
jsonpath "$.title" == "ノルウェイの森（上）"
jsonpath "$.price" == 1800


# 8. 存在しない本を取得（404エラー）
GET http://localhost:8080/api/books/00000000-0000-0000-0000-000000000000

HTTP 404
