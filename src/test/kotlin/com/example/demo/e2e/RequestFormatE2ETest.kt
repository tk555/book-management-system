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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestFormatE2ETest : E2ETestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `不正なUUID形式で400エラー`() {
        mockMvc
            .perform(get("/api/authors/invalid-uuid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `不正なJSON形式で400エラー`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `必須フィールドがないJSONで400エラー`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "テスト"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `不正な日付形式で400エラー`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "テスト", "dateOfBirth": "not-a-date"}"""),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `生年月日が未来の場合は400エラー`() {
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "テスト著者", "dateOfBirth": "2999-01-01"}"""),
            ).andExpect(status().isBadRequest)
    }
}
