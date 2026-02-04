# GET /api/authors/{id}/books - 著者の書籍一覧取得
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "川端康成",
  "dateOfBirth": "1899-06-14"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 書籍を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "雪国",
  "price": 600,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201

# 著者の書籍一覧を取得
GET http://localhost:8080/api/authors/{{author_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 1
jsonpath "$[0].title" == "雪国"
