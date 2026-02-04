package com.example.demo.e2e

import com.example.demo.E2ETestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BooksApiE2ETest : E2ETestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun createAuthor(
        name: String,
        dateOfBirth: String,
    ): String {
        val result =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name": "$name", "dateOfBirth": "$dateOfBirth"}"""),
                ).andExpect(status().isCreated)
                .andReturn()

        return objectMapper.readTree(result.response.contentAsString).get("id").asText()
    }

    @Test
    fun `書籍を作成できる`() {
        val authorId = createAuthor("夏目漱石", "1867-02-09")

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "吾輩は猫である",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("吾輩は猫である"))
            .andExpect(jsonPath("$.price").value(500))
            .andExpect(jsonPath("$.publicationStatus").value("UNPUBLISHED"))
            .andExpect(jsonPath("$.authors.length()").value(1))
            .andExpect(jsonPath("$.authors[0].name").value("夏目漱石"))
    }

    @Test
    fun `書籍を取得できる`() {
        val authorId = createAuthor("芥川龍之介", "1892-03-01")

        val createResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                        "title": "羅生門",
                        "price": 400,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                        ),
                ).andExpect(status().isCreated)
                .andReturn()

        val bookId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()

        mockMvc
            .perform(get("/api/books/$bookId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(bookId))
            .andExpect(jsonPath("$.title").value("羅生門"))
    }

    @Test
    fun `存在しない書籍を取得すると404`() {
        mockMvc
            .perform(get("/api/books/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `書籍を更新できる`() {
        val authorId = createAuthor("森鷗外", "1862-02-17")

        val createResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                        "title": "舞姫",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                        ),
                ).andExpect(status().isCreated)
                .andReturn()

        val bookId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()

        // 出版済みに更新
        mockMvc
            .perform(
                put("/api/books/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "舞姫（改訂版）",
                        "price": 600,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("舞姫（改訂版）"))
            .andExpect(jsonPath("$.price").value(600))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
    }

    @Test
    fun `出版済みから未出版への変更は400エラー`() {
        val authorId = createAuthor("川端康成", "1899-06-14")

        val createResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                        "title": "雪国",
                        "price": 700,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                        ),
                ).andExpect(status().isCreated)
                .andReturn()

        val bookId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()

        // 出版済み → 未出版への変更は失敗
        mockMvc
            .perform(
                put("/api/books/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "雪国",
                        "price": 700,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `存在しない著者で書籍を作成すると400エラー`() {
        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "テスト本",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["00000000-0000-0000-0000-000000000000"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `不正な価格（負の数）で400エラー`() {
        val authorId = createAuthor("テスト著者", "1990-01-01")

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "テスト本",
                        "price": -100,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `不正なpublicationStatusで400エラー`() {
        val authorId = createAuthor("テスト著者2", "1990-01-01")

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "テスト本",
                        "price": 500,
                        "publicationStatus": "InvalidStatus",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `書籍を検索できる`() {
        val author1Id = createAuthor("太宰治", "1909-06-19")
        val author2Id = createAuthor("三島由紀夫", "1925-01-14")

        // 書籍を作成
        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "人間失格",
                        "price": 500,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$author1Id"]
                    }
                    """,
                    ),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "金閣寺",
                        "price": 800,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$author2Id"]
                    }
                    """,
                    ),
            ).andExpect(status().isCreated)

        // タイトルで検索
        mockMvc
            .perform(get("/api/books/search").param("title", "人間"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("人間失格"))
            .andExpect(jsonPath("$.meta.totalElements").value(1))

        // 著者名で検索
        mockMvc
            .perform(get("/api/books/search").param("authorName", "三島"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("金閣寺"))

        // 価格範囲で検索
        mockMvc
            .perform(
                get("/api/books/search")
                    .param("priceFrom", "600")
                    .param("priceTo", "1000"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("金閣寺"))

        // 出版状況で検索
        mockMvc
            .perform(get("/api/books/search").param("publicationStatus", "PUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("人間失格"))
    }

    @Test
    fun `書籍タイトルが空の場合は400エラー`() {
        val authorId = createAuthor("テスト著者3", "1990-01-01")

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `書籍タイトルが長すぎる場合は400エラー`() {
        val authorId = createAuthor("テスト著者4", "1990-01-01")
        val longTitle = "あ".repeat(401)

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "$longTitle",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `著者が0人の場合は400エラー`() {
        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "著者なしの本",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": []
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `存在しない書籍を更新すると404`() {
        val authorId = createAuthor("テスト著者5", "1990-01-01")

        mockMvc
            .perform(
                put("/api/books/00000000-0000-0000-0000-000000000000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "存在しない書籍",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `存在しない著者で書籍を更新すると400エラー`() {
        val authorId = createAuthor("テスト著者6", "1990-01-01")

        val createResult =
            mockMvc
                .perform(
                    post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                        "title": "更新テスト用の本",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                        ),
                ).andExpect(status().isCreated)
                .andReturn()

        val bookId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()

        mockMvc
            .perform(
                put("/api/books/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "更新テスト用の本",
                        "price": 500,
                        "publicationStatus": "UNPUBLISHED",
                        "authorIds": ["00000000-0000-0000-0000-000000000000"]
                    }
                    """,
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `存在しない著者の書籍一覧を取得すると404エラー`() {
        mockMvc
            .perform(get("/api/authors/00000000-0000-0000-0000-000000000000/books"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `著者の書籍一覧を取得できる`() {
        val authorId = createAuthor("宮沢賢治", "1896-08-27")

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "銀河鉄道の夜",
                        "price": 500,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {
                        "title": "注文の多い料理店",
                        "price": 400,
                        "publicationStatus": "PUBLISHED",
                        "authorIds": ["$authorId"]
                    }
                    """,
                    ),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(get("/api/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
}
