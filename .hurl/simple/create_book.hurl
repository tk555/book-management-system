# POST /api/books - 書籍作成
# 事前に著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "三島由紀夫",
  "dateOfBirth": "1925-01-14"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

# 書籍を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "金閣寺",
  "price": 800,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Asserts]
jsonpath "$.id" exists
jsonpath "$.title" == "金閣寺"
jsonpath "$.price" == 800
jsonpath "$.publicationStatus" == "Unpublished"
jsonpath "$.authors" count == 1
jsonpath "$.authors[0].name" == "三島由紀夫"
jsonpath "$.createdAt" exists
jsonpath "$.updatedAt" exists
