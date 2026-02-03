# GET /api/books/{id} - 書籍取得
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "谷崎潤一郎",
  "dateOfBirth": "1886-07-24"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 書籍を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "細雪",
  "price": 1200,
  "publicationStatus": "Published",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
book_id: jsonpath "$.id"

# 作成した書籍を取得
GET http://localhost:8080/api/books/{{book_id}}

HTTP 200
[Asserts]
jsonpath "$.id" == {{book_id}}
jsonpath "$.title" == "細雪"
jsonpath "$.price" == 1200
jsonpath "$.publicationStatus" == "Published"
jsonpath "$.authors[0].name" == "谷崎潤一郎"
