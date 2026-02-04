# GET /api/books/search - 書籍検索
# 事前にテストデータを作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "森鷗外",
  "dateOfBirth": "1862-02-17"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "舞姫",
  "price": 400,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201

# タイトルで検索 (舞姫)
GET http://localhost:8080/api/books/search?title=%E8%88%9E%E5%A7%AB

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1
jsonpath "$.content[0].title" contains "舞姫"
jsonpath "$.meta.totalElements" >= 1

# 著者名で検索 (森)
GET http://localhost:8080/api/books/search?authorName=%E6%A3%AE

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1

# 出版状況で検索
GET http://localhost:8080/api/books/search?publicationStatus=PUBLISHED

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1

# 複合検索 (森)
GET http://localhost:8080/api/books/search?authorName=%E6%A3%AE&publicationStatus=PUBLISHED

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1
