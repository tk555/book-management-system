# POST /api/authors - 著者作成
POST http://localhost:8080/api/authors
Content-Type: application/json
{
  "name": "夏目漱石",
  "dateOfBirth": "1867-02-09"
}

HTTP 201
[Asserts]
jsonpath "$.id" exists
jsonpath "$.name" == "夏目漱石"
jsonpath "$.dateOfBirth" == "1867-02-09"
jsonpath "$.createdAt" exists
jsonpath "$.updatedAt" exists
