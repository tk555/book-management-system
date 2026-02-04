# 著者と本の統合シナリオ

# 1. 著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "芥川龍之介",
  "dateOfBirth": "1892-03-01"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"


# 2. 著者の本一覧を取得（まだ本がない状態）
GET http://localhost:8080/api/authors/{{author_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 0


# 3. 本を作成（その1）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "羅生門",
  "price": 500,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
book1_id: jsonpath "$.id"


# 4. 本を作成（その2）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "蜘蛛の糸",
  "price": 400,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
book2_id: jsonpath "$.id"


# 5. 本を作成（その3・未刊行）
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "地獄変",
  "price": 600,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
book3_id: jsonpath "$.id"


# 6. 著者の本一覧を取得（3冊になっている）
GET http://localhost:8080/api/authors/{{author_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 3
jsonpath "$[*].title" contains "羅生門"
jsonpath "$[*].title" contains "蜘蛛の糸"
jsonpath "$[*].title" contains "地獄変"


# 7. 各本の詳細を確認
GET http://localhost:8080/api/books/{{book1_id}}

HTTP 200
[Asserts]
jsonpath "$.title" == "羅生門"
jsonpath "$.authors[0].name" == "芥川龍之介"


GET http://localhost:8080/api/books/{{book2_id}}

HTTP 200
[Asserts]
jsonpath "$.title" == "蜘蛛の糸"


GET http://localhost:8080/api/books/{{book3_id}}

HTTP 200
[Asserts]
jsonpath "$.title" == "地獄変"
jsonpath "$.publicationStatus" == "UNPUBLISHED"


# 8. 存在しない著者の本一覧を取得（404エラー）
GET http://localhost:8080/api/authors/00000000-0000-0000-0000-000000000000/books

HTTP 404
