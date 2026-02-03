# 書籍の著者変更テストシナリオ

# 1. 著者Aを作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "著者A",
  "dateOfBirth": "1960-01-01"
}

HTTP 201
[Captures]
author_a_id: jsonpath "$.id"


# 2. 著者Bを作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "著者B",
  "dateOfBirth": "1965-06-15"
}

HTTP 201
[Captures]
author_b_id: jsonpath "$.id"


# 3. 著者Cを作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "著者C",
  "dateOfBirth": "1970-12-25"
}

HTTP 201
[Captures]
author_c_id: jsonpath "$.id"


# 4. 著者Aのみで本を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "著者変更テスト本",
  "price": 1500,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author_a_id}}"]
}

HTTP 201
[Captures]
book_id: jsonpath "$.id"
[Asserts]
jsonpath "$.authors" count == 1
jsonpath "$.authors[0].id" == "{{author_a_id}}"


# 5. 著者AをBに変更
PUT http://localhost:8080/api/books/{{book_id}}
Content-Type: application/json
{
  "title": "著者変更テスト本",
  "price": 1500,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author_b_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.authors" count == 1
jsonpath "$.authors[0].id" == "{{author_b_id}}"


# 6. 著者Aの本一覧を確認（空になっている）
GET http://localhost:8080/api/authors/{{author_a_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 0


# 7. 著者Bの本一覧を確認（1冊ある）
GET http://localhost:8080/api/authors/{{author_b_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 1
jsonpath "$[0].id" == "{{book_id}}"


# 8. 複数著者に変更（B, C）
PUT http://localhost:8080/api/books/{{book_id}}
Content-Type: application/json
{
  "title": "著者変更テスト本",
  "price": 1500,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author_b_id}}", "{{author_c_id}}"]
}

HTTP 200
[Asserts]
jsonpath "$.authors" count == 2


# 9. 著者Cの本一覧を確認（1冊ある）
GET http://localhost:8080/api/authors/{{author_c_id}}/books

HTTP 200
[Asserts]
jsonpath "$" count == 1


# 10. 更新時に存在しない著者IDを含める（エラー）
PUT http://localhost:8080/api/books/{{book_id}}
Content-Type: application/json
{
  "title": "著者変更テスト本",
  "price": 1500,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{author_b_id}}", "00000000-0000-0000-0000-000000000000"]
}

HTTP 400
