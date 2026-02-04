# PUT /api/books/{id} - 書籍更新
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "宮沢賢治",
  "dateOfBirth": "1896-08-27"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 書籍を作成（未出版）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "銀河鉄道の夜",
  "price": 500,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
book_id: jsonpath "$.id"

# 書籍を更新（出版済みに変更）
PUT http://localhost:8080/api/books/{{book_id}}
Content-Type: application/json
{
  "title": "銀河鉄道の夜（改訂版）",
  "price": 700,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.id" == {{book_id}}
jsonpath "$.title" == "銀河鉄道の夜（改訂版）"
jsonpath "$.price" == 700
jsonpath "$.publicationStatus" == "PUBLISHED"
