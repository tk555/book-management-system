# バリデーションエラーのテストシナリオ

# === 著者のバリデーション ===

# 1. 著者名が空
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "",
  "dateOfBirth": "1990-01-01"
}

HTTP 400


# 2. 著者名が空白のみ
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "   ",
  "dateOfBirth": "1990-01-01"
}

HTTP 400


# 3. 生年月日が未来
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "未来の著者",
  "dateOfBirth": "2999-01-01"
}

HTTP 400


# 4. 存在しない著者の更新
PUT http://localhost:8080/api/authors/00000000-0000-0000-0000-000000000000
Content-Type: application/json
{
  "name": "存在しない著者",
  "dateOfBirth": "1990-01-01"
}

HTTP 404


# === 書籍のバリデーション ===

# 5. 正常な著者を作成（書籍テスト用）
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "テスト著者",
  "dateOfBirth": "1980-01-01"
}

HTTP 201
[Captures]
valid_author_id: jsonpath "$.id"


# 6. タイトルが空
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "",
  "price": 1000,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{valid_author_id}}"]
}

HTTP 400


# 7. 価格が負の値
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "負の価格の本",
  "price": -100,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{valid_author_id}}"]
}

HTTP 400


# 8. 著者IDが空の配列
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "著者なしの本",
  "price": 1000,
  "publicationStatus": "Unpublished",
  "authorIds": []
}

HTTP 400


# 9. 存在しない著者IDで本を作成
POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "存在しない著者の本",
  "price": 1000,
  "publicationStatus": "Unpublished",
  "authorIds": ["00000000-0000-0000-0000-000000000000"]
}

HTTP 400


# 10. 存在しない本の更新
PUT http://localhost:8080/api/books/00000000-0000-0000-0000-000000000000
Content-Type: application/json
{
  "title": "存在しない本",
  "price": 1000,
  "publicationStatus": "Unpublished",
  "authorIds": ["{{valid_author_id}}"]
}

HTTP 404
