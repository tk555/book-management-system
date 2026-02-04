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
class AuthorsApiE2ETest : E2ETestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `著者を作成できる`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "夏目漱石", "dateOfBirth": "1867-02-09"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("夏目漱石"))
            .andExpect(jsonPath("$.dateOfBirth").value("1867-02-09"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())
    }

    @Test
    fun `著者を取得できる`() {
        // 著者を作成
        val createResult =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name": "芥川龍之介", "dateOfBirth": "1892-03-01"}"""),
                ).andExpect(status().isCreated)
                .andReturn()

        val authorId = objectMapper.readTree(createResult.response.contentAsString).get("id").asString()

        // 著者を取得
        mockMvc
            .perform(get("/api/authors/$authorId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(authorId))
            .andExpect(jsonPath("$.name").value("芥川龍之介"))
    }

    @Test
    fun `存在しない著者を取得すると404`() {
        mockMvc
            .perform(get("/api/authors/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `著者を更新できる`() {
        // 著者を作成
        val createResult =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name": "森鷗外", "dateOfBirth": "1862-02-17"}"""),
                ).andExpect(status().isCreated)
                .andReturn()

        val authorId = objectMapper.readTree(createResult.response.contentAsString).get("id").asString()

        // 著者を更新
        mockMvc
            .perform(
                put("/api/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "森鷗外（更新後）", "dateOfBirth": "1862-02-17"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("森鷗外（更新後）"))
    }

    @Test
    fun `著者名が空の場合は400エラー`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "", "dateOfBirth": "1867-02-09"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `著者名が長すぎる場合は400エラー`() {
        val longName = "あ".repeat(201)
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "$longName", "dateOfBirth": "1867-02-09"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `存在しない著者を更新すると404`() {
        mockMvc
            .perform(
                put("/api/authors/00000000-0000-0000-0000-000000000000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "更新後の名前", "dateOfBirth": "1867-02-09"}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `著者を検索できる`() {
        // 著者を作成
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "太宰治", "dateOfBirth": "1909-06-19"}"""),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "川端康成", "dateOfBirth": "1899-06-14"}"""),
            ).andExpect(status().isCreated)

        // 名前で検索
        mockMvc
            .perform(get("/api/authors/search").param("name", "太宰"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("太宰治"))
            .andExpect(jsonPath("$.meta.totalElements").value(1))

        // 生年月日範囲で検索
        mockMvc
            .perform(
                get("/api/authors/search")
                    .param("dateOfBirthFrom", "1900-01-01")
                    .param("dateOfBirthTo", "1910-12-31"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("太宰治"))
    }
}
