# GET /api/authors/search - 著者検索
# 事前にテストデータを作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "夏目漱石",
  "dateOfBirth": "1867-02-09"
}

HTTP 201
[Captures]
author_id: jsonpath "$.id"

POST http://localhost:8080/api/books
Content-Type: application/json
{
  "title": "吾輩は猫である",
  "price": 500,
  "publicationStatus": "Published",
  "authorIds": ["{{author_id}}"]
}

HTTP 201

# 名前で検索 (夏目)
GET http://localhost:8080/api/authors/search?name=%E5%A4%8F%E7%9B%AE

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1
jsonpath "$.content[0].name" contains "夏目"
jsonpath "$.meta.totalElements" >= 1

# 誕生日範囲で検索
GET http://localhost:8080/api/authors/search?dateOfBirthFrom=1867-01-01&dateOfBirthTo=1867-12-31

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1

# 書籍タイトルで検索 (猫)
GET http://localhost:8080/api/authors/search?bookTitle=%E7%8C%AB

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1

# 出版状況で検索
GET http://localhost:8080/api/authors/search?publicationStatus=Published

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1

# 複合検索 (夏目)
GET http://localhost:8080/api/authors/search?name=%E5%A4%8F%E7%9B%AE&publicationStatus=Published

HTTP 200
[Asserts]
jsonpath "$.content" count >= 1
