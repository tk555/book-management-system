# 出版ステータス遷移ルールのテストシナリオ

# 1. 著者を作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "ステータステスト著者",
  "dateOfBirth": "1970-05-15"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"


# 2. 未出版の本を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "未出版の本",
  "price": 1500,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
unpublished_book_id: jsonpath "$.id"
[Asserts]
jsonpath "$.publicationStatus" == "UNPUBLISHED"


# 3. 未出版 → 出版済み（許可される）
PUT http://localhost:8080/api/books/{{unpublished_book_id}}
Content-Type: application/json
{
  "title": "未出版の本",
  "price": 1500,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.publicationStatus" == "PUBLISHED"


# 4. 出版済み → 未出版（禁止される）
PUT http://localhost:8080/api/books/{{unpublished_book_id}}
Content-Type: application/json
{
  "title": "未出版の本",
  "price": 1500,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 400


# 5. 出版済み → 出版済み（許可される、他のフィールドの更新）
PUT http://localhost:8080/api/books/{{unpublished_book_id}}
Content-Type: application/json
{
  "title": "出版済みの本（タイトル変更）",
  "price": 2000,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.title" == "出版済みの本（タイトル変更）"
jsonpath "$.price" == 2000
jsonpath "$.publicationStatus" == "PUBLISHED"


# 6. 最初から出版済みの本を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "最初から出版済み",
  "price": 1000,
  "publicationStatus": "PUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 201
[Captures]
published_book_id: jsonpath "$.id"
[Asserts]
jsonpath "$.publicationStatus" == "PUBLISHED"


# 7. 出版済み本のステータスを未出版に変更しようとする（禁止）
PUT http://localhost:8080/api/books/{{published_book_id}}
Content-Type: application/json
{
  "title": "最初から出版済み",
  "price": 1000,
  "publicationStatus": "UNPUBLISHED",
  "authorIds": ["{{author_id}}"]
}

HTTP 400
